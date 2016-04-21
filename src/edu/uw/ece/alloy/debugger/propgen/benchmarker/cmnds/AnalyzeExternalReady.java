package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.util.Queue;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyProcessedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;

public class AnalyzeExternalReady extends ReadyMessage {

	public AnalyzeExternalReady(RemoteProcess process) {
		super(process);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 7021706770155254022L;

	public void readyToUse(final Queue<AlloyProcessedResult> queue){
		synchronized (queue) {
			queue.notify();
		}
	}
	
}
