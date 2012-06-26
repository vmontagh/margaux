package edu.uw.ece.se.sa;

import edu.mit.csail.sdg.alloy4compiler.ast.Sig;

public class EqualSig  extends Link  {

	public EqualSig(Sig left, Sig right, String label) {
		super(left, right, label);
	}

}
