package edu.mit.csail.sdg.gen.alloy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class Counter implements Iterator<List<Integer>>{

	protected final ArrayList<Integer> current ;
	protected final ArrayList<Integer> max;
	protected int maxCap = 0;

	public Counter(final ArrayList<Integer> current,final  ArrayList<Integer> max, final int maxCap){
		this.current = new ArrayList<Integer>(current);
		this.max = new ArrayList<Integer>(max);
		this.maxCap = maxCap;
	}


	public Counter(List<Integer> max){
		this();
		for(int i=0; i < max.size(); i++){
			addMax(max.get(i));
		}
	}

	public Counter(){
		this.current = new ArrayList<Integer>();
		this.max = new ArrayList<Integer>();
		this.maxCap = 0;

	}
	
	public Integer getCurrentIndex(int i){
	  if( i < 0 || i >= current.size() ){
		  throw new RuntimeException("out of bound index is requested.");		  
	  }
	  return current.get(i);
	}

	public abstract void addMax(int max);

	public abstract void resetCounter();

	public abstract Counter clone();

	public void remove() {}

}
