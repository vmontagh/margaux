package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.GeneratedStorage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ProcessIt;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ProcessRemoteMonitor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;
import edu.uw.ece.alloy.util.RetryingThread;

public class AlloyFeeder extends GeneratedStorage<AlloyProcessingParam> implements Runnable, ThreadToBeMonitored{

	final static Logger logger = Logger.getLogger(AlloyFeeder.class.getName()+"--"+Thread.currentThread().getName());
	
	final BlockingQueue<AlloyProcessingParam> queue;
	//This buffer stores as a second buffer. The content is eventually merged into the main 'queue'.
	//In case of any fault happening and the request is going to be restocked, the request is 
	//added to 'backLog' then merged into 'queue'. The 'backLog' could be either limit or unlimited.
	final BlockingQueue<AlloyProcessingParam> backLog;

	final ProcessesManager processesManager;
	ProcessRemoteMonitor monitor = null;
	
	final Thread sender = new RetryingThread(this,100);
	
	final Thread merger = new RetryingThread( new Runnable(){
		public void run() {
			try {
				merge();
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]" + "The thread is interuppted.", e);
				throw new RuntimeException(e);
			}
		}}, 100);

	

	public AlloyFeeder(final ProcessesManager processesManager, final int bufferSize, final int backLogBufferSize) {
		super();
		this.processesManager = processesManager;

		if(bufferSize <= 0){
			queue = new LinkedBlockingQueue<>();
		}else{
			queue = new LinkedBlockingQueue<>(bufferSize);
		}

		if(backLogBufferSize <= 0){
			backLog = new LinkedBlockingQueue<>();
		}else{
			backLog = new LinkedBlockingQueue<>(backLogBufferSize);
		}
	}

	public void setMonitor(final ProcessRemoteMonitor monitor){
		if(this.monitor == null)
			this.monitor = monitor;
		else
			throw new RuntimeException("Monitor cannot be changed");
	}

	public void addProcessTask(final AlloyProcessingParam p) throws InterruptedException{
		if(Configuration.IsInDeubbungMode) logger.log(Level.INFO, "["+Thread.currentThread().getName()+"]"+" a request is added to be sent:" + p);
		//If the message has to be compressed, it will be compressed next.
		try {
			queue.put(p);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]"+"a new Alloy process message cannot be added to the queue:" + p, e);
			throw e;
		}
		if(Configuration.IsInDeubbungMode) logger.log(Level.INFO, "["+Thread.currentThread().getName()+"]"+"a request is added to be sent and the queue size is:" + queue.size());
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

	public long getSize(){
		return super.size;
	}

	public void addProcessTaskToBacklog(final AlloyProcessingParam p) throws InterruptedException{
		if(Configuration.IsInDeubbungMode) logger.log(Level.INFO, "["+Thread.currentThread().getName()+"]"+" a request is added to be merged:" + p);
		try {
			backLog.put(p);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]"+"a new Alloy process message cannot be added to the backlog:" + p, e);
			throw e;
		}
		if(Configuration.IsInDeubbungMode) logger.log(Level.INFO, "["+Thread.currentThread().getName()+"]"+"a request is added to be merged and the backlog size is:" + backLog.size());
	}


	/**
	 * Pick a request from the queue and send it to a process.
	 * @throws InterruptedException
	 */
	private void sendCommand() throws InterruptedException{
		if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"]" + "Queue size is: "+queue.size());
		AlloyProcessingParam e;
		try {
			if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"]" + "Queue is: "+queue);
			//take a request, if something is in the queue. Otherwise the thread parks here.
			e = queue.take();

			if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"]" + "Queue object: "+e);

		} catch (InterruptedException e1) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]" + "The command queue is interrupted.", e1);
			throw e1;
		}
		if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"]" + "Message is taken "+e);

		//Find a processor to process the request.
		AlloyProcess process = processesManager.getActiveRandomeProcess();

		if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"]" + "got a process "+e);
		try {
			monitor.addMessage(process.address, e);
			(new ProcessIt(e, processesManager)).send(process.address);
			if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"]" + "Message sent to "+process.address);
		}catch (Throwable t){
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "The command cannot be sent.", t);
			monitor.removeMessage(process.address, e);
			//Put it back
			//queue.put(e);
			addProcessTaskToBacklog(e);
			throw t;
		}

	}

	private void merge() throws InterruptedException{
		
		if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"]" + "Backlog Queue size is: "+backLog.size());
		AlloyProcessingParam e;
		try {
			//take a request, if something is in the queue. Otherwise the thread parks here.
			e = backLog.take();
		} catch (InterruptedException e1) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]" + "The backlog queue is interrupted.", e1);
			throw e1;
		}
		if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"]" + "Message is taken "+e+ " and backLog size is:"+backLog.size());
		
		addProcessTask(e);
		
		if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"]" + "The message is added to the main queue "+e+ " and queue size is:"+queue.size() +" and the backlogqueue size is: "+backLog.size());
		
	}
	
	public void startThread(){
		if(monitor == null){
			throw new RuntimeException("Monitor has to be set, but it is null");
		}
		
		sender.start();
		merger.start();
	}

	public void cancelThread() {
		sender.interrupt();
		merger.interrupt();
	}

	public void changePriority(final int newPriority){
		sender.setPriority(newPriority);
		merger.setPriority(newPriority);
	}
	
	
	public String getStatus(){
		return (new StringBuilder())
				.append("New messages added=").append(size)
				.append("\nMessages to be sent=").append(queue.size())
				.append("\nMessages to be merged=").append(backLog.size())
				.toString();
	}

	@Override
	public void run() {
		try {
			sendCommand();
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]" + "The thread is interuppted.", e);
			throw new RuntimeException(e);
		}		
	}

	@Override
	public void actionOnNotStuck() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int triesOnStuck() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void actionOnStuck() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String amIStuck() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long isDelayed() {
		// TODO Auto-generated method stub
		return 0;
	}

}
