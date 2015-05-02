package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyExecuter;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;

public class ProcessIt extends RemoteCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8188777849612332517L;
	public final AlloyProcessingParam param;
	
	public ProcessIt(AlloyProcessingParam param) {
		super();
		this.param = param;
	}

	public void process(AlloyExecuter executer) {
		executer.process(param);
	}

	@Override
	public String toString() {
		return "ProcessIt [param=" + param + "]";
	}
	
}
