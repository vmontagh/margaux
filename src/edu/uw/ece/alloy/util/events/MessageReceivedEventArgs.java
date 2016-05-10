package edu.uw.ece.alloy.util.events;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteMessage;

/**
 * Represents the event arguments for when a {@link RemoteCommand} has been
 * received.
 * 
 * @author Fikayo Odunayo
 *
 */
public class MessageReceivedEventArgs extends MessageEventArgs {

	public MessageReceivedEventArgs(final RemoteMessage message,
			final RemoteProcess remoteProcess) {
		super(message, remoteProcess);
	}

}
