package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class OrderDecreaseStrict extends Order {


	public OrderDecreaseStrict(String rName, String sName, String sNext,
			String sFirst, String middleName, String endName,
			String rConcreteName, String sConcreteName, String sConcreteNext,
			String sConcreteFirst, String mConcreteName, String eConcreteName,
			SizeProperty sizeProp) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName, sizeProp);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String genOrder() {
		return " gt[relational_properties/min["+sizeProp.growthLocality.getLetVariable1()+","+sideOrdered.getNext()+
				"], relational_properties/max["+sizeProp.getGrowthOrderedDelta()+","+sideOrdered.getNext()+"],"+
				sideOrdered.getNext()+"]";
	}

}