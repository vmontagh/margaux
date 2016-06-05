package edu.uw.ece.alloy.debugger.mutate.experiment;

import java.io.File;

import edu.uw.ece.alloy.debugger.mutate.Approximator;
import edu.uw.ece.alloy.debugger.mutate.DebuggerAlgorithm;
import edu.uw.ece.alloy.debugger.mutate.ExampleFinder;
import edu.uw.ece.alloy.debugger.mutate.Oracle;

/**
 * @author vajih
 *
 */
public class DebuggerAlgorithmRandom extends DebuggerAlgorithm {

	final public static DebuggerAlgorithmRandom EMPTY_ALGORITHM = new DebuggerAlgorithmRandom();
	
	protected DebuggerAlgorithmRandom(File sourceFile, File destinationDir,
			Approximator approximator, Oracle oracle, ExampleFinder exampleFinder) {
		super(sourceFile, destinationDir, approximator, oracle, exampleFinder);
	}

	public DebuggerAlgorithmRandom() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.uw.ece.alloy.debugger.mutate.DebuggerAlgorithm#afterInquiryOracle()
	 */
	@Override
	protected boolean afterInquiryOracle() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.uw.ece.alloy.debugger.mutate.DebuggerAlgorithm#beforeInquiryOracle()
	 */
	@Override
	protected void beforeInquiryOracle() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uw.ece.alloy.debugger.mutate.DebuggerAlgorithm#
	 * afterCallingExampleFinder()
	 */
	@Override
	protected void afterCallingExampleFinder() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uw.ece.alloy.debugger.mutate.DebuggerAlgorithm#
	 * beforeCallingExampleFinder()
	 */
	@Override
	protected void beforeCallingExampleFinder() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uw.ece.alloy.debugger.mutate.DebuggerAlgorithm#afterMutating()
	 */
	@Override
	protected void afterMutating() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uw.ece.alloy.debugger.mutate.DebuggerAlgorithm#beforeMutating()
	 */
	@Override
	protected void beforeMutating() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uw.ece.alloy.debugger.mutate.DebuggerAlgorithm#
	 * beforePickWeakenOrStrengthenedApprox()
	 */
	@Override
	protected void beforePickWeakenOrStrengthenedApprox() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uw.ece.alloy.debugger.mutate.DebuggerAlgorithm#
	 * afterPickWeakenOrStrengthened()
	 */
	@Override
	protected void afterPickWeakenOrStrengthened() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uw.ece.alloy.debugger.mutate.DebuggerAlgorithm#
	 * beforePickWeakenOrStrengthened()
	 */
	@Override
	protected void beforePickWeakenOrStrengthened() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.uw.ece.alloy.debugger.mutate.DebuggerAlgorithm#afterPickApproximation()
	 */
	@Override
	protected void afterPickApproximation() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.uw.ece.alloy.debugger.mutate.DebuggerAlgorithm#beforePickApproximation(
	 * )
	 */
	@Override
	protected boolean beforePickApproximation() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.uw.ece.alloy.debugger.mutate.DebuggerAlgorithm#afterPickModelPart()
	 */
	@Override
	protected void afterPickModelPart() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.uw.ece.alloy.debugger.mutate.DebuggerAlgorithm#beforePickModelPart()
	 */
	@Override
	protected void beforePickModelPart() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uw.ece.alloy.debugger.mutate.DebuggerAlgorithm#afterPickField()
	 */
	@Override
	protected void afterPickField() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uw.ece.alloy.debugger.mutate.DebuggerAlgorithm#beforePickField()
	 */
	@Override
	protected void beforePickField() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uw.ece.alloy.debugger.mutate.DebuggerAlgorithm#onStartLoop()
	 */
	@Override
	protected void onStartLoop() {
	}

	public DebuggerAlgorithmRandom createIt(final File sourceFile,
			final File destinationDir, final Approximator approximator,
			final Oracle oracle, final ExampleFinder exampleFinder) {
		return new DebuggerAlgorithmRandom(sourceFile, destinationDir, approximator,
				oracle, exampleFinder);
	}

}
