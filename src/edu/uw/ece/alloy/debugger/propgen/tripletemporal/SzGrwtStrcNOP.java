package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class SzGrwtStrcNOP extends SzGrwtStrc {

	public SzGrwtStrcNOP(String rName, String sName, String sNext, String sFirst, String middleName, String endName,
			String rConcreteName, String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName, Lclty growthLocality, Emptnes empty) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName, sConcreteName, sConcreteNext, sConcreteFirst,
				mConcreteName, eConcreteName, growthLocality, empty);
	}

	@Override
	protected String genGrowthOredred() {
		return " let " + getGrowthOrderedDelta() + " = "
				+ growthLocality.getLetVariable2() + " - "
				+ growthLocality.getLetVariable1() + " |" + "  (some "
				+ getGrowthOrderedDelta() + " implies";
	}
	
}
