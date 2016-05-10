/**
 * 
 */
package edu.uw.ece.alloy.util.events;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteMessage;

/**
 * @author fikayo
 *
 */
public class MessageSentEventArgs extends MessageEventArgs {

	public MessageSentEventArgs(RemoteMessage message,
			RemoteProcess remoteProcess) {
		super(message,remoteProcess);
	}

}
