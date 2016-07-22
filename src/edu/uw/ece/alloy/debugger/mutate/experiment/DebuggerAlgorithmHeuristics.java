package edu.uw.ece.alloy.debugger.mutate.experiment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.uw.ece.alloy.MyReporter;
import edu.uw.ece.alloy.debugger.PrettyPrintExpression;
import edu.uw.ece.alloy.debugger.exec.A4CommandExecuter;
import edu.uw.ece.alloy.debugger.filters.FieldsExtractorVisitor;
import edu.uw.ece.alloy.debugger.mutate.Approximator;
import edu.uw.ece.alloy.debugger.mutate.DebuggerAlgorithm;
import edu.uw.ece.alloy.debugger.mutate.ExampleFinder;
import edu.uw.ece.alloy.debugger.mutate.Oracle;
import edu.uw.ece.alloy.util.Utils;

/**
 * @author vajih
 *
 */
public class DebuggerAlgorithmHeuristics extends DebuggerAlgorithm {

	final public static DebuggerAlgorithmHeuristics EMPTY_ALGORITHM = new DebuggerAlgorithmHeuristics();
	protected final static Logger logger = Logger
			.getLogger(DebuggerAlgorithmHeuristics.class.getName() + "--" + Thread.currentThread().getName());

	final public static String ACCECPTED_INSTANCES_PRED_NAME = "_accepted";
	final public static String REJECTED_INSTANCES_PRED_NAME = "_rejected";

	boolean breakApproximationSelection = false;
	// Whether an expression is inconsistent by itself.
	boolean inconsistentExpressions = false;
	// A map from an expression and weakest inconsistent properties.
	final Map<Field, Map<Expr, List<Pair<String, String>>>> weakestInconsistentProps, allInconsistentProps,
			weakestConsistentProps;

	protected DebuggerAlgorithmHeuristics(File sourceFile, File destinationDir, Approximator approximator,
			Oracle oracle, ExampleFinder exampleFinder) {
		super(sourceFile, destinationDir, approximator, oracle, exampleFinder);
		weakestInconsistentProps = new HashMap<>();
		weakestConsistentProps = new HashMap<>();
		allInconsistentProps = new HashMap<>();
	}

	protected DebuggerAlgorithmHeuristics() {
		super();
		weakestInconsistentProps = null;
		weakestConsistentProps = null;
		allInconsistentProps = null;
	}

	@Override
	protected boolean afterInquiryOracle() {
		// RULE: if weakened and other approximation remained and the inExample
		// is correct, then the expression's priority is degraded.

		if (!strengthened && !inExampleIsInteded) {
			fieldToModelQueues
					.get(toBeingAnalyzedField).add(
							new DecisionQueueItem<Expr>(toBeingAnalyzedModelPart,
									fieldToModelQueues.get(toBeingAnalyzedField).stream()
											.mapToInt(a -> a.getScore().get()).min()
											.orElse(DecisionQueueItem.MinUniformScore) - 1));
			approximationQueues.get(toBeingAnalyzedField).get(toBeingAnalyzedModelPart)
					.add(new DecisionQueueItem<Pair<String, String>>(toBeingWeakenOrStrengthenedApproximation,

							approximationQueues.get(toBeingAnalyzedField).get(toBeingAnalyzedModelPart).stream()
									.min((a1, a2) -> a1.compare(a1, a2))
									.map(a -> a.getScore().orElse(DecisionQueueItem.MinUniformScore) - 1)
									.orElse(DecisionQueueItem.MinUniformScore) - 1)

			);
			breakApproximationSelection = true;
			return true;
		}
		return false;
	}

	@Override
	protected void beforeInquiryOracle() {
	}

	@Override
	protected void afterCallingExampleFinder() {
	}

	@Override
	protected void beforeCallingExampleFinder() {
	}

	@Override
	protected boolean afterMutating() {
		boolean result = false;
		final boolean hasAccepted = sourceCode.contains("pred " + ACCECPTED_INSTANCES_PRED_NAME + "{");
		final boolean hasRejcted = sourceCode.contains("pred " + REJECTED_INSTANCES_PRED_NAME + "{");
		String content = "";
		final String predName = mutatedFile.getName().replace(".als", "");

		try {
			content = Util.readAll(mutatedFile.getAbsolutePath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (!content.isEmpty() && hasAccepted) {
			final String acceptedPredName = predName + ACCECPTED_INSTANCES_PRED_NAME;
			final File acceptedFile = new File(mutatedFile.getParentFile(), acceptedPredName + ".als");
			final String acceptedPredContent = String.format("pred %1$s {\n%2$s\n%3$s}\nrun %1$s", acceptedPredName,
					ACCECPTED_INSTANCES_PRED_NAME, predName);
			try {
				Util.writeAll(acceptedFile.getAbsolutePath(), content + "\n" + acceptedPredContent);
				MyReporter rep = new MyReporter();
				A4CommandExecuter.getInstance().runThenGetAnswers(acceptedFile.getAbsolutePath(), rep,
						acceptedPredName);
				System.out.println("acceptedFile->" + acceptedFile);
				result = rep.sat == -1;
			} catch (Err e) {
				e.printStackTrace();
			} finally {
				// acceptedFile.deleteOnExit();
			}
		}

		return result;
	}

	@Override
	protected void beforeMutating() {
	}

	@Override
	protected void beforePickWeakenOrStrengthenedApprox() {
	}

	@Override
	protected void afterPickWeakenOrStrengthened() {
	}

	@Override
	protected void beforePickWeakenOrStrengthened() {

		System.out.println("before strongerApproxQueues->" + strongerApproxQueues);

		// RULE: if an expression is inconsistent by itself, then do not
		// Strengthen it.

		System.out.println("inconsistentExpressions->" + inconsistentExpressions);
		if (inconsistentExpressions) {
			// emptying the strongerApproxQueue prevents any strengthening
			strongerApproxQueues.get(super.toBeingAnalyzedField).get(toBeingAnalyzedModelPart)
					.get(toBeingWeakenOrStrengthenedApproximation).clear();
		}
		// RULE: any approximation that is inconsistent with other expressions
		// should be removed or has lower priority.
		strongerApproxQueues.get(super.toBeingAnalyzedField).get(toBeingAnalyzedModelPart).put(
				toBeingWeakenOrStrengthenedApproximation,
				strongerApproxQueues.get(super.toBeingAnalyzedField).get(toBeingAnalyzedModelPart)
						.get(toBeingWeakenOrStrengthenedApproximation).stream()
						.filter(prop -> !isInconsistentWithOtherStatments(prop.getItem().get()))
						.collect(Collectors.toCollection(PriorityQueue::new)));

		System.out.println("after strongerApproxQueues->" + strongerApproxQueues);

		weakerApproxQueues.get(super.toBeingAnalyzedField).get(toBeingAnalyzedModelPart).put(
				toBeingWeakenOrStrengthenedApproximation,
				weakerApproxQueues.get(super.toBeingAnalyzedField).get(toBeingAnalyzedModelPart)
						.get(toBeingWeakenOrStrengthenedApproximation).stream()
						.filter(prop -> !isInconsistentWithOtherStatments(prop.getItem().get()))
						.collect(Collectors.toCollection(PriorityQueue::new)));

	}

	protected boolean isInconsistentWithOtherStatments(String pattern) {

		return allInconsistentProps.keySet().stream().map(f -> allInconsistentProps.get(f)).map(e -> e.values())
				.flatMap(Collection::stream).flatMap(Collection::stream).anyMatch(p -> p.b.equals(pattern));
	}

	@Override
	protected void afterPickApproximation() {
	}

	@Override
	protected boolean beforePickApproximation() {
		System.out.println("breakApproximationSelection?" + breakApproximationSelection);
		if (breakApproximationSelection) {
			breakApproximationSelection = false;
			return true;
		}
		return false;
	}

	@Override
	protected boolean afterPickModelPart() {
		boolean result = false;
		try {
			Set<Sig.Field> mentionedFields = FieldsExtractorVisitor.getReferencedFields(toBeingAnalyzedModelPart);
			result = !mentionedFields.isEmpty() && !mentionedFields.contains(toBeingAnalyzedField);
			System.out.println("Expr->" + toBeingAnalyzedModelPart + "\nfield->" + toBeingAnalyzedField
					+ "\nmentionedFields->" + mentionedFields + "\nresult->" + result);
		} catch (Err e) {
			logger.severe(Utils.threadName() + " cannot extract the mentioned fields.");
			e.printStackTrace();
		}

		if (!result) {
			String toBeingAnalyzedModelPartString = convertModelPartToString();
			result = convertModelPartToString().startsWith("ACCECPTED_INSTANCES_PRED_NAME")
					|| toBeingAnalyzedModelPartString.startsWith("REJECTED_INSTANCES_PRED_NAME");
		}

		System.out.println("Continue on afterPickModelPart?" + result);

		return result;
	}

	@Override
	protected void beforePickModelPart() {

	}

	@Override
	protected boolean afterPickField() {

		// find out whether an expression is inconsistent by itself
		try {
			inconsistentExpressions = super.approximator.isInconsistent(modelExpr, toBeingAnalyzedField, scope);
		} catch (Err e) {
			e.printStackTrace();
			logger.severe(Utils.threadName() + constraint + " cannot be converted to an inorder form.");
			throw new RuntimeException(e);
		}

		// Heuristic: If a constraint is inconsistent with approximations of
		// another constraint, then the former constraint might be
		// overconstrained and need to be weakened. Hence, weakening the former
		// constraint has a higher priority compared to latter one.

		List<DecisionQueueItem<Expr>> changedPriorityList = new LinkedList<>();
		final PriorityQueue<DecisionQueueItem<Expr>> modelQueue_ = fieldToModelQueues.get(toBeingAnalyzedField);
		while (!modelQueue_.isEmpty()) {
			DecisionQueueItem<Expr> modelPartD = modelQueue_.poll();
			Expr modelPart = modelPartD.getItem().get();
			List<Expr> restModelParts = model.stream().filter(m -> !m.equals(toBeingAnalyzedModelPart))
					.collect(Collectors.toList());

			List<Pair<String, String>> weakestIncon = weakestInconsistentProps.get(toBeingAnalyzedField).get(modelPart);
			Set<String> allImplieds = new HashSet<>();
			for (Expr restModelPart : restModelParts) {
				List<Pair<String, String>> implieds = approximations.get(toBeingAnalyzedField).get(restModelPart);
				for (Pair<String, String> implied : implieds) {
					allImplieds.add(implied.a);
					allImplieds.addAll(approximator.weakerPatterns(implied.a));
				}
			}

			if (weakestIncon.stream().anyMatch(p -> allImplieds.contains(p.a))) {
				modelPartD.setScore(Math.max(
						changedPriorityList.stream().map(w -> w.getScore().get()).max(Integer::compare)
								.orElse(Integer.MIN_VALUE),
						modelQueue_.stream().map(w -> w.getScore().get()).max(Integer::compare)
								.orElse(Integer.MIN_VALUE))
						+ 1);
			}
			changedPriorityList.add(modelPartD);

		}

		fieldToModelQueues.put(toBeingAnalyzedField, new PriorityQueue<DecisionQueueItem<Expr>>(changedPriorityList));

		if (toBeingAnalyzedField.label.contains("waits")) {
			return true;
		}

		return false;

	}

	@Override
	protected void beforePickField() {
	}

	@Override
	protected void onStartLoop() {
		Set<Expr> notApproximatedExprs = new HashSet<>();
		for (Field field : super.fields) {
			if (field.isPrivate != null)
				continue;

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
			for (Expr expr : model) {
				// Pair<Expr, Field> key = new Pair<>(expr, field);
				// fill in weakestInconsistentProps and allInconsistentProps
				if (!(weakestInconsistentProps.containsKey(field)
						&& weakestInconsistentProps.get(field).containsKey(expr))) {
					try {
						if (!weakestInconsistentProps.containsKey(field))
							weakestInconsistentProps.put(field, new HashMap<>());
						if (!weakestInconsistentProps.get(field).containsKey(expr))
							weakestInconsistentProps.get(field).put(expr, new LinkedList<>());
						weakestInconsistentProps.get(field).get(expr)
								.addAll(approximator.weakestInconsistentApproximation(expr, field, scope));

						if (!allInconsistentProps.containsKey(field))
							allInconsistentProps.put(field, new HashMap<>());
						if (!allInconsistentProps.get(field).containsKey(expr))
							allInconsistentProps.get(field).put(expr, new LinkedList<>());

						for (Pair<String, String> val : weakestInconsistentProps.get(field).get(expr)) {
							allInconsistentProps.get(field).get(expr).add(val);
							for (Pair<String, String> sotrongerProp : approximator.strongerProperties(val.a,
									field.label)) {
								allInconsistentProps.get(field).get(expr).add(sotrongerProp);
							}
						}
					} catch (Err e) {
						logger.severe(Utils.threadName() + " could not find incosistent properties for " + field + " "
								+ expr);
						e.printStackTrace();
					}
				}

				// find all implication approximations
				fillApproximations(expr, field);
				List<Pair<String, String>> approximatedExpr = super.approximations.get(field).get(expr);
				String exprString = expr.toString();
				try {
					exprString = PrettyPrintExpression.makeString(expr);
				} catch (Err e) {
					e.printStackTrace();
				}
				if (approximatedExpr.size() == 1 && approximatedExpr.get(0).b.equals(exprString)) {
					notApproximatedExprs.add(expr);
				}
			}

			// RULE: if a given expression does not approximated by any pattern,
			// then it should be weaken by its negation. Such expression has
			// lower priority compared to the expressions that could be
			// approximated by one or more predefined patterns.

			System.out.println("field->" + field);
			System.out.println("fieldToModelQueues->" + fieldToModelQueues);
			System.out.println("fieldToModelQueues.get(field)->" + fieldToModelQueues.get(field));
			final int minPriority = super.fieldToModelQueues.get(field).stream().mapToInt(a -> a.getScore().get()).min()
					.orElse(DecisionQueueItem.MinUniformScore);
			final List<DecisionQueueItem<Expr>> toBeUpdated = new LinkedList<>();
			while (!fieldToModelQueues.get(field).isEmpty()) {
				DecisionQueueItem<Expr> modelPart = fieldToModelQueues.get(field).poll();
				if (notApproximatedExprs.contains(modelPart.getItem().get())) {
					modelPart.setScore(minPriority - 1);

				}
				toBeUpdated.add(modelPart);
			}
			fieldToModelQueues.get(field).addAll(toBeUpdated);
			System.out.println("after fieldToModelQueues.get(" + field + ")->" + fieldToModelQueues.get(field));
		}

		System.out.println("after the loop");
		System.out.println("before heurists:" + fieldsQueue);
		// HEURISTIC: A field with more references should be picked first.
		List<DecisionQueueItem<Sig.Field>> changedPriorityList = new LinkedList<>();
		for (DecisionQueueItem<Sig.Field> field : fieldsQueue) {
			field.setScore(/*-1 */ model.stream().map(e -> {
				try {
					return FieldsExtractorVisitor.getReferencedCountField(e, field.getItem().get());
				} catch (Exception e1) {
					e1.printStackTrace();
					return 0;
				}
			}).collect(Collectors.summingInt(Integer::intValue)));

			changedPriorityList.add(field);
		}
		fieldsQueue.clear();
		fieldsQueue.addAll(changedPriorityList);
		System.out.println("fieldsQueue before exit->" + fieldsQueue);
		System.out.println("End of OnStartLoop");

	}

	@Override
	public DebuggerAlgorithmHeuristics createIt(File sourceFile, File destinationDir, Approximator approximator,
			Oracle oracle, ExampleFinder exampleFinder) {
		return new DebuggerAlgorithmHeuristics(sourceFile, destinationDir, approximator, oracle, exampleFinder);
	}

}
