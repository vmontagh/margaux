package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class SzShrnkNOP extends SzShrnk {

	public SzShrnkNOP(String rName, String sName, String sNext, String sFirst, String middleName, String endName,
			String rConcreteName, String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName, Lclty growthLocality, Emptnes empty) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName, sConcreteName, sConcreteNext, sConcreteFirst,
				mConcreteName, eConcreteName, growthLocality, empty);
	}
	@Override
	protected String genGrowthOredred() {
		return " let " + getGrowthOrderedDelta() + " = "
				+ growthLocality.getLetVariable1() + " - "
				+ growthLocality.getLetVariable2() + " | " + "  (some "
				+ getGrowthOrderedDelta() + " implies";
	}
}
