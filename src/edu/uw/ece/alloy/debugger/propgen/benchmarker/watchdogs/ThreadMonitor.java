package edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class ThreadMonitor implements Runnable {

	public final int monitorInterval;
	public final int RecoveryAttemtps;

	protected final static Logger logger = Logger.getLogger(ThreadMonitor.class.getName()+"--"+Thread.currentThread().getName());

	List<ThreadToBeMonitored> monitoredThreads = new LinkedList<>();

	final Thread monitor = new Thread(this);
	
	public ThreadMonitor(int monitorInterval, int RecoveryAttemtps ) {
		super();
		this.monitorInterval = monitorInterval;
		this.RecoveryAttemtps = RecoveryAttemtps;

	}

	public void addThreadToBeMonitored(ThreadToBeMonitored thread){
		monitoredThreads.add(thread);
	}

	protected void monitor() {

		while(!Thread.currentThread().isInterrupted()){
			try{
				Thread.sleep(monitorInterval/2);
				for(ThreadToBeMonitored thread: monitoredThreads){
					try{
						final long isd = thread.isDelayed();
						if(isd != 0){
							logger.warning("["+Thread.currentThread().getName()+"]"+ thread.amIStuck());
							thread.actionOnStuck();
						}else{
							thread.actionOnNotStuck();
						}
					}catch(Throwable tr){
						logger.severe("["+Thread.currentThread().getName()+"]"+ "Watchdog main loop is BADLY faced an exception!");					
					}
				}
				System.gc();

				Thread.sleep(monitorInterval/2);
			} catch (InterruptedException e) {
				logger.severe("["+Thread.currentThread().getName()+"]"+ "Watchdog main loop is interrpted.");

			}	
			
			//System.out.println("Monitor is running for "+ AlloyProcessRunner.getInstance().PID);
		}
		//System.out.println("Monitor is broken for "+ AlloyProcessRunner.getInstance().PID);
	}

	public void cancelThreads() {
		monitor.interrupt();
	}
	
	public void startThreads(){
		monitor.start();
	}

	@Override
	public void run() {
		monitor();		
	}


}
