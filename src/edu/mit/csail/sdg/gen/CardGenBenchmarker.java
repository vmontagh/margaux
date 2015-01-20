package edu.mit.csail.sdg.gen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.mit.csail.sdg.alloy4.Util;


public class CardGenBenchmarker {


	final List<String> Numbers = Arrays.asList(new String[]{ "Joker","Two","Three","Four","Five","Six","Seven","Eight","Nine","Ten","Jack","Queen","King","Ace"});
	final List<String> Suits = Arrays.asList(new String[]{"Spades","Clubs","Hearts","Diamonds"});
	final int Players = 2;

	protected static final CardGenBenchmarker myself = new CardGenBenchmarker();
	protected CardGenBenchmarker(){};
	
	public static CardGenBenchmarker getInstance(){ return myself;}
	
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


	

	
	
	//This section is for generating 

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		long timeOutMin = 1000; 
		int experiments = 1;
		final List<String> fileGroups = new ArrayList<String>();
		int numbers = 5;
		
		
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
					BenchmarkRunner.SubMemory = Integer.valueOf(args[i+1]);
			}else if(arg.equals("-s")){ // SubStack Size
				if(i+1 < args.length)
					BenchmarkRunner.SubStack = Integer.valueOf(args[i+1]);
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
				", SubMemory: "+BenchmarkRunner.SubMemory +
				", SubStack: "+BenchmarkRunner.SubStack+
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
				BenchmarkRunner.getInstance().executeTask(experiments, 
						arg.contains("old") ? new OldSyntaxExecuterJob(report) : new NewSyntaxExecuterJob(report), 
								fileName, timeOutMin*60L*1000L);
			}
		}

	}

}
