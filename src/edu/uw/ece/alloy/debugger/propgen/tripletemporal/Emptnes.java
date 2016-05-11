package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class Emptnes extends Property {

	public Emptnes(String rName, String sName, String sNext, String sFirst,
			String middleName, String endName, String rConcreteName,
			String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName);
	}

	@Override
	protected String genBody() {
		return !genEmpty().equals("") ? genEmpty() : "";
	}

	protected String genEmpty() {
		return null;
	}

	@Override
	protected String getPredecessor() {
		return null;
	}

	@Override
	protected String getSuccessor() {
		return null;
	}

	@Override
	protected boolean isConsistent() {
		return true;
	}

}
