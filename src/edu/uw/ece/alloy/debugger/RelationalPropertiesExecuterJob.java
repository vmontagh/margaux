package edu.uw.ece.alloy.debugger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.gen.ExecuterJob;
import edu.uw.ece.alloy.debugger.exec.A4CommandExecuter;

public class RelationalPropertiesExecuterJob extends ExecuterJob {

	public RelationalPropertiesExecuterJob(String reportFile) {
		super(reportFile);
	}

	private static final long serialVersionUID = 1L;

	@Override
	protected void callExecuter(String fileName) throws Err {

		super.failureRecord = fileName + ",0,-1,-1,-1,-1";

		A4CommandExecuter.getInstance().run(new String[] { fileName }, rep);
		updateResult(fileName, rep.sat, rep.solveTime, rep.trasnalationTime,
				rep.totalVaraibles, rep.clauses);
	}

}
