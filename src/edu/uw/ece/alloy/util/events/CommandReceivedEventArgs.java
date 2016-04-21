package edu.uw.ece.alloy.util.events;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteCommand;

/**
 * Represents the event arguments for when a {@link RemoteCommand} has been received.
 * 
 * @author Fikayo Odunayo
 *
 */
public class CommandReceivedEventArgs extends EventArgs {

	private final RemoteCommand command;
	
	/**
	 * Initializes a new {@link CommandReceivedEventArgs} with the given 
	 * @param command - the given {@link RemoteCommand}
	 */
	public CommandReceivedEventArgs(final RemoteCommand command) {
		this.command = command;
	}
	
	/**
	 * Returns the command associated with this args.
	 * @return
	 */
	public RemoteCommand getCommand() {
		return this.command;
	}
}
