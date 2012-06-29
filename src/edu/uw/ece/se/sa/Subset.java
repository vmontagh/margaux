package edu.uw.ece.se.sa;

import edu.mit.csail.sdg.alloy4compiler.ast.Sig;

public class Subset extends Subtype {

	public Subset(Sig left, Sig right) {
		super(left, right, "in");
	}

	@Override
	public String arrow() {
		return ARROW.normal.toString();
	}

	@Override
	public String toString() {
		return "Subset [getLeft()=" + getLeft() + ", getRight()=" + getRight()
				+ ", getLabel()=" + getLabel() + "]";
	}

	
}
