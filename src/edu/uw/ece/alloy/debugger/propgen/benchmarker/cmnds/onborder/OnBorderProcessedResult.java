package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.onborder;

import java.util.Optional;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.ProcessedResult;

/**
 * @author ooodunay
 *
 */
public class OnBorderProcessedResult extends ProcessedResult {

	private static final long serialVersionUID = -646344483367494297L;
	
	private final String result;
	
	public OnBorderProcessedResult(ProcessingParam param, Status status, String result) {
		super(param, status);
		this.result = result;
	}

	public OnBorderProcessedResult(ProcessingParam param, String result) {
		this(param, Status.NORMAL, result);
	}

	public Optional<String> getResult() {
		return Optional.ofNullable(result);
	}

	@Override
	public String toString() {
		return "OnBorderProcessedResult [result=" + result + ", param=" + param
				+ ", status=" + status + ", sat=" + sat + "]";
	}
}
