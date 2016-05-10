/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.center.communication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author vajih
 *
 */
public class Queue<T> implements Publisher<T>,Subscriber<T> {

	final BlockingQueue<T> queue;
	
	public Queue(int cap){
		queue = new LinkedBlockingQueue<>(cap);
	}
	public Queue(){
		queue = new LinkedBlockingQueue<>();
	}
	
	@Override
	public T poll() {
		return queue.poll();
	}

	@Override
	public T take() throws InterruptedException {
		return queue.take();
	}

	@Override
	public void put(T p) throws InterruptedException {
		queue.put(p);
	}
	
	@Override
	public void clear() {
		queue.clear();
	}
	
	@Override
	public int size() {
		return queue.size();
	}
	
	@Override
	public boolean isEmpty() {
		return queue.isEmpty();
	}
	
	@Override
	public List<T> toList() {
		return Collections.unmodifiableList(new ArrayList<>(queue));
	}
	@Override
	public String toString() {
		return "Queue [queue=" + queue + "]";
	}

}
