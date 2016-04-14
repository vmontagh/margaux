package edu.uw.ece.alloy.util.events;

import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents an event which may have multiple listeners fired when this event
 * is invoked.
 * 
 * @author Fikayo Odunayo
 *
 * @param <T>
 *          The type of {@link EventArgs} which this event will deliver to the
 *          listeners when the event is invoked.
 */
public final class Event<T extends EventArgs> {

	private final List<EventListener<T>> listeners;

	public Event() {
		this.listeners = new ArrayList<>();
	}

	/**
	 * Indicates whether this {@link Event} has any handlers listening to it.
	 * 
	 * @return
	 */
	public synchronized boolean hasHandlers() {
		return this.listeners.size() > 0;
	}

	/**
	 * Adds an {@link EventListener} to be fired when this event is invoked.
	 * 
	 * @param listener
	 *          - the listener to add
	 * @return true (as specified by Collection.add)
	 */
	public synchronized boolean addListener(EventListener<T> listener) {
		return this.listeners.add(listener);
	}

	/**
	 * Removes the given {@link EventListener} from the listeners to be fired when
	 * this event is invoked.
	 * 
	 * @param listener
	 *          - the listener to remove
	 * @return true (as specified by Collection.remove)
	 */
	public synchronized boolean removeListener(EventListener<T> listener) {
		return this.listeners.remove(listener);
	}

	/**
	 * Invokes the event by calling all event listeners listening to this event
	 * 
	 * @param sender
	 *          - The object invoking the event (may be null)
	 * @param e
	 *          - The event args to be used (may be null)
	 */
	public synchronized void invokeListeners(Object sender, T e) {

		for (EventListener<T> listener : this.listeners) {
			listener.onEvent(sender, e);
		}
	}
}
