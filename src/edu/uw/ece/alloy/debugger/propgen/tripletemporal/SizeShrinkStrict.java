package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class SizeShrinkStrict extends SizeShrink {

	
	public SizeShrinkStrict(String rName, String sName, String sNext,
			String sFirst, String middleName, String endName,
			String rConcreteName, String sConcreteName, String sConcreteNext,
			String sConcreteFirst, String mConcreteName, String eConcreteName,
			Locality growthLocality, Emptiness empty) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName, growthLocality, empty);
	}

	@Override
	protected String genBody() {
			return super.genBody() + " and (" + growthLocality.getLetVariable2() + " !in " +growthLocality.getLetVariable1() +" )";
	}

	@Override
	protected String genGrowthOredred() {
		return " let "+getGrowthOrderedDelta()+" = "+growthLocality.getLetVariable1()+
				" - "+growthLocality.getLetVariable2() + " | (some "+getGrowthOrderedDelta()+
				" or no "+getGrowthOrderedDelta()+") implies ";
	}
	
}
