package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public abstract class CmpstOrds extends Property {

	final Ord order1, order2;
	
	public CmpstOrds(String rName, String sName, String sNext,
			String sFirst, String middleName, String endName,
			String rConcreteName, String sConcreteName, String sConcreteNext,
			String sConcreteFirst, String mConcreteName, String eConcreteName,
			Ord order1, Ord order2) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName);
		this.order1 = order1;
		this.order2 = order2;
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

	@Override
	protected boolean isConsistent() {
		
		if( order1 == null) return false;
		if( order2 == null) return false;
		
		//order1 and order2 has to be define over the same relations.
		if(! order1.RName.equals(order2.RName)) return false;
		if(! order1.SName.equals(order2.SName)) return false;
		if(! order1.SNext.equals(order2.SNext)) return false;
		if(! order1.SFirst.equals(order2.SFirst)) return false;
		if(! order1.MiddleName.equals(order2.MiddleName)) return false;
		if(! order1.EndName.equals(order2.EndName)) return false;
		if(! order1.RConcreteName.equals(order2.RConcreteName)) return false;
		if(! order1.SConcreteName.equals(order2.SConcreteName)) return false;
		if(! order1.SConcreteNext.equals(order2.SConcreteNext)) return false;
		if(! order1.SConcreteFirst.equals(order2.SConcreteFirst)) return false;
		if(! order1.MConcreteName.equals(order2.MConcreteName)) return false;
		if(! order1.EConcreteName.equals(order2.EConcreteName)) return false;
		
		if(! order1.sizeProp.growthLocality.equals(order2.sizeProp.growthLocality)) return false;
		if(! order1.sizeProp.empty.equals(order2.sizeProp.empty)) return false;
		
		if(! order1.isConsistent()) return false;
		if(! order2.isConsistent()) return false;
		
		if( order1.equals( order2) ) return false;
		
		return true;
	}

	@Override
	protected String genBody() {

		String result = order1.genBody();
		
		String secondSize = order2.sizeProp.genGrowthOredred() + " " + order2.genOrder();
		secondSize = secondSize.replaceAll(order2.sizeProp.getGrowthOrderedDelta(), order2.sizeProp.getGrowthOrderedDelta()+"'");
		
		result = result + compositeOperator() + secondSize;
		
		return result;
	}
	
	
	@Override
	public String genPredName() {
		return super.genPredName()+order1.genPredName()+"_"+order2.genPredName();
	}

	@Override
	protected String genParameters() {
		return order1.genParameters();
	}

	public String genParametesCall(){
		return order1.genParametesCall() ;
	}
	
	protected abstract String compositeOperator();

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((order1 == null) ? 0 : order1.hashCode());
		result = prime * result + ((order2 == null) ? 0 : order2.hashCode());
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
		if (!(obj instanceof CmpstOrds)) {
			return false;
		}
		CmpstOrds other = (CmpstOrds) obj;
		if (order1 == null) {
			if (other.order1 != null) {
				return false;
			}
		} else if (!order1.equals(other.order1)) {
			return false;
		}
		if (order2 == null) {
			if (other.order2 != null) {
				return false;
			}
		} else if (!order2.equals(other.order2)) {
			return false;
		}
		return true;
	}
	
	
	

}
