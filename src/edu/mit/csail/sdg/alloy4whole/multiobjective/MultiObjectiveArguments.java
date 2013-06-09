package edu.mit.csail.sdg.alloy4whole.multiobjective;

public class MultiObjectiveArguments {
	static final String SingleSolutionPerParetoPointArg = "--SingleSolutionPerParetoPoint";
	static final String SingleSolutionPerParetoPointArg_short = "-s";
	static final String ListOnlyOneSolutionArg = "--ListOnlyOneSoultion";
	static final String LogRunningTimesArg = "--LogRunningTimesArg=";
	static final String MinMaxBoundsForGoals = "--MinMaxBoundsForGoals=";
	static final String LogPrintHeadersArg = "--LogPrintHeaders";
	static final String SymmetryBreakingArg = "--SymmetryBreaking=";
	
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
}