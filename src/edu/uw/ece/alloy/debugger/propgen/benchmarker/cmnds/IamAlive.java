package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.net.InetSocketAddress;
import java.util.logging.Level;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.AlloyProcess.Status;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ProcessesManager;


public class IamAlive extends RemoteCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6849804839852749815L;

	public final InetSocketAddress PID;
	public long time;
	public int porcessed;
	public int toBeProcessed;

	public IamAlive(final InetSocketAddress pID, long time, int porcessed, int toBeProcessed) {
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
		
		if(manager.getAlloyProcess(PID) == null){
			if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"] "+ " No Such a PID found: "+ PID+" "+this);
			return;
		}
		if( manager.getAlloyProcess(PID).status == Status.KILLING){
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
		if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"] "+ " A live message is recieved from: "+ PID+" "+this);
	}

}
