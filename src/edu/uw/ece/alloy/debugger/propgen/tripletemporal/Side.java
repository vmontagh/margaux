package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public abstract  class Side extends Property implements SideOrdered  {

	public final String EndNext, EndFirst;  
	public final String MiddleNext, MiddleFirst; 

	public final String EndConcreteNext, EndConcreteFirst;  
	public final String MiddleConcreteNext, MiddleConcreteFirst; 
	


	public Side(String rName, String sName, String sNext, String sFirst,
			String middleName, String endName, String rConcreteName,
			String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName, String endNext,
			String endFirst, String middleNext, String middleFirst,
			String endConcreteNext, String endConcreteFirst,
			String middleConcreteNext, String middleConcreteFirst) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName);
		EndNext = endNext;
		EndFirst = endFirst;
		MiddleNext = middleNext;
		MiddleFirst = middleFirst;
		EndConcreteNext = endConcreteNext;
		EndConcreteFirst = endConcreteFirst;
		MiddleConcreteNext = middleConcreteNext;
		MiddleConcreteFirst = middleConcreteFirst;
	}

	@Override
	protected String getPredecessor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getSuccessor() {
		// TODO Auto-generated method stub
		return null;
	}

	
	protected abstract String getOther() ;


	@Override
	protected boolean isConsistent() {
		return true;
	}
	
	@Override
	public String getOtherFirst() {
		throw new RuntimeException("Invalid Procedure call");
	}

	@Override
	public String getOtherNext() {
		throw new RuntimeException("Invalid Procedure call");
	}
	
	@Override
	public String getConcreteOtherFirst() {
		throw new RuntimeException("Invalid Procedure call");
	}

	@Override
	public String getConcreteOtherNext() {
		throw new RuntimeException("Invalid Procedure call");
	}

	
	final protected String letExpression = "let %1$s = %2$s(%3$s.%4$s) |";

	public abstract String genLetforLocality(final String letVar, final String quantifiedVar, final String quanitifiedOrderedVar);
	
	@Override
	public String getOderedParameters() {
		StringBuilder result = new StringBuilder();
		
		try{ result.append( getFirst()).append(": univ, ").append(getNext()).append(": univ->univ");}catch(RuntimeException e){}
		try{ result.append( getOtherFirst()).append(": univ, ").append(getOtherNext()).append(": univ->univ");}catch(RuntimeException e){}
		
		return result.length() > 0 ?  ", "+result.toString() : "";
	}
	
	@Override
	public String getConcreteOrderedParameters() {
		StringBuilder result = new StringBuilder();
		
		try{ result.append( getConcreteFirst()).append(", ").append(getConcreteNext());}catch(RuntimeException e){}
		try{ result.append( getConcreteOtherFirst()).append(", ").append(getConcreteOtherNext());}catch(RuntimeException e){}
		
		return result.length() > 0 ?  result.toString() : "";
	}
	
	@Override
	public boolean isConsistentOrdered() {
		if( !isConsistent() ) return false;
		
		if( getFirst() == null ) return false;
		if( getFirst().equals("") ) return false;

		if( getNext() == null ) return false;
		if( getNext().equals("") ) return false;
		
		return true;
		
	}
	
	@Override
	public boolean isConsistentConcreteOrdered() {
		if( !isConsistent() ) return false;
		
		if( getConcreteFirst() == null ) return false;
		if( getConcreteFirst().equals("") ) return false;

		if( getConcreteNext() == null ) return false;
		if( getConcreteNext().equals("") ) return false;
		
		return true;
		
	}
	
}
