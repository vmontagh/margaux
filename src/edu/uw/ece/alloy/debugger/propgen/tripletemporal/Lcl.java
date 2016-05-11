package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class Lcl extends Lclty {

	public Lcl(String rName, String sName, String sNext, String sFirst,
			String middleName, String endName, String rConcreteName,
			String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName, Sd side,
			String quantifiedFirstForLet, String quantifiedNextForLet) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName, side, quantifiedFirstForLet, quantifiedNextForLet);
	}

	@Override
	public String genQuantifier() {
		return " all " + getQunatifiedVar() + " : " + side.getOther() + "| ";
	}

	@Override
	public String getQunatifiedVar() {
		return side.getOther() + "'";
	}

	@Override
	protected boolean isConsistent() {
		if (!super.isConsistent())
			return false;
		if (side instanceof SdNon)
			return false;
		if (side instanceof SdBth)
			return false;
		return true;
	}

}
