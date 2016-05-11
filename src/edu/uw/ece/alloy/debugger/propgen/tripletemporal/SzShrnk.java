package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class SzShrnk extends SzPrpty {

	public SzShrnk(String rName, String sName, String sNext, String sFirst,
			String middleName, String endName, String rConcreteName,
			String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName, Lclty growthLocality,
			Emptnes empty) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName, growthLocality, empty);
	}

	@Override
	protected String genGrowth() {
		return "(" + growthLocality.getLetVariable2() + " in "
				+ growthLocality.getLetVariable1() + " )";
	}

	@Override
	protected String genGrowthOredred() {
		return " let " + getGrowthOrderedDelta() + " = "
				+ growthLocality.getLetVariable1() + " - "
				+ growthLocality.getLetVariable2() + " | " + "  ("
				+ growthLocality.getLetVariable2() + " in "
				+ growthLocality.getLetVariable1() + " ) and " + "  (some "
				+ getGrowthOrderedDelta() + " implies";
	}

	@Override
	protected String orderedChange() {
		return growthLocality.getLetVariable2();
	}

	@Override
	protected boolean isConsistent() {

		if (!super.isConsistent())
			return false;

		if (empty instanceof EmptStrt)
			return false;
		if (empty instanceof EmptStrtAndEnd)
			return false;

		return true;
	}
}
