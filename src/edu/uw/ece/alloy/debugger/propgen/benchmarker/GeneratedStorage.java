/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author vajih
 *
 */
public class GeneratedStorage<T> {
	
	protected long size = 0;
	
	final protected List<T> storage = new ArrayList<>(); 

	
	public long getSize(){ return size;}
	
	public void addGeneratedProp(final T item){
		synchronized(storage){
			storage.add(item);
			size++;
		}
	}

	public List<T> getGeneratedProps(){
		return Collections.unmodifiableList(storage);
	}

	public void clear() {
		synchronized(storage){
			storage.clear();
			size = 0;
		}		
	}
	
	public List<T> toList(){
		return new LinkedList<T>(storage);
	}
}
