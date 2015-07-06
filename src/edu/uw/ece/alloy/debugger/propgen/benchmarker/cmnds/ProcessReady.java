package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.net.InetSocketAddress;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.AlloyProcess.Status;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ProcessesManager;

public class ProcessReady extends RemoteCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final InetSocketAddress pId;
	
	public ProcessReady(final InetSocketAddress pId) {
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
