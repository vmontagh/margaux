package edu.uw.ece.alloy.debugger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.OurDialog;
import edu.mit.csail.sdg.alloy4.Pos;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;
import edu.mit.csail.sdg.alloy4viz.VizGUI;


public class PropertiesDecomposer {


	
	
	
	
	
    public static void main(String[] args) throws Err, IOException {

    	copyFromJAR();
        final String binary = alloyHome() + fs + "binary";
        //System.out.println(binary);
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
    	
        // The visualizer (We will initialize it to nonnull when we visualize an Alloy solution)
        VizGUI viz = null;

        // Alloy4 sends diagnostic messages and progress reports to the A4Reporter.
        // By default, the A4Reporter ignores all these events (but you can extend the A4Reporter to display the event for the user)
        A4Reporter rep = new A4Reporter() {
            private long lastTime=0;

            // For example, here we choose to display each "warning" by printing it to System.out
            @Override public void warning(ErrorWarning msg) {
                //System.out.println("Relevance Warning:\n"+(msg.toString().trim())+"\n\n");
                //System.out.flush();
            }
            @Override public void solve(final int primaryVars, final int totalVars, final int clauses) {
                //System.out.println("solve->"+totalVars+" vars. "+primaryVars+" primary vars. "+clauses+" clauses. "+(System.currentTimeMillis()-lastTime)+"ms.\n");
                lastTime = System.currentTimeMillis();
                //System.out.flush();

            }
            @Override public void translate(String solver, int bitwidth, int maxseq, int skolemDepth, int symmetry) {
                lastTime = System.currentTimeMillis();
                /*System.out.println("translate->Solver="+solver+" Bitwidth="+bitwidth+" MaxSeq="+maxseq
                + (skolemDepth==0?"":" SkolemDepth="+skolemDepth)
                + " Symmetry="+(symmetry>0 ? (""+symmetry) : "OFF")+'\n');
                System.out.flush();*/

            }
            
        };
        
        
/*        Module world1 = CompUtil.parseEverything_fromFile(rep, null, "models/debugger/tmp-propDeadlock_irreflexiveStateholds.als");
        A4Options options2 = new A4Options();
        options2.solver = A4Options.SatSolver.MiniSatJNI;
        Command command2 = world1.getAllCommands().get(0);
        A4Solution ans2 = TranslateAlloyToKodkod.execute_command(rep, world1.getAllReachableSigs(), command2, options2);
        System.out.println(ans2.satisfiable());
        System.exit(-10);*/
        
        
        String filename = args[0];
        String predName = args[1];
        String cmdName = args[2];
        {

            // Parse+typecheck the model
            //System.out.println("=========== Parsing+Typechecking "+filename+" =============");
            Module world = CompUtil.parseEverything_fromFile(rep, null, filename);

            List<String> fields = new ArrayList<String>();
            
            //what is inside the world? I am looking for fields
            for(Sig sig:world.getAllSigs()){
            	for(Sig.Field field: sig.getFields()){
            		if(field.type().arity() == 2)
            			fields.add(field.label);
            		if(field.type().arity() == 3)
            			fields.add(field.sig.label.replace("this/", "")+"."+field.label);
            	}
            }

            String source = Util.readAll(filename);
            String scope ="";
            for (Command command: world.getAllCommands()) {
            	if(command.label.equals(cmdName)){
            		scope = command.toString().substring(command.toString().indexOf("for"));
            	}
            	//removing the previous commands from the source
            	source = source.replaceAll(exrtactPos( command.pos,filename), "");
            }
            
           
            
            File templateFile = new File("models/debugger/properties_template.als");
            Scanner scnr = new Scanner(templateFile);
            StringBuilder result = new StringBuilder();
            while(scnr.hasNextLine()){
                String prop = scnr.nextLine();
                for(String field: fields){
                	String newCommand = prop.replaceAll("\\?PRED\\?", predName)
                							.replaceAll("\\?FEILD\\?", field)
                							.replaceAll("\\?DOM\\?", "dom["+field+"]")
                							.replaceAll("\\?RAN\\?", "ran["+field+"]");
                	String newFileName = newCommand	.replaceAll("[\\[.\\]]", "")
                									.replaceAll("\\ =>\\ ", "_")
                									.replaceAll("!", "")
                									.replaceAll(",","")
                									.replaceAll("\\.","")
                									.replaceAll("\\ ","");
                	newCommand = "check {"+newCommand+"}"+scope+" expect 0";
                	final String propOutputFile = "models/debugger/tmp-prop"+newFileName+".als";
                	//System.out.println(propOutputFile);
                	Util.writeAll(propOutputFile,"open util/relation\n open general_properties\n"+ source +"\n" + newCommand);
                	Module propWorld = CompUtil.parseEverything_fromFile(rep, null, propOutputFile);
                    A4Options options = new A4Options();
                    options.solver = A4Options.SatSolver.MiniSatJNI;
                    Command command = propWorld.getAllCommands().get(0);
                    A4Solution ans = TranslateAlloyToKodkod.execute_command(rep, propWorld.getAllReachableSigs(), command, options);
                    result.append(prop.substring(prop.indexOf("=> ")+"=> ".length(),prop.indexOf("[?"))).append("\t").append(field).append("\t").append(!ans.satisfiable()).append("\n");
                }
            }
            System.out.println(result);
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
        // Write a few test files
        try {
            (new File(platformBinary)).mkdirs();
            Util.writeAll(platformBinary + fs + "tmp.cnf", "p cnf 3 1\n1 0\n");
        } catch(Err er) {
            // The error will be caught later by the "berkmin" or "spear" test
        }
        // Copy the platform-dependent binaries
        Util.copy(true, false, platformBinary,
           arch+"/libminisat.so", arch+"/libminisatx1.so", arch+"/libminisat.jnilib",
           arch+"/libminisatprover.so", arch+"/libminisatproverx1.so", arch+"/libminisatprover.jnilib",
           arch+"/libzchaff.so", arch+"/libzchaffx1.so", arch+"/libzchaff.jnilib",
           arch+"/berkmin", arch+"/spear");
        Util.copy(false, false, platformBinary,
           arch+"/minisat.dll", arch+"/minisatprover.dll", arch+"/zchaff.dll",
           arch+"/berkmin.exe", arch+"/spear.exe");
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
    
    private static String exrtactPos(Pos pos, String fileName) throws FileNotFoundException{
    	File templateFile = new File(fileName);
        Scanner scnr = new Scanner(templateFile);
        StringBuilder result = new StringBuilder();
        int lineNum = 0;
        while(scnr.hasNextLine()){
            String line = scnr.nextLine();
            lineNum++;
        	if(lineNum == pos.y && lineNum == pos.y2){
        			result.append(line.substring(pos.x-1,pos.x2));
        			break;
        	}else if(lineNum == pos.y){
        		result.append(line.substring(pos.x-1)).append("\n");
        	}else if(lineNum == pos.y2){
        		result.append(line.substring(0,pos.x2));
        	}else if(lineNum > pos.y && lineNum < pos.y2){
        		result.append(line).append("\n");
        	}
        	
        }
        return result.toString();
    
    }
    
    private static String alloyHome = null;
    private static final String fs = System.getProperty("file.separator");

	
	
}
