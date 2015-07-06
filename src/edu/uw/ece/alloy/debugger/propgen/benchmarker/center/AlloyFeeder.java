package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.GeneratedStorage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ProcessIt;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ProcessRemoteMonitor;

public class AlloyFeeder extends GeneratedStorage<AlloyProcessingParam> implements Runnable {

	final static BlockingQueue<ProcessIt> queue = new LinkedBlockingQueue<>();

	final ProcessesManager processesManager;
	ProcessRemoteMonitor monitor = null;

	final static Logger logger = Logger.getLogger(AlloyFeeder.class.getName()+"--"+Thread.currentThread().getName());

	public AlloyFeeder(final ProcessesManager processesManager) {
		super();
		this.processesManager = processesManager;
	}

	public void setMonitor(final ProcessRemoteMonitor monitor){
		if(this.monitor == null)
			this.monitor = monitor;
		else
			throw new RuntimeException("Monitor cannot be changed");
	}

	public void addProcessTask(final ProcessIt p) throws InterruptedException{
		logger.log(Level.INFO, "["+Thread.currentThread().getName()+"]"+" a request is added to be sent:" + p);
		//If the message has to be compressed, it will be compressed next.
		ProcessIt cP = p;
		cP = new ProcessIt(p.param, processesManager);
		try {
			queue.put(cP);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]"+"a new Alloy process message cannot be added to the queue:" + p, e);
			throw e;
		}

		logger.log(Level.INFO, "["+Thread.currentThread().getName()+"]"+"a request is added to be sent and the queue size is:" + queue.size());

	}


	public void clear(){
		super.size = 0;
		queue.clear();
	}

	public void addGeneratedProp(final AlloyProcessingParam item){
		try {
			super.size++;
			this.addProcessTask(item);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]"+"Cannot add a new Alloy procesing param:" + item, e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	public void addProcessTask( AlloyProcessingParam p) throws InterruptedException{
		addProcessTask(new ProcessIt(p,processesManager));
	}

	/*public void addProcessTask( File src, File dest, int priority, String content) throws InterruptedException{
		addProcessTask(new ProcessIt( AlloyProcessingParam(src, dest, priority ,content) ,processesManager));
	}

	public void addProcessTask( File src, File dest) throws InterruptedException{
		addProcessTask(new ProcessIt( new AlloyProcessingParam(src, dest, 1) ,processesManager));
	}*/


	/**
	 * Pick a request from the queue and send it to a process.
	 * @throws InterruptedException
	 */
	private void sendCommand() throws InterruptedException{
		logger.info("["+Thread.currentThread().getName()+"]" + "Queue size is: "+queue.size());
		ProcessIt e;
		try {
			//take a request, if something is in the queue. Otherwise the thread parks here.
			e = queue.take();
		} catch (InterruptedException e1) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]" + "The command queue is interrupted.", e1);
			throw e1;
		}
		logger.info("["+Thread.currentThread().getName()+"]" + "Message is taken "+e);

		//Find a processor to process the request.
		AlloyProcess process = processesManager.getActiveRandomeProcess();

		logger.info("["+Thread.currentThread().getName()+"]" + "got a process "+e);
		try {
			monitor.addMessage(process.address, e.param);
			e.send(process.address);
			logger.info("["+Thread.currentThread().getName()+"]" + "Message sent to "+process.address);
		} /*catch (InterruptedException e1) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "The command cannot be sent.", e1);
			monitor.removeMessage(process.address.getPort(), e.param);
			//Put it back
			queue.put(e);
			throw e1;
		} */catch (Throwable t){
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "The command cannot be sent.", t);
			monitor.removeMessage(process.address, e.param);
			//Put it back
			queue.put(e);
			throw t;
		}

	}

	public void run(){

		if(monitor == null){
			throw new RuntimeException("Monitor has to be set, but it is null");
		}

		int retry = 0;
		final int maxRetry = 1000; 

		while (!Thread.currentThread().isInterrupted()){
			if( retry == maxRetry) throw new RuntimeException("Constantly interrupted.");
			try {
				logger.info("["+Thread.currentThread().getName()+"]" + "Message is going to be sent, queue size is: "+queue.size());
				sendCommand();
				logger.info("["+Thread.currentThread().getName()+"]" + "Message sent now send another message.");
				//reset the retry counter
				retry = 0;
			} catch (Throwable e) {
				logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]" + "Sending a command is failed for the "+retry+"'th time.", e);
				retry++;
			}
		}

	}

	public void cancel() {
		Thread.currentThread().interrupt();
	}


	public String getStatus(){
		return "Number of requests waiting to be fed: "+queue.size();
	}

}
