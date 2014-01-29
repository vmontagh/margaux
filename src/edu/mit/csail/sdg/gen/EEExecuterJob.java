package edu.mit.csail.sdg.gen;

import java.io.File;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4whole.ExampleUsingTheCompiler;
import edu.mit.csail.sdg.gen.alloy.Configuration;

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
		updateResult(System.currentTimeMillis(), fileName, rep.evalTime,
				rep.solveTime, rep.trasnalationTime, rep.totalVaraibles, rep.clauses,/*ans.satisfiable()*/rep.sat==1,rep.evalInsts,-1);

	}
	
	
	public static void main(String[] args) throws Err {
		// TODO Auto-generated method stub
		EEExecuterJob nsej = new EEExecuterJob("expr_output/report.txt");
		nsej.callExecuter("models/partial/gen/stm/tmp/BST_EE_gpce2013_template_4.als");
//		nsej = new EEExecuterJob("report.txt");
//		nsej.callExecuter("models/partial/gen/stm/tmp/LLS_EE_gpce2013_template_3.als");
	}
}
