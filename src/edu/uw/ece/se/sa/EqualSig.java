package edu.uw.ece.se.sa;

import edu.mit.csail.sdg.alloy4compiler.ast.Sig;

public class EqualSig  extends Subtype  {

	public EqualSig(Sig left, Sig right) {
		super(left, right, "=");
	}

	@Override
	public String arrow() {
		return ARROW.none.toString();
	}

	@Override
	public String toString() {
		return "EqualSig [getLeft()=" + getLeft() + ", getRight()="
				+ getRight() + ", getLabel()=" + getLabel() + "]";
	}

	
}
