package edu.mit.csail.sdg.gen;

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
		ExampleUsingTheCompiler.usingKodkod = false;
		ExampleUsingTheCompiler.run(new String[]{fileName},rep);
		updateResult(System.currentTimeMillis(), fileName, rep.evalTime,
				rep.solveTime, rep.trasnalationTime, rep.totalVaraibles, rep.clauses,/*ans.satisfiable()*/rep.sat==1,rep.evalInsts,-1);

	}

	


}
