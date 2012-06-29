package edu.uw.ece.se.sa;

import edu.mit.csail.sdg.alloy4compiler.ast.Sig;

public abstract class Subtype extends Link {

	public Subtype(Sig left, Sig right, String label) {
		super(left, right, label);
	}
}
