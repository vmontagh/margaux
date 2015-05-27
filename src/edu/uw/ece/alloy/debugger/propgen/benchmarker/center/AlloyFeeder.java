package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ProcessesManager.AlloyProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ProcessIt;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ProcessRemoteMonitor;

public class AlloyFeeder implements Runnable {

	final static BlockingQueue<ProcessIt> queue = new LinkedBlockingQueue<>();
	
	final ProcessesManager processesManager;
	ProcessRemoteMonitor monitor = null;
	
	
	final static Logger logger = Logger.getLogger(AlloyFeeder.class.getName()+"--"+Thread.currentThread().getName());

	
	
	public AlloyFeeder(final ProcessesManager processesManager) {
		this.processesManager = processesManager;
	}
	
	public void setMonitor(final ProcessRemoteMonitor monitor){
		if(this.monitor == null)
			this.monitor = monitor;
		else
			throw new RuntimeException("Monitor cannot be changed");
	}
	
	public void addProcessTask( ProcessIt p) throws InterruptedException{
		try {
			logger.log(Level.INFO, "["+Thread.currentThread().getName()+"]"+"a request is added to be sent:" + p);
			queue.put(p);
			logger.log(Level.INFO, "["+Thread.currentThread().getName()+"]"+"a request is added to be sent and the queue size is:" + queue.size());
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]"+"a new Alloy process message cannot be added to the queue:" + p, e);
			throw e;
		}
	}

	public void addProcessTask( AlloyProcessingParam p) throws InterruptedException{
		addProcessTask(new ProcessIt(p,processesManager));
	}

	public void addProcessTask( File src, File dest, int priority, String content) throws InterruptedException{
		addProcessTask(new ProcessIt( new AlloyProcessingParam(src, dest, priority ,content) ,processesManager));
	}

	public void addProcessTask( File src, File dest) throws InterruptedException{
		addProcessTask(new ProcessIt( new AlloyProcessingParam(src, dest, 1) ,processesManager));
	}
	
	
	private void sendCommand() throws InterruptedException{
		ProcessIt e;
		try {
			e = queue.take();
		} catch (InterruptedException e1) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]" + "The command queue is interrupted.", e1);
			throw e1;
		}
		logger.info("["+Thread.currentThread().getName()+"]" + "Message is taken "+e);
		
		ProcessesManager.AlloyProcess process = processesManager.getActiveRandomeProcess();
		logger.info("["+Thread.currentThread().getName()+"]" + "got a process "+e);
		try {
			monitor.addMessage(process.address.getPort(), e.param);
			e.sendMe(process.address);
			logger.info("["+Thread.currentThread().getName()+"]" + "Message sent to "+process.address);
		} catch (InterruptedException e1) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "The command cannot be sent.", e1);
			monitor.removeMessage(process.address.getPort(), e.param);
			//Put it back
			queue.put(e);
			throw e1;
		} catch (Throwable t){
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "The command cannot be sent.", t);
			monitor.removeMessage(process.address.getPort(), e.param);
			//Put it back
			queue.put(e);
			throw t;
		}
		
	}
	
	public void run(){
		
		if(monitor == null){
			throw new RuntimeException("Monitor has to be set, but it is null");
		}
		
		int i = 0;
		final int maxInterrupt = 1000;  
		while (!Thread.currentThread().isInterrupted()){
			if( i == maxInterrupt) throw new RuntimeException("Constantly interrupted.");
			try {
				logger.info("["+Thread.currentThread().getName()+"]" + "Message is going to be sent, queue size is: "+queue.size());
				sendCommand();
				
				logger.info("["+Thread.currentThread().getName()+"]" + "Message sent ");
				i = 0;
			} catch (Throwable e) {
				logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]" + "Sending a command is failed.", e);
				i++;
			}
		}

	}
	
	public void cancel() {
		Thread.currentThread().interrupt();
	}

	
	public String getStatus(){
		return "Number of requests waiting to be fed: "+queue.size();
	}
	
	public static void main(String ... args) throws InterruptedException{
		
		AlloyFeeder af = new AlloyFeeder(null);
		af.addProcessTask(new File("1"), new File("2"));
		af.addProcessTask(new File("2"), new File("2"));
		af.addProcessTask(new File("3"), new File("2"));
		
		
		Thread t = new Thread(af);
		t.start();
		
	}
	
}
