package edu.uw.ece.alloy.debugger.propgen.tripletemporal;


public abstract class  CompositeOrdersStrict extends CompositeOrders {

	public CompositeOrdersStrict(String rName, String sName, String sNext,
			String sFirst, String middleName, String endName,
			String rConcreteName, String sConcreteName, String sConcreteNext,
			String sConcreteFirst, String mConcreteName, String eConcreteName,
			Order order1, Order order2) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName, order1, order2);
	}
	
	protected String genBody() {
		return super.genBody() + " and not(" + order1.sizeProp.growthLocality.getLetVariable1() + " = " + order1.sizeProp.growthLocality.getLetVariable2() + ") ";   
	}
}
