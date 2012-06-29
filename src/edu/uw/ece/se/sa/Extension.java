package edu.uw.ece.se.sa;

import edu.mit.csail.sdg.alloy4compiler.ast.Sig;

public class Extension extends Subtype {

	public Extension(Sig left, Sig right) {
		super(left, right, "ext");
	}

	@Override
	public String toString() {
		return "Extension [getLeft()=" + getLeft() + ", getRight()="
				+ getRight() + ", getLabel()=" + getLabel() + "]";
	}

	@Override
	public String arrow() {
		return ARROW.empty.toString();
	}

}
