package edu.uw.ece.alloy.debugger.mutate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Predicate;
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
import edu.mit.csail.sdg.alloy4compiler.ast.ExprList;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprList.Op;
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
	 * The class pair any item to an integer that is comparable to be compared
	 * in the priority queue.
	 * 
	 * @author vajih
	 *
	 * @param <T>
	 */
	public static class DecisionQueueItem<T>
			implements Comparator<DecisionQueueItem<T>>, Comparable<DecisionQueueItem<T>> {
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

		public static <T> DecisionQueueItem<T> createwithRandomPriority(final T item) {
			return new DecisionQueueItem<T>(item,
					RandomGenerator.nextInt(MaxUniformScore - MinUniformScore + 1) + MinUniformScore);
		}

		public static <T> DecisionQueueItem<T> createwithUnformPriority(final T item) {
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
			.getLogger(DebuggerAlgorithm.class.getName() + "--" + Thread.currentThread().getName());

	final protected Map<Integer, Pair<REVIEW_RESPONSE, String>> resultInterpretaionMap;

	public enum REC {
		TRUE, FALSE, DONTCARE
	};

	public enum REVIEW_RESPONSE {
		ACCEPT, REJECT, NA
	};

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
	final protected Expr modelExpr;
	// In a model in the form of M => P, P is a conjunction of constraints.
	// constraint = model => property
	final protected List<Expr> property;// = Collections.emptyList();

	// final
	protected Approximator approximator;
	final Oracle oracle;
	final ExampleFinder exampleFinder;

	final protected PriorityQueue<DecisionQueueItem<Field>> fieldsQueue;
	final protected Map<Field, PriorityQueue<DecisionQueueItem<Expr>>> fieldToModelQueues;
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

	/*
	 * If a model part is not approximated, but is inconsistent with other
	 * properties, so is save here
	 */
	final protected Map<Field, Map<Expr, List<Pair<String, String>>>> notApproximationedButInconsistent;

	protected final Map<Field, Map<Expr, Map<Pair<String, String>, PriorityQueue<DecisionQueueItem<String>>>>> strongerApproxQueues,
			weakerApproxQueues;

	// Whether an expression is inconsistent by itself.
	protected boolean inconsistentExpressions = false;
	// A map from an expression and weakest inconsistent properties.
	protected final Map<Field, Map<Expr, List<Pair<String, String>>>> weakestInconsistentProps, allInconsistentProps,
			weakestConsistentProps;

	/*
	 * A PQ to determine which approximation should be fixed first. It varies at
	 * each iteration
	 */
	final protected Map<Field, Map<Expr, PriorityQueue<DecisionQueueItem<Pair<String, String>>>>> approximationQueues;
	/* The property that is chosen to be weaken or strengthened */
	protected Pair<String, String> toBeingWeakenOrStrengthenedApproximation;
	PriorityQueue<DecisionQueueItem<String>> toBePickedQueueFromWeakenOrStrengthened;
	protected boolean strengthened;
	protected File mutatedFile;
	/*
	 * findOnBorderExamples finds two examples close to border:
	 * inAndOutExamples.a is inside and inAndOutExamples.b is outside.
	 */
	protected Pair<Optional<String>, Optional<String>> inAndOutExamples;

	protected boolean inExampleIsInteded, outExampleIsInteded;

	/* Examples that are reviewed by oracle are stored in the following sets */
	final protected Set<String> acceptedExamples, rejectedExamples;
	final Set<String> skipSequences, reviewedExamples;

	final File newReviewedExamples;

	/**
	 * 
	 * @param sourceFile
	 * @param destinationDir
	 * @param approximator
	 * @param oracle
	 * @param exampleFinder
	 * @param reviewedExamples
	 *            A file containing all examples that have been reviewed
	 *            previously and are used for killing
	 * @param newREviewedExamples
	 *            A file for storing the reviewed examples. New reviewed files
	 *            plus previous ones.
	 * @param skipTerms
	 *            The terms should be skipped due to incomplete implementation.
	 */
	public DebuggerAlgorithm(final File sourceFile, final File destinationDir, final Approximator approximator,
			final Oracle oracle, final ExampleFinder exampleFinder, final File reviewedExamples,
			final File newReviewedExamples, final File skipTerms) {
		this.sourceFile = sourceFile;
		this.sourceCode = Utils.readFile(sourceFile.getAbsolutePath());
		CompModule world;
		try {
			world = CompUtil.parseEverything_fromFile(A4Reporter.NOP, null, sourceFile.getAbsolutePath());
		} catch (Err e) {
			logger.severe(Utils.threadName() + "The Alloy file cannot be loaded or parsed:" + sourceFile + "\n" + e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		if (world.getAllCommands().size() != 1)
			throw new RuntimeException(
					"Only one valid command should be passed. " + "Comment out the rest or add at least one.");

		final Command command = world.getAllCommands().get(0);
		constraint = command.formula;

		Pair<List<Expr>, List<Expr>> propertyChecking = Decompose.decomposetoImplications(constraint);
		model = Collections.unmodifiableList(propertyChecking.a);
		modelExpr = ExprList.make(model.get(0).pos(), model.get(model.size() - 1).pos(), Op.AND, model);
		property = Collections.unmodifiableList(propertyChecking.b);
		scope = ExtractorUtils.extractScopeFromCommand(command);
		try {
			fields = Collections.unmodifiableList(
					FieldsExtractorVisitor.getReferencedFields(constraint).stream().collect(Collectors.toList()));
		} catch (Err e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		this.approximator = approximator;
		this.oracle = oracle;
		this.exampleFinder = exampleFinder;

		// The fields are picked based on their priority in the priority queue
		fieldsQueue = new PriorityQueue<>();
		fieldToModelQueues = new HashMap<>();
		approximationQueues = new HashMap<>();
		strongerApproxQueues = new HashMap<>();
		weakerApproxQueues = new HashMap<>();

		approximations = new HashMap<>();
		notApproximationedButInconsistent = new HashMap<>();
		this.destinationDir = destinationDir;

		acceptedExamples = new HashSet<>();
		rejectedExamples = new HashSet<>();
		weakestInconsistentProps = new HashMap<>();
		weakestConsistentProps = new HashMap<>();
		allInconsistentProps = new HashMap<>();
		resultInterpretaionMap = new HashMap<>();

		skipSequences = new HashSet<>();
		this.reviewedExamples = new HashSet<>();
		this.newReviewedExamples = newReviewedExamples;

		if (reviewedExamples.exists())
			this.reviewedExamples.addAll(Utils.readFileLines(reviewedExamples.getAbsolutePath()));
		if (skipTerms.exists())
			this.skipSequences.addAll(Utils.readFileLines(skipTerms.getAbsolutePath()));

	}

	protected DebuggerAlgorithm() {
		sourceFile = null;
		destinationDir = null;
		sourceCode = null;
		fields = null;
		constraint = null;
		scope = null;
		model = null;
		modelExpr = null;
		property = null;
		oracle = null;
		exampleFinder = null;
		fieldsQueue = null;
		fieldToModelQueues = null;
		approximationQueues = null;
		strongerApproxQueues = null;
		weakerApproxQueues = null;
		approximations = null;
		notApproximationedButInconsistent = null;
		acceptedExamples = null;
		rejectedExamples = null;
		weakestInconsistentProps = null;
		weakestConsistentProps = null;
		allInconsistentProps = null;
		resultInterpretaionMap = null;
		skipSequences = null;
		reviewedExamples = null;
		this.newReviewedExamples = null;
	}

	/**
	 * Given a statement, then an strongest approximation is found and returned.
	 * The return is a the actual call. E.g. acyclic[r]
	 * 
	 * @param statement
	 * @return
	 */
	public String strongestApproximation(Expr statement, Field field) {
		return null; // approximator.strongestApproximationModel(statement,
						// field,
						// scope);
	}

	public void run() {

		StringBuilder report = new StringBuilder();
		StringBuilder MutationsPath = new StringBuilder();
		String reportHeader = new String();

		System.out.println("fields->" + fields);
		// The private fields are not considered. The Order/next fields are
		// private.
		fields.stream().filter(f -> f.isPrivate == null).forEach(field -> {
			fieldsQueue.add(DecisionQueueItem.<Field> createwithRandomPriority(field));
			fieldToModelQueues.put(field, model.stream().map(m -> DecisionQueueItem.<Expr> createwithRandomPriority(m))
					.collect(Collectors.toCollection(PriorityQueue::new)));

		});
		System.out.println("fieldsQueue->" + fieldsQueue);
		onStartLoop();
		beforePickField();
		while (!fieldsQueue.isEmpty()) {
			DecisionQueueItem<Field> field = fieldsQueue.poll();
			toBeingAnalyzedField = field.getItem().get();
			checkIfModelIsInconsistent();
			if (afterPickField())
				break;

			// System.exit(-1);
			beforePickModelPart();
			final PriorityQueue<DecisionQueueItem<Expr>> modelQueue = fieldToModelQueues.get(toBeingAnalyzedField);
			while (!fieldToModelQueues.get(toBeingAnalyzedField).isEmpty()) {
				System.out.println("before--->" + modelQueue);
				System.out.println("before 2--->" + fieldToModelQueues.get(toBeingAnalyzedField));
				DecisionQueueItem<Expr> modelPart = fieldToModelQueues.get(toBeingAnalyzedField).poll();
				System.out.println("after--->" + modelQueue);
				System.out.println("after 2--->" + fieldToModelQueues.get(toBeingAnalyzedField));

				toBeingAnalyzedModelPart = modelPart.getItem().get();
				System.out.println("picked model part before after is:" + toBeingAnalyzedModelPart);
				if (afterPickModelPart())
					continue;

				String toBeingAnalyzedModelPartString = convertModelPartToString();
				restModelParts = model.stream().filter(m -> !m.equals(toBeingAnalyzedModelPart))
						.collect(Collectors.toList());
				String restModel = restModelParts.stream().map(m -> m.toString()).collect(Collectors.joining(" and "));

				System.out.println("Model part: " + toBeingAnalyzedModelPartString);
				System.out.println("Rest: " + restModel);

				fillApproximations(toBeingAnalyzedModelPart, toBeingAnalyzedField);
				List<Pair<String, String>> approximation = approximations.get(toBeingAnalyzedField)
						.get(toBeingAnalyzedModelPart);
				logger.info(Utils.threadName() + "The approximations for Expr:<" + toBeingAnalyzedModelPart + "> is: "
						+ approximation);

				System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
				System.out.println(toBeingAnalyzedModelPart);
				System.out.println(toBeingAnalyzedField);
				System.out.println(scope);
				System.out.println(approximation);
				System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
				if (!approximationQueues.containsKey(toBeingAnalyzedField)) {
					approximationQueues.put(toBeingAnalyzedField, new HashMap<>());
				}
				if (!approximationQueues.get(toBeingAnalyzedField).containsKey(toBeingAnalyzedModelPart)) {
					// initializing the priority queue.
					approximationQueues.get(toBeingAnalyzedField)
							.put(toBeingAnalyzedModelPart,
									approximation.stream()
											.map(m -> DecisionQueueItem
													.<Pair<String, String>> createwithRandomPriority(m))
											.collect(Collectors.toCollection(PriorityQueue::new)));
				}
				// converting the approximations into a PQ.
				PriorityQueue<DecisionQueueItem<Pair<String, String>>> approximationQueue = approximationQueues
						.get(toBeingAnalyzedField).get(toBeingAnalyzedModelPart);

				beforePickApproximation();
				while (!approximationQueue.isEmpty()) {
					DecisionQueueItem<Pair<String, String>> approx = approximationQueue.poll();
					toBeingWeakenOrStrengthenedApproximation = approx.getItem().get();
					afterPickApproximation();
					if (!strongerApproxQueues.containsKey(toBeingAnalyzedField))
						strongerApproxQueues.put(toBeingAnalyzedField, new HashMap<>());
					if (!strongerApproxQueues.get(toBeingAnalyzedField).containsKey(toBeingAnalyzedModelPart))
						strongerApproxQueues.get(toBeingAnalyzedField).put(toBeingAnalyzedModelPart, new HashMap<>());
					if (!strongerApproxQueues.get(toBeingAnalyzedField).get(toBeingAnalyzedModelPart)
							.containsKey(toBeingWeakenOrStrengthenedApproximation)) {
						strongerApproxQueues.get(toBeingAnalyzedField).get(toBeingAnalyzedModelPart).put(
								toBeingWeakenOrStrengthenedApproximation,
								approximator
										.strongerProperties(toBeingWeakenOrStrengthenedApproximation.a,
												toBeingAnalyzedField.label)
										.stream().map(s -> DecisionQueueItem.<String> createwithRandomPriority(s.b))
										.collect(Collectors.toCollection(PriorityQueue::new)));
						System.out.println("In stronger if: fld= " + toBeingAnalyzedField + ", part="
								+ toBeingAnalyzedModelPart + " = " + approximator.strongerProperties(
										toBeingWeakenOrStrengthenedApproximation.a, toBeingAnalyzedField.label));
						System.out.println(approximator
								.strongerProperties(toBeingWeakenOrStrengthenedApproximation.a,
										toBeingAnalyzedField.label)
								.stream().map(s -> DecisionQueueItem.<String> createwithRandomPriority(s.b))
								.collect(Collectors.toCollection(PriorityQueue::new)));
						System.out.println("strongerApproxQueues=" + strongerApproxQueues);
						System.out.println("After=" + strongerApproxQueues.get(toBeingAnalyzedField)
								.get(toBeingAnalyzedModelPart).get(toBeingWeakenOrStrengthenedApproximation));
					}
					if (!weakerApproxQueues.containsKey(toBeingAnalyzedField))
						weakerApproxQueues.put(toBeingAnalyzedField, new HashMap<>());
					if (!weakerApproxQueues.get(toBeingAnalyzedField).containsKey(toBeingAnalyzedModelPart))
						weakerApproxQueues.get(toBeingAnalyzedField).put(toBeingAnalyzedModelPart, new HashMap<>());
					if (!weakerApproxQueues.get(toBeingAnalyzedField).get(toBeingAnalyzedModelPart)
							.containsKey(toBeingWeakenOrStrengthenedApproximation))
						weakerApproxQueues.get(toBeingAnalyzedField).get(toBeingAnalyzedModelPart).put(
								toBeingWeakenOrStrengthenedApproximation,
								Stream.concat(
										// The current pattern should also be
										// added to the
										// list
										Arrays.asList(toBeingWeakenOrStrengthenedApproximation).stream(),
										approximator.weakerProperties(toBeingWeakenOrStrengthenedApproximation.a,
												toBeingAnalyzedField.label).stream())
										.map(s -> DecisionQueueItem.<String> createwithRandomPriority(s.b))
										.collect(Collectors.toCollection(PriorityQueue::new)));
					beforePickWeakenOrStrengthened();

					PriorityQueue<DecisionQueueItem<String>> strongerApproxQueue = strongerApproxQueues
							.get(toBeingAnalyzedField).get(toBeingAnalyzedModelPart)
							.get(toBeingWeakenOrStrengthenedApproximation);
					PriorityQueue<DecisionQueueItem<String>> weakerApproxQueue = weakerApproxQueues
							.get(toBeingAnalyzedField).get(toBeingAnalyzedModelPart)
							.get(toBeingWeakenOrStrengthenedApproximation);
					toBePickedQueueFromWeakenOrStrengthened = strongerApproxQueue;

					while (!strongerApproxQueue.isEmpty() || !weakerApproxQueue.isEmpty()) {
						toBePickedQueueFromWeakenOrStrengthened = strongerApproxQueue;
						strengthened = true;
						if (!weakerApproxQueue.isEmpty()
								&& (strongerApproxQueue.isEmpty() || DecisionQueueItem.RandomGenerator.nextBoolean())) {
							toBePickedQueueFromWeakenOrStrengthened = weakerApproxQueue;
							strengthened = false;
						}
						afterPickWeakenOrStrengthened();

						beforePickWeakenOrStrengthenedApprox();
						String approximationProperty = toBePickedQueueFromWeakenOrStrengthened.poll().getItem()
								.orElseThrow(() -> new RuntimeException("The stronger form cannot be null"));
						beforePickWeakenOrStrengthenedApprox();
						beforeMutating();

						// The model part is not approximated.
						if (!notApproximationedButInconsistent.get(toBeingAnalyzedField).get(toBeingAnalyzedModelPart)
								.isEmpty() && inconsistentExpressions) {

							for (Pair<String, String> incon : notApproximationedButInconsistent
									.get(toBeingAnalyzedField).get(toBeingAnalyzedModelPart)) {
								// The mutatedFile is global to be accessible
								// within other functions.
								strengthened = false;
								mutatedFile = makeWeakeningModelPartMutationWithInconsistent(
										toBeingAnalyzedModelPartString, restModel, incon.b).get();

								System.out.println("mutation:--->" + mutatedFile.getAbsolutePath());
								MutationsPath.append(mutatedFile.getAbsolutePath()).append("\n");
								if (afterMutating())
									continue;
								beforeCallingExampleFinder();
								// the examples are in Alloy format.
								inAndOutExamples = exampleFinder.findOnBorderExamples(mutatedFile,
										mutatedFile.getName().replace(".als", ""),
										"NOT_" + mutatedFile.getName().replace(".als", ""));
								afterCallingExampleFinder();
								beforeInquiryOracle();

								Pair<String, String> headerRow = Utils
										.extractHeader(interpretMutationResultByMap(approximationProperty, incon.a));
								reportHeader = headerRow.a;
								report = filterRow(report, headerRow.b);
								// store the answer
								// if (afterInquiryOracle())
								// break;
								// Call APIs to change the priority of the next
								// steps
								if (!strongerApproxQueue.isEmpty() || !weakerApproxQueue.isEmpty())
									beforePickWeakenOrStrengthened();
							}

						} else {

							// The mutatedFile is global to be accessible within
							// other functions.
							mutatedFile = (strengthened
									? makeStrengtheningModelPartMutation(toBeingAnalyzedModelPartString, restModel,
											toBeingWeakenOrStrengthenedApproximation.b, approximationProperty)
									: makeWeakeningModelPartMutation(toBeingAnalyzedModelPartString, restModel,
											toBeingWeakenOrStrengthenedApproximation.b, approximationProperty)).get();
							System.out.println("mutation:--->" + mutatedFile.getAbsolutePath());
							MutationsPath.append(mutatedFile.getAbsolutePath()).append("\n");
							if (afterMutating())
								continue;
							beforeCallingExampleFinder();
							// the examples are in Alloy format.
							inAndOutExamples = exampleFinder.findOnBorderExamples(mutatedFile,
									mutatedFile.getName().replace(".als", ""),
									"NOT_" + mutatedFile.getName().replace(".als", ""));
							afterCallingExampleFinder();
							beforeInquiryOracle();
							// ask the user
							// Interpreting the result
							Pair<String, String> headerRow = Utils.extractHeader(interpretMutationResultByMap(
									toBeingWeakenOrStrengthenedApproximation.b, approximationProperty));
							reportHeader = headerRow.a;
							report = filterRow(report, headerRow.b);
							// store the answer
							if (afterInquiryOracle())
								break;
							// Call APIs to change the priority of the next
							// steps
							if (!strongerApproxQueue.isEmpty() || !weakerApproxQueue.isEmpty())
								beforePickWeakenOrStrengthened();
						}
					}
					if (!approximationQueue.isEmpty())
						if (beforePickApproximation())
							break;
				}
				if (!modelQueue.isEmpty())
					beforePickModelPart();
			}

			// check the whole statement
			// The model is sat
			if (!inconsistentExpressions) {
				strengthened = true;
				toBeingAnalyzedModelPart = modelExpr;

				for (Pair<Pair<String, String>, File> file : DFSOnProperties(toBeingAnalyzedModelPart,
						getWeakestPropertiesConsistetWithConstraint(toBeingAnalyzedField, propertiesSorter()),
						getStrongerPropertiesConsistentWithConstraintOfAProperty(toBeingAnalyzedField.label,
								propertiesSorter()))) {
					mutatedFile = file.b;
					MutationsPath.append(mutatedFile.getAbsolutePath()).append("\n");
					inAndOutExamples = exampleFinder.findOnBorderExamples(mutatedFile,
							mutatedFile.getName().replace(".als", ""),
							"NOT_" + mutatedFile.getName().replace(".als", ""));
					afterCallingExampleFinder();
					beforeInquiryOracle();
					// ask the user
					// Interpreting the result
					Pair<String, String> headerRow = Utils
							.extractHeader(interpretMutationResultByMap(file.a.a, file.a.b));
					reportHeader = headerRow.a;
					report = filterRow(report, headerRow.b);
					if (afterInquiryOracle())
						break;

				}

			}

			if (!fieldsQueue.isEmpty())
				beforePickField();
		}

		System.out.println("--------------------------");
		System.out.println(reportHeader);
		System.out.println(report);

		System.out.println(MutationsPath.toString());

		System.out.println(approximator.getAllChachedResults());
		try {
			Util.writeAll("tmp/" + sourceFile.getName() + "." + this.getClass().getSimpleName() + ".csv",
					reportHeader + "\n" + report);
		} catch (Err e) {
			e.printStackTrace();
		}
		System.out.println("--------------------------");
		System.out.println("resultInterpretaionMap=" + resultInterpretaionMap);
		try {
			Util.writeAll(newReviewedExamples.getAbsolutePath(),
					reviewedExamples.stream().collect(Collectors.joining("\n")));
		} catch (Err e) {
			e.printStackTrace();
		}
	}

	/**
	 * Given a row, the function filters them based on some criteria and add the
	 * passed rows to report.
	 * 
	 * @param report
	 * @param row
	 */
	protected StringBuilder filterRow(StringBuilder report, String row) {
		//if (!reviewedExamples.contains(inAndOutExamples.b.orElse(""))) {
			//if (!row.contains("error"))
				report.append(row).append("\n");
			//inAndOutExamples.b.ifPresent(a -> reviewedExamples.add(a));
		//}
		return report;
	}

	protected abstract Comparator<String> propertiesSorter();

	protected Function<Expr, List<String>> getWeakestPropertiesConsistetWithConstraint(Field toBeingAnalyzedField,
			Comparator<String> porpertySorter) {
		return (Expr C) -> {
			// TODO is already stored in weakestConsistentProps but because of
			// the type incompatibility, it cannot be used here.
			List<String> res = weakestConsistentProps.get(toBeingAnalyzedField).get(C).stream()
					.sorted((p1, p2) -> porpertySorter.compare(p1.a, p2.a)).map(p -> p.b)
					.filter((String s) -> skipSequences.stream().anyMatch(a -> !s.contains(a)))
					.collect(Collectors.toList());
			return res;
		};
	}

	protected Function<String, List<String>> getStrongerPropertiesConsistentWithConstraintOfAProperty(String fieldLabel,
			Comparator<String> porpertySorter) {
		return (String P) -> {
			List<String> res = approximator.strongerNextProperties(P.substring(0, P.indexOf("[")), fieldLabel).stream()
					.sorted(((p1, p2) -> porpertySorter.compare(p1.a, p2.a))).map(p -> p.b)
					.filter((String s) -> skipSequences.stream().anyMatch(a -> !s.contains(a)))
					.collect(Collectors.toList());

			if (P.contains("_Glbl_") && !res.contains(P.replace("_Glbl_", "_Lcl_"))) {
				ArrayList<String> tmpResult = new ArrayList<>();
				tmpResult.add(P.replace("_Glbl_", "_Lcl_"));
				tmpResult.addAll(res);
				res = tmpResult;
			}
			return res;
		};
	}

	protected List<Pair<Pair<String, String>, File>> DFSOnProperties(Expr constraint,
			Function<Expr, List<String>> getWeakestPropertiesConsistetWithConstraint,
			Function<String, List<String>> getStrongerPropertiesConsistentWithConstraintOfAProperty) {
		final Stack<String> stack = new Stack<>();
		// storing all the properties that their children are mutated.
		final Set<Pair<String, String>> donePairs = new HashSet<>();
		final Set<String> doneProps = new HashSet<>();
		final List<Pair<Pair<String, String>, File>> result = new LinkedList<>();
		stack.push(convertModelPartToString(constraint));
		while (!stack.isEmpty()) {
			String p = stack.peek();
			String o = "";
			if (stack.size() == 1) {
				assert p.equals(convertModelPartToString(constraint));
				for (String weakestConsistentProperty : getWeakestPropertiesConsistetWithConstraint.apply(constraint)) {
					if (!donePairs.contains(new Pair<>(p, weakestConsistentProperty))) {
						o = weakestConsistentProperty;
						break;
					}
				}
			} else {
				for (String weakestConsistentProperty : getStrongerPropertiesConsistentWithConstraintOfAProperty
						.apply(p)) {
					if (!donePairs.contains(new Pair<>(p, weakestConsistentProperty))) {
						o = weakestConsistentProperty;
						break;
					}
				}
			}

			System.out.println("<p=" + p + ", o=" + o + ">");

			if (o.isEmpty()) {
				// all outgoing edges are done
				doneProps.add(p);
				stack.pop();
			} else {
				if (!doneProps.contains(o))
					stack.push(o);
				donePairs.add(new Pair<>(p, o));
				Optional<File> mutatedFile = makeStrengtheningModelMutation(convertModelPartToString(constraint), p, o);
				final Pair<String, String> strengthendPair = new Pair<>(p, o);
				mutatedFile.ifPresent(m -> result.add(new Pair<>(strengthendPair, m)));
			}
		}

		return Collections.unmodifiableList(result);
	}

	protected Optional<File> makeStrengtheningModelPartMutation(String modelPart, String restModel, String property,
			String approximation) {

		interpretStrengtheningModelPartMutation();

		String approximatedProperty = new String();
		String notApproximatedProperty = new String();
		final String restModels = !restModel.trim().isEmpty() ? " and " + restModel : "";
		// The model is not approximated by any pattern. So that it will be
		// weaken/strengthened by itself
		if (modelPart.equals(property)) {
			// The approximation is supposed to be the negation of the modelPart
			approximatedProperty = "( " + approximation + " )" + restModels;
			notApproximatedProperty = "( " + modelPart + ")";

		} else
		// the property cannot be strengthened to any stronger pattern
		if (property.equals(approximation)) {
			approximatedProperty = "(" + modelPart + ")" + restModels;
			notApproximatedProperty = "( not(" + modelPart + "))" + restModels;
		} else {
			approximatedProperty = modelPart + " and (not(" + approximation + "))" + restModels;
			notApproximatedProperty = modelPart + " and (" + approximation + ")" + restModels;
		}
		return makeMutation(approximatedProperty, notApproximatedProperty);
	}

	protected void interpretStrengtheningModelPartMutation() {
		resultInterpretaionMap.clear();
		// in/out, exampleExists?, intended?, strengthened?

		registerResultInterpretation(REC.TRUE, REC.TRUE, REC.TRUE, REC.TRUE, REVIEW_RESPONSE.NA, "correct");
		registerResultInterpretation(REC.TRUE, REC.TRUE, REC.FALSE, REC.TRUE, REVIEW_RESPONSE.NA, "underconstraint");
		registerResultInterpretation(REC.TRUE, REC.FALSE, REC.TRUE, REC.TRUE, REVIEW_RESPONSE.NA, "error-unsat");
		registerResultInterpretation(REC.TRUE, REC.FALSE, REC.FALSE, REC.TRUE, REVIEW_RESPONSE.NA, "error-unsat");

		registerResultInterpretation(REC.FALSE, REC.TRUE, REC.TRUE, REC.TRUE, REVIEW_RESPONSE.ACCEPT, "correct");
		registerResultInterpretation(REC.FALSE, REC.TRUE, REC.FALSE, REC.TRUE, REVIEW_RESPONSE.REJECT,
				"underconstraint");
		registerResultInterpretation(REC.FALSE, REC.FALSE, REC.TRUE, REC.TRUE, REVIEW_RESPONSE.NA, "error-unsat");
		registerResultInterpretation(REC.FALSE, REC.FALSE, REC.FALSE, REC.TRUE, REVIEW_RESPONSE.NA, "error-unsat");

		registerResultInterpretation(REC.DONTCARE, REC.DONTCARE, REC.DONTCARE, REC.FALSE, REVIEW_RESPONSE.NA,
				"error-noweaken");
	}

	protected Optional<File> makeWeakeningModelPartMutation(String modelPart, String restModel, String property,
			String approximation) {

		interpretWeakeningModelPartMutation();

		String approximatedProperty = new String();
		String notApproximatedProperty = new String();
		final String restModels = !restModel.trim().isEmpty() ? " and " + restModel : "";

		// The model is not approximated by any pattern. So that it will be
		// weaken/strengthened by itself
		if (modelPart.equals(property)) {
			// The approximation is supposed to be the negation of the modelPart
			approximatedProperty = "( " + modelPart + ")" + restModels;
			notApproximatedProperty = "( not " + approximation + " )" + restModels;

		} else if (property.equals(approximation)) {
			approximatedProperty = "(" + modelPart + ")";
			notApproximatedProperty = "(not " + modelPart + " and " + property + ")" + restModels;
		} else {
			approximatedProperty = "(" + modelPart + ")";
			notApproximatedProperty = "(not " + modelPart + " and " + approximation + ")" + restModels;
		}
		return makeMutation(approximatedProperty, notApproximatedProperty);
	}

	protected void interpretWeakeningModelPartMutation() {
		resultInterpretaionMap.clear();
		// in/out, exampleExists?, intended?, strengthened?

		registerResultInterpretation(REC.TRUE, REC.TRUE, REC.TRUE, REC.FALSE, REVIEW_RESPONSE.NA, "correct");
		registerResultInterpretation(REC.TRUE, REC.TRUE, REC.FALSE, REC.FALSE, REVIEW_RESPONSE.NA,
				"overconstraint-random");
		registerResultInterpretation(REC.TRUE, REC.FALSE, REC.TRUE, REC.FALSE, REVIEW_RESPONSE.NA, "error-unsat");
		registerResultInterpretation(REC.TRUE, REC.FALSE, REC.FALSE, REC.FALSE, REVIEW_RESPONSE.NA, "error-unsat");

		registerResultInterpretation(REC.FALSE, REC.TRUE, REC.TRUE, REC.FALSE, REVIEW_RESPONSE.ACCEPT,
				"overconstraint");
		registerResultInterpretation(REC.FALSE, REC.TRUE, REC.FALSE, REC.FALSE, REVIEW_RESPONSE.REJECT, "correct");
		registerResultInterpretation(REC.FALSE, REC.FALSE, REC.TRUE, REC.FALSE, REVIEW_RESPONSE.NA, "error-unsat");
		registerResultInterpretation(REC.FALSE, REC.FALSE, REC.FALSE, REC.FALSE, REVIEW_RESPONSE.NA, "error-unsat");

		registerResultInterpretation(REC.DONTCARE, REC.DONTCARE, REC.DONTCARE, REC.TRUE, REVIEW_RESPONSE.NA,
				"error-nostrengthen");
	}

	protected Optional<File> makeWeakeningModelPartMutationWithInconsistent(String modelPart, String restModel,
			String inConProperty) {

		interpretWeakeningModelPartMutationWithInconsistent();

		String approximatedProperty = new String();
		String notApproximatedProperty = new String();
		final String restModels = !restModel.trim().isEmpty() ? " and " + restModel : "";

		// The model is not approximated by any pattern. So that it will be
		// weaken/strengthened by itself
		if (inConProperty.isEmpty()) {
			// The approximation is supposed to be the negation of the modelPart
			approximatedProperty = "( " + modelPart + ")";
			notApproximatedProperty = "( not " + modelPart + " )" + restModels;

		} else {
			approximatedProperty = "(" + modelPart + ")";
			notApproximatedProperty = "(not " + modelPart + " and " + inConProperty + ")" + restModels;
		}
		return makeMutation(approximatedProperty, notApproximatedProperty);
	}

	protected void interpretWeakeningModelPartMutationWithInconsistent() {
		resultInterpretaionMap.clear();
		// in/out, exampleExists?, intended?, strengthened?

		registerResultInterpretation(REC.TRUE, REC.TRUE, REC.TRUE, REC.FALSE, REVIEW_RESPONSE.NA,
				"overconstraint-opposite");
		registerResultInterpretation(REC.TRUE, REC.TRUE, REC.FALSE, REC.FALSE, REVIEW_RESPONSE.NA, "correct");
		registerResultInterpretation(REC.TRUE, REC.FALSE, REC.TRUE, REC.FALSE, REVIEW_RESPONSE.NA, "correct-unsat");
		registerResultInterpretation(REC.TRUE, REC.FALSE, REC.FALSE, REC.FALSE, REVIEW_RESPONSE.NA, "error-unsat");

		registerResultInterpretation(REC.FALSE, REC.TRUE, REC.TRUE, REC.FALSE, REVIEW_RESPONSE.ACCEPT,
				"overconstraint");
		registerResultInterpretation(REC.FALSE, REC.TRUE, REC.FALSE, REC.FALSE, REVIEW_RESPONSE.REJECT, "correct");
		registerResultInterpretation(REC.FALSE, REC.FALSE, REC.TRUE, REC.FALSE, REVIEW_RESPONSE.NA, "error-unsat");
		registerResultInterpretation(REC.FALSE, REC.FALSE, REC.FALSE, REC.FALSE, REVIEW_RESPONSE.NA, "error-unsat");

		registerResultInterpretation(REC.DONTCARE, REC.DONTCARE, REC.DONTCARE, REC.TRUE, REVIEW_RESPONSE.NA,
				"error-nostrengthen");
	}

	protected Optional<File> makeStrengtheningModelMutation(String model, String property, String approximation) {

		String approximatedProperty = new String();
		String notApproximatedProperty = new String();
		if (model.equals(property)) {
			approximatedProperty = "( " + approximation + " ) and " + model;
			notApproximatedProperty = "( not " + approximation + ") and" + model;
		} else {
			approximatedProperty = model + " and " + property + " and " + approximation;
			notApproximatedProperty = model + " and " + property + " and not " + approximation;
		}
		interpretStrengtheningModelMutation();
		return makeMutation(approximatedProperty, notApproximatedProperty);
	}

	protected void interpretStrengtheningModelMutation() {
		resultInterpretaionMap.clear();
		// in/out, exampleExists?, intended?, strengthened?

		registerResultInterpretation(REC.TRUE, REC.TRUE, REC.TRUE, REC.TRUE, REVIEW_RESPONSE.NA, "correct");
		registerResultInterpretation(REC.TRUE, REC.TRUE, REC.FALSE, REC.TRUE, REVIEW_RESPONSE.NA, "underconstraint");
		registerResultInterpretation(REC.TRUE, REC.TRUE, REC.TRUE, REC.FALSE, REVIEW_RESPONSE.NA, "error");
		registerResultInterpretation(REC.TRUE, REC.TRUE, REC.FALSE, REC.FALSE, REVIEW_RESPONSE.NA, "error");

		registerResultInterpretation(REC.FALSE, REC.TRUE, REC.FALSE, REC.TRUE, REVIEW_RESPONSE.REJECT,
				"underconstraint");
		registerResultInterpretation(REC.FALSE, REC.TRUE, REC.FALSE, REC.FALSE, REVIEW_RESPONSE.NA, "error");
		registerResultInterpretation(REC.FALSE, REC.TRUE, REC.TRUE, REC.TRUE, REVIEW_RESPONSE.ACCEPT, "correct");
		registerResultInterpretation(REC.FALSE, REC.TRUE, REC.TRUE, REC.FALSE, REVIEW_RESPONSE.NA, "error");

		registerResultInterpretation(REC.DONTCARE, REC.FALSE, REC.DONTCARE, REC.DONTCARE, REVIEW_RESPONSE.NA, "error");
	}

	/**
	 * @param in
	 *            inExample or outExample?
	 * @param exampleExists
	 *            Does example exists?
	 * @param intended
	 *            Is example intended?
	 * @param strengthened
	 *            strengthened/Weakened
	 * @return
	 */
	protected Integer encodeInterpretation(boolean in, boolean exampleExists, boolean intended, boolean strengthened) {
		int result = 0;
		result |= in ? 1 << 3 : 0;
		result |= exampleExists ? 1 << 2 : 0;
		result |= intended ? 1 << 1 : 0;
		result |= strengthened ? 1 << 0 : 0;
		return result;
	}

	protected void registerResultInterpretation(REC in, REC exampleExists, REC intended, REC strengthened,
			REVIEW_RESPONSE accepted, String message) {

		if (in.equals(REC.DONTCARE)) {
			registerResultInterpretation(REC.TRUE, exampleExists, intended, strengthened, accepted, message);
			registerResultInterpretation(REC.FALSE, exampleExists, intended, strengthened, accepted, message);
		}

		if (exampleExists.equals(REC.DONTCARE)) {
			registerResultInterpretation(in, REC.TRUE, intended, strengthened, accepted, message);
			registerResultInterpretation(in, REC.FALSE, intended, strengthened, accepted, message);
		}

		if (intended.equals(REC.DONTCARE)) {
			registerResultInterpretation(in, exampleExists, REC.TRUE, strengthened, accepted, message);
			registerResultInterpretation(in, exampleExists, REC.FALSE, strengthened, accepted, message);
		}

		if (strengthened.equals(REC.DONTCARE)) {
			registerResultInterpretation(in, exampleExists, intended, REC.TRUE, accepted, message);
			registerResultInterpretation(in, exampleExists, intended, REC.FALSE, accepted, message);
		}

		registerResultInterpretation(in.equals(REC.TRUE), exampleExists.equals(REC.TRUE), intended.equals(REC.TRUE),
				strengthened.equals(REC.TRUE), accepted, message);

	}

	protected void registerResultInterpretation(boolean in, boolean exampleExists, boolean intended,
			boolean strengthened, REVIEW_RESPONSE accepted, String message) {
		int encoded = encodeInterpretation(in, exampleExists, intended, strengthened);
		resultInterpretaionMap.put(encoded, new Pair<>(accepted, message + "-" + Integer.toString(encoded, 2)));
	}

	protected Pair<REVIEW_RESPONSE, String> interpretResult(boolean in, boolean exampleExists, boolean intended,
			boolean strengthened) {
		Pair<REVIEW_RESPONSE, String> result = new Pair<>(REVIEW_RESPONSE.NA, "UNINTERPRETED4-in=" + in + "-exists="
				+ exampleExists + "-intended=" + intended + "-strength=" + strengthened);
		if (resultInterpretaionMap.containsKey(encodeInterpretation(in, exampleExists, intended, strengthened))) {
			result = resultInterpretaionMap.get(encodeInterpretation(in, exampleExists, intended, strengthened));
		}
		return result;
	}

	boolean continueRecordingReviews = true;

	protected String interpretMutationResultByMap(String property, String approximation) {
		StringBuilder rowReport = new StringBuilder();
		rowReport.append("toBeingAnalyzedModelPart=").append("\"" + convertModelPartToString() + "\"").append(",");
		rowReport.append("property=").append("\"" + property + "\"").append(",");
		rowReport.append("approximationProperty=").append("\"" + approximation + "\"").append(",");

		rowReport.append("inExamples=").append("\"" + inAndOutExamples.a.orElse("").replaceAll("=", " eq ") + "\"")
				.append(",");
		rowReport.append("inExampleIsInteded=").append(inExampleIsInteded).append(",");

		System.out.println("out example=" + inAndOutExamples.b);

		rowReport.append("outExamples=").append("\"" + inAndOutExamples.b.orElse("").replaceAll("=", " eq ") + "\"")
				.append(",");
		rowReport.append("outExampleIsInteded=").append(outExampleIsInteded).append(",");

		rowReport.append("strengthened=").append(strengthened).append(",");

		inExampleIsInteded = inAndOutExamples.a.isPresent() ? oracle.isIntended(inAndOutExamples.a.get()) : false;
		Pair<REVIEW_RESPONSE, String> result = interpretResult(true, inAndOutExamples.a.isPresent(), inExampleIsInteded,
				strengthened);
		if (inAndOutExamples.a.isPresent() && result.a.equals(REVIEW_RESPONSE.ACCEPT))
			acceptedExamples.add(inAndOutExamples.a.get());
		if (inAndOutExamples.a.isPresent() && result.a.equals(REVIEW_RESPONSE.REJECT))
			rejectedExamples.add(inAndOutExamples.a.get());
		rowReport.append("Error=").append(result.b).append(",");

		outExampleIsInteded = inAndOutExamples.b.isPresent() ? oracle.isIntended(inAndOutExamples.b.get()) : false;
		result = interpretResult(false, inAndOutExamples.b.isPresent(), outExampleIsInteded, strengthened);
		if (inAndOutExamples.b.isPresent() && result.a.equals(REVIEW_RESPONSE.ACCEPT))
			acceptedExamples.add(inAndOutExamples.b.get());
		if (inAndOutExamples.b.isPresent() && result.a.equals(REVIEW_RESPONSE.REJECT))
			rejectedExamples.add(inAndOutExamples.b.get());
		rowReport.append("Error=").append(result.b).append(",");

		
		String example = inAndOutExamples.b.orElse("").replace("\n", " and ").replace(" eq ", " = ");
		
		// record until an error is detected
		// only outexample matters.
		if (!result.a.equals(REVIEW_RESPONSE.NA) && continueRecordingReviews && !reviewedExamples.contains(example)) {
			reviewedExamples.add(example);
			if (!(strengthened ^ result.a.equals(REVIEW_RESPONSE.REJECT))) {
				// a bug is found
				assert (!result.b.contains("correct")) : "An error should be detected! the result is:" + result;
				continueRecordingReviews = false;
			}
		}

		return rowReport.toString().replaceAll("\n", " and ");
	}

	/**
	 * @return
	 */
	protected Optional<File> makeMutation(String approximatedProperty, String notApproximatedProperty) {

		// make sure the relationalPropModule and temporalPropModule files
		// are tin the path.
		File relationalPropModule = new File(destinationDir, approximator.relationalPropModule.getName());
		if (!relationalPropModule.exists())
			try {
				Files.copy(approximator.relationalPropModule, relationalPropModule);
			} catch (IOException e1) {
				e1.printStackTrace();
				logger.severe(Utils.threadName() + " " + approximator.relationalPropModule
						+ " cannot be copied in destination folder: " + relationalPropModule);
			}

		File temporalPropModule = new File(destinationDir, approximator.temporalPropModule.getName());
		if (!temporalPropModule.exists())
			try {
				Files.copy(approximator.temporalPropModule, temporalPropModule);
			} catch (IOException e1) {
				e1.printStackTrace();
				logger.severe(Utils.threadName() + " " + approximator.temporalPropModule
						+ " cannot be copied in destination folder: " + temporalPropModule);
			}

		final List<String> generatedExamples = new LinkedList<>();
		final String acceptedGeneratedExamples = getAllNoAcceptedExamples();
		if (!acceptedGeneratedExamples.isEmpty())
			generatedExamples.add(acceptedGeneratedExamples);
		/*
		 * final String rejectedGeneratedExamples = getAllNoRejectedExamples();
		 * if (!rejectedGeneratedExamples.isEmpty())
		 * generatedExamples.add(rejectedGeneratedExamples);
		 */
		String notExistingExamples = Arrays.asList(getAllNoAcceptedExamples(), getAllNoRejectedExamples()).stream()
				.filter(a -> !a.isEmpty()).map(a -> "not(" + a + ")").collect(Collectors.joining(" and "));
		notExistingExamples = notExistingExamples.isEmpty() ? "" : " and (" + notExistingExamples + ")";
		String modelPartialApproximation = approximatedProperty
				/* + notExistingExamples */ + " some " + toBeingAnalyzedField.label;

		String notModelPartialApproximation = notApproximatedProperty + " some " + toBeingAnalyzedField.label;

		// Make a new Model
		String predName = "approximate_"
				+ Math.abs(modelPartialApproximation.hashCode() + notApproximatedProperty.hashCode());
		String pred = String.format("pred %s[]{%s}\n", predName, modelPartialApproximation);

		String predNameNot = "NOT_" + predName;
		String notPred = String.format("pred %s[]{%s}\n", predNameNot, notModelPartialApproximation);

		String newHeader = "open " + relationalPropModule.getName().replace(".als", "\n");
		newHeader += "open " + temporalPropModule.getName().replace(".als", "\n");
		String newCommandName = "run " + predName + " " + scope;
		String newCommandNameNot = "run " + predNameNot + " " + scope;

		String newCode = newHeader + "\n" + sourceCode + "\n" + pred + "\n" + notPred + "\n" + newCommandName + "\n"
				+ newCommandNameNot;
		File newCodeFile = new File(destinationDir, predName + ".als");
		try {
			System.out.println("The mutated file is: " + newCodeFile.getAbsolutePath());
			Util.writeAll(newCodeFile.getAbsolutePath(), newCode);
		} catch (Err e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return Optional.ofNullable(newCodeFile);
	}

	/**
	 * Given an expr and field, the implication approximation of the expr is
	 * determined and put in the approximations map. If the approximations map
	 * has already have the implication analysis result, the call to
	 * approximator is ignored.
	 * 
	 * @param expr
	 * @param field
	 */
	protected void fillApproximations(Expr expr, Field field) {

		if (!(approximations.containsKey(field) && approximations.get(field).containsKey(expr))) {
			try {
				List<Pair<String, String>> approximation_ = approximator.strongestImplicationApproximation(expr, field,
						scope);
				List<Pair<String, String>> inconsistencies_ = new ArrayList<>();
				if (approximation_.isEmpty()) {
					String toBeingAnalyzedModelPartString = PrettyPrintExpression.makeString(expr);
					approximation_ = Arrays.asList(new Pair<String, String>(
							toBeingAnalyzedModelPartString.replace("[", "").replace("]", "").replace(" ", ""),
							toBeingAnalyzedModelPartString));

					inconsistencies_.addAll(approximator.weakestInconsistentApproximation(expr, field, scope));

				}
				if (!approximations.containsKey(field)) {
					approximations.put(field, new HashMap<>());
					notApproximationedButInconsistent.put(field, new HashMap<>());
				}
				if (!approximations.get(field).containsKey(expr)) {
					approximations.get(field).put(expr, new LinkedList<>());
					notApproximationedButInconsistent.get(field).put(expr, new LinkedList<>());
				}
				approximations.get(field).get(expr).addAll(approximation_);
				notApproximationedButInconsistent.get(field).get(expr).addAll(inconsistencies_);
			} catch (Err e) {
				e.printStackTrace();
				logger.severe(Utils.threadName() + expr + " cannot be converted to an inorder form.");
				throw new RuntimeException(e);
			}
		}
		// fill the consistency
		if (!(weakestConsistentProps.containsKey(field) && weakestConsistentProps.get(field).containsKey(modelExpr))) {
			if (!weakestConsistentProps.containsKey(field))
				weakestConsistentProps.put(field, new HashMap<>());
			if (!weakestConsistentProps.get(field).containsKey(modelExpr))
				weakestConsistentProps.get(field).put(modelExpr, new ArrayList<>());
			// compute the weakest consistent properties with modelExpr
			try {
				weakestConsistentProps.get(field).get(modelExpr)
						.addAll(approximator.weakestConsistentApproximation(modelExpr, field, scope));
			} catch (Err e) {
				logger.severe(
						Utils.threadName() + " could not find incosistent properties for " + field + " " + modelExpr);
				e.printStackTrace();
			}
		}
		System.out.println("notApproximationedButInconsistent-->" + notApproximationedButInconsistent);

	}

	/**
	 * A conjunction of accepted instances is returned (some ..) and (some ..)
	 * If the accepted examples is empty the return is an empty string.
	 * 
	 * @return
	 */
	public String getAllSomeAcceptedExamples() {
		return acceptedExamples.stream().filter(a -> !a.isEmpty()).map(a -> "(" + a + ")")
				.collect(Collectors.joining(" and "));
	}

	public String getAllNoAcceptedExamples() {
		return acceptedExamples.stream().filter(a -> !a.isEmpty())// .map(a ->
																	// "(" +
																	// a.replaceFirst("some
																	// ", "no ")
																	// + ")")
				.collect(Collectors.joining(" and "));
	}

	/**
	 * A conjunction of rejected examples is returned: (no ...) and (no ..) If
	 * the rejected examples is empty the return is an empty string.
	 * 
	 * @return
	 */
	public String getAllNoRejectedExamples() {
		return rejectedExamples.stream().filter(a -> !a.isEmpty())// .map(a ->
																	// "(" +
																	// a.replaceFirst("some
																	// ", "no ")
																	// + ")")
				.collect(Collectors.joining(" and "));
	}

	public String getAllSomeRejectedExamples() {
		return rejectedExamples.stream().filter(a -> !a.isEmpty()).map(a -> "(" + a + ")")
				.collect(Collectors.joining(" and "));
	}

	protected Optional<Field> pickField() {
		return this.fields.size() > 0 ? Optional.of(this.fields.remove(0)) : Optional.empty();
	}

	protected void checkIfModelIsInconsistent() {
		// find out whether an expression is inconsistent by itself
		try {
			inconsistentExpressions = approximator.isInconsistent(modelExpr, toBeingAnalyzedField, scope);
		} catch (Err e) {
			e.printStackTrace();
			logger.severe(Utils.threadName() + constraint + " cannot be converted to an inorder form.");
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DebuggerAlgorithm [sourceFile=" + sourceFile + ", fields=" + fields + ", constraint=" + constraint
				+ ", model=" + model + ", property=" + property + "]";
	}

	protected String convertModelPartToString(Expr xpr) {
		String toBeingAnalyzedModelPartString = xpr.toString();
		try {
			toBeingAnalyzedModelPartString = PrettyPrintExpression.makeString(xpr);
		} catch (Err e) {
			e.printStackTrace();
		}
		return toBeingAnalyzedModelPartString;
	}

	protected String convertModelPartToString() {
		return convertModelPartToString(toBeingAnalyzedModelPart);
	}

	/**
	 * Create an instance of the class. The subclasses should return their
	 * object and types.
	 * 
	 * @param sourceFile
	 * @param destinationDir
	 * @param approximator
	 * @param oracle
	 * @param exampleFinder
	 * @return
	 */
	public abstract DebuggerAlgorithm createIt(final File sourceFile, final File destinationDir,
			final Approximator approximator, final Oracle oracle, final ExampleFinder exampleFinder, final File reviewedExamples,
			final File newReviewedExamples, final File skipTerms);

	protected abstract boolean afterInquiryOracle();

	protected abstract void beforeInquiryOracle();

	protected abstract void afterCallingExampleFinder();

	protected abstract void beforeCallingExampleFinder();

	protected abstract boolean afterMutating();

	protected abstract void beforeMutating();

	protected abstract void beforePickWeakenOrStrengthenedApprox();

	protected abstract void afterPickWeakenOrStrengthened();

	protected abstract void beforePickWeakenOrStrengthened();

	protected abstract void afterPickApproximation();

	protected abstract boolean beforePickApproximation();

	protected abstract boolean afterPickModelPart();

	protected abstract void beforePickModelPart();

	protected abstract boolean afterPickField();

	protected abstract void beforePickField();

	protected abstract void onStartLoop();

}
