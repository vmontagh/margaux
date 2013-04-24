package edu.mit.csail.sdg.gen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.OurDialog;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4.WorkerEngine.WorkerCallback;
import edu.mit.csail.sdg.alloy4.WorkerEngine.WorkerTask;

public abstract class ExecuterJob implements WorkerTask {
	
	
	protected final  String reportFile;
	protected MyReporter rep = new MyReporter();
	protected static String alloyHome = null;
	protected static final String fs = System.getProperty("file.separator");

	/** Copy the required files from the JAR into a temporary directory. */
	protected static void copyFromJAR() {
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



	protected static synchronized String alloyHome() {
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


	public ExecuterJob(String reportFile) {
		super();
		this.reportFile = reportFile;
	}

	public synchronized void updateResult(long time, String name, long evaluationTime, 
			long executionTime, long traslationTime, long variables, long clauses, boolean sat){
/*		String out = "";
		try {
			
			out = Util.readAll(reportFile);
		} catch (FileNotFoundException e) {}
		catch (IOException e) {}
		try{
			Util.writeAll(reportFile, out+"\n"+time+","+name+","+evaluationTime+
					","+executionTime+","+traslationTime+","+variables+","+clauses+","+sat);
		} catch (Err e) {
			e.printStackTrace();
		}*/
		edu.mit.csail.sdg.gen.Util.Logger(reportFile, String.valueOf(time),  name, String.valueOf( evaluationTime), 
				String.valueOf( executionTime), String.valueOf( traslationTime), String.valueOf( variables), String.valueOf( clauses), String.valueOf( sat));
	}

	
	public void run(WorkerCallback out) throws Exception {
		String fileName = Util.readAll("fileName.txt");
		fileName = fileName.split("\n")[0];
		
		callExecuter(fileName);
	}

	protected abstract void callExecuter(String fileName) throws Err;

	
	
}