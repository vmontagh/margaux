package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class OrdDcrsStrc extends Ord {

	public OrdDcrsStrc(String rName, String sName, String sNext, String sFirst,
			String middleName, String endName, String rConcreteName,
			String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName, SzPrpty sizeProp) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName, sizeProp);
	}

	@Override
	protected String genOrder() {
		return "(some " + sizeProp.orderedChange() + " implies"
				+ " relational_properties/lt[relational_properties/max["
				+ sizeProp.getGrowthOrderedDelta() + "," + sideOrdered.getNext() + "],"
				+ "relational_properties/min[" + sizeProp.orderedChange() + ","
				+ sideOrdered.getNext() + "]," + sideOrdered.getNext() + "] ) )";

	}
	
	@Override
	int getPriorityInClass() {
		return 2;
	}

}
