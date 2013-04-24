package edu.mit.csail.sdg.gen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4.WorkerEngine;
import edu.uw.ece.alloy.util.TestInputs;

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
				try{Thread.sleep(500L);}catch(Exception e){}
			}
			if(WorkerEngine.isBusy()){
				task.updateResult(System.currentTimeMillis(), fileName, -1, -1, -1, -1, -1, false);
				//Wait until write everything, eh?
				try{Thread.sleep(1000L);}catch(Exception e){}
				WorkerEngine.stop();
				break;
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
	private static  int SubMemory = 1024;
	/** The amount of stack (in K) to allocate for Kodkod and the SAT solvers. */
	private static  int SubStack = 8192;
	private static final String fs = System.getProperty("file.separator");

	private static void doTest(String name,int experiments,long timeOutMin, Collection<Object[]> tests) throws InterruptedException, Err{

		String report = "report_"+name+"_"+System.currentTimeMillis()+".txt";
		for(Object[] obj : tests){
			
			if(name.toLowerCase().contains("new")){
				getInstance().executeTask(experiments, 
						  new NewSyntaxExecuterJob(report), 
						 obj[0].toString(), timeOutMin*60L*1000L);
			}else if(name.toLowerCase().contains("new")){
				getInstance().executeTask(experiments, 
						new OldSyntaxExecuterJob(report), 
						 obj[0].toString(), timeOutMin*60L*1000L);
			}else if(name.toLowerCase().contains("walker")){
				getInstance().executeTask(experiments, 
						new WalkerExecuterJob(report), 
						 obj[0].toString(), timeOutMin*60L*1000L);
			}
			
		}

		
	}
	

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		long timeOutMin = 30; 
		int experiments = 1;
		final List<String> fileGroups = new ArrayList<String>();
		int numbers = 5;

		System.out.println(TestInputs.generatorBenchmarkWalker().size());
		
		System.exit(-10);
		
		doTest("Walker",experiments, timeOutMin, TestInputs.generatorBenchmarkWalker());
		
		
/*		doTest("NewWithConstraint1",experiments, timeOutMin, TestInputs.generatorBenchmarkNewWithConstraint1());
		doTest("NewWithConstraint2",experiments, timeOutMin, TestInputs.generatorBenchmarkNewWithConstraint2());
		doTest("NewWithout",experiments, timeOutMin, TestInputs.generatorBenchmarkNewWithoutConstraint());

		
		doTest("NewWithConstraint1_Itr",experiments, timeOutMin, TestInputs.generatorBenchmarkNewWithConstraint1());
		doTest("NewWithConstraint2_Itr",experiments, timeOutMin, TestInputs.generatorBenchmarkNewWithConstraint2());
		doTest("NewWithout_Itr",experiments, timeOutMin, TestInputs.generatorBenchmarkNewWithoutConstraint());

		
/*		doTest("OldWithConstraint1_Itr",experiments, timeOutMin, TestInputs.generatorBenchmarkNormalWithConstraint1());
		doTest("OldWithConstraint2_Itr",experiments, timeOutMin, TestInputs.generatorBenchmarkNormalWithConstraint1());
		doTest("OldWithout_Itr",experiments, timeOutMin, TestInputs.generatorBenchmarkNormal());
	*/	
		System.exit(-10);
		
		
		
		
		
		int suits = 2;
		int players = 2;
		String report = "";
		
		
		
		
		for(int i=0; i<args.length;i++ ){
			String arg = args[i];
			if(arg.equals("-n")){ //new syntax file path
				if(i+1 < args.length) 
					fileGroups.add(args[i+1]);
			}else if(arg.equals("-o")){ //old syntax file path
				if(i+1 < args.length)
					fileGroups.add(args[i+1]);
			}else if(arg.equals("-m")){ // SubMemory size
				if(i+1 < args.length)
					SubMemory = Integer.valueOf(args[i+1]);
			}else if(arg.equals("-s")){ // SubStack Size
				if(i+1 < args.length)
					SubStack = Integer.valueOf(args[i+1]);
			}else if(arg.equals("-nm")){ //numbers
				if(i+1 < args.length)
					numbers = Integer.valueOf(args[i+1]);
			}else if(arg.equals("-st")){ //suits
				if(i+1 < args.length)
					suits = Integer.valueOf(args[i+1]);
			}else if(arg.equals("-x")){ // experiments
				if(i+1 < args.length)
					experiments = Integer.valueOf(args[i+1]);
			}else if(arg.equals("-t")){ //timeout in minute
				if(i+1 < args.length)
					timeOutMin = Integer.valueOf(args[i+1]);
			}else if(arg.equals("-p")){ //Player
				if(i+1 < args.length)
					players = Integer.valueOf(args[i+1]);
			}else if(arg.equals("-f")){ //file name
				if(i+1 < args.length)
					report = args[i+1];
			}
		}
		
		System.out.println(
				"The Configuration is: "+
				", Old and New File templates are: "+fileGroups.toString()+
				", SubMemory: "+SubMemory +
				", SubStack: "+SubStack+
				", #Numbers: "+numbers+
				", #Suits: "+suits+
				", #Playres: "+players+
				", #Experiments: "+experiments+
				", Timeout in minutes: "+ timeOutMin+
				", Report file: "+report
				
				);
		
		
		for(String arg:fileGroups){
			List<String> fileNames = getInstance().makeSmaples(arg, players, numbers, suits, arg.contains("old"));
			for(String fileName:fileNames){
				getInstance().executeTask(experiments, 
						arg.contains("old") ? new OldSyntaxExecuterJob(report) : new NewSyntaxExecuterJob(report), 
								fileName, timeOutMin*60L*1000L);
			}
		}

	}

}
