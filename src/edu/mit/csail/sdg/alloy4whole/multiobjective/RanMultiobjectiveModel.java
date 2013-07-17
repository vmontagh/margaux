/* Alloy Analyzer 4 -- Copyright (c) 2006-2009, Felix Chang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF
 * OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package edu.mit.csail.sdg.alloy4whole.multiobjective;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.OurDialog;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;
import edu.mit.csail.sdg.alloy4viz.VizGUI;

import kodkod.multiobjective.statistics.IndividualStats;
import kodkod.multiobjective.MeasuredSolution;
import kodkod.multiobjective.statistics.StatKey;
import kodkod.multiobjective.statistics.Stats;

/** This class demonstrates how to access Alloy4 via the compiler methods. */

public final class RanMultiobjectiveModel {

	/*
     * Execute every command in every file.
     *
     * This method parses every file, then execute every command.
     *
     * If there are syntax or type errors, it may throw
     * a ErrorSyntax or ErrorType or ErrorAPI or ErrorFatal exception.
     * You should catch them and display them,
     * and they may contain filename/line/column information.
     */
    public static void main(String[] args) throws Err, IOException {
    	copyFromJAR();
        final String binary = alloyHome() + fs + "binary";
        final String jars = alloyHome() + fs + "jars";

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
        if( !loadLibrary("minisat") ) {
            throw new RuntimeException("Failed to load minisat solver library");
        }

        // Add the jars to the System Class Loader.
        // This is somewhat of a hack.
        try {
          URLClassLoader systemClassLoader =
              (URLClassLoader)ClassLoader.getSystemClassLoader();
          Class classLoaderClass = URLClassLoader.class;
          Method addUrlMethod = classLoaderClass.getDeclaredMethod("addURL", new Class[] {URL.class});
          addUrlMethod.setAccessible(true);
          addUrlMethod.invoke(systemClassLoader, new Object[] {
            (new File(jars + fs + "com.microsoft.z3.jar")).toURL()
          });
        } catch (Throwable ex) { }
        
        MultiObjectiveArguments parsedParameters  = MultiObjectiveArguments.parseCommandLineArguments(args);
        /* Finished Extracting Arguments */
        Handler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);

        Logger logger = Logger.getLogger("");
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);

        A4Reporter rep = new A4Reporter() {
            private long lastTime=0;

            // For example, here we choose to display each "warning" by printing it to System.out
            @Override public void warning(ErrorWarning msg) {
                //System.out.println("Relevance Warning:\n"+(msg.toString().trim())+"\n\n");
                //System.out.flush();
            }
            @Override public void solve(final int primaryVars, final int totalVars, final int clauses) {
                //System.out.println("solve->"+totalVars+" vars. "+primaryVars+" primary vars. "+clauses+" clauses. "+(System.currentTimeMillis()-lastTime)+"ms.\n");
                //lastTime = System.currentTimeMillis();
                //System.out.flush();

            }
            @Override public void translate(String solver, int bitwidth, int maxseq, int skolemDepth, int symmetry) {
                //lastTime = System.currentTimeMillis();
                //System.out.println("translate->Solver="+solver+" Bitwidth="+bitwidth+" MaxSeq="+maxseq
                //+ (skolemDepth==0?"":" SkolemDepth="+skolemDepth)
                //+ " Symmetry="+(symmetry>0 ? (""+symmetry) : "OFF")+'\n');
                //System.out.flush();

            }
            
        };
        

    	Module world = CompUtil.parseEverything_fromFile(rep, null, parsedParameters.getFilename());

        // Choose some default options for how you want to execute the commands
        A4Options options = new A4Options();
        options.solver = A4Options.SatSolver.Z3;
        options.MoolloyListAllSolutionsForParetoPoint = parsedParameters.getListAllSolutionsForAParetoPoint();
        options.symmetry = parsedParameters.getSymmetryBreaking();
        
        
        FileWriter fp_logFile = null; 
        FileWriter fp_logFileIndividualCallStats = null;
        if ( parsedParameters.getLogRunningTimes() ){        	
        	fp_logFile  = new FileWriter(parsedParameters.getLogFilename(), true);        	
        	System.out.println("Trying initialize with " + parsedParameters.getLogFilenameIndividualStats());
        	fp_logFileIndividualCallStats = new FileWriter(parsedParameters.getLogFilenameIndividualStats(), true);        	
        }

        
        for (Command command: world.getAllCommands()) {
            // Execute the command
            //System.out.println("============ Command "+command+": ============");
            
            long start_time = System.currentTimeMillis();
            A4Solution ans = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command, options);
            
            

            int solution_number = 1;
            
            
            ans.writeXML("alloy_solutions_" + solution_number  + ".xml");
            
            if (ans.hasNext()) {
            	if (!parsedParameters.getListOnlyOneSolution()){
            		System.out.println("To List all Solutions");
	                A4Solution ans_next = ans.next();
	                while(ans_next.satisfiable()){            		
	            		solution_number++;            		
	            		ans_next.writeXML("alloy_solutions_" + solution_number  + ".xml");
	            		if (!ans_next.hasNext()) {
	            			break;
	            		}
	            		ans_next = ans_next.next();            		
	            	}
            	}
            }

            long end_time = System.currentTimeMillis();
            
            long time_taken = end_time - start_time  ;
            
            String LogLine = parsedParameters.getFilename() + ",";
            LogLine += parsedParameters.getListAllSolutionsForAParetoPoint() == true ? "ListAllSolutionsForAParetoPoint": "ListOneSolutionForAParetoPoint" ;
            LogLine += "," + Integer.toString(parsedParameters.getSymmetryBreaking());
            LogLine += "," + time_taken;
            LogLine += "," + "SummaryStatsNext";

            Stats SummaryStatistics  = TranslateAlloyToKodkod.getStats();
            LogLine += "," + SummaryStatistics.get(StatKey.REGULAR_SAT_CALL);
            LogLine += "," + SummaryStatistics.get(StatKey.REGULAR_UNSAT_CALL) ;

            LogLine += "," + SummaryStatistics.get(StatKey.REGULAR_SAT_TIME);
            LogLine += "," + SummaryStatistics.get(StatKey.REGULAR_UNSAT_TIME);
            
            LogLine += "," + SummaryStatistics.get(StatKey.REGULAR_SAT_TIME_SOLVING);
            LogLine += "," + SummaryStatistics.get(StatKey.REGULAR_UNSAT_TIME_SOLVING); 
            		
            LogLine += "," + SummaryStatistics.get(StatKey.REGULAR_SAT_TIME_TRANSLATION);
            LogLine += "," + SummaryStatistics.get(StatKey.REGULAR_UNSAT_TIME_TRANSLATION); 

            
            LogLine += "," + "GiaCountCallsOnEachMovementToParetoFront";            
            LogLine +=  "," + TranslateAlloyToKodkod.getGIACountCallsOnEachMovementToParetoFront().toString() + "\n";;

            
            String LogHeaderLine = "";

            LogHeaderLine += "Filename";
            LogHeaderLine += "," + "NumberSolutionsToListPerParetoPoint";
            LogHeaderLine += "," + "SymmetryBreaking";
            LogHeaderLine += "," + "Total Time(ms)";
            LogHeaderLine += "," + "SummaryStatsNext";

            LogHeaderLine += "," + "# of Regular Sat Calls";
            LogHeaderLine += "," + "# f Regular Unsat Calls";

            
            LogHeaderLine += "," + "Total Time of Regular Sat Calls";
            LogHeaderLine += "," + "Total Time of Regular Unsat Calls";

            LogHeaderLine += "," + "Total Time Solving Regular Sat Calls";
            LogHeaderLine += "," + "Total Time Solving Regular Unsat Calls"; 
            
            LogHeaderLine += "," + "Total Time Translating Regular Sat Calls";
            LogHeaderLine += "," + "Total Time Translating Regular Unsat Calls"; 

            LogHeaderLine += "," + "GiaCountCallsOnEachMovementToParetoFront" + "\n" ;
            
            String LogIndividualCallsHeaderLine = parsedParameters.getFilename() +  "\n";
            LogIndividualCallsHeaderLine +=  IndividualStats.getHeaderLine()  + "\n";
            		
            
            
            if ( parsedParameters.getLogRunningTimes()  == true){    
            	System.out.println("Writing LogLine General");            	
            	if (parsedParameters.getWriteHeaderLogfile()){            		
                    fp_logFile.write(LogHeaderLine);            		
            	}
                fp_logFile.write(LogLine);            
                fp_logFile.close();

            	System.out.println("Writing Individual Loglines, header is " + LogIndividualCallsHeaderLine);        

            	fp_logFileIndividualCallStats.write(LogIndividualCallsHeaderLine);
            	for (IndividualStats  IndividualCallsStats : SummaryStatistics.getIndividualStats() ){
            		fp_logFileIndividualCallStats.write(IndividualCallsStats + "\n");
            	}
            	
            	fp_logFileIndividualCallStats.close();
            }
            
            
            
            
            
         }
        				    
    }

    private static boolean loadLibrary(String library) {
        try { System.loadLibrary(library);      return true; } catch(UnsatisfiedLinkError ex) { }
        try { System.loadLibrary(library+"x1"); return true; } catch(UnsatisfiedLinkError ex) { }
        try { System.loadLibrary(library+"x2"); return true; } catch(UnsatisfiedLinkError ex) { }
        try { System.loadLibrary(library+"x3"); return true; } catch(UnsatisfiedLinkError ex) { }
        try { System.loadLibrary(library+"x4"); return true; } catch(UnsatisfiedLinkError ex) { }
        try { System.loadLibrary(library+"x5"); return true; } catch(UnsatisfiedLinkError ex) { return false; }
    }
    
    /** Copy the required files from the JAR into a temporary directory. */
    private static void copyFromJAR() {
        // Compute the appropriate platform
        String os = System.getProperty("os.name").toLowerCase(Locale.US).replace(' ','-');
        if (os.startsWith("mac-")) os="mac"; else if (os.startsWith("windows-")) os="windows";
        String arch = System.getProperty("os.arch").toLowerCase(Locale.US).replace(' ','-');
        if (arch.equals("powerpc")) arch="ppc-"+os; else arch=arch.replaceAll("\\Ai[3456]86\\z","x86")+"-"+os;
        if (os.equals("mac")) arch="x86-mac"; // our pre-compiled binaries are all universal binaries
        // Find out the appropriate Alloy directory
        final String platformBinary = alloyHome() + fs + "binary";
        final String jars = alloyHome() + fs + "jars";

        // Write a few test files
        try {
            (new File(platformBinary)).mkdirs();
            Util.writeAll(platformBinary + fs + "tmp.cnf", "p cnf 3 1\n1 0\n");
            (new File(jars)).mkdirs();
            Util.writeAll(jars + fs + "tmp.cnf", "p cnf 3 1\n1 0\n");
        } catch(Err er) {
            // The error will be caught later by the "berkmin" or "spear" test
        }
        // Copy the jars
        Util.copy(true, false, jars, "com.microsoft.z3.jar");
        // Copy the platform-dependent binaries
        Util.copy(true, false, platformBinary,
           arch+"/libminisat.so", arch+"/libminisatx1.so", arch+"/libminisat.jnilib",
           arch+"/libminisatprover.so", arch+"/libminisatproverx1.so", arch+"/libminisatprover.jnilib",
           arch+"/libzchaff.so", arch+"/libzchaffx1.so", arch+"/libzchaff.jnilib",
           arch+"/berkmin", arch+"/spear");
        Util.copy(false, false, platformBinary,
           arch+"/minisat.dll", arch+"/minisatprover.dll", arch+"/zchaff.dll",
           arch+"/berkmin.exe", arch+"/spear.exe",
           arch+"/libz3.so", arch+"/libz3java.so");
        // Copy the model files
        Util.copy(false, true, alloyHome(),
           "models/book/appendixA/addressBook1.als", "models/book/appendixA/addressBook2.als", "models/book/appendixA/barbers.als",
           "models/book/appendixA/closure.als", "models/book/appendixA/distribution.als", "models/book/appendixA/phones.als",
           "models/book/appendixA/prison.als", "models/book/appendixA/properties.als", "models/book/appendixA/ring.als",
           "models/book/appendixA/spanning.als", "models/book/appendixA/tree.als", "models/book/appendixA/tube.als", "models/book/appendixA/undirected.als",
           "models/book/appendixE/hotel.thm", "models/book/appendixE/p300-hotel.als", "models/book/appendixE/p303-hotel.als", "models/book/appendixE/p306-hotel.als",
           "models/book/chapter2/addressBook1a.als", "models/book/chapter2/addressBook1b.als", "models/book/chapter2/addressBook1c.als",
           "models/book/chapter2/addressBook1d.als", "models/book/chapter2/addressBook1e.als", "models/book/chapter2/addressBook1f.als",
           "models/book/chapter2/addressBook1g.als", "models/book/chapter2/addressBook1h.als", "models/book/chapter2/addressBook2a.als",
           "models/book/chapter2/addressBook2b.als", "models/book/chapter2/addressBook2c.als", "models/book/chapter2/addressBook2d.als",
           "models/book/chapter2/addressBook2e.als", "models/book/chapter2/addressBook3a.als", "models/book/chapter2/addressBook3b.als",
           "models/book/chapter2/addressBook3c.als", "models/book/chapter2/addressBook3d.als", "models/book/chapter2/theme.thm",
           "models/book/chapter4/filesystem.als", "models/book/chapter4/grandpa1.als",
           "models/book/chapter4/grandpa2.als", "models/book/chapter4/grandpa3.als", "models/book/chapter4/lights.als",
           "models/book/chapter5/addressBook.als", "models/book/chapter5/lists.als", "models/book/chapter5/sets1.als", "models/book/chapter5/sets2.als",
           "models/book/chapter6/hotel.thm", "models/book/chapter6/hotel1.als", "models/book/chapter6/hotel2.als",
           "models/book/chapter6/hotel3.als", "models/book/chapter6/hotel4.als", "models/book/chapter6/mediaAssets.als",
           "models/book/chapter6/memory/abstractMemory.als", "models/book/chapter6/memory/cacheMemory.als",
           "models/book/chapter6/memory/checkCache.als", "models/book/chapter6/memory/checkFixedSize.als",
           "models/book/chapter6/memory/fixedSizeMemory.als", "models/book/chapter6/memory/fixedSizeMemory_H.als",
           "models/book/chapter6/ringElection.thm", "models/book/chapter6/ringElection1.als", "models/book/chapter6/ringElection2.als",
           "models/examples/algorithms/dijkstra.als", "models/examples/algorithms/dijkstra.thm",
           "models/examples/algorithms/messaging.als", "models/examples/algorithms/messaging.thm",
           "models/examples/algorithms/opt_spantree.als", "models/examples/algorithms/opt_spantree.thm",
           "models/examples/algorithms/peterson.als",
           "models/examples/algorithms/ringlead.als", "models/examples/algorithms/ringlead.thm",
           "models/examples/algorithms/s_ringlead.als",
           "models/examples/algorithms/stable_mutex_ring.als", "models/examples/algorithms/stable_mutex_ring.thm",
           "models/examples/algorithms/stable_orient_ring.als", "models/examples/algorithms/stable_orient_ring.thm",
           "models/examples/algorithms/stable_ringlead.als", "models/examples/algorithms/stable_ringlead.thm",
           "models/examples/case_studies/INSLabel.als", "models/examples/case_studies/chord.als",
           "models/examples/case_studies/chord2.als", "models/examples/case_studies/chordbugmodel.als",
           "models/examples/case_studies/com.als", "models/examples/case_studies/firewire.als", "models/examples/case_studies/firewire.thm",
           "models/examples/case_studies/ins.als", "models/examples/case_studies/iolus.als",
           "models/examples/case_studies/sync.als", "models/examples/case_studies/syncimpl.als",
           "models/examples/puzzles/farmer.als", "models/examples/puzzles/farmer.thm",
           "models/examples/puzzles/handshake.als", "models/examples/puzzles/handshake.thm",
           "models/examples/puzzles/hanoi.als", "models/examples/puzzles/hanoi.thm",
           "models/examples/systems/file_system.als", "models/examples/systems/file_system.thm",
           "models/examples/systems/javatypes_soundness.als",
           "models/examples/systems/lists.als", "models/examples/systems/lists.thm",
           "models/examples/systems/marksweepgc.als", "models/examples/systems/views.als",
           "models/examples/toys/birthday.als", "models/examples/toys/birthday.thm",
           "models/examples/toys/ceilingsAndFloors.als", "models/examples/toys/ceilingsAndFloors.thm",
           "models/examples/toys/genealogy.als", "models/examples/toys/genealogy.thm",
           "models/examples/toys/grandpa.als", "models/examples/toys/grandpa.thm",
           "models/examples/toys/javatypes.als", "models/examples/toys/life.als", "models/examples/toys/life.thm",
           "models/examples/toys/numbering.als", "models/examples/toys/railway.als", "models/examples/toys/railway.thm",
           "models/examples/toys/trivial.als",
           "models/examples/tutorial/farmer.als",
           "models/util/boolean.als", "models/util/graph.als", "models/util/integer.als", "models/util/natural.als",
           "models/util/ordering.als", "models/util/relation.als", "models/util/seqrel.als", "models/util/sequence.als",
           "models/util/sequniv.als", "models/util/ternary.als", "models/util/time.als"
           );
        // Record the locations
        System.setProperty("alloy.theme0", alloyHome() + fs + "models");
        System.setProperty("alloy.home", alloyHome());
    }
    
    private static synchronized String alloyHome() {
        if (alloyHome!=null) return alloyHome;
        String temp=System.getProperty("java.io.tmpdir");
        if (temp==null || temp.length()==0)
            OurDialog.fatal("Error. JVM need to specify a temporary directory using java.io.tmpdir property.");
        String username=System.getProperty("user.name");
        File tempfile=new File(temp+File.separatorChar+"alloy4tmp40-"+(username==null?"":username));
        tempfile.mkdirs();
        String ans=Util.canon(tempfile.getPath());
        if (!tempfile.isDirectory()) {
            OurDialog.fatal("Error. Cannot create the temporary directory "+ans);
        }
        if (!Util.onWindows()) {
            String[] args={"chmod", "700", ans};
            try {Runtime.getRuntime().exec(args).waitFor();}
            catch (Throwable ex) {} // We only intend to make a best effort.
        }
        return alloyHome=ans;
    }
    
    private static String alloyHome = null;
    private static final String fs = System.getProperty("file.separator");	
    
}

