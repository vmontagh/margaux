package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class Global extends Locality {


	public Global(String rName, String sName, String sNext, String sFirst,
			String middleName, String endName, String rConcreteName,
			String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName, Side side,
			String quantifiedFirstForLet, String quantifiedNextForLet) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName, side, quantifiedFirstForLet, quantifiedNextForLet);
	}

	@Override
	protected boolean isConsistent() {
		if( !super.isConsistent() ) return false;
		if( side instanceof SideNone ) return false;
		return true;
	}

	@Override
	public String genQuantifier() {
		return "";
	}

	@Override
	public String getQunatifiedVar() {
		return side.genBody() ;
	}

}
