package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public abstract  class Sd extends Property implements SdOrd  {

	public final String EndNext, EndFirst;  
	public final String MiddleNext, MiddleFirst; 

	public final String EndConcreteNext, EndConcreteFirst;  
	public final String MiddleConcreteNext, MiddleConcreteFirst; 
	


	public Sd(String rName, String sName, String sNext, String sFirst,
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((EndConcreteFirst == null) ? 0 : EndConcreteFirst.hashCode());
		result = prime * result
				+ ((EndConcreteNext == null) ? 0 : EndConcreteNext.hashCode());
		result = prime * result
				+ ((EndFirst == null) ? 0 : EndFirst.hashCode());
		result = prime * result + ((EndNext == null) ? 0 : EndNext.hashCode());
		result = prime
				* result
				+ ((MiddleConcreteFirst == null) ? 0 : MiddleConcreteFirst
						.hashCode());
		result = prime
				* result
				+ ((MiddleConcreteNext == null) ? 0 : MiddleConcreteNext
						.hashCode());
		result = prime * result
				+ ((MiddleFirst == null) ? 0 : MiddleFirst.hashCode());
		result = prime * result
				+ ((MiddleNext == null) ? 0 : MiddleNext.hashCode());
		result = prime * result
				+ ((letExpression == null) ? 0 : letExpression.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof Sd)) {
			return false;
		}
		Sd other = (Sd) obj;
		if (EndConcreteFirst == null) {
			if (other.EndConcreteFirst != null) {
				return false;
			}
		} else if (!EndConcreteFirst.equals(other.EndConcreteFirst)) {
			return false;
		}
		if (EndConcreteNext == null) {
			if (other.EndConcreteNext != null) {
				return false;
			}
		} else if (!EndConcreteNext.equals(other.EndConcreteNext)) {
			return false;
		}
		if (EndFirst == null) {
			if (other.EndFirst != null) {
				return false;
			}
		} else if (!EndFirst.equals(other.EndFirst)) {
			return false;
		}
		if (EndNext == null) {
			if (other.EndNext != null) {
				return false;
			}
		} else if (!EndNext.equals(other.EndNext)) {
			return false;
		}
		if (MiddleConcreteFirst == null) {
			if (other.MiddleConcreteFirst != null) {
				return false;
			}
		} else if (!MiddleConcreteFirst.equals(other.MiddleConcreteFirst)) {
			return false;
		}
		if (MiddleConcreteNext == null) {
			if (other.MiddleConcreteNext != null) {
				return false;
			}
		} else if (!MiddleConcreteNext.equals(other.MiddleConcreteNext)) {
			return false;
		}
		if (MiddleFirst == null) {
			if (other.MiddleFirst != null) {
				return false;
			}
		} else if (!MiddleFirst.equals(other.MiddleFirst)) {
			return false;
		}
		if (MiddleNext == null) {
			if (other.MiddleNext != null) {
				return false;
			}
		} else if (!MiddleNext.equals(other.MiddleNext)) {
			return false;
		}
		if (letExpression == null) {
			if (other.letExpression != null) {
				return false;
			}
		} else if (!letExpression.equals(other.letExpression)) {
			return false;
		}
		return true;
	}
	
	
	
}
