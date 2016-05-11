package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.ProcessedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.ProcessedResult.Status;

public class AlloyProcessedResult extends ProcessedResult {

	private static final long serialVersionUID = 650740082971241007L;

	public AlloyProcessedResult(AlloyProcessingParam param) {
		super(param);
	}

	public AlloyProcessedResult(AlloyProcessingParam param, Status status) {
		super(param, status);
	}

	public AlloyProcessedResult(AlloyProcessedResult result) {
		this(result.getParam(), result.status);
		this.clauses = result.clauses;
		this.evalInsts = result.evalInsts;
		this.trasnalationTime = result.trasnalationTime;
		this.totalVaraibles = result.totalVaraibles;
		this.solveTime = result.solveTime;
		this.evalTime = result.evalTime;
		this.sat = result.sat;
	}

	public AlloyProcessedResult(AlloyProcessingParam param, Status status,
			long clauses, long evalInsts, long trasnalationTime, long totalVaraibles,
			long solveTime, long evalTime, int sat) {
		this(param, status);
		this.clauses = clauses;
		this.evalInsts = evalInsts;
		this.trasnalationTime = trasnalationTime;
		this.totalVaraibles = totalVaraibles;
		this.solveTime = solveTime;
		this.evalTime = evalTime;
		this.sat = sat;
	}

	public AlloyProcessingParam getParam() {
		return (AlloyProcessingParam) param;
	}

	public String asRecord() {
		return trasnalationTime + "," + totalVaraibles + "," + clauses + ","
				+ solveTime + "," + evalTime + "," + evalInsts + "," + sat;
	}

	public AlloyProcessedResult changeParams(final AlloyProcessingParam param) {
		return new AlloyProcessedResult(param, status, clauses, evalInsts,
				trasnalationTime, totalVaraibles, solveTime, evalTime, sat);
	}

	public String asRecordHeader() {
		return "trasnalationTime,trasnalationTime,totalVaraibles,clauses,solveTime,evalTime,evalInsts,sat";
	}

	/**
	 * return a copy of the result with actual property call
	 * 
	 * @return
	 */
	public AlloyProcessedResult updatePropertyCall() {
		return this;
	}

}
