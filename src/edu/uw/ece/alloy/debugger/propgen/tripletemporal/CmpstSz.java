package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public abstract class CmpstSz extends Property {

	final SzPrpty size1, size2;

	public CmpstSz(String rName, String sName, String sNext,
			String sFirst, String middleName, String endName,
			String rConcreteName, String sConcreteName, String sConcreteNext,
			String sConcreteFirst, String mConcreteName, String eConcreteName,
			SzPrpty size1, SzPrpty size2) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName);
		this.size1 = size1;
		this.size2 = size2;
	}

	@Override
	protected String getPredecessor() {
		return null;
	}

	@Override
	protected String getSuccessor() {
		return null;
	}

	@Override
	protected boolean isConsistent() {
		
		if( size1 == null) return false;
		if( size2 == null) return false;
		
		//size1 and size2 has to be define over the same relations.
		if(! size1.RName.equals(size2.RName)) return false;
		if(! size1.SName.equals(size2.SName)) return false;
		if(! size1.SNext.equals(size2.SNext)) return false;
		if(! size1.SFirst.equals(size2.SFirst)) return false;
		if(! size1.MiddleName.equals(size2.MiddleName)) return false;
		if(! size1.EndName.equals(size2.EndName)) return false;
		if(! size1.RConcreteName.equals(size2.RConcreteName)) return false;
		if(! size1.SConcreteName.equals(size2.SConcreteName)) return false;
		if(! size1.SConcreteNext.equals(size2.SConcreteNext)) return false;
		if(! size1.SConcreteFirst.equals(size2.SConcreteFirst)) return false;
		if(! size1.MConcreteName.equals(size2.MConcreteName)) return false;
		if(! size1.EConcreteName.equals(size2.EConcreteName)) return false;

		if(! size1.growthLocality.equals(size2.growthLocality)) return false;
		if(! size1.empty.equals(size2.empty)) return false;

		if(! size1.isConsistent()) return false;
		if(! size2.isConsistent()) return false;

		if( size1.equals(size2)) return false;
		
		return true;
	}

	@Override
	protected String genBody() {
		String result = size1.genBody();
		result = result + compositeOperator() + size2.genGrowth();
		return result;
	}

	@Override
	public String genPredName() {
		return super.genPredName()+size1.genPredName()+"_"+size2.genPredName();
	}

	@Override
	protected String genParameters() {
		return size1.genParameters();
	}

	public String genParametesCall(){
		return size1.genParametesCall() ;
	}

	protected abstract String compositeOperator();

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((size1 == null) ? 0 : size1.hashCode());
		result = prime * result + ((size2 == null) ? 0 : size2.hashCode());
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
		if (!(obj instanceof CmpstSz)) {
			return false;
		}
		CmpstSz other = (CmpstSz) obj;
		if (size1 == null) {
			if (other.size1 != null) {
				return false;
			}
		} else if (!size1.equals(other.size1)) {
			return false;
		}
		if (size2 == null) {
			if (other.size2 != null) {
				return false;
			}
		} else if (!size2.equals(other.size2)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CompositeSizes [size1=" + size1 + ", size2=" + size2 + "]";
	}
	
	
	
}
