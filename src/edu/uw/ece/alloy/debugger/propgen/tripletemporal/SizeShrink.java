package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class SizeShrink extends SizeProperty {


	public SizeShrink(String rName, String sName, String sNext, String sFirst,
			String middleName, String endName, String rConcreteName,
			String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName,
			Locality growthLocality, Emptiness empty) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName, growthLocality, empty);
	}

	@Override
	protected String genGrowth() {
		return "(" + growthLocality.getLetVariable2() + " in " +growthLocality.getLetVariable1() +" )";
	}
	
	@Override
	protected String genGrowthOredred() {
		return " let "+getGrowthOrderedDelta()+" = "+growthLocality.getLetVariable1()+
				" - "+growthLocality.getLetVariable2() + " | " +
				"  (" + growthLocality.getLetVariable2() + " in " +growthLocality.getLetVariable1() +" ) and "+
				"  (some "+growthLocality.getLetVariable2()+" implies"
				;
	}

	@Override
	protected String orderedChange() {
		return growthLocality.getLetVariable2();
	}
	
	@Override
	protected boolean isConsistent() {
		
		if(!super.isConsistent()) return false;
		
		if(empty instanceof EmptyStart) return false;
		if(empty instanceof EmptyStartAndEnd) return false;
		
		return true;
	}
}
