package edu.uw.ece.alloy.debugger.knowledgebase;

/**
 * @author vajih
 *
 */
public final class TernaryImplicationLatticImperative extends ImplicationLatticImperative {

	protected TernaryImplicationLatticImperative(ImplicationLatticeImeprativeGenerator generator) {
		super(generator);
	}

	public TernaryImplicationLatticImperative() {
		this(new TemporalImplicationLatticeImperativeGenerator());
	}

}