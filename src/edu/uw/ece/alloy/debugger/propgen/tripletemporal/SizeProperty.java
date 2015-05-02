package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public abstract class SizeProperty extends Property {
	
	
	final protected Locality growthLocality;
	final protected Emptiness empty;

	public SizeProperty(String rName, String sName, String sNext,
			String sFirst, String middleName, String endName,
			String rConcreteName, String sConcreteName, String sConcreteNext,
			String sConcreteFirst, String mConcreteName, String eConcreteName,
			Locality growthLocality, Emptiness empty) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName);
		this.growthLocality = growthLocality;
		this.empty = empty;
	}

	@Override
	protected String getPredecessor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getSuccessor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean isConsistent() {
		
		if(!growthLocality.isConsistent()) return false;
		
		return true;
	}

	@Override
	protected String genPredName() {
		return super.genPredName() + growthLocality.genPredName() + empty.genPredName();
	}

	
	private String genBody(final String append){
		return empty.genBody() + "\n"+ 
				"all "+getTemporalQuantifiedVar()+": " + SName + " - last["+SName +","+SNext+"] |"+
					"let " + getNextTemporalQuantifiedVar() + " = " + SName+"."+SNext +" |\n" +
						growthLocality.genBody(getTemporalQuantifiedVar(), getNextTemporalQuantifiedVar())  + append;
	}
	
	@Override
	protected String genBody() {
		
		return genBody(genGrowth());
				
	}

	protected String genBodyOrdered() {
		
		return genBody(genGrowthOredred());
	}
	
	
	protected abstract String genGrowth();
	
	protected abstract String genGrowthOredred();

	protected String getGrowthOrderedDelta(){
		return "delta";
	}
	
	protected String getTemporalQuantifiedVar(){
		return SName+"'";
	}
	
	protected String getNextTemporalQuantifiedVar(){
		return SName+"''";
	}

}
