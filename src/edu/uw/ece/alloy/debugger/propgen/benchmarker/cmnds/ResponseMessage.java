/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.util.Map;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;

/**
 * @author vajih
 *
 */
public abstract class ResponseMessage extends RemoteMessage {

	private static final long serialVersionUID = -2659549170674865030L;
	// TODO add a new Result
	// public final AlloyProcessedResult result;

	public ResponseMessage(RemoteProcess process, long creationTime) {
		super(process, creationTime);
	}

	public ResponseMessage(RemoteProcess process) {
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
