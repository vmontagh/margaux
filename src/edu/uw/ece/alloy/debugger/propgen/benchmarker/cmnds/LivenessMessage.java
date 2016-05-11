/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.util.Map;
import java.util.logging.Level;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcessLogger;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcessRecord;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcessRecord.Status;
import edu.uw.ece.alloy.util.Utils;

/**
 * Every time the runner is booted, it has to periodically sends a liveness
 * message to a predetermined destination.
 * 
 * @author vajih
 *
 */
public abstract class LivenessMessage extends RemoteMessage {

	private static final long serialVersionUID = 8342150804289910842L;
	/* How many messages are processed so far */
	public final int processed;
	/* How many messages are in the queue to be processed */
	public final int toBeProcessed;

	public LivenessMessage(RemoteProcess process, long creationTime,
			int processed, int toBeProcessed) {
		super(process, creationTime);
		this.processed = processed;
		this.toBeProcessed = toBeProcessed;
	}

	public LivenessMessage(RemoteProcess process, int processed,
			int toBeProcessed) {
		super(process);
		this.processed = processed;
		this.toBeProcessed = toBeProcessed;
	}

	@Override
	public void onAction(Map<String, Object> context)
			throws InvalidParameterException {
		RemoteProcessLogger manager = (RemoteProcessLogger) context
				.get("RemoteProcessLogger");
		RemoteProcessRecord record = manager.getRemoteProcessRecord(process);

		if (record == null) {
			if (Configuration.IsInDeubbungMode)
				logger.info(Utils.threadName() + " No Such a PID found: " + process
						+ " " + this);
			return;
		}

		if (record.status == Status.KILLING) {
			logger.log(Level.SEVERE, Utils.threadName()
					+ "The killing process is sending a live signal: " + process);
		} else {

			// Does not need to be done atomically.
			manager.changeDoingTasks(process, toBeProcessed);
			manager.changeDoneTasks(process, processed);
			manager.changeLastLiveTimeReported(process, creationTime);
			manager.changeLastLiveTimeRecieved(process, System.currentTimeMillis());

			if (toBeProcessed <= 0) {
				manager.changeStatusToIDLE(process);

			} else {
				manager.changeStatusToWORKING(process);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LivenessMessage [process=" + process + ", creationTime="
				+ creationTime + ", processed=" + processed + ", toBeProcessed="
				+ toBeProcessed + "]";
	}

}
