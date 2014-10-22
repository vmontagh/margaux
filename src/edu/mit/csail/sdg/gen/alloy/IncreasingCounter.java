package edu.mit.csail.sdg.gen.alloy;

import java.util.ArrayList;
import java.util.List;

public class IncreasingCounter extends Counter{

	public IncreasingCounter(final ArrayList<Integer> current,final  ArrayList<Integer> max, final int maxCap){
		super(current,max,maxCap);
	}

	public IncreasingCounter(List<Integer> max){
		super(max);
	}

	public IncreasingCounter(){
		super();
	}

	public boolean hasNext() {
		return maxCap>0;
	}

	public void addMax(int max){
		if(maxCap==0) maxCap=1;
		maxCap = maxCap* max;
		this.max.add(max);
		this.current.add(0);
		resetCounter();    		
	}

	public  void resetCounter(){
		for(int i=0; i< current.size(); i++){
			current.set(i, 0);
		}
		if(current.size() > 0){
			current.set(current.size()-1, -1);    			
		}
	}


	public List<Integer> next() {
		int index = current.size()-1;
		do{
			if (current.get(index) > max.get(index)-2){
				current.set(index, 0); 
				index--;
				if(index  < 0)
					return null;
			}else{
				current.set(index,current.get(index)+1);
				maxCap--;
				return current;
			}
		}while(true);
	}

	public Counter clone(){
		return new IncreasingCounter(current, max, maxCap);
	}

}

