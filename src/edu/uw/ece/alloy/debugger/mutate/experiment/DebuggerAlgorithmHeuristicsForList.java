package edu.uw.ece.alloy.debugger.mutate.experiment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.uw.ece.alloy.debugger.mutate.Approximator;
import edu.uw.ece.alloy.debugger.mutate.DebuggerAlgorithm;
import edu.uw.ece.alloy.debugger.mutate.ExampleFinder;
import edu.uw.ece.alloy.debugger.mutate.Oracle;
import edu.uw.ece.alloy.debugger.mutate.DebuggerAlgorithm.DecisionQueueItem;
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
	final Map<Pair<Expr, Field>, List<Pair<String, String>>> weakestInconsistentProps;
	final Map<Pair<Expr, Field>, List<Pair<String, String>>> allInconsistentProps;

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
		// RULE: any approximation that is inconsistent with other expressions should be 
		// removed or has lower priority.
		PriorityQueue<DecisionQueueItem<String>> newStrongerApproxQueue = new PriorityQueue<>();
		for (DecisionQueueItem<String> prop: strongerApproxQueue){
			if (!isInconsistentWithOtherStatments(prop.getItem().get())){
				newStrongerApproxQueue.add(prop);
			}
		}
		strongerApproxQueue = newStrongerApproxQueue;
		
		PriorityQueue<DecisionQueueItem<String>> newWeakerApproxQueue = new PriorityQueue<>();
		for (DecisionQueueItem<String> prop: weakerApproxQueue){
			if (!isInconsistentWithOtherStatments(prop.getItem().get())){
				newWeakerApproxQueue.add(prop);
			}
		}
		weakerApproxQueue = newWeakerApproxQueue;
	}
	
	protected boolean isInconsistentWithOtherStatments(String pattern){
		for (List<Pair<String, String>> list: allInconsistentProps.values()){
			for (Pair<String, String> inconProp: list){
				if (inconProp.b.equals(pattern)){
					return true;
				}
			}
		}
		return false;
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
		// fill in weakestInconsistentProps and allInconsistentProps
		for (Field field : super.fields) {
			for (Expr expr : model) {
				Pair<Expr, Field> key = new Pair<>(expr, field);
				if (!weakestInconsistentProps.containsKey(key)) {
					try {
						weakestInconsistentProps.put(key, approximator
								.weakestInconsistentApproximation(expr, field, scope));
						allInconsistentProps.put(key, new ArrayList<>());
						for (Pair<String, String> val : weakestInconsistentProps.get(key)) {
							allInconsistentProps.get(key).add(val);
							for (Pair<String, String> sotrongerProp : approximator
									.strongerProperties(val.a, field.label)) {
								allInconsistentProps.get(key).add(sotrongerProp);
							}
						}
					} catch (Err e) {
						logger.severe(Utils.threadName()
								+ " could not find incosistent properties for " + key);
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public DebuggerAlgorithmHeuristicsForList createIt(File sourceFile,
			File destinationDir, Approximator approximator, Oracle oracle,
			ExampleFinder exampleFinder) {
		return new DebuggerAlgorithmHeuristicsForList(sourceFile, destinationDir,
				approximator, oracle, exampleFinder);
	}

}
