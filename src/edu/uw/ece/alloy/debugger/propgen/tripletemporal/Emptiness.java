package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public  class Emptiness extends Property {
	

	public Emptiness(String rName, String sName, String sNext, String sFirst,
			String middleName, String endName, String rConcreteName,
			String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String genBody() {
		return !genEmpty().equals("") ? genEmpty()  : "" ;
	}
	
	protected String genEmpty() {
		return null;
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
		return true;
	}


	
}
