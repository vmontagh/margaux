package edu.uw.ece.alloy.debugger.mutate;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.io.Files;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.uw.ece.alloy.debugger.filters.Decompose;
import edu.uw.ece.alloy.debugger.filters.ExtractorUtils;
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
	protected static class DecisionQueueItem<T> implements
			Comparator<DecisionQueueItem<T>>, Comparable<DecisionQueueItem<T>> {
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
	/* The mutation are stored in this directory. */
	final public File destinationDir;
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
	final Oracle oracle;
	final ExampleFinder exampleFinder;

	final PriorityQueue<DecisionQueueItem<Field>> fieldsQueue;
	final PriorityQueue<DecisionQueueItem<Expr>> modelQueue;
	/*
	 * Part of the model that is being analyzed. This property is changed while
	 * the algorithm is run. Other methods have access to this variable.
	 */
	Expr toBeingAnalyzedModelPart;
	Field toBeingAnalyzedField;
	/* The rest the mode. I.e Model - toBeingAnalyzedModelPart */
	List<Expr> restModelParts;
	/* Mapping from What is analyzed far to its approximations */
	final Map<String, List<Pair<String, String>>> approximations;
	/*
	 * A PQ to determine which approximation should be fixed first. It varies at
	 * each iteration
	 */
	PriorityQueue<DecisionQueueItem<Pair<String, String>>> approximationQueue;
	/* The property that is chosen to be weaken or strengthened */
	Pair<String, String> toBeingWeakenOrStrengthenedApproximation;
	PriorityQueue<DecisionQueueItem<String>> toBePickedQueueFromWeakenOrStrengthened;
	boolean strengthened;
	/*
	 * findOnBorderExamples finds two examples close to border: inAndOutExamples.a
	 * is inside and inAndOutExamples.b is outside.
	 */
	protected Pair<Optional<String>, Optional<String>> inAndOutExamples;

	/* Examples that are reviewed by oracle are stored in the following sets */
	final protected Set<String> acceptedExamples, rejectedExamples;

	public DebuggerAlgorithm(final File sourceFile, final File destinationDir,
			final Approximator approximator, final Oracle oracle,
			final ExampleFinder exampleFinder) {
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

		if (world.getAllCommands().size() != 1)
			throw new RuntimeException("Only one valid command should be passed. "
					+ "Comment out the rest or add at least one.");

		final Command command = world.getAllCommands().get(0);
		constraint = command.formula;

		Pair<List<Expr>, List<Expr>> propertyChecking = Decompose
				.decomposetoImplications(constraint);
		model = Collections.unmodifiableList(propertyChecking.a);
		property = Collections.unmodifiableList(propertyChecking.b);
		scope = ExtractorUtils.extractScopeFromCommand(command);
		try {
			fields = Collections.unmodifiableList(
					FieldsExtractorVisitor.getReferencedFields(constraint).stream()
							.collect(Collectors.toList()));
		} catch (Err e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		this.approximator = approximator;
		this.oracle = oracle;
		this.exampleFinder = exampleFinder;

		// The fields are picked based on their priority in the priority queue
		fieldsQueue = new PriorityQueue<>();
		fields.stream().forEach(field -> fieldsQueue
				.add(DecisionQueueItem.<Field> createwithRandomPriority(field)));

		modelQueue = new PriorityQueue<>();
		model.stream().forEach(m -> modelQueue
				.add(DecisionQueueItem.<Expr> createwithRandomPriority(m)));

		approximations = new HashMap<>();
		this.destinationDir = destinationDir;

		acceptedExamples = new HashSet<>();
		rejectedExamples = new HashSet<>();
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
		onStartLoop();
		beforePickField();
		for (DecisionQueueItem<Field> field : fieldsQueue) {
			afterPickField();
			toBeingAnalyzedField = field.getItem().get();
			beforePickModelPart();
			for (DecisionQueueItem<Expr> modelPart : modelQueue) {
				afterPickModelPart();
				toBeingAnalyzedModelPart = modelPart.getItem().get();

				restModelParts = model.stream()
						.filter(m -> !m.equals(modelPart.getItem().get()))
						.collect(Collectors.toList());

				String restModel = restModelParts.stream().map(m -> m.toString())
						.collect(Collectors.joining(" and "));

				approximations.put(modelPart.getItem().get().toString(),
						approximator.strongestApproximation(modelPart.getItem().get(),
								field.getItem().get(), scope));

				List<Pair<String, String>> approximation = approximations
						.get(modelPart.getItem().get().toString());

				logger.info(Utils.threadName() + "The approximations for Expr:<"
						+ modelPart.getItem().get() + "> is: " + approximation);

				System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
				System.out.println(modelPart.getItem().get());
				System.out.println(field.getItem().get());
				System.out.println(scope);
				System.out.println(approximation);
				System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");

				// converting the approximations into a PQ.
				this.approximationQueue = new PriorityQueue<>();
				approximation.stream()
						.forEach(m -> approximationQueue.add(DecisionQueueItem
								.<Pair<String, String>> createwithRandomPriority(m)));
				beforePickApproximation();
				for (DecisionQueueItem<Pair<String, String>> approx : approximationQueue) {
					afterPickApproximation();
					toBeingWeakenOrStrengthenedApproximation = approx.getItem().get();

					final List<String> strongerApprox = approximator.strongerProperties(
							toBeingWeakenOrStrengthenedApproximation.b,
							field.getItem().get().label);
					final PriorityQueue<DecisionQueueItem<String>> strongerApproxQueue = new PriorityQueue<>();
					strongerApprox.stream().forEach(m -> strongerApproxQueue
							.add(DecisionQueueItem.<String> createwithRandomPriority(m)));

					final List<String> weakerApprox = approximator.weakerProperties(
							toBeingWeakenOrStrengthenedApproximation.b,
							field.getItem().get().label);
					final PriorityQueue<DecisionQueueItem<String>> weakerApproxQueue = new PriorityQueue<>();
					weakerApprox.stream().forEach(m -> weakerApproxQueue
							.add(DecisionQueueItem.<String> createwithRandomPriority(m)));

					toBePickedQueueFromWeakenOrStrengthened = strongerApproxQueue;

					while (!strongerApproxQueue.isEmpty()
							|| !weakerApproxQueue.isEmpty()) {

						toBePickedQueueFromWeakenOrStrengthened = strongerApproxQueue;
						beforePickWeakenOrStrengthened();
						strengthened = true;
						afterPickWeakenOrStrengthened();

						if (!weakerApproxQueue.isEmpty() && (strongerApproxQueue.isEmpty()
								|| DecisionQueueItem.randomGenerator.nextBoolean())) {
							toBePickedQueueFromWeakenOrStrengthened = weakerApproxQueue;
							strengthened = false;
						}

						beforePickWeakenOrStrengthenedApprox();
						String approximationProperty = toBePickedQueueFromWeakenOrStrengthened
								.poll().getItem().orElseThrow(() -> new RuntimeException(
										"The stronger form cannot be null"));
						beforePickWeakenOrStrengthenedApprox();

						beforeMutating();
						File mutatedFile = makeMutation(approximationProperty, restModel)
								.get();
						afterMutating();

						beforeCallingExampleFinder();
						// the examples are in Alloy format.
						inAndOutExamples = exampleFinder.findOnBorderExamples(mutatedFile,
								mutatedFile.getName().replace(".als", ""),
								"NOT_" + mutatedFile.getName().replace(".als", ""));
						afterCallingExampleFinder();

						beforeInquiryOracle();
						// ask the user

						// Interpreting the result
						boolean inExampleIsInteded = inAndOutExamples.a.isPresent()
								? oracle.isIntended(inAndOutExamples.a.get()) : false;
						boolean outExampleIsInteded = inAndOutExamples.b.isPresent()
								? oracle.isIntended(inAndOutExamples.b.get()) : false;

						if (strengthened) {
							if (inExampleIsInteded) {
								logger.info("The model is correct so far.");
								acceptedExamples.add(inAndOutExamples.a.get());
							} else {
								logger.info("The model has underconstraint issue.");
								rejectedExamples.add(inAndOutExamples.a.orElse(""));
							}
							if (outExampleIsInteded) {
								logger.info("The model is overconstraint issue.");
								acceptedExamples.add(inAndOutExamples.b.get());
							} else {
								logger.info("The model is correct.");
								rejectedExamples.add(inAndOutExamples.b.orElse(""));
							}
						} else {
							if (inExampleIsInteded) {
								logger.info("The model is in overconstraint issue");
								acceptedExamples.add(inAndOutExamples.a.get());
							} else {
								logger.info("The model is correct.");
								rejectedExamples.add(inAndOutExamples.a.orElse(""));
							}
							if (outExampleIsInteded) {
								logger.info("The model is overconstraint issue.");
								acceptedExamples.add(inAndOutExamples.b.get());
							} else {
								logger.info("The model is correct.");
								rejectedExamples.add(inAndOutExamples.b.orElse(""));
							}
						}

						// store the answer
						afterInquiryOracle();
						// Call APIs to change the priority of the next steps
					}

				}
			}
			if (!fieldsQueue.isEmpty())
				beforePickField();
		}

	}

	protected abstract void afterInquiryOracle();

	protected abstract void beforeInquiryOracle();

	protected abstract void afterCallingExampleFinder();

	protected abstract void beforeCallingExampleFinder();

	protected abstract void afterMutating();

	protected abstract void beforeMutating();

	protected abstract void beforePickWeakenOrStrengthenedApprox();

	protected abstract void afterPickWeakenOrStrengthened();

	protected abstract void beforePickWeakenOrStrengthened();

	protected abstract void afterPickApproximation();

	protected abstract void beforePickApproximation();

	protected abstract void afterPickModelPart();

	protected abstract void beforePickModelPart();

	protected abstract void afterPickField();

	protected abstract void beforePickField();

	protected abstract void onStartLoop();

	protected Optional<File> makeMutation(String approximationProperty,
			String restModel) {

		// make sure the relationalPropModule and temporalPropModule files
		// are tin the path.
		File relationalPropModule = new File(destinationDir,
				approximator.relationalPropModule.getName());
		if (!relationalPropModule.exists())
			try {
				Files.copy(approximator.relationalPropModule, relationalPropModule);
			} catch (IOException e1) {
				e1.printStackTrace();
				logger
						.severe(Utils.threadName() + " " + approximator.relationalPropModule
								+ " cannot be copied in destination folder: "
								+ relationalPropModule);
			}

		File temporalPropModule = new File(destinationDir,
				approximator.temporalPropModule.getName());
		if (!temporalPropModule.exists())
			try {
				Files.copy(approximator.temporalPropModule, temporalPropModule);
			} catch (IOException e1) {
				e1.printStackTrace();
				logger.severe(Utils.threadName() + " " + approximator.temporalPropModule
						+ " cannot be copied in destination folder: " + temporalPropModule);
			}

		String ModelPartialApproximation = approximationProperty
				+ (!restModel.trim().isEmpty() ? " and " + restModel : "");

		// Make a new Model
		String predName = "approximate_"
				+ Math.abs(ModelPartialApproximation.hashCode());
		String pred = String.format("pred %s[]{%s}\n", predName,
				ModelPartialApproximation);

		String predNameNot = "NOT_" + predName;
		String notPred = String.format("pred %s[]{not (%s)}\n", predNameNot,
				ModelPartialApproximation);

		String newHeader = "open "
				+ relationalPropModule.getName().replace(".als", "\n");
		newHeader += "open " + temporalPropModule.getName().replace(".als", "\n");
		String newCommandName = "run " + predName + " " + scope;
		String newCommandNameNot = "run " + predNameNot + " " + scope;

		String newCode = newHeader + "\n" + sourceCode + "\n" + pred + "\n"
				+ notPred + "\n" + newCommandName + "\n" + newCommandNameNot;
		File newCodeFile = new File(destinationDir, predName + ".als");
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
