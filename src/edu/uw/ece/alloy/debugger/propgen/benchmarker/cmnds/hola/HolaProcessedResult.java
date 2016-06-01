package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.hola;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.ProcessedResult;

public class HolaProcessedResult extends ProcessedResult {

	private static final long serialVersionUID = 2058796798876672112L;
	
	private Object result;
	
	public HolaProcessedResult(HolaProcessedResult result) {
		this(result.getParam());
		this.result = result.result;
	}
	
	public HolaProcessedResult(HolaProcessingParam param) {
		super(param);
	}

	public HolaProcessedResult(HolaProcessingParam param, Status status) {
		super(param, status);
	}
	
	public HolaProcessedResult(HolaProcessingParam param, Status status, Object result) {
		super(param, status);
		this.result = result;
	}
	
	public Object getResult() {
		return result;
	}
	
	public HolaProcessingParam getParam() {
		return (HolaProcessingParam) param;
	}
}
