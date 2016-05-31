package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.onborder;

import java.util.Optional;

import edu.mit.csail.sdg.alloy4.Pair;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.ProcessedResult;

/**
 * @author ooodunay
 *
 */
public class OnBorderProcessedResult extends ProcessedResult {

	private static final long serialVersionUID = -646344483367494297L;
	
	private final Pair<Optional<String>, Optional<String>> results;
	
	public OnBorderProcessedResult(ProcessingParam param, Status status, Pair<Optional<String>, Optional<String>> results) {
		super(param, status);
		this.results = results;
	}

	public OnBorderProcessedResult(ProcessingParam param, Pair<Optional<String>, Optional<String>> results) {
		super(param);
		this.results = results;
	}

	public Optional<Pair<Optional<String>, Optional<String>>> getResults() {
		return Optional.ofNullable(results);
	}

	@Override
	public String toString() {
		return "OnBorderProcessedResult [results=" + results + ", param=" + param
				+ ", status=" + status + ", sat=" + sat + "]";
	}
}
