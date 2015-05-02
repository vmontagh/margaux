package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class SizeNoChange extends SizeProperty {


	public SizeNoChange(String rName, String sName, String sNext,
			String sFirst, String middleName, String endName,
			String rConcreteName, String sConcreteName, String sConcreteNext,
			String sConcreteFirst, String mConcreteName, String eConcreteName,
			Locality growthLocality, Emptiness empty) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName, growthLocality, empty);
	}

	@Override
	protected String genGrowth() {
		return " (" + growthLocality.getLetVariable2() + " in " +growthLocality.getLetVariable1() +
				") and (" + growthLocality.getLetVariable2() + " in " +growthLocality.getLetVariable1() +" )";
	}
	
	@Override
	protected String genGrowthOredred() {
		return " let "+getGrowthOrderedDelta()+" = "+growthLocality.getLetVariable1()+
				" - "+growthLocality.getLetVariable2() + " | (no "+getGrowthOrderedDelta()+
				") implies ";
	}
	
}
