/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.util.Map;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcessLogger;

/**
 * Once a remote process becomes ready, it sends a ready message.
 * 
 * @author vajih
 *
 */
public class ReadyMessage extends RemoteMessage {

	private static final long serialVersionUID = -7460192740763100407L;

	public ReadyMessage(RemoteProcess process, long creationTime) {
		super(process, creationTime);
	}

	public ReadyMessage(RemoteProcess process) {
		super(process);
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
		manager.changeStatusToIDLE(process);
	}

}
