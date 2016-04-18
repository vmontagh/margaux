/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.util.Map;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcessLogger;

/**
 * A remote process suicides and send a message to notify.
 * 
 * @author vajih
 *
 */
public abstract class DiedMessage extends RemoteMessage {

	private static final long serialVersionUID = 7943163272099474387L;

	public DiedMessage(RemoteProcess process) {
		super(process);
	}

	public DiedMessage(RemoteProcess process, long creationTime) {
		super(process, creationTime);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteMessage#onAction(
	 * java.util.Map)
	 */
	@Override
	public void onAction(Map<Class, Object> context)
			throws InvalidParameterException {
		RemoteProcessLogger manager = retrieveRemoteProcessLoggerFromContext(
				context);
		if (Configuration.IsInDeubbungMode)
			logger.info("[" + Thread.currentThread().getName() + "] "
					+ " A proces asked to be killed: " + process);
		manager.changeStatusToKILLING(process);
		if (Configuration.IsInDeubbungMode)
			logger.info("[" + Thread.currentThread().getName() + "] "
					+ " A proces asked to be killed: " + process);
	}

}
