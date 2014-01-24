package edu.mit.csail.sdg.gen;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4whole.ExampleUsingTheCompiler;

public class NextExecuterJob extends ExecuterJob {

	public NextExecuterJob(String reportFile) {

		super(reportFile);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void callExecuter(String fileName) throws Err {
		ExampleUsingTheCompiler.usingKodkod = true;
		ExampleUsingTheCompiler.usingKKItr = false;
		long now = System.currentTimeMillis();
		ExampleUsingTheCompiler.run(new String[]{fileName},rep);
		System.out.println(rep.solveTime);
		System.out.println(rep.trasnalationTime);
		long sTime = rep.started2Solve - now - rep.trasnalationTime;
		updateResult(System.currentTimeMillis(), fileName, rep.evalTime,
				sTime/*rep.solveTime*/, rep.trasnalationTime, rep.totalVaraibles, rep.clauses,/*ans.satisfiable()*/rep.sat==1,rep.evalInsts,-2);

	}
	
	public static void main(String[] args) throws Err {
		// TODO Auto-generated method stub
		NextExecuterJob nsej = new NextExecuterJob("report.txt");
		nsej.callExecuter(args[0]);
	}
	
	
}
