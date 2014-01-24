package edu.mit.csail.sdg.gen;


import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;

public class OldSyntaxExecuterJob extends ExecuterJob  {


	public OldSyntaxExecuterJob(String reportFile) {
		super(reportFile);
		// TODO Auto-generated constructor stub
	}


	/**
	 * 
	 */
	private static final long serialVersionUID = 14343240957083L;


	protected void callExecuter(String fileName) {

		copyFromJAR();
		final String binary = alloyHome() + fs + "binary";
		System.out.println(binary);
		// Add the new JNI location to the java.library.path
		try {
			System.setProperty("java.library.path", binary);
			// The above line is actually useless on Sun JDK/JRE (see Sun's bug ID 4280189)
			// The following 4 lines should work for Sun's JDK/JRE (though they probably won't work for others)
			String[] newarray = new String[]{binary};
			java.lang.reflect.Field old = ClassLoader.class.getDeclaredField("usr_paths");
			old.setAccessible(true);
			old.set(null,newarray);
		} catch (Throwable ex) { }


		System.out.println("=========== Parsing+Typechecking "+fileName+" =============");
		try {
			CompModule world = CompUtil.parseEverything_fromFile(rep, null, fileName);

			// Choose some default options for how you want to execute the commands
			A4Options options = new A4Options();

			options.solver = A4Options.SatSolver.MiniSatJNI;
			for (Command command: world.getAllCommands()) {
				// Execute the command
				System.out.println("============ Command "+command+": ============");
				A4Solution ans = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command, options);

				updateResult(System.currentTimeMillis(), fileName, -1,
						rep.solveTime, rep.trasnalationTime, rep.totalVaraibles, rep.clauses,ans.satisfiable(),-1,-1);

				

				if (ans.satisfiable()) {ans.writeXML(fileName+".xml");}

			}

		} catch (Err e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		OldSyntaxExecuterJob nsej = new OldSyntaxExecuterJob("report.txt");
		nsej.callExecuter(args[0]);
		nsej = new OldSyntaxExecuterJob("report.txt");
		nsej.callExecuter(args[1]);
	}

}
