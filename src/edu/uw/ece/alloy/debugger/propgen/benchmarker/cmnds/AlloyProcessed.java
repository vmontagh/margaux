package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessesManager;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessesManager.AlloyProcess.Status;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ProcessRemoteMonitor;

public class AlloyProcessed extends RemoteCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7812337897314648833L;
	public final int PID;
	public final AlloyProcessingParam param;
	
	public AlloyProcessed(int pID, AlloyProcessingParam param) {
		super();
		PID = pID;
		this.param = new AlloyProcessingParam(param);
	}

	public void processDone(ProcessRemoteMonitor monitor, final ProcessesManager manager){
		logger.info("["+Thread.currentThread().getName()+"] " + "Done and reported: pID= "+PID +" param="+param);
		monitor.removeMessage(PID, param);
		manager.changeStatus(PID, Status.WORKING);
		manager.changeLastLiveTimeReported(PID, System.currentTimeMillis());		
	}

	@Override
	public String toString() {
		return "AlloyProcessed [PID=" + PID + ", param=" + param + "]";
	}

	
	
}
