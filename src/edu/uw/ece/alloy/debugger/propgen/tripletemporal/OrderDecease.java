package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class OrderDecease extends Order {



	public OrderDecease(String rName, String sName, String sNext,
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
		return " lte[relational_properties/max["+sizeProp.getGrowthOrderedDelta()+","+sideOrdered.getNext()+
				"], relational_properties/max["+sizeProp.growthLocality.getLetVariable1()+","+sideOrdered.getNext()+"],"+
				sideOrdered.getNext()+"]";
		
	}

}
