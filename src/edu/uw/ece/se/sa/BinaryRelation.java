package edu.uw.ece.se.sa;

import edu.mit.csail.sdg.alloy4compiler.ast.Sig;

public class BinaryRelation extends Link {

	public enum Multiplicity {NONE,SET,ONE,LONE}
	
	final private Multiplicity multiplicity;
	
	public BinaryRelation(Sig left, Sig right, String label, Multiplicity multiplicity) {
		super(left, right, label);
		this.multiplicity = multiplicity;
	}

	public Multiplicity getMultiplicity() {
		return multiplicity;
	}

	@Override
	public String toString() {
		return "BinaryRelation [multiplicity=" + multiplicity + ", getLeft()="
				+ getLeft() + ", getRight()=" + getRight() + ", getLabel()="
				+ getLabel() + "]";
	}
	
	
}
