package edu.mit.csail.sdg.gen;

import java.io.File;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4whole.ExampleUsingTheCompiler;

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

		int[] paces = new int[]{2,2,2,4,4,8,8,16,16,32,32,64,64};
		
		
		String logName = Util.tail(fileName).replace(".", "_")+".log";
		File oldfile =new File(logName);
		File newfile =new File(logName.replace(".", System.currentTimeMillis()+ "."));
		
		oldfile.renameTo(newfile);
		
		for(int pace:paces){
			ExampleUsingTheCompiler.PACE = pace;
			ExampleUsingTheCompiler.run(new String[]{fileName},rep);
			updateResult(System.currentTimeMillis(), fileName, rep.evalTime,
					rep.solveTime, rep.trasnalationTime, rep.totalVaraibles, rep.clauses,/*ans.satisfiable()*/rep.sat==1,-1,-1);

		}

	}
	
	/**
	 * @param args
	 * @throws Err 
	 */
	public static void main(String[] args) throws Err {
		// TODO Auto-generated method stub
		WalkerExecuterJob nsej = new WalkerExecuterJob("report.txt");
		nsej.callExecuter(args[0]);
	}

}
