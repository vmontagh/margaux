package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyProcessedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ProcessesManager;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ProcessesManager.AlloyProcess.Status;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ProcessRemoteMonitor;

public class AlloyProcessed extends RemoteCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7812337897314648833L;
	public final int PID;
	public final AlloyProcessedResult result;

	public AlloyProcessed(final int pID, final AlloyProcessedResult result) {
		super();
		PID = pID;
		this.result = result;
	}

	public void processDone(ProcessRemoteMonitor monitor, final ProcessesManager manager){
		if(manager.getAlloyProcess(PID) == null){
			logger.info("["+Thread.currentThread().getName()+"] "+ " No Such a PID found: "+ PID+" "+this);
			return;
		}
		logger.info("["+Thread.currentThread().getName()+"] " + "Done and reported: pID= "+PID +" param="+result.params);
		if(result.isTimedout() || result.isFailed()){
			logger.info("["+Thread.currentThread().getName()+"] " + "The process is timed out and is pushed back to be retried later: pID= "+PID +" param="+result.params);
			monitor.removeAndPushUndoneRequest(PID, result.params);
		}else{
			monitor.removeMessage(PID, result.params);
		}
		manager.changeStatus(PID, Status.WORKING);
		manager.changeLastLiveTimeReported(PID, System.currentTimeMillis());
		manager.decreaseMessageCounter(PID);
	}


	@Override
	public String toString() {
		return "AlloyProcessed [PID=" + PID + ", param=" + result.params + "]";
	}



}
