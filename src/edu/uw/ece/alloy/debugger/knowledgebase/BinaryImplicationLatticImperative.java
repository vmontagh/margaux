package edu.uw.ece.alloy.debugger.knowledgebase;

public class BinaryImplicationLatticImperative  extends ImplicationLatticImperative {

	protected BinaryImplicationLatticImperative(ImplicationLatticeImeprativeGenerator generator) {
		super(generator);
	}

	public BinaryImplicationLatticImperative() {
		this(new StructuralImplicationLatticeImperativeGenerator());
	}

}