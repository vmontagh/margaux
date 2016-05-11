/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.util.events.MessageEventArgs;

/**
 * Once a remote process becomes ready, it sends a ready message.
 * 
 * @author vajih
 *
 */
public abstract class ReadyMessage extends RemoteMessage {

	private static final long serialVersionUID = -7460192740763100407L;

	public ReadyMessage(RemoteProcess process, long creationTime) {
		super(process, creationTime);
	}

	public ReadyMessage(RemoteProcess process) {
		super(process);
	}

	@Override
	public void onEvent(MessageListenerAction listener, MessageEventArgs args) {
		listener.actionOn(this, args);
	}

}
