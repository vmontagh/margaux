package edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs;

public interface ThreadToBeMonitored {

	public void startThread();

	public void cancelThread();

	public void changePriority(final int newPriority);

	public void actionOnNotStuck();

	public int triesOnStuck();

	public void actionOnStuck();

	public String amIStuck();

	/**
	 * How many tasks are delayed
	 * @return 0 if there is no delay.
	 */
	// TODO(vajih) rename isDelayed to delayedTasks.
	public long isDelayed();
	
	/**
	 * There is no delay
	 * @return
	 */
	public default boolean noDelay(){
		return isDelayed() <= 0;
	}

	public default String getStatus() {
		return "amIStuck? " + amIStuck();
	}
}
