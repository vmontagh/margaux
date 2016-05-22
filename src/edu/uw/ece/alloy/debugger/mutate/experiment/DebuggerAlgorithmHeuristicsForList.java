/**
 * 
 */
package edu.uw.ece.alloy.debugger.mutate.experiment;

import java.io.File;

import edu.mit.csail.sdg.alloy4.Err;
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

	// Whether an expression is inconsistent by itself.
	boolean inconsistentExpressions = false;

	public DebuggerAlgorithmHeuristicsForList(File sourceFile,
			File destinationDir, Approximator approximator, Oracle oracle,
			ExampleFinder exampleFinder) {
		super(sourceFile, destinationDir, approximator, oracle, exampleFinder);
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
		System.out.println(constraint.toString() + " " + toBeingAnalyzedField);
		// find out whether an expression is inconsistent by itself
		try {
			inconsistentExpressions = super.approximator
					.isInconsistent(constraint, toBeingAnalyzedField, scope).isEmpty();
		} catch (Err e) {
			e.printStackTrace();
			logger.severe(Utils.threadName() + constraint + " cannot be converted to an inorder form.");
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void beforePickField() {
	}

	@Override
	protected void onStartLoop() {
	}

}
