package edu.mit.csail.sdg.alloy4whole.multiobjective;

public class MultiObjectiveArguments {
	static final String SingleSolutionPerParetoPointArg = "--SingleSolutionPerParetoPoint";
	static final String SingleSolutionPerParetoPointArg_short = "-s";
	static final String ListOnlyOneSolutionArg = "--ListOnlyOneSoultion";
	static final String LogRunningTimesArg = "--LogRunningTimesArg=";
	static final String NoAdaptableMinimumImprovement = "--NoAdaptableImprovement";

	Boolean ListAllSolutionsForAParetoPoint = true;


	private Boolean ListOnlyOneSolution = false;
	private Boolean LogRunningTimes = false;
	private Boolean UseAdaptableMinimumImprovement = true;
	private String LogFilename = "";
	private String filename;
	
	public static MultiObjectiveArguments parseCommandLineArguments(final String args[]){
		MultiObjectiveArguments  parsedParams =  new MultiObjectiveArguments();

    	
    	for (int i = 0;i < (args.length-1);i++){
    		if  (args[i].equals(MultiObjectiveArguments.SingleSolutionPerParetoPointArg) || args[i].equals(MultiObjectiveArguments.SingleSolutionPerParetoPointArg_short)){
    			parsedParams.ListAllSolutionsForAParetoPoint = false;
    		}else if (args[i].startsWith(MultiObjectiveArguments.LogRunningTimesArg)) {
    			parsedParams.LogRunningTimes = true;
    			parsedParams.LogFilename = args[i].substring(MultiObjectiveArguments.LogRunningTimesArg.length());    			
    		} else if (args[i].equals(MultiObjectiveArguments.ListOnlyOneSolutionArg)){    			
    			parsedParams.ListOnlyOneSolution = true;    			    			
    		} else if (args[i].equals(MultiObjectiveArguments.NoAdaptableMinimumImprovement)){
    			parsedParams.UseAdaptableMinimumImprovement = false;    			
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

	public Boolean getUseAdaptableMinimumImprovement() {
		return UseAdaptableMinimumImprovement;
	}

	public String getLogFilename() {
		return LogFilename;
	}

	public String getFilename() {
		return filename;
	}	
}