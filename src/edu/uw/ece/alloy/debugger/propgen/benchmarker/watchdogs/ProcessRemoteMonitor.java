package edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyFeeder;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessesManager;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessesManager.AlloyProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ProcessIt;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteCommand;

public class ProcessRemoteMonitor implements Runnable {


	final InetSocketAddress hostAddress;

	final AlloyFeeder feeder;
	final ProcessesManager manager;
	public final int monitorInterval;

	//TODO change the key to AlloyProcessingParam from Integer
	final private ConcurrentHashMap<Integer, Map<AlloyProcessingParam, Integer/*The number of duplications*/>>  incompleteMessages =  new ConcurrentHashMap<Integer, Map<AlloyProcessingParam,Integer>>();
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
			incompleteMessages.put(pId, new HashMap<>());
		}

		Map<AlloyProcessingParam, Integer> mapValue = incompleteMessages.get(pId);

		if(mapValue.containsKey(e)){
			logger.severe("["+Thread.currentThread().getName()+"] "+"Message duplication for "+e+" of process: "+pId);
		}

		mapValue.put(e, mapValue.containsKey(e) ? (mapValue.get(e).intValue() + 1) : 1  );
		logger.info("["+Thread.currentThread().getName()+"] "+"Message "+e+" is added and sent to process: "+pId);

	}

	public void removeMessage(final int pId, AlloyProcessingParam e){
		if( ! incompleteMessages.containsKey(pId) ){
			logger.severe("["+Thread.currentThread().getName()+"] "+"No message set is available for process: "+pId);
		}else{
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
	}

	private void processCommand(final RemoteCommand command){
		command.killProcess(manager);
		command.updatePorcessorLiveness(manager);
		command.processDone(this, manager);
		command.activateMe(manager);
	}

	public void listening(){
		try {
			final AsynchronousServerSocketChannel serverSocketChannel = AsynchronousServerSocketChannel
					.open().bind(hostAddress);
			while(!Thread.currentThread().isInterrupted()){

				Future<AsynchronousSocketChannel> serverFuture = serverSocketChannel
						.accept();
				final AsynchronousSocketChannel clientSocket = serverFuture.get();

				if ((clientSocket != null) && (clientSocket.isOpen())) {
					InputStream connectionInputStream = Channels.newInputStream(clientSocket);

					ObjectInputStream ois = null;
					ois = new ObjectInputStream(connectionInputStream);

					processCommand( (RemoteCommand)ois.readObject() );

					ois.close();
					connectionInputStream.close();
					clientSocket.close();
				}
			}

		} catch (IOException e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while monitoring: ", e);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while monitoring: ", e);
		} catch (ExecutionException e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while monitoring: ", e);
		} catch (ClassNotFoundException e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while monitoring: ", e);
		}

	}

	@Override
	public void run() {
		
		listening();

	}

	public void cancel() {
		Thread.currentThread().interrupt();
	}



	private void pushUndoneRequests(final int pId, Iterable<AlloyProcessingParam> itr) {

		for(AlloyProcessingParam param: itr){
			try {
				feeder.addProcessTask(param);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"The request is not queued:"+param+" of pId: "+pId, e);
			}
		}

	}

	/**
	 * Only pushes t...
	 * @param pId
	 */
	public void pushUndoneRequests(final int pId){
		if( manager.getAlloyProcess(pId).isActive() ){
			logger.log(Level.WARNING, "["+Thread.currentThread().getName()+"] "+"The process is still active: "+pId);
		}
		if(! incompleteMessages.containsKey(pId)){
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"The request is not in the map, pId: "+pId);
		}else{
			pushUndoneRequests(pId, incompleteMessages.get(pId).keySet() );
		}
	}

	/**
	 * Remove from the pId from incompleteMessages and push the params into the feeder 
	 * @param pId
	 */
	public void removeAndPushUndoneRequests(final int pId){
		if(manager.getAlloyProcess(pId) == null){
			logger.log(Level.WARNING, "["+Thread.currentThread().getName()+"] "+"The process is not avaialable: "+pId);
		}
		if( manager.getAlloyProcess(pId).isActive() ){
			logger.log(Level.WARNING, "["+Thread.currentThread().getName()+"] "+"The process is still active: "+pId);
		}
		if(! incompleteMessages.containsKey(pId)){
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"The request is not in the map, pId: "+pId);
		}else{
			synchronized (incompleteMessages) {
				Iterable<AlloyProcessingParam> itr = incompleteMessages.get(pId).keySet();
				incompleteMessages.remove(pId);
				pushUndoneRequests(pId, itr );
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
					for(AlloyProcess ap: manager.getTimedProcess(monitorInterval)){
						logger.log(Level.INFO, "["+Thread.currentThread().getName()+"] "+"The processes is timedout and will be killed:",+ap.getPId());
						manager.killAndReplaceProcess(ap.getPId());
						removeAndPushUndoneRequests(ap.getPId());
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
	
	public static void main(String... args) throws InterruptedException{

		ProcessRemoteMonitor f = new ProcessRemoteMonitor(0,new AlloyFeeder(null),null, new InetSocketAddress(45321));

		//(new Thread(f)).start();

		ProcessIt c = new ProcessIt(new AlloyProcessingParam(new File("."),new File(".."),1));
		
		f.addMessage(1, c.param);
		
		f.removeMessage(1, new AlloyProcessingParam( c.param ));

	}




}
