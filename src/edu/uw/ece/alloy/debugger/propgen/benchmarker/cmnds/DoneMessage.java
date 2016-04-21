/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.util.Map;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;

/**
 * Once all tasks are done, a message is sent.
 * 
 * @author vajih
 *
 */
public abstract class DoneMessage extends RemoteMessage {

	private static final long serialVersionUID = 3151193439710793564L;

	public DoneMessage(RemoteProcess process, long creationTime) {
		super(process, creationTime);
	}

	public DoneMessage(RemoteProcess process) {
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
	public abstract void onAction(Map<Class, Object> context)
			throws InvalidParameterException;

}
