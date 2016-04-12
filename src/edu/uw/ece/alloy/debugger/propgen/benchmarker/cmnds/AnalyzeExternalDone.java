package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

public class AnalyzeExternalDone extends RemoteCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8426603406144315894L;
	
	public void patternExtractionDone(Object lock) {
		System.out.println("Pattern is done->"+lock);
		synchronized (lock) {
			lock.notify();
		}
	}

	@Override
	public String toString() {
		return "AnalyzeExternalDone []";
	}
	
}
