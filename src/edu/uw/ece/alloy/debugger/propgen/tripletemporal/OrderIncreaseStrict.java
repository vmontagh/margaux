package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class OrderIncreaseStrict extends Order {


	public OrderIncreaseStrict(String rName, String sName, String sNext,
			String sFirst, String middleName, String endName,
			String rConcreteName, String sConcreteName, String sConcreteNext,
			String sConcreteFirst, String mConcreteName, String eConcreteName,
			SizeProperty sizeProp) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName, sizeProp);
	}

	@Override
	protected String genOrder() {
		return " lt[relational_properties/max["+sizeProp.orderedChange()+","
				+sideOrdered.getNext()+"],relational_properties/min["+sizeProp.getGrowthOrderedDelta()+","
				+sideOrdered.getNext()+"],"+sideOrdered.getNext()+"] )";
	}

}
