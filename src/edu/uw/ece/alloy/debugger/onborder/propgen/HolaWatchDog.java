package edu.uw.ece.alloy.debugger.onborder.propgen;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.util.Utils;

public class HolaWatchDog implements Runnable {

	protected final static Logger logger = Logger.getLogger(HolaWatchDog.class.getName()+"--"+Thread.currentThread().getName());
	
	private final Object lock = new Object();
	
	private volatile boolean stop;
	private Runnable callback;
	private int interval;
	private Thread thread;
		
	/**
	 * Set the wait interval in milliseconds
	 * @param interval
	 */
	public void setInterval(int interval) {
		synchronized (lock) {
			this.interval = interval;
		}
	}
	
	/**
	 * Get the wait interval in milliseconds
	 * @return
	 */
	public int getInterval() {
		int i;
		synchronized (lock) {
			i = this.interval;
		}
		
		return i;
	}
	
	public void startTimer(Runnable callback) {
		this.callback = callback;
		
		this.stop = false;		
		thread = new Thread(this);
		thread.start();
	}
	
	public void stopTimer() {
		
		if(this.thread != null) {
  		this.stop = true;		
  		this.thread.interrupt();
  		this.thread = null;
  		this.callback();
		}
	}
	
	@Override
	public void run() {
		this.watch();
		this.callback();
	}
	
	private void watch() {
		
		int count = 1000;
		while(!stop && count <= interval) {

			try {
				
				Thread.sleep(count);
				count += Math.min(interval - count, 1000);
				
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, Utils.threadName() + "Hola Watchdog loop is interrupted ", e);
				break;
			}
			
		}
		
		this.stop = true;
		
		if(Configuration.IsInDeubbungMode)logger.info(Utils.threadName() + "Watchdog exited.");
	}

	private void callback() {
		this.callback.run();
	}
	
}
