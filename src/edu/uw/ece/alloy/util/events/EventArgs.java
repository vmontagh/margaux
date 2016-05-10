package edu.uw.ece.alloy.util.events;

/**
 * This is the default type of event args. It is empty.
 * 
 * @author Fikayo Odunayo
 *
 */
public class EventArgs {

	public final long creationTime;
	
	private static EventArgs empty = new EventArgs();

	protected EventArgs() {
		this.creationTime = System.currentTimeMillis();
	}
	
	protected EventArgs(final long creationTime) {
		this.creationTime = creationTime;
	}

	/**
	 * Returns an empty set of args
	 * 
	 * @return - the event args
	 */
	public static EventArgs empty() {
		return empty;
	}
}
