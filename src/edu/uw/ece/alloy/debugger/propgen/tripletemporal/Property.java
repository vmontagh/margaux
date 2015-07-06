package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

/**
 * The triple relation is r:S->A->B
 * S has a temporal relation
 * A is called middle
 * B is called End
 * @author vajih
 *
 */
public abstract class Property {
	
	//The local variable names used for generated preds. 
	public final String RName;
	public final String SName;
	public final String SNext;
	public final String SFirst;
	public final String MiddleName;
	public final String EndName;
	
	//The concrete variables store the parameters names that are passed to a pred call. 
	public final String RConcreteName;
	public final String SConcreteName;
	public final String SConcreteNext;
	public final String SConcreteFirst;
	public final String MConcreteName;
	public final String EConcreteName;
	


	public Property(String rName, String sName, String sNext, String sFirst,
			String middleName, String endName, String rConcreteName,
			String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName) {
		super();
		RName = rName;
		SName = sName;
		SNext = sNext;
		SFirst = sFirst;
		MiddleName = middleName;
		EndName = endName;
		RConcreteName = rConcreteName;
		SConcreteName = sConcreteName;
		SConcreteNext = sConcreteNext;
		SConcreteFirst = sConcreteFirst;
		MConcreteName = mConcreteName;
		EConcreteName = eConcreteName;
	}
	protected abstract String getPredecessor();
	protected abstract String getSuccessor();
	
	protected abstract boolean isConsistent();
	
	public  String genPredName(){
		return this.getClass().getSimpleName()+"_";
	}
	protected String genParameters(){
		return RName+": univ->univ->univ, "+SName+", "+MiddleName+", "+EndName+": univ, "+SFirst+": univ, "+SNext+": univ->univ";
	}
	protected abstract String genBody();
	
	protected String genParametesCall(){
		return RConcreteName+", "+SConcreteName+", "+MConcreteName+", "+EConcreteName+", "+SConcreteFirst+", "+SConcreteNext;
	}
	
	public String generateProp(){
		
		if (!isConsistent()){
			return "";
		}
		
		StringBuilder result = new StringBuilder();
		
		result.append("pred ").append(genPredName()).append("[").append(genParameters()).append("]{\n");
		result.append(genBody()).append("\n}");
		return result.toString();
	}

	
	public String genPredCall(){
		if (!isConsistent()){
			return "";
		}
		
		StringBuilder result = new StringBuilder();
		
		
		result.append(genPredName()).append("[").append(genParametesCall()).append("]\n");
		
		return result.toString();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((EConcreteName == null) ? 0 : EConcreteName.hashCode());
		result = prime * result + ((EndName == null) ? 0 : EndName.hashCode());
		result = prime * result
				+ ((MConcreteName == null) ? 0 : MConcreteName.hashCode());
		result = prime * result
				+ ((MiddleName == null) ? 0 : MiddleName.hashCode());
		result = prime * result
				+ ((RConcreteName == null) ? 0 : RConcreteName.hashCode());
		result = prime * result + ((RName == null) ? 0 : RName.hashCode());
		result = prime * result
				+ ((SConcreteFirst == null) ? 0 : SConcreteFirst.hashCode());
		result = prime * result
				+ ((SConcreteName == null) ? 0 : SConcreteName.hashCode());
		result = prime * result
				+ ((SConcreteNext == null) ? 0 : SConcreteNext.hashCode());
		result = prime * result + ((SFirst == null) ? 0 : SFirst.hashCode());
		result = prime * result + ((SName == null) ? 0 : SName.hashCode());
		result = prime * result + ((SNext == null) ? 0 : SNext.hashCode());
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
		if (obj == null) {
			return false;
		}		
		if( !( obj.getClass().equals(this.getClass()) ) ){
			return false;
		}
		if (!(obj instanceof Property)) {
			return false;
		}
		Property other = (Property) obj;
		if (EConcreteName == null) {
			if (other.EConcreteName != null) {
				return false;
			}
		} else if (!EConcreteName.equals(other.EConcreteName)) {
			return false;
		}
		if (EndName == null) {
			if (other.EndName != null) {
				return false;
			}
		} else if (!EndName.equals(other.EndName)) {
			return false;
		}
		if (MConcreteName == null) {
			if (other.MConcreteName != null) {
				return false;
			}
		} else if (!MConcreteName.equals(other.MConcreteName)) {
			return false;
		}
		if (MiddleName == null) {
			if (other.MiddleName != null) {
				return false;
			}
		} else if (!MiddleName.equals(other.MiddleName)) {
			return false;
		}
		if (RConcreteName == null) {
			if (other.RConcreteName != null) {
				return false;
			}
		} else if (!RConcreteName.equals(other.RConcreteName)) {
			return false;
		}
		if (RName == null) {
			if (other.RName != null) {
				return false;
			}
		} else if (!RName.equals(other.RName)) {
			return false;
		}
		if (SConcreteFirst == null) {
			if (other.SConcreteFirst != null) {
				return false;
			}
		} else if (!SConcreteFirst.equals(other.SConcreteFirst)) {
			return false;
		}
		if (SConcreteName == null) {
			if (other.SConcreteName != null) {
				return false;
			}
		} else if (!SConcreteName.equals(other.SConcreteName)) {
			return false;
		}
		if (SConcreteNext == null) {
			if (other.SConcreteNext != null) {
				return false;
			}
		} else if (!SConcreteNext.equals(other.SConcreteNext)) {
			return false;
		}
		if (SFirst == null) {
			if (other.SFirst != null) {
				return false;
			}
		} else if (!SFirst.equals(other.SFirst)) {
			return false;
		}
		if (SName == null) {
			if (other.SName != null) {
				return false;
			}
		} else if (!SName.equals(other.SName)) {
			return false;
		}
		if (SNext == null) {
			if (other.SNext != null) {
				return false;
			}
		} else if (!SNext.equals(other.SNext)) {
			return false;
		}
		return true;
	}
	
	
	
}
