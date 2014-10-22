package edu.mit.csail.sdg.gen.alloy;

import java.util.ArrayList;
import java.util.List;

public class DecreasingCounter extends Counter{

	public DecreasingCounter(final ArrayList<Integer> current,final  ArrayList<Integer> max, final int maxCap){
		super(current,max,maxCap);
	}

	public DecreasingCounter(List<Integer> max){
		super(max);
	}

	public DecreasingCounter(){
		super();
	}

	public void addMax(int max){
		if(maxCap==0) maxCap=1;
		maxCap = maxCap* max;
		this.max.add(max);
		current.add(max-1);
		resetCounter();    		
	}

	public  void resetCounter(){
		for(int i=0; i< current.size(); i++){
			current.set(i, max.get(i)-1);
		}
		if(current.size() > 0){
			current.set(current.size()-1, max.get(current.size()-1));    			
		}
	}


	public boolean hasNext() {
		return maxCap>0;
	}


	public List<Integer> next() {
		int index = current.size()-1;
		do{
			if (current.get(index) == 0){
				current.set(index, max.get(index)-1);
				index--;
				if(index  < 0)
					return null;
			}else{
				current.set(index,current.get(index)-1);
				maxCap--;
				return current;
			}
		}while(true);
	}

	public Counter clone(){
		return new DecreasingCounter(current, max, maxCap);
	}

}
