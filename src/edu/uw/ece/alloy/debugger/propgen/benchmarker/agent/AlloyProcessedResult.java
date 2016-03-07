package edu.uw.ece.alloy.debugger.propgen.benchmarker.agent;

import edu.mit.csail.sdg.gen.MyReporter;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.PropertyToAlloyCode;

public class AlloyProcessedResult extends MyReporter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9097043109204297771L;
	final public AlloyProcessingParam params;

	public AlloyProcessedResult(AlloyProcessingParam params) {
		super();
		this.params = 
				params!=null ? 
						params.createItself() : 
							AlloyProcessingParam.EMPTY_PARAM;
	}
	
	public AlloyProcessedResult(AlloyProcessedResult result) {
		this(result.params);
		this.clauses = result.clauses;
		this.evalInsts = result.evalInsts;
		this.trasnalationTime = result.trasnalationTime;
		this.totalVaraibles = result.totalVaraibles;
		this.solveTime = result.solveTime;
		this.evalTime = result.evalTime;
		this.sat = result.sat;
	}
	
	public AlloyProcessedResult(AlloyProcessingParam params,long clauses, long evalInsts, long trasnalationTime, long totalVaraibles, long solveTime, long evalTime, int sat) {
		this(params);
		this.clauses = clauses;
		this.evalInsts = evalInsts;
		this.trasnalationTime = trasnalationTime;
		this.totalVaraibles = totalVaraibles;
		this.solveTime = solveTime;
		this.evalTime = evalTime;
		this.sat = sat;
	}
	
	
	
	public String asRecord() {
		return trasnalationTime + "," + totalVaraibles
				+ "," + clauses + "," + solveTime
				+ "," + evalTime + "," + evalInsts
				+ "," + sat ;
	}
	
	public AlloyProcessedResult changeParams(final AlloyProcessingParam params){
		return new AlloyProcessedResult(params, clauses,  evalInsts,  trasnalationTime,  totalVaraibles,  solveTime,  evalTime,  sat);
	}
	
	public boolean isTimedout(){
		return (this instanceof TimeoutResult) ;
	}
	
	public boolean isFailed() {
		return (this instanceof FailedResult) ;
	}
	
	public boolean isInferred() {
		return (this instanceof InferredResult) ;
	}
	
	public String asRecordHeader() {
		return "trasnalationTime,trasnalationTime,totalVaraibles,clauses,solveTime,evalTime,evalInsts,sat";
	}
	
	
	public static class InferredResult extends AlloyProcessedResult{

		public InferredResult(AlloyProcessingParam params) {
			super(params);
			this.trasnalationTime = 0;
			this.totalVaraibles = 0;
			this.clauses = 0;
			this.solveTime = 0;
			this.evalInsts = 0;
			this.evalTime = 0;
			this.sat = 0;
		}
		
		

		public InferredResult(AlloyProcessedResult result) {
			super(result);
			// TODO Auto-generated constructor stub
		}

		public InferredResult(AlloyProcessingParam params, long clauses,
				long evalInsts, long trasnalationTime, long totalVaraibles,
				long solveTime, long evalTime, int sat) {
			super(params, clauses, evalInsts, trasnalationTime, totalVaraibles, solveTime,
					evalTime, sat);
			// TODO Auto-generated constructor stub
		}



		/**
		 * 
		 */
		private static final long serialVersionUID = 7694649523710787654L;
		
		/**
		 * Creating a an Inferred result from another result. 
		 * If the inferring steps are not valid, then it throws exception. 
		 * @param inferredFrom
		 * @param coder
		 * @return
		 */
		public static InferredResult createInferredResult(AlloyProcessedResult inferredFrom, PropertyToAlloyCode coder, boolean sat){
			return new InferredResult(inferredFrom.params.createIt(coder), 0, 0, 0, 0, 0, 0, sat ? 1 : 0); 
		}
		
		
	}
	
	public static class FailedResult extends AlloyProcessedResult{
		
		final public String REASON;
		/**
		 * 
		 */
		private static final long serialVersionUID = -8538317738030842240L;

		public FailedResult(AlloyProcessingParam params) {
			this(params, "?");			
		}
		
		public FailedResult(final AlloyProcessingParam params, final String reason) {
			super(params);
			this.trasnalationTime = 0;
			this.totalVaraibles = 0;
			this.clauses = 0;
			this.solveTime = 0;
			this.evalInsts = 0;
			this.evalTime = 0;
			this.sat = 0;
			this.REASON = reason;
		}
		
		public AlloyProcessedResult changeParams(final AlloyProcessingParam params){
			return new FailedResult(params, REASON);
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
		
		public AlloyProcessedResult changeParams(final AlloyProcessingParam params){
			return new TimeoutResult(params);
		}
		
		
	}


}
