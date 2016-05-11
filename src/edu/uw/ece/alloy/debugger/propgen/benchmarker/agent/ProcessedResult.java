package edu.uw.ece.alloy.debugger.propgen.benchmarker.agent;

import edu.mit.csail.sdg.gen.MyReporter;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;

/**
 * @author fikayo
 * 
 */
public abstract class ProcessedResult extends MyReporter {

	private static final long serialVersionUID = 1L;
	protected final ProcessingParam param;

	public static enum Status {
		NORMAL, TIMEOUT, FAILED, INFERRED
	};

	public final Status status;

	protected ProcessedResult(final ProcessingParam param, Status status) {
		this.param = param;
		this.status = status;
	}

	protected ProcessedResult(final ProcessingParam param) {
		this(param, Status.NORMAL);
	}

	public boolean isTimedout() {
		return status.equals(Status.TIMEOUT);
	}

	public boolean isFailed() {
		return status.equals(Status.FAILED);
	}

	public boolean isInferred() {
		return status.equals(Status.INFERRED);
	}

	public boolean isNormal() {
		return status.equals(Status.NORMAL);
	}

	public ProcessingParam getParam() {
		return param;
	}

}
