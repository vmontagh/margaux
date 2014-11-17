package edu.mit.csail.sdg.gen;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4.WorkerEngine;

public class BenchmarkRunner {

	/** The amount of memory (in M) to allocate for Kodkod and the SAT solvers. */
	public  static  int SubMemory = 1024;
	/** The amount of stack (in K) to allocate for Kodkod and the SAT solvers. */
	public static  int SubStack = 8192;
	private static final String fs = System.getProperty("file.separator");
	/** This variable caches the result of alloyHome() function call. */
	private  String alloyHome = null;

	protected final static BenchmarkRunner  myself = new BenchmarkRunner();

	protected BenchmarkRunner(){}

	
	public static BenchmarkRunner getInstance(){
		return myself;
	}
	
	public final void executeTask(int times, ExecuterJob task, String fileName, long timeout) throws InterruptedException, Err{
		int newmem = SubMemory, newstack = SubStack;
		MyWorkerCallback mwc = new MyWorkerCallback(fileName);
		//Message passing to the Task
		Util.writeAll("fileName.txt", fileName);
		for(int i=0; i<times;i++){
			long startTime = System.currentTimeMillis();
			try {
				WorkerEngine.run(task, newmem, newstack, alloyHome() + fs + "binary", "", mwc);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			while(WorkerEngine.isBusy() &&  ((System.currentTimeMillis() - startTime) < timeout /*timeOutMin*2L*1000L*/)){
				try{Thread.sleep(500L);}catch(Exception e){}
			}
			if(WorkerEngine.isBusy()){
				task.updateResult(System.currentTimeMillis(), fileName, -1, -1, -1, -1, -1, false, 0, -1);
				//Wait until write everything, eh?
				try{Thread.sleep(1000L);}catch(Exception e){}
				WorkerEngine.stop();
				break;
			}
			// sched.shutdown(false);
			WorkerEngine.stop();
		}

	}
	

	/** Find a temporary directory to store Alloy files; it's guaranteed to be a canonical absolute path. */
	private  synchronized String alloyHome() {
		if (alloyHome!=null) return alloyHome;
		String temp=System.getProperty("java.io.tmpdir");
		if (temp==null || temp.length()==0)
			System.err.println("Error. JVM need to specify a temporary directory using java.io.tmpdir property.");
		String username=System.getProperty("user.name");
		File tempfile=new File(temp+File.separatorChar+"alloy4tmp40-"+(username==null?"":username));
		tempfile.mkdirs();
		String ans=Util.canon(tempfile.getPath());
		if (!tempfile.isDirectory()) {
			System.err.println("Error. Cannot create the temporary directory "+ans);
		}
		if (!Util.onWindows()) {
			String[] args={"chmod", "700", ans};
			try {Runtime.getRuntime().exec(args).waitFor();}
			catch (Throwable ex) {} // We only intend to make a best effort.
		}
		return alloyHome=ans;
	}

	public final void doTest(String name,int experiments,long timeOutMin, Collection<Object[]> tests) throws InterruptedException, Err{
		
		String report = "expr_output/report_"+name+"_"+(new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss-a")).format(new Date( System.currentTimeMillis()))  +".txt";
		for(Object[] obj : tests){
			
			if(name.toLowerCase().contains("new")){
				getInstance().executeTask(experiments, 
						  new NewSyntaxExecuterJob(report), 
						 obj[0].toString(), timeOutMin*60L*1000L);
			}else if(name.toLowerCase().contains("old")){
				getInstance().executeTask(experiments, 
						new OldSyntaxExecuterJob(report), 
						 obj[0].toString(), timeOutMin*60L*1000L);
			}else if(name.toLowerCase().contains("walker")){
				getInstance().executeTask(experiments, 
						new WalkerExecuterJob(report), 
						 obj[0].toString(), timeOutMin*60L*1000L);
			}else if(name.toLowerCase().contains("ee")){
				getInstance().executeTask(experiments, 
						new EEExecuterJob(report), 
						 obj[0].toString(), timeOutMin*60L*1000L);
			}
			
		}
		
	}
	
}
