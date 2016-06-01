package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.hola;

import java.util.HashMap;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.ProcessedResult;

public class HolaProcessedResult extends ProcessedResult {

	private static final long serialVersionUID = 2058796798876672112L;
	
	private HashMap<String, String> result;
	
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
	
	public HolaProcessedResult(HolaProcessingParam param, Status status, HashMap<String, String> map) {
		super(param, status);
		this.result = map;
	}
	
	public HashMap<String, String> getResults() {
		return result;
	}
	
	public HolaProcessingParam getParam() {
		return (HolaProcessingParam) param;
	}
}
