package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class CmpstSzOR extends CmpstSz {

	public CmpstSzOR(String rName, String sName, String sNext, String sFirst,
			String middleName, String endName, String rConcreteName,
			String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName, SzPrpty size1,
			SzPrpty size2) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName, size1, size2);
	}

	@Override
	protected String compositeOperator() {
		return " or ";
	}

}
