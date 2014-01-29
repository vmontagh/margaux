package edu.mit.csail.sdg.gen;

import java.io.File;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4whole.ExampleUsingTheCompiler;
import edu.mit.csail.sdg.gen.alloy.Configuration;

public class WalkerExecuterJob extends ExecuterJob {

	public WalkerExecuterJob(String reportFile) {
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
		Configuration.setProp(Configuration.USING_KK_ITR, String.valueOf(true));
		Configuration.setProp(Configuration.PACE, String.valueOf(4));

		ExampleUsingTheCompiler.run(new String[]{fileName},rep);
		updateResult(System.currentTimeMillis(), fileName, rep.evalTime,
				rep.solveTime, rep.trasnalationTime, rep.totalVaraibles, rep.clauses,/*ans.satisfiable()*/rep.sat==1, rep.evalInsts, 
				Integer.valueOf(Configuration.getProp(Configuration.PACE)) );

	}

	/**
	 * @param args
	 * @throws Err 
	 */
	public static void main(String[] args) throws Err {
		// TODO Auto-generated method stub
		WalkerExecuterJob nsej = new WalkerExecuterJob("report.txt");
		nsej.callExecuter("models/partial/gen/stm/tmp/BST_EE_gpce2013_template_3.als");
		//nsej.callExecuter("models/partial/gen/phone.als");
	}

}
