package edu.mit.csail.sdg.alloy4compiler.translator;


import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import edu.mit.csail.sdg.moolloy.solver.kodkod.api.Objective;

public class JavaFilePrinter {
	
	PrintWriter file;
	public JavaFilePrinter(PrintWriter fw) {
		declarations = new ArrayList<String>();
		statements = new ArrayList<String>();
		
		file = fw;
	}
	
	public void pushDeclaration(String decl) {
		declarations.add(decl);
	}
	
	public void pushStatement(String stmt){
		statements.add(stmt);
	}
	
	public static final int MAX_STATEMENTS = 20;
	
	public void printToFile(TreeSet<Objective> objectives, int bitwidth, String result) {

      file.printf("import java.util.Arrays;%n");
      file.printf("import java.util.List;%n");
      file.printf("import java.util.TreeSet;%n");
      file.printf("import java.io.PrintWriter;%n");
      file.printf("import java.io.FileWriter;%n");
      file.printf("import kodkod.ast.*;%n");
      file.printf("import kodkod.ast.operator.*;%n");
      file.printf("import kodkod.instance.*;%n");
      file.printf("import kodkod.engine.*;%n");
      file.printf("import kodkod.engine.satlab.SATFactory;%n");
      file.printf("import kodkod.engine.config.Options;%n%n");
      file.printf("import edu.mit.csail.sdg.moolloy.solver.kodkod.api.*;%n%n");
      
      file.printf("public final class Test {%n%n");
      
      for( String str : declarations ) {
    	  file.print("static ");
    	  file.print(str);
    	  file.print("\n");
      }
      
      int numberOfMethods = ( declarations.size() / MAX_STATEMENTS ) + 1;
      Iterator<String> itr = statements.iterator();
      
      for( int i=0; i<numberOfMethods; i++ ) {
    	  file.printf("static void Do_%d(){", i );
    	  
    	  for( int j=0; j<MAX_STATEMENTS; j++ ) {
	    	  if(itr.hasNext()) {
	    		  file.print(itr.next());
	    		  file.print("\n");
	    	  }
    	  }
    	  
    	  file.print("}\n");
    	
      }
      
      
      file.printf("public static void main(String[] args) throws Exception {%n%n");
      
      
      for( int i=0; i<numberOfMethods; i++ ) {
    	  file.printf("Do_%d();%n", i );
      }
      
      
      
      
      
      
	      
	      
		if (objectives == null) {
			file.printf("%nSolver solver = new Solver();");
			file.printf("%nsolver.options().setSolver(SATFactory.DefaultSAT4J);");
			file.printf("%nsolver.options().setBitwidth(%d);", bitwidth);
			file.printf("%nsolver.options().setFlatten(false);");
			file.printf("%nsolver.options().setIntEncoding(Options.IntEncoding.TWOSCOMPLEMENT);");
			file.printf("%nsolver.options().setSymmetryBreaking(20);");
			file.printf("%nsolver.options().setSkolemDepth(0);");
			file.printf("%nSystem.out.println(\"Solving...\");");
			file.printf("%nSystem.out.flush();");
			file.printf("%nSolution sol = solver.solve(%s,bounds);", result);
			file.printf("%nSystem.out.println(sol.toString());");
		} else {
			file.printf(
					"%nMultiObjectiveProblem problem = new MultiObjectiveProblem(bounds, %d, %s, objectives);",
					bitwidth, result);
			file.printf("%nGuidedImprovementAlgorithm gia = new GuidedImprovementAlgorithm(\"asdf\", false);");

			file.printf("%nSolutionNotifier notifier = new SolutionNotifier() {");
			file.printf("%nint solution_count;");
			file.printf("%npublic void tell(final MeasuredSolution s) {");
			file.printf("%ntry {");
			file.printf("%nFileWriter file = new FileWriter(\"kodkod_solutions_\" + solution_count + \".txt\");");
			file.printf("%nPrintWriter print = new PrintWriter(file);");
			file.printf("%nprint.println(s.toString());");
			file.printf("%nsolution_count += 1;");
			file.printf("%nprint.flush();");
			file.printf("%nprint.close();");
			file.printf("%nfile.close();");
			file.printf("%n} catch (Exception e) {}");
			file.printf("%n}");
			file.printf("%npublic void tell(Solution s, MetricPoint values) {");
			file.printf("%ntell(new MeasuredSolution(s, values));");
			file.printf("%n}");
			file.printf("%npublic void done(){};");
			file.printf("%n};");

			file.printf("%nSystem.out.println(\"Solving...\");");
			file.printf("%nSystem.out.flush();");
			file.printf("%ngia.moosolve(problem, notifier, true);");
		}

		file.printf("%n}}%n");
		file.close();
	}
	final List<String> declarations;
	final List<String> statements; 
}
