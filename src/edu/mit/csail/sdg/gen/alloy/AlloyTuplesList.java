package edu.mit.csail.sdg.gen.alloy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import kodkod.instance.Tuple;
import kodkod.instance.TupleFactory;
import kodkod.instance.TupleSet;

public 	 class AlloyTuplesList extends ArrayList<TupleSet> {


	private static final long serialVersionUID = 11478473247238472L;

	public AlloyTuplesList() {
		super();
	}

	public AlloyTuplesList(Collection<TupleSet> alloyTuplesList) {
		super(alloyTuplesList);
	}



	public final TupleSet list2TupleSet(){
		if(!isEmpty()){
			TupleSet empty_tupleSet = get(0).clone();
			empty_tupleSet.clear();
			TupleSet ret = empty_tupleSet.clone();
			for(int i=0;i<size();i++){
				ret.addAll(get(i));
			}
			return ret;
		}else{
			return null;
		}
	}


	public final TupleSet getMaxTupleSet(){
		TupleSet max = null;
		if(this.size()>0)
			max = this.get(0);
		for(TupleSet current:this){
			if(max.size() < current.size())
				max=current;
		}
		return max;
	}

	public final AlloyTuples getFieldTuple(final TupleFactory tupleFactory) throws Exception{
		return this.getFieldTuple(tupleFactory,"");
	}


	public final AlloyTuples getFieldTuple(final TupleFactory tupleFactory, final String uniqAtom) throws Exception{
		AlloyTuples ret = new AlloyTuples();
		if(this.size() > 1)
			throw new Exception("Each instances is supposed to have one TupleSet");

		//Get first
		TupleSet firstTuple = this.get(0);

		for(Tuple rightTuple:firstTuple){
			List<Object> notPrccsed = new ArrayList<Object>();
			//To support the one signature
			if(!uniqAtom.equals(""))
				notPrccsed.add(uniqAtom);
			for(int i=0;i<rightTuple.arity();i++){
				notPrccsed.add(rightTuple.atom(i));
			}
			Tuple urightTuple = tupleFactory.tuple(notPrccsed);
			ret.add(urightTuple);
		}
		return ret;
	}

	public String toString(){
		Object[] items = super.toArray();
		StringBuilder ret = new StringBuilder();

		for(int i=0; i < items.length; i++){
			ret.append( items[i].toString()).append( ",");
		}
		if(ret.length() > 0) ret.delete(ret.length()-1, ret.length());
		ret.insert(0, "(").append(")");
		return ret.toString();	
	}	

	public AlloyTuplesList clone(){
		return new AlloyTuplesList(this);
	}
}

