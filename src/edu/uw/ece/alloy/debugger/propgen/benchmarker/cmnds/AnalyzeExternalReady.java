package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.util.Queue;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyProcessedResult;

public class AnalyzeExternalReady extends RemoteCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7021706770155254022L;

	public void readyToUse(final Queue<AlloyProcessedResult> queue){
		synchronized (queue) {
			queue.notify();
		}
	}
	
}
