/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.util.Map;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.util.events.MessageEventArgs;

/**
 * Message to be sent to a remote listener to stop listening on a port.
 * @author vajih
 *
 */
public abstract class TerminateMessage extends RemoteMessage {

	private static final long serialVersionUID = 7440654795553977371L;

	public TerminateMessage(RemoteProcess process) {
		super(process);
	}

	public TerminateMessage(RemoteProcess process, long creationTime) {
		super(process, creationTime);
	}
	
	/* (non-Javadoc)
	 * @see edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteMessage#onAction(java.util.Map)
	 */
	@Override
	public abstract void onAction(final Map<String, Object> context)
			throws InvalidParameterException;

	@Override
	public void onEvent(MessageListenerAction listener, MessageEventArgs args) {
		listener.actionOn(this, args);
	}

}
