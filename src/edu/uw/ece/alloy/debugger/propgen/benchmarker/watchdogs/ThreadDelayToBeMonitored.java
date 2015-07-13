package edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs;

public interface ThreadDelayToBeMonitored {

	public void startThread();
	public void actionOnNotStuck();
	public int triesOnStuck();
	public void actionOnStuck();
	public String amIStuck();
	public long isDelayed();
}
