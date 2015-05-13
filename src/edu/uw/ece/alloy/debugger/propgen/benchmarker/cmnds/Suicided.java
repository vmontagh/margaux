package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessesManager;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessesManager.AlloyProcess.Status;

public class Suicided extends RemoteCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7428348074019906490L;
	
	public final int PID;
	public final long time;
	
	public Suicided(int pID, long time) {
		super();
		PID = pID;
		this.time = time;
	}

	@Override
	public String toString() {
		return "Suicided [PID=" + PID + ", time=" + time + "]";
	}

	public void killProcess(ProcessesManager manager){
		logger.info("["+Thread.currentThread().getName()+"] " + " A proces asked to be killed: " +PID);
		manager.changeStatus(PID, Status.KILLING);
		logger.info("["+Thread.currentThread().getName()+"] " + " A proces asked to be killed: " +PID);
	}
}
