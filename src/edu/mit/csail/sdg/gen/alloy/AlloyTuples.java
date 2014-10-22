package edu.mit.csail.sdg.gen.alloy;

import java.util.ArrayList;

import kodkod.instance.Tuple;

public  class AlloyTuples extends ArrayList<Tuple> {

	public String toString(){
		StringBuilder ret = new StringBuilder();
		ret.append("<");
		for(int i=0; i < this.size(); i++){
			ret.append( get(i).getClass()).append( "->");
		}
		ret.delete(ret.length()-2, ret.length());
		ret.append(">");
		return ret.toString();

	}

}
