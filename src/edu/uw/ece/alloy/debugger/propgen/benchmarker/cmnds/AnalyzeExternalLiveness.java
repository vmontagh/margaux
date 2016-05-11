package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;

@Deprecated
public class AnalyzeExternalLiveness extends LivenessMessage {

	private static final long serialVersionUID = 5660547845381764587L;

	public AnalyzeExternalLiveness(RemoteProcess process, int processed,
			int toBeProcessed) {
		super(process, processed, toBeProcessed);
	}

}
