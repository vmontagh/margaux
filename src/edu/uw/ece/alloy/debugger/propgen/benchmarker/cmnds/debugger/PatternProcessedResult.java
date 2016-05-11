/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.ProcessedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy.AlloyProcessedResult;

/**
 * Contains results from ExpressionAnalyzer
 * 
 * @author vajih
 *
 */
public class PatternProcessedResult extends ProcessedResult {

	private static final long serialVersionUID = 8620200527042392241L;
	/* contains valid results from expression analyzing. */
	final Set<AlloyProcessedResult> results;

	public PatternProcessedResult(ProcessingParam param,
			Set<AlloyProcessedResult> results) {
		super(param);
		this.results = Collections.unmodifiableSet(new HashSet<>(results));
	}

	public PatternProcessedResult(ProcessingParam param, Status status,
			Set<AlloyProcessedResult> results) {
		super(param, status);
		this.results = Collections.unmodifiableSet(new HashSet<>(results));
	}

	public Optional<Set<AlloyProcessedResult>> getResults() {
		return Optional.ofNullable(results);
	}

	@Override
	public String toString() {
		return "PatternProcessedResult [results=" + results + ", param=" + param
				+ ", status=" + status + ", sat=" + sat + "]";
	}
}