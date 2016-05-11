package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public interface SdOrd {

	public String getFirst();

	public String getNext();

	public String getOtherFirst();

	public String getOtherNext();

	public String getConcreteFirst();

	public String getConcreteNext();

	public String getConcreteOtherFirst();

	public String getConcreteOtherNext();

	public String getOderedParameters();

	public String getConcreteOrderedParameters();

	public boolean isConsistentOrdered();

	public boolean isConsistentConcreteOrdered();

}
