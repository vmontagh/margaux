package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class CmpstOrdOR extends CmpstOrds {

	public CmpstOrdOR(String rName, String sName, String sNext,
			String sFirst, String middleName, String endName,
			String rConcreteName, String sConcreteName, String sConcreteNext,
			String sConcreteFirst, String mConcreteName, String eConcreteName,
			Ord order1, Ord order2) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName, order1, order2);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String compositeOperator() {
		return " or ";
	}

}
