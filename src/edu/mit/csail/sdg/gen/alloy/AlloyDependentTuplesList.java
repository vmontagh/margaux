package edu.mit.csail.sdg.gen.alloy;

import java.util.Collection;
import java.util.HashMap;

import kodkod.instance.TupleSet;

public  class AlloyDependentTuplesList extends AlloyTuplesList/* implements Iterable<TupleSet> */{

	//Map the dependent tupleset to the range
	private final HashMap<String, AlloyTuplesList> lists = new HashMap<String, AlloyTuplesList>();
	private final static String UNKNOWN = "unknown";

	public AlloyDependentTuplesList(){}


	public AlloyDependentTuplesList(AlloyDependentTuplesList that){
		super();
		super.addAll(that);
		this.lists.putAll(that.lists);
	}

	public AlloyDependentTuplesList(final TupleSet tupleSet, final Collection<TupleSet> alloyTuplesList){
		super(alloyTuplesList);
		lists.put(tupleSet.toString(), new AlloyTuplesList(alloyTuplesList));
	}

	private boolean addLocal(final String key, final TupleSet e){
		AlloyTuplesList currentList = lists.get(key);
		if(currentList == null){
			currentList = new AlloyTuplesList();
		}
		return (currentList.add(e)) && (lists.put(key, currentList)!=null);
	}

	private boolean add(final String key, final TupleSet e){
		boolean ret = false;
		ret = super.add(e);
		ret = ret & addLocal(key,e);
		return ret;
	}

	public boolean add(final TupleSet tupleSet, final TupleSet e){
		return add(tupleSet.toString(),e);
	}


	public boolean add(TupleSet e){
		return add(UNKNOWN,e);
	}

	private boolean addAllLocal(final String key, final AlloyTuplesList e){
		AlloyTuplesList currentList = lists.get(key);
		if(currentList == null){
			currentList = new AlloyTuplesList();
		}
		return (currentList.addAll(e)) && (lists.put(key, currentList)!=null);
	}

	private boolean addAll(final String key, final AlloyTuplesList e){
		boolean ret = true;
		for(TupleSet ts:e){
			ret = ret & super.add(ts);
		}
		return ret & this.addAllLocal(key,e) ;
	}

	public boolean addAll(final TupleSet tupleSet, final AlloyTuplesList group){
		return addAll(tupleSet.toString(),group);
	}

	private boolean addAll(final String key, final Collection<TupleSet> c){
		AlloyTuplesList alloyTuplesList = new AlloyTuplesList();
		alloyTuplesList.addAll(c);
		return addAll(key,alloyTuplesList);
	}

	public boolean addAll(final TupleSet key, final Collection<TupleSet> c){
		return this.addAll(key.toString(),c);
	}



	public void add(int index,TupleSet element){
		addLocal(UNKNOWN,element);
		super.add(index, element); 
	}

	public final AlloyTuplesList getAllAlloyTuplesLists(){
		AlloyTuplesList ret = new AlloyTuplesList();
		for(AlloyTuplesList alloyTuplesList: lists.values()){
			ret.addAll(alloyTuplesList);
		}
		return ret;
	}

	public AlloyDependentTuplesList clone(){
		return new AlloyDependentTuplesList(this);
	}

	public String toString(){
		StringBuilder ret = new StringBuilder();
		ret.append("#").append(this.size());
		for(String key:lists.keySet()){
			ret.append("{#").append(key).append("=").append(lists.get(key).size()).append("->").append(lists.get(key)).append("}").append("\n");
		}
		if(ret.length() > 0) ret.delete(ret.length()-1, ret.length());

		ret.insert(0,"<").append(">");
		return ret.toString();
	}
	/*
	@Override
	public Iterator<TupleSet> iterator() {
		// TODO Auto-generated method stub
		return new AlloyDependentTuplesListIterator();
	}

	private class AlloyDependentTuplesListIterator implements Iterator<TupleSet>{

		private int currentRow;
		private int currentCol;
		//private final  HashMap<String, AlloyTuplesList> lists;
		private final String[] keys;

		public AlloyDependentTuplesListIterator(/*final HashMap<String, AlloyTuplesList> lists*//*){
			//this.lists = lists;
			keys = (String[]) lists.keySet().toArray();
			currentRow = 0;
			currentCol = -1;
		}

		@Override
		public boolean hasNext() {
			if((currentRow == keys.length-1) && 
					(currentCol == lists.get(keys[currentRow]).size()) )
				return true;
			return false;
		}

		@Override
		public TupleSet next() {

			if(currentCol < lists.get(keys[currentRow]).size()){
				currentCol++;
			}else if(currentRow < keys.length){
				currentRow++;
				currentCol=0;
			}else{
				return null;
			}
			return lists.get(keys[currentRow]).get(currentCol);
		}

		@Override
		public void remove() {
			altered = true;
			lists.get(keys[keys.length-1]).removeLast();
		}

	}
		 */

}

