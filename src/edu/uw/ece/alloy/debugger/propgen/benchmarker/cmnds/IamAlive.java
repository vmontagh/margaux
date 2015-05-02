package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.util.logging.Level;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessesManager;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessesManager.AlloyProcess.Status;


public class IamAlive extends RemoteCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6849804839852749815L;

	public final int PID;
	public long time;
	public int porcessed;
	public int toBeProcessed;

	public IamAlive(int pID, long time, int porcessed, int toBeProcessed) {
		super();
		PID = pID;
		this.time = time;
		this.porcessed = porcessed;
		this.toBeProcessed = toBeProcessed;
	}

	@Override
	public String toString() {
		return "IamAlive [PID=" + PID + ", time=" + time + ", porcessed="
				+ porcessed + ", toBeProcessed=" + toBeProcessed + "]";
	}

	public void updatePorcessorLiveness(final ProcessesManager manager){
		if(manager.getAlloyProcess(PID).status == Status.KILLING){
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"The killing process is sending a live signal: "+PID);
		}else{
			//Does not need to be done atomically.
			manager.changeDoingTasks(PID, toBeProcessed);
			manager.changeDoneTasks(PID, porcessed);
			manager.changeLastLiveTimeReported(PID, System.currentTimeMillis());
			manager.changeLastLiveTimeRecieved(PID, time);
			if(toBeProcessed <= 0){
				manager.changeStatus(PID, Status.IDLE);
			}else{
				manager.changeStatus(PID, Status.WORKING);
			}
		}
		logger.info("["+Thread.currentThread().getName()+"] "+ " A live message is recieved from: "+ PID+" "+this);
	}

}
