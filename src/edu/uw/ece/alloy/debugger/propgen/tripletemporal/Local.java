package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class Local extends Locality {

	
	
	public Local(String rName, String sName, String sNext, String sFirst,
			String middleName, String endName, String rConcreteName,
			String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName, Side side,
			String quantifiedFirstForLet, String quantifiedNextForLet) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName, side, quantifiedFirstForLet, quantifiedNextForLet);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String genQuantifier() {
		return " all "+getQunatifiedVar() + " : "+side.genBody()+"| ";
	}

	@Override
	public String getQunatifiedVar() {
		return side.genBody()+"'";
	}
	
	@Override
	protected  boolean isConsistent(){
		if( !super.isConsistent()) return false;
		if( side instanceof SideNone ) return false;
		if( side instanceof SideBoth ) return false;
		return true;		
	}

}
