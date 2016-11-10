package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public abstract class SzPrpty extends Property {

	final protected Lclty growthLocality;
	final protected Emptnes empty;

	public SzPrpty(String rName, String sName, String sNext, String sFirst,
			String middleName, String endName, String rConcreteName,
			String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName, Lclty growthLocality,
			Emptnes empty) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName);
		this.growthLocality = growthLocality;
		this.empty = empty;
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

		if (!growthLocality.isConsistent())
			return false;

		return true;
	}

	@Override
	public String genPredName() {
		return super.genPredName() + growthLocality.genPredName()
				+ empty.genPredName();
	}

	private String genBody(final String append) {
		return empty.genBody() + "\n" + "all " + getTemporalQuantifiedVar() + ": "
				+ SName + " - relational_properties/last[" + SName + "," + SNext + "] |"
				+ "let " + getNextTemporalQuantifiedVar() + " = "
				+ getTemporalQuantifiedVar()
				+ "." + SNext + " |\n" + growthLocality
						.genBody(getTemporalQuantifiedVar(), getNextTemporalQuantifiedVar())
				+ append;
	}

	@Override
	protected String genBody() {
		return genBody(genGrowth());
	}

	protected String genBodyOrdered() {
		return genBody(genGrowthOredred());
	}

	protected abstract String genGrowth();

	protected abstract String genGrowthOredred();

	protected String getGrowthOrderedDelta() {
		return "delta";
	}

	protected String getTemporalQuantifiedVar() {
		return SName + "'";
	}

	protected String getNextTemporalQuantifiedVar() {
		return getTemporalQuantifiedVar() + "'";
	}

	protected abstract String orderedChange();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((empty == null) ? 0 : empty.hashCode());
		result = prime * result
				+ ((growthLocality == null) ? 0 : growthLocality.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
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
		if (!(obj instanceof SzPrpty)) {
			return false;
		}
		SzPrpty other = (SzPrpty) obj;
		if (empty == null) {
			if (other.empty != null) {
				return false;
			}
		} else if (!empty.equals(other.empty)) {
			return false;
		}
		if (growthLocality == null) {
			if (other.growthLocality != null) {
				return false;
			}
		} else if (!growthLocality.equals(other.growthLocality)) {
			return false;
		}
		return true;
	}
	
	@Override
	int getClassPriority() {
		return 5;
	}
	@Override
	int getOtherComponentPriorities() {
		return empty.getPriority() + growthLocality.getPriority();
	}

}
