package edu.mit.csail.sdg.gen.alloy;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;


public  class AlloyTuplesTable extends TreeMap<String, AlloyTuplesList>{

	//private Counter counter;

	public AlloyTuplesTable(AlloyTuplesTable that){
		super(that);
		//this.counter = that.counter.clone();
	}

	public AlloyTuplesTable clone(){

		return new AlloyTuplesTable(this);
	}

	public AlloyTuplesTable(){
		super();
		//this.counter = new IncreasingCounter();
	}

	public String toString(){
		StringBuilder ret = new StringBuilder();
		ret.append("\t{");
		for(String key: keySet()){
			ret.append(key).append("=").append(get(key).toString()).append(",");
		}
		ret.delete(ret.length()-1, ret.length());
		ret.append("}");
		return ret.toString();	
	}			


	public void merge(AlloyTuplesTable that){
		for(String thatKey:that.keySet()){
			if(this.containsKey(thatKey)){
				AlloyTuplesList thatList = that.get(thatKey);
				AlloyTuplesList thisList = this.get(thatKey);
				thisList.addAll(thatList);
				this.put(thatKey, thisList);
			}else{
				this.put(thatKey, that.get(thatKey));
			}
		}
	}

	private class InstancesIterator implements Iterable<AlloyTuplesTable>,Iterator<AlloyTuplesTable>{

		final AlloyTuplesTable table;
		final Counter counter;

		public InstancesIterator(AlloyTuplesTable outerClass,Counter counter){
			table = outerClass;
			this.counter = counter;
			for(String key:descendingKeySet()){
				counter.addMax(get(key).size());
			}
		}

		@Override
		public boolean hasNext() {
			return counter.hasNext();
		}

		@Override
		public AlloyTuplesTable next() {
			AlloyTuplesTable ret = new AlloyTuplesTable();
			if (!counter.hasNext())
				return null;
			else{
				List<Integer> inst = counter.next();
				int i=0;
				for(String key:table.descendingKeySet()){
					//A unary tupleList
					AlloyTuplesList tuplesList = new AlloyTuplesList();
					tuplesList.add(table.get(key).get(inst.get(i)));
					ret.put(key, tuplesList);
					i++;
				}
			}
			return ret;
		}

		@Override
		public void remove() {}

		@Override
		public Iterator<AlloyTuplesTable> iterator() {
			// TODO Auto-generated method stub
			return this;
		}
	}

	public Iterable<AlloyTuplesTable> getInstanceIteratorIncreasing(){
		return new InstancesIterator(this,new IncreasingCounter());
	}

	public Iterable<AlloyTuplesTable> getInstanceIteratorDecreasing(){
		return new InstancesIterator(this,new DecreasingCounter());
	}

}

