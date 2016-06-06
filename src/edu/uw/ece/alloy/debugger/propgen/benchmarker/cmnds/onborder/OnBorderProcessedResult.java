package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.onborder;

import java.util.HashMap;
import java.util.Optional;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.ProcessedResult;

/**
 * @author ooodunay
 *
 */
public class OnBorderProcessedResult extends ProcessedResult {

	private static final long serialVersionUID = -646344483367494297L;
	
	private final HashMap<String, String> results;
	
	public OnBorderProcessedResult(ProcessingParam param, Status status, HashMap<String, String> results) {
		super(param, status);
		this.results = results;
	}

	public OnBorderProcessedResult(ProcessingParam param, HashMap<String, String> results) {
		this(param, Status.NORMAL, results);
	}

	public Optional<HashMap<String, String>> getResults() {
		return Optional.ofNullable(results);
	}

	@Override
	public String toString() {
		return "OnBorderProcessedResult [results=" + results + ", param=" + param
				+ ", status=" + status + ", sat=" + sat + "]";
	}
}
