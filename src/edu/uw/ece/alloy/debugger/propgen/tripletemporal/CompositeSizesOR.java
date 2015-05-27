package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class CompositeSizesOR extends CompositeSizes {

	public CompositeSizesOR(String rName, String sName, String sNext,
			String sFirst, String middleName, String endName,
			String rConcreteName, String sConcreteName, String sConcreteNext,
			String sConcreteFirst, String mConcreteName, String eConcreteName,
			SizeProperty size1, SizeProperty size2) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName, size1, size2);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String compositeOperator() {
		return " or ";
	}

}
