package edu.uw.ece.alloy.util.events;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteMessage;

/**
 * 
 * @author vajih
 *
 */
public class MessageEventArgs extends EventArgs {
	/* The message that is received */
	protected final RemoteMessage message;
	/* The process that sent the message */
	protected final RemoteProcess remoteProcess;

	public MessageEventArgs(final RemoteMessage message,
			final RemoteProcess remoteProcess) {
		super();
		this.message = message;
		this.remoteProcess = remoteProcess;
	}

	/**
	 * Returns the command associated with this args.
	 * 
	 * @return
	 */
	public RemoteMessage getMessage() {
		return this.message;
	}

	public RemoteProcess getRemoteProcess() {
		return this.remoteProcess;
	}
}
