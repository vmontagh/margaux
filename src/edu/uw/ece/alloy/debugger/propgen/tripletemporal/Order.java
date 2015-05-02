package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public abstract class Order extends Property {

	final SizeProperty sizeProp;
	final SideOrdered sideOrdered;  
	
	public Order(String rName, String sName, String sNext, String sFirst,
			String middleName, String endName, String rConcreteName,
			String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName, SizeProperty sizeProp) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName);
		this.sizeProp = sizeProp;
		sideOrdered = (SideOrdered)sizeProp.growthLocality.side;

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
		if( ! sizeProp.isConsistent() ) return false;

		if( ! (sizeProp.growthLocality.side instanceof  SideOrdered) ) return false;
		if( ! sideOrdered.isConsistentOrdered() ) return false;
		return true;
	}

	@Override
	protected String genPredName() {
		return super.genPredName()+sizeProp.genPredName();
	}

	@Override
	protected String genParameters() {
		return super.genParameters() + sideOrdered.getOderedParameters();
	}

	@Override
	protected String genBody() {
		return sizeProp.genBodyOrdered() + genOrder();
	}
	
	protected abstract String genOrder();
	
	public String genParametesCall(){
		return super.genParametesCall()+","+sideOrdered.getConcreteOrderedParameters() ;
	}

}
