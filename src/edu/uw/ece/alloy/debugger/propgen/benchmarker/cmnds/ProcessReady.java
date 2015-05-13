package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessesManager;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessesManager.AlloyProcess.Status;

public class ProcessReady extends RemoteCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final int pId;
	
	public ProcessReady(final int pId) {
		this.pId = pId;
	}

	public void activateMe(ProcessesManager manager){
		/*try {
			(new RegisterCallback(manager.getProcessRemoteMonitorAddress())).sendMe(manager.getAlloyProcess(pId).address);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Failed to transfer registercallback: "+pId);
		}*/
		manager.changeStatus(pId, Status.IDLE);	
		manager.resetMessageCounter(pId);
	}
	
}
