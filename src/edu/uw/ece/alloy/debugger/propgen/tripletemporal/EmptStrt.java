package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class EmptStrt extends Emptnes {

	public EmptStrt(String rName, String sName, String sNext, String sFirst,
			String middleName, String endName, String rConcreteName,
			String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName);
	}

	@Override
	protected String genEmpty() {
		return "no " + SFirst + "." + RName + " \n";
	}

}
