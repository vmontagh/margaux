package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public abstract class CompositeSizesStrict extends CompositeSizes {

	public CompositeSizesStrict(String rName, String sName, String sNext,
			String sFirst, String middleName, String endName,
			String rConcreteName, String sConcreteName, String sConcreteNext,
			String sConcreteFirst, String mConcreteName, String eConcreteName,
			SizeProperty size1, SizeProperty size2) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName, size1, size2);
	}
	
	protected String genBody() {
		return super.genBody() + " and not(" + size1.growthLocality.getLetVariable1() + " = " + size1.growthLocality.getLetVariable2() + ") ";   
	}

}
