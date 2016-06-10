/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;


import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.util.events.MessageEventArgs;

/**
 * The message is sent to set up folders or copy files on the new server.
 * This message has to be sent once after readyness message is sent.
 * @author vajih
 *
 */
public abstract class SetupMessage extends RemoteMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7666928638992349250L;

	/**
	 * @param process
	 * @param creationTime
	 */
	public SetupMessage(RemoteProcess process, long creationTime) {
		super(process, creationTime);
	}

	/**
	 * @param process
	 */
	public SetupMessage(RemoteProcess process) {
		super(process);
	}
	
	@Override
	public void onEvent(MessageListenerAction listener, MessageEventArgs args) {
		listener.actionOn(this, args);
	}
}
