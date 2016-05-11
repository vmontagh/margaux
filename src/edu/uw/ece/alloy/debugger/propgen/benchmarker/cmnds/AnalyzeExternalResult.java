package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.util.Queue;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy.AlloyProcessedResult;

@Deprecated
public class AnalyzeExternalResult extends RemoteCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final AlloyProcessedResult result;

	public AnalyzeExternalResult(final AlloyProcessedResult result) {
		super();
		this.result = result;
	}

	// It is called on the Requester side, i.e. Debugger's listener
	public void storeResult(final Queue<AlloyProcessedResult> queue) {
		queue.add(this.result);
	}

}
