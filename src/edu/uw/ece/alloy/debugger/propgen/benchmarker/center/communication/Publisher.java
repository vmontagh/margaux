/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.center.communication;

/**
 * @author vajih
 *
 */
public interface Publisher<T> {
	/**
	 * insert an item into the queue.
	 * @param p
	 * @throws InterruptedException 
	 */
	public void put(T p) throws InterruptedException;
	public int size();
	public boolean isEmpty();
}
