/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.util.events.MessageEventArgs;

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

	@Override
	public void onEvent(MessageListenerAction listener, MessageEventArgs args) {
		listener.actionOn(this, args);
	}

}
