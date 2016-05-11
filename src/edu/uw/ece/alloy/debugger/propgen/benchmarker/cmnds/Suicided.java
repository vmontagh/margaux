package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.net.InetSocketAddress;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.AlloyProcess.Status;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ProcessesManager;

@Deprecated
public class Suicided extends RemoteCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7428348074019906490L;

	public final InetSocketAddress PID;
	public final long time;

	public Suicided(final InetSocketAddress pID, long time) {
		super();
		PID = pID;
		this.time = time;
	}

	@Override
	public String toString() {
		return "Suicided [PID=" + PID + ", time=" + time + "]";
	}

	public void killProcess(ProcessesManager manager) {
		if (Configuration.IsInDeubbungMode)
			logger.info("[" + Thread.currentThread().getName() + "] "
					+ " A proces asked to be killed: " + PID);
		manager.changeStatus(PID, Status.KILLING);
		if (Configuration.IsInDeubbungMode)
			logger.info("[" + Thread.currentThread().getName() + "] "
					+ " A proces asked to be killed: " + PID);
	}
}
