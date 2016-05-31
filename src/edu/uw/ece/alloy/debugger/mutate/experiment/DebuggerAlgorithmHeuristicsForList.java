package edu.uw.ece.alloy.debugger.mutate.experiment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.uw.ece.alloy.debugger.PrettyPrintExpression;
import edu.uw.ece.alloy.debugger.mutate.Approximator;
import edu.uw.ece.alloy.debugger.mutate.DebuggerAlgorithm;
import edu.uw.ece.alloy.debugger.mutate.ExampleFinder;
import edu.uw.ece.alloy.debugger.mutate.Oracle;
import edu.uw.ece.alloy.util.Utils;

/**
 * @author vajih
 *
 */
public class DebuggerAlgorithmHeuristicsForList extends DebuggerAlgorithm {

	final public static DebuggerAlgorithmHeuristicsForList EMPTY_ALGORITHM = new DebuggerAlgorithmHeuristicsForList();
	protected final static Logger logger = Logger
			.getLogger(DebuggerAlgorithmHeuristicsForList.class.getName() + "--"
					+ Thread.currentThread().getName());

	// Whether an expression is inconsistent by itself.
	boolean inconsistentExpressions = false;
	// A map from an expression and weakest inconsistent properties.
	final Map<Field, Map<Expr, List<Pair<String, String>>>> weakestInconsistentProps,
			allInconsistentProps;

	protected DebuggerAlgorithmHeuristicsForList(File sourceFile,
			File destinationDir, Approximator approximator, Oracle oracle,
			ExampleFinder exampleFinder) {
		super(sourceFile, destinationDir, approximator, oracle, exampleFinder);
		weakestInconsistentProps = new HashMap<>();
		allInconsistentProps = new HashMap<>();
	}

	protected DebuggerAlgorithmHeuristicsForList() {
		super();
		weakestInconsistentProps = null;
		allInconsistentProps = null;
	}

	@Override
	protected void afterInquiryOracle() {
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
	protected void afterMutating() {
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

		// RULE: if an expression is inconsistent by itself, then do not Strengthen
		// it.
		if (inconsistentExpressions) {
			// emptying the strongerApproxQueue prevents any strengthening
			strongerApproxQueue.clear();
		}
		// RULE: any approximation that is inconsistent with other expressions
		// should be
		// removed or has lower priority.
		PriorityQueue<DecisionQueueItem<String>> newStrongerApproxQueue = new PriorityQueue<>();
		for (DecisionQueueItem<String> prop : strongerApproxQueue) {
			if (!isInconsistentWithOtherStatments(prop.getItem().get())) {
				newStrongerApproxQueue.add(prop);
			}
		}
		strongerApproxQueue = newStrongerApproxQueue;

		PriorityQueue<DecisionQueueItem<String>> newWeakerApproxQueue = new PriorityQueue<>();
		for (DecisionQueueItem<String> prop : weakerApproxQueue) {
			if (!isInconsistentWithOtherStatments(prop.getItem().get())) {
				newWeakerApproxQueue.add(prop);
			}
		}
		weakerApproxQueue = newWeakerApproxQueue;
	}

	protected boolean isInconsistentWithOtherStatments(String pattern) {
		
		return  allInconsistentProps.keySet().stream()
				.map(f -> allInconsistentProps.get(f)).map(e -> e.values())
				.flatMap(Collection::stream).flatMap(Collection::stream)
				.anyMatch(p -> p.b.equals(pattern));
	}

	@Override
	protected void afterPickApproximation() {
	}

	@Override
	protected void beforePickApproximation() {
	}

	@Override
	protected void afterPickModelPart() {
	}

	@Override
	protected void beforePickModelPart() {
	}

	@Override
	protected void afterPickField() {
		// find out whether an expression is inconsistent by itself
		try {

			inconsistentExpressions = super.approximator.isInconsistent(constraint,
					toBeingAnalyzedField, scope);
		} catch (Err e) {
			e.printStackTrace();
			logger.severe(Utils.threadName() + constraint
					+ " cannot be converted to an inorder form.");
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void beforePickField() {
	}

	@Override
	protected void onStartLoop() {
		Set<Expr> notApproximatedExprs = new HashSet<>();
		for (Field field : super.fields) {
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
						weakestInconsistentProps.get(field).get(expr).addAll(approximator
								.weakestInconsistentApproximation(expr, field, scope));

						if (!allInconsistentProps.containsKey(field))
							allInconsistentProps.put(field, new HashMap<>());
						if (!allInconsistentProps.get(field).containsKey(expr))
							allInconsistentProps.get(field).put(expr, new LinkedList<>());

						for (Pair<String, String> val : weakestInconsistentProps.get(field)
								.get(expr)) {
							allInconsistentProps.get(field).get(expr).add(val);
							for (Pair<String, String> sotrongerProp : approximator
									.strongerProperties(val.a, field.label)) {
								allInconsistentProps.get(field).get(expr).add(sotrongerProp);
							}
						}
					} catch (Err e) {
						logger.severe(Utils.threadName()
								+ " could not find incosistent properties for " + field + " "
								+ expr);
						e.printStackTrace();
					}
				}

				// find all implication approximations
				fillApproximations(expr, field);
				List<Pair<String, String>> approximatedExpr = super.approximations
						.get(field).get(expr);
				String exprString = expr.toString();
				try {
					exprString = PrettyPrintExpression.makeString(expr);
				} catch (Err e) {
					e.printStackTrace();
				}
				if (approximatedExpr.size() == 1
						&& approximatedExpr.get(0).a.equals(exprString)) {
					notApproximatedExprs.add(expr);
				}
			}
		}

		// RULE: if a given expression does not approximated by any pattern, then it
		// should be weaken by its
		// negation. Such expression has lower priority compared to the expressions
		// that could be approximated
		// by one or more predefined patterns.
		int minPriority = super.modelQueue.stream()
				.mapToInt(a -> a.getScore().get()).max()
				.orElse(DecisionQueueItem.MinUniformScore);
		for (DecisionQueueItem<Expr> modelPart : super.modelQueue) {
			if (notApproximatedExprs.contains(modelPart.getItem().get())) {
				modelPart.setScore(minPriority + 1);
			}
		}

		modelQueue.stream().forEach(
				m -> System.out.println(m.getItem().get() + " " + m.getScore().get()));
	}

	@Override
	public DebuggerAlgorithmHeuristicsForList createIt(File sourceFile,
			File destinationDir, Approximator approximator, Oracle oracle,
			ExampleFinder exampleFinder) {
		return new DebuggerAlgorithmHeuristicsForList(sourceFile, destinationDir,
				approximator, oracle, exampleFinder);
	}

}
