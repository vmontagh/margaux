package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class SzShrnkStrc extends SzShrnk {

	public SzShrnkStrc(String rName, String sName, String sNext, String sFirst,
			String middleName, String endName, String rConcreteName,
			String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName, Lclty growthLocality,
			Emptnes empty) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName, growthLocality, empty);
	}

	@Override
	protected String genBody() {
		return super.genBody() + " and (" + growthLocality.getLetVariable1()
				+ " !in " + growthLocality.getLetVariable2() + " )";
	}

	@Override
	protected String genGrowthOredred() {

		// System.out.println(genBody());
		// System.exit(-10);
		// return this.genBody() + " and some " + growthLocality.getLetVariable2() +
		// " implies ";
		return " let " + getGrowthOrderedDelta() + " = "
				+ growthLocality.getLetVariable1() + " - "
				+ growthLocality.getLetVariable2() + " | " + "  ("
				+ growthLocality.getLetVariable2() + " in "
				+ growthLocality.getLetVariable1() + " ) and " + "  ("
				+ growthLocality.getLetVariable1() + " !in "
				+ growthLocality.getLetVariable2() + " ) and " + "  (some "
				+ getGrowthOrderedDelta() + " implies";
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
	
	@Override
	int getPriorityInClass() {
		return 4;
	}

}
