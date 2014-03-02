package edu.mit.csail.sdg.alloy4whole.multiobjective;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import kodkod.multiobjective.*;
import kodkod.multiobjective.algorithms.*;
import kodkod.multiobjective.MultiObjectiveOptions;

public class MultiObjectiveArguments {
	static final String SingleSolutionPerParetoPointArg = "--SingleSolutionPerParetoPoint";
	static final String SingleSolutionPerParetoPointArg_short = "-s";
	static final String ListOnlyOneSolutionArg = "--ListOnlyOneSoultion";
	static final String LogRunningTimesArg = "--LogRunningTimesArg=";
	static final String MinMaxBoundsForGoals = "--MinMaxBoundsForGoals=";
	static final String LogPrintHeadersArg = "--LogPrintHeaders";
	static final String SymmetryBreakingArg = "--SymmetryBreaking=";
	static final String Algorithm = "--MooAlgorithm=";
	static final String Help = "--help";
	
	Boolean ListAllSolutionsForAParetoPoint = true;

	private Boolean ListOnlyOneSolution = false;
	private Boolean LogRunningTimes = false;
	private Boolean UseMinMaxBoundsForGoals = false;
	private Boolean LogPrintHeaders = false;
	
	private String LogFilename = "";
	private String LogFilenameIndividualCallsStats = "";
	private String filename;
	private String MinMaxBoundsContent = "";
	
	private int SymmetryBreaking = 1000;
	private MultiObjectiveAlgorithm algorithm = new CheckpointedGuidedImprovementAlgorithm("CGIA", new MultiObjectiveOptions());
	
	public static MultiObjectiveArguments parseCommandLineArguments(final String args[]){
		MultiObjectiveArguments  parsedParams =  new MultiObjectiveArguments();

    	for (int i = 0;i < (args.length-1);i++){
    		if  (args[i].equals(MultiObjectiveArguments.SingleSolutionPerParetoPointArg) || args[i].equals(MultiObjectiveArguments.SingleSolutionPerParetoPointArg_short)){
    			parsedParams.ListAllSolutionsForAParetoPoint = false;
    		}else if (args[i].startsWith(MultiObjectiveArguments.SymmetryBreakingArg)) {
    			parsedParams.SymmetryBreaking = Integer.parseInt(args[i].substring(MultiObjectiveArguments.SymmetryBreakingArg.length()));
    		}else if (args[i].startsWith(MultiObjectiveArguments.LogRunningTimesArg)) {
    			parsedParams.LogRunningTimes = true;
    			parsedParams.LogFilename = args[i].substring(MultiObjectiveArguments.LogRunningTimesArg.length());    
    			
    			if (parsedParams.LogFilename.indexOf(".") == -1){
    				parsedParams.LogFilenameIndividualCallsStats = parsedParams.LogFilename +  "_indvidualCallStatistics";
    			} else {
        			parsedParams.LogFilenameIndividualCallsStats =  parsedParams.LogFilename.substring(0, parsedParams.LogFilename.indexOf(".")) + "_indvidualCallStatistics"  ;
        			parsedParams.LogFilenameIndividualCallsStats +=   parsedParams.LogFilename.substring(parsedParams.LogFilename.indexOf("."));
    			}

    		} else if (args[i].equals(MultiObjectiveArguments.ListOnlyOneSolutionArg)){    			
    			parsedParams.ListOnlyOneSolution = true;
    		} else if (args[i].equals(MultiObjectiveArguments.MinMaxBoundsForGoals)){
    			parsedParams.UseMinMaxBoundsForGoals = true;
    			parsedParams.MinMaxBoundsContent =  args[i].substring(MultiObjectiveArguments.MinMaxBoundsForGoals.length());
    		} else if (args[i].equals(MultiObjectiveArguments.LogPrintHeadersArg)){
    			parsedParams.LogPrintHeaders = true;
    		} else if(args[i].startsWith(MultiObjectiveArguments.Algorithm)){
    			String algorithm_name = args[i].substring(MultiObjectiveArguments.Algorithm.length());
    			if(algorithm_name.equals("PGIA")){
    				parsedParams.algorithm = new PartitionedGuidedImprovementAlgorithm("PGIA",new MultiObjectiveOptions());
    			}
    			else if(algorithm_name.equals("IGIA")){
    				parsedParams.algorithm = new IncrementalGuidedImprovementAlgorithm("IGIA",new MultiObjectiveOptions());
    			} else{
    				throw new RuntimeException("Failed to parse command Algorithm argument:"+ algorithm_name);
    			}
    		}else if(args[i].equals(MultiObjectiveArguments.Help)){
    			try{
    				String help = renderHelp();
    				System.out.println(help);
    				System.exit(0);
    			}catch(IOException ex){
    				System.out.println(ex.toString());
    				System.out.println("Could not find file arguments_help.txt");
    			}
    			
    		
    		}
    		
    	}    	
    	parsedParams.filename = args[args.length-1];    
    	
		return parsedParams;
	}
	
	/* Getters */
	public Boolean getListAllSolutionsForAParetoPoint() {
		return ListAllSolutionsForAParetoPoint;
	}

	public Boolean getListOnlyOneSolution() {
		return ListOnlyOneSolution;
	}

	public Boolean getLogRunningTimes() {
		return LogRunningTimes;
	}

	public Boolean getUseMinMaxBoundsForGoals() {
		return UseMinMaxBoundsForGoals;
	}
	
	public String getLogFilename() {
		return LogFilename;
	}

	public String getFilename() {
		return filename;
	}

	public boolean getWriteHeaderLogfile() {
		return this.LogPrintHeaders;
	}

	public String getLogFilenameIndividualStats() {
		return this.LogFilenameIndividualCallsStats;
	}
	
	public int getSymmetryBreaking() {
		return this.SymmetryBreaking;
	}
	
	public MultiObjectiveAlgorithm getAlgorithm(){
		return this.algorithm;
	}
	
	private static String renderHelp() throws IOException{
		    BufferedReader reader = new BufferedReader( new FileReader ("../alloy/arguments_help.txt"));
		    String         line = null;
		    StringBuilder  stringBuilder = new StringBuilder();
		    String         ls = System.getProperty("line.separator");
		
		    while( ( line = reader.readLine() ) != null ) {
		        stringBuilder.append( line );
		        stringBuilder.append( ls );
		    }

		    return stringBuilder.toString();
		}
	
}
