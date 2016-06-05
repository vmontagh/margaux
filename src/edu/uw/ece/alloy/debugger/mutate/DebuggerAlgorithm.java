package edu.uw.ece.alloy.debugger.mutate;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import edu.uw.ece.alloy.debugger.PrettyPrintExpression;
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
	public static class DecisionQueueItem<T> implements
			Comparator<DecisionQueueItem<T>>, Comparable<DecisionQueueItem<T>> {
		// higher score is more probable to be processed first.
		Integer score;
		final T item;
		final protected static Random RandomGenerator = new Random();
		final public static Integer MinUniformScore = 0;
		final public static Integer MaxUniformScore = Integer.MAX_VALUE / 2;

		public DecisionQueueItem(final T item, Integer score) {
			this.item = item;
			this.score = score;
		}

		public static <T> DecisionQueueItem<T> createwithRandomPriority(
				final T item) {
			return new DecisionQueueItem<T>(item,
					RandomGenerator.nextInt(MaxUniformScore - MinUniformScore + 1)
							+ MinUniformScore);
		}

		public static <T> DecisionQueueItem<T> createwithUnformPriority(
				final T item) {
			return new DecisionQueueItem<T>(item, MinUniformScore);
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

		@Override
		public String toString() {
			return "DecisionQueueItem [score=" + score + ", item=" + item + "]";
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
	final protected List<Expr> model;// = Collections.emptyList();
	// In a model in the form of M => P, P is a conjunction of constraints.
	// constraint = model => property
	final protected List<Expr> property;// = Collections.emptyList();

	// final
	protected Approximator approximator;
	final Oracle oracle;
	final ExampleFinder exampleFinder;

	final protected PriorityQueue<DecisionQueueItem<Field>> fieldsQueue;
	final protected PriorityQueue<DecisionQueueItem<Expr>> modelQueue;
	/*
	 * Part of the model that is being analyzed. This property is changed while
	 * the algorithm is run. Other methods have access to this variable.
	 */
	protected Expr toBeingAnalyzedModelPart;
	protected Field toBeingAnalyzedField;
	/* The rest the mode. I.e Model - toBeingAnalyzedModelPart */
	List<Expr> restModelParts;
	/* Mapping from What is analyzed so far to its approximations */
	final protected Map<Field, Map<Expr, List<Pair<String, String>>>> approximations;

	protected final Map<Field, Map<Expr, Map<Pair<String, String>, PriorityQueue<DecisionQueueItem<String>>>>> strongerApproxQueues,
			weakerApproxQueues;
	/*
	 * A PQ to determine which approximation should be fixed first. It varies at
	 * each iteration
	 */
	final protected Map<Field, Map<Expr, PriorityQueue<DecisionQueueItem<Pair<String, String>>>>> approximationQueues;
	/* The property that is chosen to be weaken or strengthened */
	protected Pair<String, String> toBeingWeakenOrStrengthenedApproximation;
	PriorityQueue<DecisionQueueItem<String>> toBePickedQueueFromWeakenOrStrengthened;
	protected boolean strengthened;
	/*
	 * findOnBorderExamples finds two examples close to border: inAndOutExamples.a
	 * is inside and inAndOutExamples.b is outside.
	 */
	protected Pair<Optional<String>, Optional<String>> inAndOutExamples;

	protected boolean inExampleIsInteded, outExampleIsInteded;

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

		approximationQueues = new HashMap<>();
		strongerApproxQueues = new HashMap<>();
		weakerApproxQueues = new HashMap<>();

		approximations = new HashMap<>();
		this.destinationDir = destinationDir;

		acceptedExamples = new HashSet<>();
		rejectedExamples = new HashSet<>();
	}

	protected DebuggerAlgorithm() {
		sourceFile = null;
		destinationDir = null;
		sourceCode = null;
		fields = null;
		constraint = null;
		scope = null;
		model = null;
		property = null;
		oracle = null;
		exampleFinder = null;
		fieldsQueue = null;
		modelQueue = null;
		approximationQueues = null;
		strongerApproxQueues = null;
		weakerApproxQueues = null;
		approximations = null;
		acceptedExamples = null;
		rejectedExamples = null;
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

		StringBuilder report = new StringBuilder();
		String reportHeader = new String();

		onStartLoop();
		beforePickField();
		while (!fieldsQueue.isEmpty()) {
			DecisionQueueItem<Field> field = fieldsQueue.poll();
			toBeingAnalyzedField = field.getItem().get();
			afterPickField();

			beforePickModelPart();
			while (!modelQueue.isEmpty()) {
				DecisionQueueItem<Expr> modelPart = modelQueue.poll();
				afterPickModelPart();
				toBeingAnalyzedModelPart = modelPart.getItem().get();
				String toBeingAnalyzedModelPartString = toBeingAnalyzedModelPart
						.toString();
				try {
					toBeingAnalyzedModelPartString = PrettyPrintExpression
							.makeString(toBeingAnalyzedModelPart);
				} catch (Err e) {
					e.printStackTrace();
				}
				restModelParts = model.stream()
						.filter(m -> !m.equals(toBeingAnalyzedModelPart))
						.collect(Collectors.toList());
				String restModel = restModelParts.stream().map(m -> m.toString())
						.collect(Collectors.joining(" and "));

				fillApproximations(toBeingAnalyzedModelPart, toBeingAnalyzedField);

				List<Pair<String, String>> approximation = approximations
						.get(toBeingAnalyzedField).get(toBeingAnalyzedModelPart);

				logger.info(Utils.threadName() + "The approximations for Expr:<"
						+ toBeingAnalyzedModelPart + "> is: " + approximation);

				System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
				System.out.println(toBeingAnalyzedModelPart);
				System.out.println(toBeingAnalyzedField);
				System.out.println(scope);
				System.out.println(approximation);
				System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");

				if (!approximationQueues.containsKey(toBeingAnalyzedField))
					approximationQueues.put(toBeingAnalyzedField, new HashMap<>());
				if (!approximationQueues.get(toBeingAnalyzedField)
						.containsKey(toBeingAnalyzedModelPart)) {
					// initializing the priority queue.
					approximationQueues.get(toBeingAnalyzedField).put(
							toBeingAnalyzedModelPart,
							approximation.stream()
									.map(m -> DecisionQueueItem
											.<Pair<String, String>> createwithRandomPriority(m))
									.collect(Collectors.toCollection(PriorityQueue::new)));
				}

				// converting the approximations into a PQ.
				PriorityQueue<DecisionQueueItem<Pair<String, String>>> approximationQueue = approximationQueues
						.get(toBeingAnalyzedField).get(toBeingAnalyzedModelPart);

				System.out.println("approximationQueue->" + approximationQueue);

				beforePickApproximation();
				while (!approximationQueue.isEmpty()) {
					DecisionQueueItem<Pair<String, String>> approx = approximationQueue
							.poll();

					toBeingWeakenOrStrengthenedApproximation = approx.getItem().get();
					afterPickApproximation();

					if (!strongerApproxQueues.containsKey(toBeingAnalyzedField))
						strongerApproxQueues.put(toBeingAnalyzedField, new HashMap<>());
					if (!strongerApproxQueues.get(toBeingAnalyzedField)
							.containsKey(toBeingAnalyzedModelPart))
						strongerApproxQueues.get(toBeingAnalyzedField)
								.put(toBeingAnalyzedModelPart, new HashMap<>());
					if (!strongerApproxQueues.get(toBeingAnalyzedField)
							.get(toBeingAnalyzedModelPart)
							.containsKey(toBeingWeakenOrStrengthenedApproximation))
						strongerApproxQueues.get(toBeingAnalyzedField)
								.get(toBeingAnalyzedModelPart)
								.put(toBeingWeakenOrStrengthenedApproximation,
										approximator
												.strongerProperties(
														toBeingWeakenOrStrengthenedApproximation.a,
														toBeingAnalyzedField.label)
												.stream()
												.map(s -> DecisionQueueItem
														.<String> createwithRandomPriority(s.b))
												.collect(Collectors.toCollection(PriorityQueue::new)));

					if (!weakerApproxQueues.containsKey(toBeingAnalyzedField))
						weakerApproxQueues.put(toBeingAnalyzedField, new HashMap<>());
					if (!weakerApproxQueues.get(toBeingAnalyzedField)
							.containsKey(toBeingAnalyzedModelPart))
						weakerApproxQueues.get(toBeingAnalyzedField)
								.put(toBeingAnalyzedModelPart, new HashMap<>());
					if (!weakerApproxQueues.get(toBeingAnalyzedField)
							.get(toBeingAnalyzedModelPart)
							.containsKey(toBeingWeakenOrStrengthenedApproximation))
						weakerApproxQueues
								.get(
										toBeingAnalyzedField)
								.get(toBeingAnalyzedModelPart)
								.put(toBeingWeakenOrStrengthenedApproximation,
										Stream
												.concat(
														// The current pattern should also be added to the
														// list
														Arrays
																.asList(
																		toBeingWeakenOrStrengthenedApproximation)
																.stream(),
														approximator.weakerProperties(
																toBeingWeakenOrStrengthenedApproximation.a,
																toBeingAnalyzedField.label).stream())
												.map(s -> DecisionQueueItem
														.<String> createwithRandomPriority(s.b))
												.collect(Collectors.toCollection(PriorityQueue::new)));

					beforePickWeakenOrStrengthened();

					PriorityQueue<DecisionQueueItem<String>> strongerApproxQueue = strongerApproxQueues
							.get(toBeingAnalyzedField).get(toBeingAnalyzedModelPart)
							.get(toBeingWeakenOrStrengthenedApproximation);
					PriorityQueue<DecisionQueueItem<String>> weakerApproxQueue = weakerApproxQueues
							.get(toBeingAnalyzedField).get(toBeingAnalyzedModelPart)
							.get(toBeingWeakenOrStrengthenedApproximation);

					toBePickedQueueFromWeakenOrStrengthened = strongerApproxQueue;

					while (!strongerApproxQueue.isEmpty()
							|| !weakerApproxQueue.isEmpty()) {
						toBePickedQueueFromWeakenOrStrengthened = strongerApproxQueue;
						strengthened = true;

						if (!weakerApproxQueue.isEmpty() && (strongerApproxQueue.isEmpty()
								|| DecisionQueueItem.RandomGenerator.nextBoolean())) {
							toBePickedQueueFromWeakenOrStrengthened = weakerApproxQueue;
							strengthened = false;
						}

						afterPickWeakenOrStrengthened();

						beforePickWeakenOrStrengthenedApprox();
						String approximationProperty = toBePickedQueueFromWeakenOrStrengthened
								.poll().getItem().orElseThrow(() -> new RuntimeException(
										"The stronger form cannot be null"));
						beforePickWeakenOrStrengthenedApprox();

						beforeMutating();
						File mutatedFile = makeMutation(toBeingAnalyzedModelPartString,
								toBeingWeakenOrStrengthenedApproximation.b,
								approximationProperty, strengthened, restModel).get();
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
						inExampleIsInteded = inAndOutExamples.a.isPresent()
								? oracle.isIntended(inAndOutExamples.a.get()) : false;
						outExampleIsInteded = inAndOutExamples.b.isPresent()
								? oracle.isIntended(inAndOutExamples.b.get()) : false;

						StringBuilder rowRoport = new StringBuilder();

						rowRoport.append("toBeingAnalyzedModelPart=")
								.append("\"" + toBeingAnalyzedModelPartString + "\"")
								.append(",");
						rowRoport.append("toBeingWeakenOrStrengthenedApproximation=")
								.append("\"" + toBeingWeakenOrStrengthenedApproximation + "\"")
								.append(",");
						rowRoport.append("approximationProperty=")
								.append("\"" + approximationProperty + "\"").append(",");

						rowRoport.append("inExamples=")
								.append("\"" + inAndOutExamples.a.orElse("") + "\"")
								.append(",");
						rowRoport.append("inExampleIsInteded=").append(inExampleIsInteded)
								.append(",");

						rowRoport.append("outExamples=")
								.append("\"" + inAndOutExamples.b.orElse("") + "\"")
								.append(",");
						rowRoport.append("outExampleIsInteded=").append(outExampleIsInteded)
								.append(",");

						rowRoport.append("strengthened=").append(strengthened).append(",");

						if (strengthened) {
							if (inExampleIsInteded) {
								logger.info("The model is correct so far.");
								rowRoport.append("Error=correct,");
								acceptedExamples.add(inAndOutExamples.a.get());
							} else {
								logger.info("The model has underconstraint issue.");
								rowRoport.append("Error=underconstraint,");
								rejectedExamples.add(inAndOutExamples.a.orElse(""));
							}
							if (outExampleIsInteded) {
								logger.info("The model is overconstraint issue.");
								rowRoport.append("Error=overconstraint,");
								acceptedExamples.add(inAndOutExamples.b.get());
							} else {
								logger.info("The model is correct.");
								rowRoport.append("Error=correct,");
								rejectedExamples.add(inAndOutExamples.b.orElse(""));
							}
						} else {
							if (inExampleIsInteded) {
								logger.info("The model is in overconstraint issue");
								rowRoport.append("Error=overconstraint,");
								acceptedExamples.add(inAndOutExamples.a.get());
							} else {
								logger.info("The model is correct.");
								rowRoport.append("Error=correct,");
								rejectedExamples.add(inAndOutExamples.a.orElse(""));
							}
							if (outExampleIsInteded) {
								logger.info("The model is overconstraint issue.");
								rowRoport.append("Error=overconstraint,");
								acceptedExamples.add(inAndOutExamples.b.get());
							} else {
								logger.info("The model is correct.");
								rowRoport.append("Error=correct,");
								rejectedExamples.add(inAndOutExamples.b.orElse(""));
							}
						}

						String row = rowRoport.toString();
						row = row.replaceAll("\n", " and ");
						Pair<String, String> headerRow = Utils.extractHeader(row);
						reportHeader = headerRow.a;
						report.append(headerRow.b).append("\n");
						// store the answer
						if (afterInquiryOracle())
							break;
						// Call APIs to change the priority of the next steps
						if (!strongerApproxQueue.isEmpty() || !weakerApproxQueue.isEmpty())
							beforePickWeakenOrStrengthened();
					}
					if (!approximationQueue.isEmpty())
						if (beforePickApproximation())
							break;
				}
				if (!modelQueue.isEmpty())
					beforePickModelPart();
			}
			if (!fieldsQueue.isEmpty())
				beforePickField();
		}
		System.out.println("--------------------------");
		System.out.println(reportHeader);
		System.out.println(report);
		
		System.out.println(approximator.getAllChachedResults());
		try {
			Util.writeAll("tmp/" + sourceFile.getName() + ".csv",
					reportHeader + "\n" + report);
		} catch (Err e) {
			e.printStackTrace();
		}
		System.out.println("--------------------------");
	}

	protected abstract boolean afterInquiryOracle();

	protected abstract void beforeInquiryOracle();

	protected abstract void afterCallingExampleFinder();

	protected abstract void beforeCallingExampleFinder();

	protected abstract void afterMutating();

	protected abstract void beforeMutating();

	protected abstract void beforePickWeakenOrStrengthenedApprox();

	protected abstract void afterPickWeakenOrStrengthened();

	protected abstract void beforePickWeakenOrStrengthened();

	protected abstract void afterPickApproximation();

	protected abstract boolean beforePickApproximation();

	protected abstract void afterPickModelPart();

	protected abstract void beforePickModelPart();

	protected abstract void afterPickField();

	protected abstract void beforePickField();

	protected abstract void onStartLoop();

	/**
	 * 
	 * @param toBeingAnalyzedModelPart
	 *          A part of the model that is being analyzed.
	 * @param property
	 *          A property that implied from toBeingAnalyzedModelPart
	 * @param approximationProperty
	 *          An approximation of property. It could be weaker or stronger.
	 * @param strenghtened
	 *          strengthened = (approximationProperty => property)
	 * @param restModel
	 * @return
	 */
	protected Optional<File> makeMutation(String toBeingAnalyzedModelPart,
			String property, String approximationProperty, boolean strengthened,
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

		String approximatedProperty = new String();
		String notApproximatedProperty = new String();
		if (toBeingAnalyzedModelPart.equals(property)) {
			approximatedProperty = "( " + toBeingAnalyzedModelPart + ")";
			notApproximatedProperty = "(not " + toBeingAnalyzedModelPart + ")";
		} else if (property.equals(approximationProperty)) {
			approximatedProperty = "(not " + toBeingAnalyzedModelPart + " and "
					+ property + ")";
			notApproximatedProperty = "(not " + approximatedProperty + ")";
		} else {
			if (strengthened) {
				// need to be extended.
				approximatedProperty = approximationProperty;
				notApproximatedProperty = "(not " + approximationProperty + " and "
						+ property + ")";
			} else {
				approximatedProperty = "(not " + property + " and "
						+ approximationProperty + ")";
				notApproximatedProperty = "(not " + approximationProperty + ")";
			}
		}

		String modelPartialApproximation = approximatedProperty
				+ (!restModel.trim().isEmpty() ? " and " + restModel : "");

		String notModelPartialApproximation = notApproximatedProperty
				+ (!restModel.trim().isEmpty() ? " and " + restModel : "");

		System.out.println("*************");
		System.out.println("toBeingAnalyzedModelPart=" + toBeingAnalyzedModelPart);
		System.out.println("property=" + property);
		System.out.println("approximationProperty=" + approximationProperty);
		System.out.println("strengthened=" + strengthened);
		System.out
				.println("modelPartialApproximation=" + modelPartialApproximation);
		System.out.println(
				"notModelPartialApproximation=" + notModelPartialApproximation);

		System.out.println("*************");

		// Make a new Model
		String predName = "approximate_"
				+ Math.abs(modelPartialApproximation.hashCode());
		String pred = String.format("pred %s[]{%s}\n", predName,
				modelPartialApproximation);

		String predNameNot = "NOT_" + predName;
		String notPred = String.format("pred %s[]{%s}\n", predNameNot,
				notModelPartialApproximation);

		String newHeader = "open "
				+ relationalPropModule.getName().replace(".als", "\n");
		newHeader += "open " + temporalPropModule.getName().replace(".als", "\n");
		String newCommandName = "run " + predName + " " + scope;
		String newCommandNameNot = "run " + predNameNot + " " + scope;

		String newCode = newHeader + "\n" + sourceCode + "\n" + pred + "\n"
				+ notPred + "\n" + newCommandName + "\n" + newCommandNameNot;
		File newCodeFile = new File(destinationDir, predName + ".als");
		try {
			System.out.println("The mutated file is: "+ newCodeFile.getAbsolutePath());
			Util.writeAll(newCodeFile.getAbsolutePath(), newCode);
		} catch (Err e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return Optional.ofNullable(newCodeFile);
	}

	/**
	 * Given an expr and field, the implication approximation of the expr is
	 * determined and put in the approximations map. If the approximations map has
	 * already have the implication analysis result, the call to approximator is
	 * ignored.
	 * 
	 * @param expr
	 * @param field
	 */
	protected void fillApproximations(Expr expr, Field field) {

		if (approximations.containsKey(field)
				&& approximations.get(field).containsKey(expr))
			return;
		try {
			List<Pair<String, String>> approximation_ = approximator
					.strongestImplicationApproximation(expr, field, scope);
			if (approximation_.isEmpty()) {
				String toBeingAnalyzedModelPartString = PrettyPrintExpression
						.makeString(expr);
				approximation_ = Arrays
						.asList(
								new Pair<String, String>(
										toBeingAnalyzedModelPartString.replace("[", "")
												.replace("]", "").replace(" ", ""),
										toBeingAnalyzedModelPartString));
			}
			if (!approximations.containsKey(field))
				approximations.put(field, new HashMap<>());
			if (!approximations.get(field).containsKey(expr))
				approximations.get(field).put(expr, new LinkedList<>());
			approximations.get(field).get(expr).addAll(approximation_);
		} catch (Err e) {
			e.printStackTrace();
			logger.severe(Utils.threadName() + expr
					+ " cannot be converted to an inorder form.");
			throw new RuntimeException(e);
		}
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

	/**
	 * Create an instance of the class. The subclasses should return their object
	 * and types.
	 * 
	 * @param sourceFile
	 * @param destinationDir
	 * @param approximator
	 * @param oracle
	 * @param exampleFinder
	 * @return
	 */
	public abstract DebuggerAlgorithm createIt(final File sourceFile,
			final File destinationDir, final Approximator approximator,
			final Oracle oracle, final ExampleFinder exampleFinder);

}
