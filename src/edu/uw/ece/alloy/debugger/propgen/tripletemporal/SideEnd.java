package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class SideEnd extends Side {
	


	public SideEnd(String rName, String sName, String sNext, String sFirst,
			String middleName, String endName, String rConcreteName,
			String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName, String endNext,
			String endFirst, String middleNext, String middleFirst,
			String endConcreteNext, String endConcreteFirst,
			String middleConcreteNext, String middleConcreteFirst) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName, endNext, endFirst, middleNext, middleFirst,
				endConcreteNext, endConcreteFirst, middleConcreteNext,
				middleConcreteFirst);
	}

	@Override
	protected String genBody() {
		return EndName;
	}

	@Override
	protected String getOther() {
		return MiddleName;
	}

	
	
	@Override
	public String getFirst() {
		return this.EndFirst;
	}

	@Override
	public String getNext() {
		return this.EndNext;
	}
	
	@Override
	public String genLetforLocality(String letVar, String quantifiedVar, final String quanitifiedOrderedVar) {
		final String letExpression = "let %1$s = (%3$s.%4$s)%2$s |";
		return String.format(letExpression, letVar, "."+quantifiedVar, quanitifiedOrderedVar, RName );
	}

	@Override
	public String getConcreteFirst() {
		return this.EndConcreteFirst;
	}

	@Override
	public String getConcreteNext() {
		return this.EndConcreteNext;
	}
	

}
