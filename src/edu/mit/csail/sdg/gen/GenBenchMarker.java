package edu.mit.csail.sdg.gen;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.DateBuilder.*;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.DateBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.OurDialog;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4.WorkerEngine;
import edu.mit.csail.sdg.alloy4.Util.IntPref;

public class GenBenchMarker {



	final List<String> Numbers = Arrays.asList(new String[]{ "Joker","Two","Three","Four","Five","Six","Seven","Eight","Nine","Ten","Jack","Queen","King","Ace"});
	final List<String> Suits = Arrays.asList(new String[]{"Spades","Clubs","Hearts","Diamonds"});
	final int Players = 2;
	protected final static GenBenchMarker  myself = new GenBenchMarker();

	private List<String> makeGT(List<String> numbers){
		List<String> gt = new LinkedList<String>();
		//Return empty list if there is just one number
		if(numbers.size() < 2) 
			return gt;
		for(int i=0; i < numbers.size()-1; i++){
			gt.add(numbers.get(i)+"->"+numbers.get(i+1));
		}
		gt.add(numbers.get(numbers.size()-1)+"->"+numbers.get(0));
		return gt;
	}

	private String makeAlloyList(List<String> list){
		StringBuilder sb = new StringBuilder();
		for(int i=0; i < list.size()-1; i++){
			sb.append(list.get(i)).append("+");
		}
		if(list.size()>0){
			sb.append(list.get(list.size()-1));
		}
		return sb.toString();
	}

	private String makeComplement_NewSyntax(List<String> included, String name, List<String> Sigs){
		Set<String> set = new HashSet<String>(Sigs);
		set.removeAll(included);
		StringBuilder sb = new StringBuilder();
		List<String> tmpList = new ArrayList<String>(set);
		for(int i=0; i< tmpList.size()-1;i++){
			sb.append(" ").append(name).append("!=").append(tmpList.get(i)).append(" and \n");
		}
		if(!set.isEmpty())
			sb.append(" ").append(name).append("!=").append(tmpList.get(tmpList.size()-1));
		return sb.toString();
	}

	private String makeComplementNumber_NewSyntax(List<String> included){
		return makeComplement_NewSyntax(included, "number", Numbers);
	}

	private String makeComplementSuit_NewSyntax(List<String> included){
		return makeComplement_NewSyntax(included, "suit", Suits);
	}


	private String makeComplement_OldSyntax(List<String> included, String name, List<String> Sigs, String Sig){
		Set<String> set = new HashSet<String>(Sigs);
		set.removeAll(included);
		StringBuilder sb = new StringBuilder();
		if(!set.isEmpty())
			sb.append("no ").append("c:").append(Sig).append("| c.").append(name).append(" in (").append(makeAlloyList(new ArrayList<String>(set))).append(")") ;

		return sb.toString();
	}

	private String makeComplementNumber_OldSyntax(List<String> included){
		return makeComplement_OldSyntax(included,"number",Numbers,"Card");
	}

	private String makeComplementSuit_OldSyntax(List<String> included){
		return makeComplement_OldSyntax(included,"suit",Suits,"Card");

	}

	private String makeNewSyntax(String newString,List<String> numbers, List<String> suits, String maxNumber, int players){
		newString=newString.replace("--The Card Constraints", makeComplementNumber_NewSyntax(numbers) + "\n and \n"+makeComplementSuit_NewSyntax(suits));
		newString=newString.replace("--Suit Atoms", "Suit="+makeAlloyList(Suits));
		newString=newString.replace("--Number Atoms", "Number="+makeAlloyList(Numbers));
		newString=newString.replace("--gt Fields", "gt="+makeAlloyList(makeGT(Numbers)));
		newString=newString.replace("--Max Number", maxNumber);
		newString=newString.replace("--Players#", String.valueOf( players));
		return newString;
	}


	private String makeOldSyntax(String newString,List<String> numbers, List<String> suits, String maxNumber, int players, int cards){
		newString=newString.replace("--The Card Constraints", makeComplementNumber_OldSyntax(numbers) + " \n"+makeComplementSuit_OldSyntax(suits));
		newString=newString.replace("--Suit Atoms", "Suit="+makeAlloyList(Suits));
		newString=newString.replace("--Number Atoms", "Number="+makeAlloyList(Numbers));
		newString=newString.replace("--gt Fields", "gt="+makeAlloyList(makeGT(Numbers)));
		newString=newString.replace("--Max Number", maxNumber);
		newString=newString.replace("--Cards#", String.valueOf( cards));
		newString=newString.replace("--Players#", String.valueOf( players));
		return newString;
	}


	public List<String> makeSmaples(String fName,int players,int number, int suit, boolean old) throws Exception{
		if(number > Numbers.size() || suit > Suits.size()) throw new Exception("Out of index parameters");
		List<String> files = new ArrayList<String>();
		String content = Util.readAll(fName);
		for(int i=5 ; i<=number; i++)
			for(int j=1; j <= suit; j++){
				Util.writeAll(fName.replace(".als", "_"+i+"_"+j+".als"),
						!old?
								makeNewSyntax( content,Numbers.subList(Numbers.size()-i, Numbers.size()),
										Suits.subList(Suits.size()-j, Suits.size()),
										Numbers.get(Numbers.size()-1),
										players)
										:
											makeOldSyntax( content,Numbers.subList(Numbers.size()-i, Numbers.size()),
													Suits.subList(Suits.size()-j, Suits.size()),Numbers.get(Numbers.size()-1),
													players,i*j)	
						);
				files.add(fName.replace(".als", "_"+i+"_"+j+".als"));
			}
		return files;	
	}

	public void executeTask(int times, ExecuterJob task, String fileName, long timeout) throws InterruptedException, Err{
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
				Thread.sleep(500L);
			}
			if(WorkerEngine.isBusy()){
				task.updateResult(System.currentTimeMillis(), fileName, -1, -1, -1, -1, -1, false);
			}
			// sched.shutdown(false);
			WorkerEngine.stop();
		}

	}

	protected GenBenchMarker(){}

	public static GenBenchMarker getInstance(){
		return myself;
	}


	/** This variable caches the result of alloyHome() function call. */
	private static String alloyHome = null;

	/** Find a temporary directory to store Alloy files; it's guaranteed to be a canonical absolute path. */
	private static synchronized String alloyHome() {
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

	/** The amount of memory (in M) to allocate for Kodkod and the SAT solvers. */
	private static final int SubMemory = 1024;
	/** The amount of stack (in K) to allocate for Kodkod and the SAT solvers. */
	private static final int SubStack = 8192;
	private static final String fs = System.getProperty("file.separator");


	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		final long timeOutMin = 30; 
		final int experiments = 3;
		final String report = "report"+System.currentTimeMillis()+".txt";
		
		for(String arg:args){
			List<String> fileNames = getInstance().makeSmaples(arg, 2, 5, 2, arg.contains("old"));
			for(String fileName:fileNames){
				getInstance().executeTask(experiments, 
						arg.contains("old") ? new OldSyntaxExecuterJob(report) : new NewSyntaxExecuterJob(report), 
								fileName, timeOutMin*60L*1000L);
			}
		}

	}

}
