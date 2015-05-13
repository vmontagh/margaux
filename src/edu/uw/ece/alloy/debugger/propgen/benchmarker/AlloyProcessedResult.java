package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import edu.mit.csail.sdg.gen.MyReporter;

public class AlloyProcessedResult extends MyReporter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9097043109204297771L;
	final public AlloyProcessingParam params;

	public AlloyProcessedResult(AlloyProcessingParam params) {
		super();
		this.params = new AlloyProcessingParam(params);
	}
	
	
	public String asRecord() {
		return trasnalationTime + "," + totalVaraibles
				+ "," + clauses + "," + solveTime
				+ "," + evalTime + "," + evalInsts
				+ "," + sat ;
	}
	
	public boolean isTimedout(){
		return (this instanceof TimeoutResult) ;
	}
	
	public boolean isFailed() {
		return (this instanceof FailedResult) ;
	}
	
	
	public String asRecordHeader() {
		return "trasnalationTime,trasnalationTime,totalVaraibles,clauses,solveTime,evalTime,evalInsts,sat";
	}
	
	public static class FailedResult extends AlloyProcessedResult{

		/**
		 * 
		 */
		private static final long serialVersionUID = -8538317738030842240L;

		public FailedResult(AlloyProcessingParam params) {
			super(params);
			this.trasnalationTime = 0;
			this.totalVaraibles = 0;
			this.clauses = 0;
			this.solveTime = 0;
			this.evalInsts = 0;
			this.evalTime = 0;
			this.sat = 0;
		}
		
	}
	
	public static class TimeoutResult extends AlloyProcessedResult{

		/**
		 * 
		 */
		private static final long serialVersionUID = -1237495607768251197L;

		public TimeoutResult(AlloyProcessingParam params) {
			super(params);
			this.trasnalationTime = -1;
			this.totalVaraibles = -1;
			this.clauses = -1;
			this.solveTime = -1;
			this.evalInsts = -1;
			this.evalTime = -1;
			this.sat = 0;
		}
		
	}


	

}
