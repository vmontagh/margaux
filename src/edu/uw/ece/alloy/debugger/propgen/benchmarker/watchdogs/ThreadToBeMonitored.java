package edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs;

public interface ThreadToBeMonitored {

	public void startThread();
	public void cancelThread();
	public void changePriority(final int newPriority);
	public void actionOnNotStuck();
	public int triesOnStuck();
	public void actionOnStuck();
	public String amIStuck();
	public long isDelayed();
	public default String getStatus(){return "amIStuck? " + amIStuck();}
}
