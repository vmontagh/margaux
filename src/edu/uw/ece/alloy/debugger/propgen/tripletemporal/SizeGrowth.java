package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class SizeGrowth extends SizeProperty {



	public SizeGrowth(String rName, String sName, String sNext, String sFirst,
			String middleName, String endName, String rConcreteName,
			String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName,
			Locality growthLocality, Emptiness empty) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName, growthLocality, empty);
		// TODO Auto-generated constructor stub
	}


	@Override
	protected String genGrowth() {
		return  " (" + growthLocality.getLetVariable1() + " in " +growthLocality.getLetVariable2() +" ) ";
	}


	@Override
	protected String genGrowthOredred() {
		return " let "+getGrowthOrderedDelta()+" = "+growthLocality.getLetVariable2()+
				" - "+growthLocality.getLetVariable1() + " | (some "+getGrowthOrderedDelta()+
				") implies ";
	}
	
}
