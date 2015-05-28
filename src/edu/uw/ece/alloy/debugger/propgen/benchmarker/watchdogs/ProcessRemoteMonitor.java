package edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.AlloyFeeder;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ProcessesManager;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ProcessesManager.AlloyProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ProcessesManager.AlloyProcess.Status;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ProcessIt;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteCommand;

public class ProcessRemoteMonitor implements Runnable {


	final static int MaxTimeoutRetry = Integer.valueOf(Configuration.getProp("remote_timeout_retry")); 
	
	final InetSocketAddress hostAddress;

	final AlloyFeeder feeder;
	final ProcessesManager manager;
	public final int monitorInterval;

	//TODO change the key to AlloyProcessingParam from Integer
	final private Map<Integer, Map<AlloyProcessingParam, Integer/*The number of duplications*/>>  incompleteMessages =  new ConcurrentHashMap<Integer, Map<AlloyProcessingParam,Integer>>();
	final private Map<AlloyProcessingParam, List<Integer>>  sentMessages =  new ConcurrentHashMap<>();
	final private Map<AlloyProcessingParam, Integer>  timeoutRetry =  new ConcurrentHashMap<>();
	///Once a message is removed from incompleteMessages, its value is increased.
	final private Map<Integer, AtomicInteger>  receivedMessagesNumber =  new ConcurrentHashMap<>();
	protected final static Logger logger = Logger.getLogger(ProcessRemoteMonitor.class.getName()+"--"+Thread.currentThread().getName());

	public ProcessRemoteMonitor(int monitorInterval, AlloyFeeder feeder, ProcessesManager manager, final InetSocketAddress hostAddress) {
		super();
		this.feeder = feeder;
		this.manager = manager;
		this.monitorInterval = monitorInterval;
		this.hostAddress = hostAddress;
		this.feeder.setMonitor(this);
	}

	public ProcessRemoteMonitor(int monitorInterval, AlloyFeeder feeder, ProcessesManager manager, final int port) {
		this(monitorInterval, feeder, manager, new InetSocketAddress( port));
	}

	public ProcessRemoteMonitor(int monitorInterval, AlloyFeeder feeder, ProcessesManager manager, final String address, final int port) {
		this(monitorInterval, feeder, manager, new InetSocketAddress(address, port));

	}

	public void addMessage(final int pId, AlloyProcessingParam e){
		if( ! incompleteMessages.containsKey(pId) ){
			incompleteMessages.put(pId,new ConcurrentHashMap<>() );
		}
		synchronized (incompleteMessages) {
			Map<AlloyProcessingParam, Integer> mapValue = incompleteMessages.get(pId);

			if(mapValue.containsKey(e)){
				logger.severe("["+Thread.currentThread().getName()+"] "+"Message duplication for "+e+" of process: "+pId);
			}

			logger.info("["+Thread.currentThread().getName()+"] "+"The map size [[[[before]]]] adding is ||"+ mapValue.size() +"|| Message for: "+pId);
			mapValue.put(e, mapValue.containsKey(e) ? (mapValue.get(e).intValue() + 1) : 1  );
			logger.info("["+Thread.currentThread().getName()+"] "+"The map size [[[[after]]]] adding is ||"+ mapValue.size() +"|| Message for: "+pId);

			logger.info("["+Thread.currentThread().getName()+"] "+"Message "+e+" is added and sent to process: "+pId);
			logger.info("["+Thread.currentThread().getName()+"] "+"Unrespoded messages are "+ mapValue.size() +" Message "+e+" is added and sent to process: "+pId);

		}

		if( ! sentMessages.containsKey(e) ){
			sentMessages.put(e,Collections.synchronizedList(new LinkedList()) );
		}

		List<Integer> listPID = sentMessages.get(e);
		listPID.add(pId);



	}

	public void removeMessage(final int pId, AlloyProcessingParam e){
		if( ! incompleteMessages.containsKey(pId) ){
			logger.severe("["+Thread.currentThread().getName()+"] "+"No message set is available for process: "+pId);
		}else{

			logger.info("["+Thread.currentThread().getName()+"] " + "The message is: "+e+"\tThe PID is: "+pId+" and message was sent to: "+sentMessages.get(e));


			synchronized (incompleteMessages) {
				Map<AlloyProcessingParam, Integer> mapValue = incompleteMessages.get(pId);

				logger.info("["+Thread.currentThread().getName()+"] " + " The map size is before: " + mapValue.size()+ " for pId:"+pId);

				if(!mapValue.containsKey(e)){
					logger.info("["+Thread.currentThread().getName()+"] " + mapValue);
					logger.severe("["+Thread.currentThread().getName()+"] "+"Message "+e+" is not found for process: "+pId);
				}else{
					mapValue.remove(e);
					logger.info("["+Thread.currentThread().getName()+"] "+"Message "+e+" is received and removed for process: "+pId);
				}
				logger.info("["+Thread.currentThread().getName()+"] " + " The map size is after: " + mapValue.size()+ " for pId:"+pId);

			}
			logger.info("["+Thread.currentThread().getName()+"] " + " The message is removed? "+incompleteMessages.get(pId).get(e) +"for pId:"+pId+" "+e);
			recordRemovedMessage(pId);
		}
	}

	private void recordRemovedMessage(Integer pId){
		if(! receivedMessagesNumber.containsKey(pId)){
			receivedMessagesNumber.put(pId, new AtomicInteger(1));
		}else{
			receivedMessagesNumber.get(pId).incrementAndGet();
		}
	}

	public final String getStatus(){
		StringBuilder result = new StringBuilder();
		int waiting = 0;
		for(Integer pId: incompleteMessages.keySet()){
			int waitingForPId = incompleteMessages.get(pId).size();
			waiting += waitingForPId;
			result.append("Unresponded Message for PID<").append(pId).append(">=").append(waitingForPId).append("\n");
		}
		int done = 0;
		for(Integer pId: receivedMessagesNumber.keySet()){
			int doneForPID = receivedMessagesNumber.get(pId).intValue();
			done += doneForPID;
			result.append("Responded Message for PID<").append(pId).append(">=").append(doneForPID).append("\n");
		}

		result.append("Total waiting: ").append(waiting);
		result.append("Total done: ").append(done);

		return result.toString();
	}

	private void processCommand(final RemoteCommand command){
		logger.info("["+Thread.currentThread().getName()+"] "+"processCommand Enter:" +command);
		command.killProcess(manager);
		//logger.info("["+Thread.currentThread().getName()+"] "+"processCommand 2:" +command);
		command.updatePorcessorLiveness(manager);
		//logger.info("["+Thread.currentThread().getName()+"] "+"processCommand 3:" +command);
		command.processDone(this, manager);
		//logger.info("["+Thread.currentThread().getName()+"] "+"processCommand 4:" +command);
		command.activateMe(manager);
		logger.info("["+Thread.currentThread().getName()+"] "+"processCommand Exit:" +command);
	}

	public void listening(){

		AsynchronousServerSocketChannel serverSocketChannel = null;
		try {
			serverSocketChannel = AsynchronousServerSocketChannel
					.open().bind(hostAddress);
			logger.log(Level.WARNING, "["+Thread.currentThread().getName()+"] "+"The remote monitor is tarted to monitor the process on: "+hostAddress);
			while(!Thread.currentThread().isInterrupted()){

				Future<AsynchronousSocketChannel> serverFuture = null;
				AsynchronousSocketChannel clientSocket = null;
				ObjectInputStream ois = null;
				InputStream connectionInputStream = null;
				try{
					serverFuture = serverSocketChannel.accept();
					clientSocket = serverFuture.get();

					if ((clientSocket != null) && (clientSocket.isOpen())) {
						connectionInputStream = Channels.newInputStream(clientSocket);
						ois = new ObjectInputStream(connectionInputStream);
						processCommand( (RemoteCommand)ois.readObject() );

					}
				} catch (EOFException e) {
					logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while monitoring: ", e);
				} catch (IOException e) {
					logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while monitoring: ", e);
				} catch (InterruptedException e) {
					logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while monitoring: ", e);
				} catch (ExecutionException e) {
					logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while monitoring: ", e);
				} catch (ClassNotFoundException e) {
					logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while monitoring: ", e);
				}finally{
					if(ois != null)
						try{
							ois.close();
						}catch(IOException e){
							logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while closing InputOutputstream: ", e);
						}
					if(connectionInputStream != null)
						try{
							connectionInputStream.close();
						}catch(IOException e){
							logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while closing Connection Inputputstream: ", e);
						}
					if(clientSocket!=null && clientSocket.isOpen())
						try{
							clientSocket.close();
						}catch(IOException e){
							logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while closing Client socket: ", e);
						}
				}

			}

		} catch (Throwable t  ){
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"A serious error breaks the monitoring loop: ", t);
		} finally{
			if(serverSocketChannel!=null && serverSocketChannel.isOpen())
				try {
					serverSocketChannel.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while closing AsynchronousServerSocketChannel socket: ", e);
				}
		}

	}

	@Override
	public void run() {

		listening();

	}

	public void cancel() {
		Thread.currentThread().interrupt();
	}


	/**
	 * In the case of timeout, the process might be crashed. So the message is resent to the process to be processed again.
	 * If after retrying MaxTimeoutRetry times, still timeout is reported, then something actually happened.
	 * @param pId
	 * @param param
	 */
	public void removeAndPushUndoneRequest(final int pId, final AlloyProcessingParam param) {

		removeMessage(pId, param);
		
		if(!timeoutRetry.containsKey(param)){
			timeoutRetry.put(param, 1);
		}
		
		if(timeoutRetry.get(param) <= MaxTimeoutRetry){
			logger.info("["+Thread.currentThread().getName()+"] " + "The task was timed out on " + pId + " but it will be retried for: " + timeoutRetry.get(param) + " time.");	
			pushUndoneRequest(pId, param);
			timeoutRetry.replace(param, timeoutRetry.get(param)+1);
		}
		

	}


	private void pushUndoneRequest(final int pId, AlloyProcessingParam param) {
		try {
			feeder.addProcessTask(param);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"The request is not queued:"+param+" of pId: "+pId, e);
		}
	}

	private void pushUndoneRequests(final int pId, Iterable<AlloyProcessingParam> itr) {
		for(AlloyProcessingParam param: itr){
			pushUndoneRequest(pId,param);
		}
	}

	/**
	 * Remove from the pId from incompleteMessages and push the params into the feeder 
	 * @param pId
	 */
	public void removeAndPushUndoneRequests(final int pId){
		if(manager.getAlloyProcess(pId) == null){
			logger.log(Level.WARNING, "["+Thread.currentThread().getName()+"] "+"The process is not avaialable: "+pId);
			return;
		}
		logger.log(Level.INFO, "["+Thread.currentThread().getName()+"] "+"Remove process "+manager.getAlloyProcess(pId)+" as pId: "+pId);

		if( manager.getAlloyProcess(pId).isActive() ){
			logger.log(Level.WARNING, "["+Thread.currentThread().getName()+"] "+"The process is still active: "+pId);
		}
		if(! incompleteMessages.containsKey(pId)){
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"The request is not in the map, pId: "+pId);
		}else{
			Map<AlloyProcessingParam,Integer> map = incompleteMessages.get(pId);
			logger.log(Level.INFO, "["+Thread.currentThread().getName()+"] "+"Removing "+map.size()+" undone messages from PID:"+pId);
			synchronized (map) {
				logger.log(Level.INFO, "["+Thread.currentThread().getName()+"] "+"Starting to remove "+map.size()+" messages from PID:"+pId);
				Iterable<AlloyProcessingParam> itr = incompleteMessages.get(pId).keySet();
				logger.log(Level.INFO, "["+Thread.currentThread().getName()+"] "+"Starting to push back "+incompleteMessages.get(pId).keySet().size()+" messages from PID:"+pId);
				pushUndoneRequests(pId, itr );
				logger.log(Level.INFO, "["+Thread.currentThread().getName()+"] "+incompleteMessages.get(pId).keySet().size()+" messages are pushed back from PID:"+pId);
				logger.log(Level.INFO, "["+Thread.currentThread().getName()+"] "+"The process: "+pId+" is going to be removed form the incompleteMessages: "+incompleteMessages.keySet());
				incompleteMessages.remove(pId);			
				logger.log(Level.INFO, "["+Thread.currentThread().getName()+"] "+"The process: "+pId+" is removed form the incompleteMessages: "+incompleteMessages.keySet());
			}
		}
	}

	/*public void removeDoneRequest(final int pId, final AlloyProcessingParam param){
		if( ! incompleteMessages.containsKey(pId) ){
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"The request of PId:"+pId+" is not in the map: "+param);
		}else{
			incompleteMessages.get(pId).remove(param);
		}
	}*/


	public class MonitorTimedoutProcesses implements Runnable{

		@Override
		public void run() {
			while(!Thread.currentThread().isInterrupted()){
				try {
					Thread.currentThread().sleep(monitorInterval);
					for(AlloyProcess ap: manager.getTimedoutProcess(monitorInterval)){
						logger.log(Level.INFO, "["+Thread.currentThread().getName()+"] "+"The processes is timedout and will be killed:"+ap.getPId());
						manager.changeStatus(ap.getPId(), Status.KILLING);
						logger.log(Level.INFO, "["+Thread.currentThread().getName()+"] "+"Removing undone requests:"+ap.getPId());
						removeAndPushUndoneRequests(ap.getPId());
						logger.log(Level.INFO, "["+Thread.currentThread().getName()+"] "+"Requests are removed for the killing process:"+ap.getPId());
						manager.killAndReplaceProcess(ap.getPId());
					}

					for(Integer i: findOrphanProcessTasks(manager)){
						logger.log(Level.INFO, "["+Thread.currentThread().getName()+"] "+"Orphan processes are to be removed from the process: "+i);
						removeAndPushUndoneRequests(i);
						logger.log(Level.INFO, "["+Thread.currentThread().getName()+"] "+"Orphan processes are are removed process: "+i);
					}

				} catch (InterruptedException e) {
					logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"The time-out monitor is interrupted.",e);
				}

			}
		}

		public void cancel() {
			Thread.currentThread().interrupt();
		}
	}

	public synchronized Set<Integer> findOrphanProcessTasks(ProcessesManager manager){
		Set<Integer> result = new HashSet( incompleteMessages.keySet());
		logger.log(Level.INFO, "["+Thread.currentThread().getName()+"] "+"registered incmplemete processes are: "+result);
		logger.log(Level.INFO, "["+Thread.currentThread().getName()+"] "+"Active processes are: "+manager.getLiveProcessIDs());
		result.removeAll(manager.getLiveProcessIDs());
		logger.log(Level.INFO, "["+Thread.currentThread().getName()+"] "+"orphan process are: "+result);
		return Collections.unmodifiableSet(result);
	}

	public static void main(String... args) throws InterruptedException{

		ProcessRemoteMonitor f = new ProcessRemoteMonitor(0,new AlloyFeeder(null),null, new InetSocketAddress(45321));

		//(new Thread(f)).start();

		ProcessIt c = new ProcessIt(new AlloyProcessingParam(new File("."),new File(".."),1),f.manager);

		f.addMessage(1, c.param);

		f.removeMessage(1, new AlloyProcessingParam( c.param ));

	}


}
