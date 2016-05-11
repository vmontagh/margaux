/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.center.communication;

import java.util.List;

/**
 * A subscriber that polls from the queue. Only a subscriber can read from a
 * queue.
 * 
 * @author vajih
 *
 */
public interface Subscriber<T> {
	/**
	 * @return the head of the queue.
	 */
	public T poll();

	/**
	 * @return remove the head and return.
	 * @throws InterruptedException
	 */
	public T take() throws InterruptedException;

	public void clear();

	public List<T> toList();

	public int size();

	public boolean isEmpty();

}
