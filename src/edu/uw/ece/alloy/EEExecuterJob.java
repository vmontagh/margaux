package edu.uw.ece.alloy;

import java.io.File;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4whole.ExampleUsingTheCompiler;

public class EEExecuterJob extends ExecuterJob {

	public EEExecuterJob(String reportFile) {
		super(reportFile);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void callExecuter(String fileName) throws Err {
		
		Configuration.setProp(Configuration.USING_KODKOD, String.valueOf(true));
		Configuration.setProp(Configuration.USING_KK_ITR, String.valueOf(false));
		Configuration.setProp(Configuration.SYMMETRY_OFF, String.valueOf(true));
		Configuration.setProp(Configuration.USING_SYMMETRY, String.valueOf(false));

		
		ExampleUsingTheCompiler.run(new String[]{fileName},rep);
		updateResult( fileName,3,"--", rep.evalInsts, rep.evalTime, rep.solveTime, 
				rep.trasnalationTime, rep.totalVaraibles, rep.clauses, rep.solveTime +rep.trasnalationTime +rep.evalTime,rep.sat);


		
	}
}
