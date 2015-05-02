package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class SideBoth extends Side {

 

	public SideBoth(String rName, String sName, String sNext, String sFirst,
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
		return "";
	}

	@Override
	protected String getOther() {
		throw new RuntimeException("Invalid Procedure call");
	}

	@Override
	public String getFirst() {
		return this.MiddleFirst;
	}

	@Override
	public String getNext() {
		// TODO Auto-generated method stub
		return this.MiddleNext;
	}

	@Override
	public String getOtherFirst() {
		return this.EndFirst;
	}

	@Override
	public String getOtherNext() {
		return this.EndNext;
	}

	@Override
	public String genLetforLocality(String letVar, String quantifiedVar, final String quanitifiedOrderedVar) {
		final String letExpression = "let %1$s = (%2$s.%3$s) |";
		return String.format(letExpression, letVar, quanitifiedOrderedVar, RName );
	}

	
	@Override
	public boolean isConsistentOrdered() {
		if( !super.isConsistent() ) return false;

		if( getOtherFirst() == null ) return false;
		if( getOtherFirst().equals("") ) return false;

		if( getOtherNext() == null ) return false;
		if( getOtherNext().equals("") ) return false;
		
		return true;
		
	}

	
	
	
	@Override
	public String getConcreteFirst() {
		return this.MiddleConcreteFirst;
	}

	@Override
	public String getConcreteNext() {
		return this.MiddleConcreteNext;
	}

	@Override
	public String getConcreteOtherFirst() {
		return this.EndConcreteFirst;
	}

	@Override
	public String getConcreteOtherNext() {
		return this.EndConcreteNext;
	}
	
	
	@Override
	public boolean isConsistentConcreteOrdered() {
		if( !super.isConsistent() ) return false;

		if( getConcreteOtherFirst() == null ) return false;
		if( getConcreteOtherFirst().equals("") ) return false;

		if( getConcreteOtherNext() == null ) return false;
		if( getConcreteOtherNext().equals("") ) return false;
		
		return true;
		
	}
	
}
