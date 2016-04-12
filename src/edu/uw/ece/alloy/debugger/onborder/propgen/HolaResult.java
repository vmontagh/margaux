package edu.uw.ece.alloy.debugger.onborder.propgen;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyProcessedResult;
import kodkod.instance.Instance;

public class HolaResult extends AlloyProcessedResult {

	private static final long serialVersionUID = -8861660779730219972L;
	
	private Instance instance;
	private boolean lastResult;
	
	public HolaResult(Instance instance) {
		this(instance, false);
	}

	public HolaResult(Instance instance, final boolean lastResult) {
		super(AlloyProcessingParam.EMPTY_PARAM);
		this.instance = instance;
		this.lastResult = lastResult;
	}
	
	public Instance getInstance() {
		return this.instance;
	}
	
	public boolean isLast() {
		return this.lastResult;
	}
}
