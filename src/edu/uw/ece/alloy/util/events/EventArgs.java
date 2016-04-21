package edu.uw.ece.alloy.util.events;

/**
 * This is the default type of event args. It is empty.
 * 
 * @author Fikayo Odunayo
 *
 */
public class EventArgs {

	private static EventArgs empty = new EventArgs();

	protected EventArgs() {
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
