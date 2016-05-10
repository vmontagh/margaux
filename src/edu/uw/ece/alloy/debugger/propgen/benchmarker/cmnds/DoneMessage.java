/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.util.events.MessageEventArgs;

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
	
	@Override
	public void onEvent(MessageListenerAction listener, MessageEventArgs args) {
		listener.actionOn(this, args);
	}

}
