package edu.uw.ece.alloy.util.events;

/**
 * Represents a listener which is fired when an event is invoked.
 * 
 * @author Fikayo Odunayo
 *
 * @param <T>
 *          The type of {@link EventArgs} which this handler will accept when it
 *          is invoked.
 */
public interface EventListener<T extends EventArgs> {

	/**
	 * This is fired when the associated {@link Event} is invoked.
	 * 
	 * @param sender
	 *          - The Object which invoked the event
	 * @param e
	 *          - The type of arguments which this event carries. 
	 */
	// TODO change the Object to other type. Object is so general.
	public void onEvent(Object sender, T e);

}
