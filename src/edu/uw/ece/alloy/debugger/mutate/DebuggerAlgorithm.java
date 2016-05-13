package edu.uw.ece.alloy.debugger.mutate;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.CommandScope;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.uw.ece.alloy.debugger.filters.Decompose;
import edu.uw.ece.alloy.debugger.filters.FieldsExtractorVisitor;
import edu.uw.ece.alloy.util.Utils;

/**
 * The debugger algorithm is an abstract implementation of the Discriminating
 * Examples Search Procedure.
 * 
 * The Object-Oriented implementation follows template pattern, which some parts
 * of the algorithm is abstracted and should be implemented in the actual
 * implementations.
 * 
 * @author vajih
 *
 */
public abstract class DebuggerAlgorithm {

	/**
	 * The class pair any item to an integer that is comparable to be compared in
	 * the priority queue.
	 * 
	 * @author vajih
	 *
	 * @param <T>
	 */
	protected static class DecisionQueueItem<T> implements Comparator<DecisionQueueItem<T>>, Comparable<DecisionQueueItem<T>> {
		// higher score is more probable to be processed first.
		Integer score;
		final T item;
		final static Random randomGenerator = new Random();

		public DecisionQueueItem(final T item, Integer score) {
			this.item = item;
			this.score = score;
		}

		public static <T> DecisionQueueItem<T> createwithRandomPriority(
				final T item) {
			return new DecisionQueueItem<T>(item, randomGenerator.nextInt());
		}

		public static <T> DecisionQueueItem<T> createwithUnformPriority(
				final T item) {
			return new DecisionQueueItem<T>(item, Integer.MIN_VALUE);
		}

		@Override
		public int compare(DecisionQueueItem<T> o1, DecisionQueueItem<T> o2) {
			return o2.score - o1.score;
		}

		public void setScore(Integer score) {
			this.score = score;
		}

		public Optional<Integer> getScore() {
			return Optional.ofNullable(score);
		}

		public Optional<T> getItem() {
			return Optional.ofNullable(item);
		}

		@Override
		public int compareTo(DecisionQueueItem<T> that) {
			return this.compare(this, that);
		}
	}

	protected final static Logger logger = Logger
			.getLogger(DebuggerAlgorithm.class.getName() + "--"
					+ Thread.currentThread().getName());

	/* The source of an Alloy file */
	final public File sourceFile;
	final public String sourceCode;
	/* Extracted fields are stored here */
	final protected List<Field> fields;
	/* The whole constraint = */
	final protected Expr constraint;
	final protected String scope;

	// A model is a conjunction of constraints. this.constraint = model
	final List<Expr> model;// = Collections.emptyList();
	// In a model in the form of M => P, P is a conjunction of constraints.
	// constraint = model => property
	final List<Expr> property;// = Collections.emptyList();

	// final
	Approximator approximator;

	final PriorityQueue<DecisionQueueItem<Field>> fieldsQueue;
	final PriorityQueue<DecisionQueueItem<Expr>> modelQueue;

	public DebuggerAlgorithm(final File sourceFile,
			final Approximator approximator) {
		this.sourceFile = sourceFile;
		this.sourceCode = Utils.readFile(sourceFile.getAbsolutePath());
		CompModule world;
		try {
			world = CompUtil.parseEverything_fromFile(A4Reporter.NOP, null,
					sourceFile.getAbsolutePath());
		} catch (Err e) {
			logger.severe(
					Utils.threadName() + "The Alloy file cannot be loaded or parsed:"
							+ sourceFile + "\n" + e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		constraint = world.getAllCommands().get(0).formula;

		if (world.getAllCommands().size() != 1)
			throw new RuntimeException("Only one valid command should be passed. "
					+ "Comment out the rest or add at least one.");

		final Command command = world.getAllCommands().get(0);
		
		Expr toBeCheckedModel = command.formula;
		Pair<List<Expr>, List<Expr>> propertyChecking = Decompose
				.decomposetoImplications(toBeCheckedModel);
		model = Collections.unmodifiableList(propertyChecking.a);
		property = Collections.unmodifiableList(propertyChecking.b);
		// TODO(vajih) extract Scope from the command
		scope = extractScopeFromCommand(command);
		try {
			fields = Collections.unmodifiableList(
					FieldsExtractorVisitor.getReferencedFields(toBeCheckedModel).stream()
							.collect(Collectors.toList()));
		} catch (Err e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		this.approximator = approximator;

		// The fields are picked based on their priority in the priority queue
		fieldsQueue = new PriorityQueue<>();
		fields.stream().forEach(field -> fieldsQueue
				.add(DecisionQueueItem.<Field> createwithRandomPriority(field)));

		modelQueue = new PriorityQueue<>();
		model.stream().forEach(m -> modelQueue
				.add(DecisionQueueItem.<Expr> createwithRandomPriority(m)));

	}

	/**
	 * Given a command, its scope is returned as String
	 * @param command
	 * @return
	 */
	protected String extractScopeFromCommand(Command command) {
		boolean first = true;
		StringBuilder sb = new StringBuilder();
		if (command.overall >= 0 && (command.bitwidth >= 0 || command.maxseq >= 0
				|| command.scope.size() > 0))
			sb.append(" for ").append(command.overall).append(" but");
		else if (command.overall >= 0)
			sb.append(" for ").append(command.overall);
		else if (command.bitwidth >= 0 || command.maxseq >= 0
				|| command.scope.size() > 0)
			sb.append(" for");
		if (command.bitwidth >= 0) {
			sb.append(" ").append(command.bitwidth).append(" int");
			first = false;
		}
		if (command.maxseq >= 0) {
			sb.append(first ? " " : ", ").append(command.maxseq).append(" seq");
			first = false;
		}
		for (CommandScope e : command.scope) {
			sb.append(first ? " " : ", ").append(e);
			first = false;
		}
		if (command.expects >= 0)
			sb.append(" expect ").append(command.expects);
		return sb.toString();
	}

	/**
	 * Given a statement, then an strongest approximation is found and returned.
	 * The return is a the actual call. E.g. acyclic[r]
	 * 
	 * @param statement
	 * @return
	 */
	public String strongestApproximation(Expr statement, Field field) {
		return null; // approximator.strongestApproximationModel(statement, field,
								 // scope);
	}

	public void run() {
		for (DecisionQueueItem<Field> field : fieldsQueue) {
			for (DecisionQueueItem<Expr> modelPart : modelQueue) {
				String restModel = model.stream()
						.filter(m -> !m.equals(modelPart.getItem().get()))
						.map(a -> a.toString()).collect(Collectors.joining(" and "));

				List<Pair<String, String>> approximations = approximator
						.strongestApproximation(modelPart.getItem().get(),
								field.getItem().get(), scope);

				logger.info(Utils.threadName() + "The approximations for Expr:<"
						+ "> is: " + approximations);

				final PriorityQueue<DecisionQueueItem<Pair<String, String>>> approximationQueue = new PriorityQueue<>();
				approximations.stream()
						.forEach(m -> approximationQueue.add(DecisionQueueItem
								.<Pair<String, String>> createwithRandomPriority(m)));

				for (DecisionQueueItem<Pair<String, String>> approximation : approximationQueue) {

					final List<String> strongerApprox = approximator
							.strongerProperties(approximation.getItem().get().b);
					final PriorityQueue<DecisionQueueItem<String>> strongerApproxQueue = new PriorityQueue<>();
					strongerApprox.stream().forEach(m -> strongerApproxQueue
							.add(DecisionQueueItem.<String> createwithRandomPriority(m)));

					final List<String> weakerApprox = approximator
							.weakerProperties(approximation.getItem().get().b);
					final PriorityQueue<DecisionQueueItem<String>> weakerApproxQueue = new PriorityQueue<>();
					weakerApprox.stream().forEach(m -> weakerApproxQueue
							.add(DecisionQueueItem.<String> createwithRandomPriority(m)));

					while (!strongerApproxQueue.isEmpty()
							|| !weakerApproxQueue.isEmpty()) {

						PriorityQueue<DecisionQueueItem<String>> toBePickedQueue = strongerApproxQueue;

						if (!weakerApproxQueue.isEmpty() && (strongerApproxQueue.isEmpty()
								|| !DecisionQueueItem.randomGenerator.nextBoolean()))
							toBePickedQueue = weakerApproxQueue;
						
						String approximationProperty = toBePickedQueue.poll().getItem()
								.orElseThrow(() -> new RuntimeException(
										"The stronger form cannot be null"));

						findOnBorderExamples(
								makeMutation(approximationProperty, restModel).get());
						// ask the user
						// Call APIs to change the priority of the next steps
					}

				}
			}
		}

	}

	protected Optional<File> makeMutation(String approximationProperty,
			String restModel) {
		String ModelPartialApproximation = approximationProperty
				+ (!restModel.trim().isEmpty() ? " and " + restModel : "");

		// Make a new Model
		String predName = "approximate_"
				+ Math.abs(ModelPartialApproximation.hashCode());
		String pred = String.format("pred %s[]{%s}\n", predName,
				ModelPartialApproximation);
		String newHeader = "open " + approximator.relationalPropModuleOriginal
				.getName().replace(".als", "\n");
		newHeader += "open " + approximator.temporalPropModuleOriginal.getName()
				.replace(".als", "\n");
		String newCommandName = "run " + predName + " " + scope;
		String newCode = newHeader + "\n" + sourceCode + "\n" + pred + "\n"
				+ newCommandName;
		File newCodeFile = new File(sourceFile.getParentFile(), predName + ".als");
		try {
			System.out.println("newCodeFile->" + newCodeFile);
			Util.writeAll(newCodeFile.getAbsolutePath(), newCode);
		} catch (Err e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return Optional.ofNullable(newCodeFile);
	}

	protected Optional<Field> pickField() {
		return this.fields.size() > 0 ? Optional.of(this.fields.remove(0))
				: Optional.empty();
	}

	/**
	 * TODO implement the call to on border example generator
	 * 
	 * @return
	 */
	public List<String> findOnBorderExamples(File path) {
		return Collections.EMPTY_LIST;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DebuggerAlgorithm [sourceFile=" + sourceFile + ", fields=" + fields
				+ ", constraint=" + constraint + ", model=" + model + ", property="
				+ property + "]";
	}

}
