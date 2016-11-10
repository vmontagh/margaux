package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class CmpstOrdStrcOR extends CmpstOrdStrct {

	public CmpstOrdStrcOR(String rName, String sName, String sNext, String sFirst,
			String middleName, String endName, String rConcreteName,
			String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName, Ord order1, Ord order2) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName, order1, order2);
	}

	@Override
	protected String compositeOperator() {
		return " or ";
	}
	
	@Override
	int getPriorityInClass() {
		return 1;
	}


}
