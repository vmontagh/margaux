package edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.util.Collections;
import java.util.HashSet;
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

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ExpressionPropertyGenerator;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.GeneratedStorage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyProcessedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.AlloyFeeder;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.AlloyProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.AlloyProcess.Status;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ExpressionAnalyzerRunner;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ProcessesManager;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteCommand;
import edu.uw.ece.alloy.util.RetryingThread;
import edu.uw.ece.alloy.util.Utils;

public class ProcessRemoteMonitor
		implements Runnable, ThreadDelayToBeMonitored {

	final static int MaxTimeoutRetry = Integer
			.valueOf(Configuration.getProp("remote_timeout_retry"));

	final InetSocketAddress hostAddress;

	final AlloyFeeder feeder;
	final ProcessesManager manager;
	public final int monitorInterval;

	// TODO change the key to AlloyProcessingParam from Integer
	final private Map<InetSocketAddress, Map<AlloyProcessingParam, Integer/*
																																				 * The
																																				 * number
																																				 * of
																																				 * duplications
																																				 */>> incompleteMessages = new ConcurrentHashMap<InetSocketAddress, Map<AlloyProcessingParam, Integer>>();
	final private Map<AlloyProcessingParam, List<InetSocketAddress>> sentMessages = new ConcurrentHashMap<>();
	// The checks is done and no need to recreate it again.
	final private Set<String> doneChecks = Collections
			.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	final private Set<String> toBedoneChecks = Collections
			.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	final private Map<AlloyProcessingParam, Integer> timeoutRetry = new ConcurrentHashMap<>();
	/// Once a message is removed from incompleteMessages, its value is increased.
	final private Map<InetSocketAddress, AtomicInteger> receivedMessagesNumber = new ConcurrentHashMap<>();
	protected final static Logger logger = Logger
			.getLogger(ProcessRemoteMonitor.class.getName() + "--"
					+ Thread.currentThread().getName());

	// For more threads, the can be stored in a list.
	final Thread monitor = new RetryingThread(this, 90);

	final Thread timeoutMonitor = new Thread() {
		@Override
		public void run() {
			monitorTimeouts();
		}
	};

	volatile boolean monitorWorking = false;

	public ProcessRemoteMonitor(int monitorInterval, AlloyFeeder feeder,
			ProcessesManager manager, final InetSocketAddress hostAddress) {
		super();
		this.feeder = feeder;
		this.manager = manager;
		this.monitorInterval = monitorInterval;
		this.hostAddress = hostAddress;
		this.feeder.setMonitor(this);
	}

	public ProcessRemoteMonitor(final int monitorInterval,
			final AlloyFeeder feeder, final ProcessesManager manager,
			final int port) {
		this(monitorInterval, feeder, manager, new InetSocketAddress(port));
	}

	public ProcessRemoteMonitor(final int monitorInterval,
			final AlloyFeeder feeder, final ProcessesManager manager,
			final String address, final int port) {
		this(monitorInterval, feeder, manager,
				new InetSocketAddress(address, port));

	}

	public boolean isDone(AlloyProcessingParam param){
		System.out.println("doneChecks:"+param.alloyCoder.getPredName() + "?" + doneChecks.contains(param.alloyCoder.getPredName()) );
		return (doneChecks.contains(param.alloyCoder.getPredName()));
	}
	
	public boolean isTobeDone(AlloyProcessingParam param){
		System.out.println("isTobeDone:"+param.alloyCoder.getPredName() + "?" + toBedoneChecks.contains(param.alloyCoder.getPredName()) );
		return (toBedoneChecks.contains(param.alloyCoder.getPredName()));
	}
	
	public void addMessage(final InetSocketAddress pId,
			final AlloyProcessingParam e) {

		// TODO message: All message the AlloyProcessingParam objects are compressed
		// already. to read them, they have to be decompressed.
		if (!incompleteMessages.containsKey(pId)) {
			incompleteMessages.put(pId, new ConcurrentHashMap<>());
		}
		synchronized (incompleteMessages) {
			Map<AlloyProcessingParam, Integer> mapValue = incompleteMessages.get(pId);

			if (mapValue.containsKey(e)) {
				logger.severe("[" + Thread.currentThread().getName() + "] "
						+ "Message duplication for " + e + " of process: " + pId);
			}

			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "] "
						+ "The map size [[[[before]]]] adding is ||" + mapValue.size()
						+ "|| Message for: " + pId);
			mapValue.put(e,
					mapValue.containsKey(e) ? (mapValue.get(e).intValue() + 1) : 1);
			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "] "
						+ "The map size [[[[after]]]] adding is ||" + mapValue.size()
						+ "|| Message for: " + pId);

			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "] " + "Message "
						+ e + " is added and sent to process: " + pId);
			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "] "
						+ "Unrespoded messages are " + mapValue.size() + " Message " + e
						+ " is added and sent to process: " + pId);

		}

		if (!sentMessages.containsKey(e)) {
			sentMessages.put(e,
					Collections.synchronizedList(new LinkedList<InetSocketAddress>()));			
		}

		List<InetSocketAddress> listPID = sentMessages.get(e);
		listPID.add(pId);

		toBedoneChecks.add(e.alloyCoder.getPredName());
	}

	public void removeMessage(final InetSocketAddress pId,
			final AlloyProcessingParam e) {

		// Safety checking
		InetSocketAddress bPid = null;
		for (InetSocketAddress pid : incompleteMessages.keySet()) {
			if (incompleteMessages.get(pid).containsKey(e)) {
				bPid = pid;
				break;
			}
		}

		if (bPid == null) {
			logger.warning("Surprisssssseeeee!!!! " + e
					+ " does not belong to any pid and the sent pid is wrong:" + pId);
		}

		if (!incompleteMessages.containsKey(pId)) {
			logger.warning("[" + Thread.currentThread().getName() + "] "
					+ "No message set is available for process: " + pId);
		} else {

				if (Configuration.IsInDeubbungMode)
					logger.info("[" + Thread.currentThread().getName() + "] "
							+ "The message is: " + e + "\tThe PID is: " + pId
							+ " and message was sent to: " + sentMessages.get(e));

			synchronized (incompleteMessages) {
				Map<AlloyProcessingParam, Integer> mapValue = incompleteMessages
						.get(pId);

				if (Configuration.IsInDeubbungMode)
					logger.info("[" + Thread.currentThread().getName() + "] "
							+ " The map size is before: " + mapValue.size() + " for pId:"
							+ pId);

				if (!mapValue.containsKey(e)) {
					logger.warning(
							"[" + Thread.currentThread().getName() + "] " + mapValue);
					logger.warning("[" + Thread.currentThread().getName() + "] "
							+ "Message " + e + " is not found for process: " + pId);
				} else {
					mapValue.remove(e);
					if (Configuration.IsInDeubbungMode)
						logger
								.info("[" + Thread.currentThread().getName() + "] " + "Message "
										+ e + " is received and removed for process: " + pId);

					recordRemovedMessage(pId);
					if (Configuration.IsInDeubbungMode)
						logger.info("[" + Thread.currentThread().getName() + "] "
								+ " The message is removed? "
								+ incompleteMessages.get(pId).get(e) + "for pId:" + pId + " "
								+ e);

				}

				if (!toBedoneChecks.contains(e.alloyCoder.getPredName())){
					if (Configuration.IsInDeubbungMode){
						logger.warning(Utils.threadName() + "The property checking does not exit:"+e.alloyCoder.getPredName());
					}					
				}else{
					toBedoneChecks.remove(e.alloyCoder.getPredName());
				}

				if (!doneChecks.contains(e.alloyCoder.getPredName()) && Configuration.IsInDeubbungMode){
						logger.warning(Utils.threadName() + "The property is already done:"+e.alloyCoder.getPredName());
					}	
				doneChecks.add(e.alloyCoder.getPredName());
				
				if (Configuration.IsInDeubbungMode)
					logger.info("[" + Thread.currentThread().getName() + "] "
							+ " The map size is after: " + mapValue.size() + " for pId:"
							+ pId);

			}
		}
	}

	private void recordRemovedMessage(final InetSocketAddress pId) {
		if (!receivedMessagesNumber.containsKey(pId)) {
			receivedMessagesNumber.put(pId, new AtomicInteger(1));
		} else {
			receivedMessagesNumber.get(pId).incrementAndGet();
		}
	}
	
	protected int getAllWaitings(){
		int waiting = 0;
		for (InetSocketAddress pId : incompleteMessages.keySet()) {
			int waitingForPId = incompleteMessages.get(pId).size();
			waiting += waitingForPId;
		}
		
		return waiting;
	}

	public final String getStatus() {
		StringBuilder result = new StringBuilder();
		int waiting = 0;
		for (InetSocketAddress pId : incompleteMessages.keySet()) {
			int waitingForPId = incompleteMessages.get(pId).size();
			waiting += waitingForPId;
			result.append("Unresponded Message for PID<").append(pId).append(">=")
					.append(waitingForPId).append("\n");
		}
		int done = 0;
		for (InetSocketAddress pId : receivedMessagesNumber.keySet()) {
			int doneForPID = receivedMessagesNumber.get(pId).intValue();
			done += doneForPID;
			result.append("Responded Message for PID<").append(pId).append(">=")
					.append(doneForPID).append("\n");
		}

		result.append("Total waiting: ").append(waiting);
		result.append("\tTotal done: ").append(done);
		result.append("\nMonitor is working? ").append(monitorWorking);
		result.append("\n").append(manager.getStatus());

		return result.toString();
	}

	private void processCommand(final RemoteCommand command) {
		if (Configuration.IsInDeubbungMode)
			logger.info("[" + Thread.currentThread().getName() + "] "
					+ "processCommand Enter:" + command);
		command.killProcess(manager);
		// logger.info("["+Thread.currentThread().getName()+"] "+"processCommand 2:"
		// +command);
		command.updatePorcessorLiveness(manager);
		// logger.info("["+Thread.currentThread().getName()+"] "+"processCommand 3:"
		// +command);
		command.processDone(this);
		// logger.info("["+Thread.currentThread().getName()+"] "+"processCommand 4:"
		// +command);
		command.activateMe(manager);
		if (Configuration.IsInDeubbungMode)
			logger.info("[" + Thread.currentThread().getName() + "] "
					+ "processCommand Exit:" + command);
	}

	public void listening() {
		AsynchronousServerSocketChannel serverSocketChannel = null;
		try {

			System.out.println("listening Host Address->" + hostAddress);

			serverSocketChannel = AsynchronousServerSocketChannel.open()
					.bind(hostAddress);
			logger.log(Level.WARNING,
					"[" + Thread.currentThread().getName() + "] "
							+ "The remote monitor is started to monitor the process on: "
							+ hostAddress);
			while (!Thread.currentThread().isInterrupted()) {
				monitorWorking = true;
				Future<AsynchronousSocketChannel> serverFuture = null;
				AsynchronousSocketChannel clientSocket = null;
				ObjectInputStream ois = null;
				InputStream connectionInputStream = null;
				try {
					serverFuture = serverSocketChannel.accept();
					clientSocket = serverFuture.get();

					if ((clientSocket != null) && (clientSocket.isOpen())) {
						connectionInputStream = Channels.newInputStream(clientSocket);
						ois = new ObjectInputStream(connectionInputStream);
						processCommand((RemoteCommand) ois.readObject());

					}
				} catch (BindException be) {
					logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
							+ "Error while monitoring: ", be);
				} catch (EOFException e) {
					logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
							+ "Error while monitoring: ", e);
				} catch (IOException e) {
					logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
							+ "Error while monitoring: ", e);
				} catch (InterruptedException e) {
					logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
							+ "Error while monitoring: ", e);
				} catch (ExecutionException e) {
					logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
							+ "Error while monitoring: ", e);
				} catch (ClassNotFoundException e) {
					logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
							+ "Error while monitoring: ", e);
				} finally {
					if (ois != null)
						try {
							ois.close();
						} catch (IOException e) {
							logger.log(Level.SEVERE, "[" + Thread.currentThread().getName()
									+ "] " + "Error while closing InputOutputstream: ", e);
						}
					if (connectionInputStream != null)
						try {
							connectionInputStream.close();
						} catch (IOException e) {
							logger
									.log(Level.SEVERE,
											"[" + Thread.currentThread().getName() + "] "
													+ "Error while closing Connection Inputputstream: ",
											e);
						}
					if (clientSocket != null && clientSocket.isOpen())
						try {
							clientSocket.close();
						} catch (IOException e) {
							logger.log(Level.SEVERE, "[" + Thread.currentThread().getName()
									+ "] " + "Error while closing Client socket: ", e);
						}
				}
			}

		} catch (Throwable t) {
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
					+ "A serious error breaks the monitoring loop: ", t);
			monitorWorking = false;
			throw new RuntimeException(t);
		} finally {
			if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
				try {
					serverSocketChannel.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE,
							"[" + Thread.currentThread().getName() + "] "
									+ "Error while closing AsynchronousServerSocketChannel socket: ",
							e);
				}
			}
		}
		// throw breakerLoopReason;
	}

	/**
	 * In the case of timeout, the process might be crashed. So the message is
	 * resent to the process to be processed again. If after retrying
	 * MaxTimeoutRetry times, still timeout is reported, then something actually
	 * happened.
	 * 
	 * @param pId
	 * @param param
	 */
	public void removeAndPushUndoneRequest(final InetSocketAddress pId,
			final AlloyProcessingParam param) {

		removeMessage(pId, param);

		if (!timeoutRetry.containsKey(param)) {
			timeoutRetry.put(param, 1);
		}

		if (timeoutRetry.get(param) <= MaxTimeoutRetry) {
			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "] "
						+ "The task was timed out on " + pId
						+ " but it will be retried for: " + timeoutRetry.get(param)
						+ " time.");
			pushUndoneRequest(pId, param);
			timeoutRetry.replace(param, timeoutRetry.get(param) + 1);
		}
	}

	private void pushUndoneRequest(final InetSocketAddress pId,
			AlloyProcessingParam param) {
		try {
			feeder.addProcessTaskToBacklog(param);
		} catch (InterruptedException e) {
			logger
					.log(Level.SEVERE,
							"[" + Thread.currentThread().getName() + "] "
									+ "The request is not queued:" + param + " of pId: " + pId,
							e);
		}
	}

	private void pushUndoneRequests(final InetSocketAddress pId,
			Iterable<AlloyProcessingParam> itr) {
		for (AlloyProcessingParam param : itr) {
			pushUndoneRequest(pId, param);
		}
	}

	public boolean isAllProcessesNotWorking() {
		return manager.allProcessesNotWorking();
	}

	/**
	 * Remove from the pId from incompleteMessages and push the params into the
	 * feeder
	 * 
	 * @param pId
	 */
	public void removeAndPushUndoneRequests(final InetSocketAddress pId) {
		if (manager.getAlloyProcess(pId) == null) {
			logger.log(Level.WARNING, "[" + Thread.currentThread().getName() + "] "
					+ "The process is not avaialable: " + pId);
			return;
		}
		if (Configuration.IsInDeubbungMode)
			logger.log(Level.INFO,
					"[" + Thread.currentThread().getName() + "] " + "Remove process "
							+ manager.getAlloyProcess(pId) + " as pId: " + pId);

		if (manager.getAlloyProcess(pId).isActive()) {
			logger.log(Level.WARNING, "[" + Thread.currentThread().getName() + "] "
					+ "The process is still active: " + pId);
		}
		if (!incompleteMessages.containsKey(pId)) {
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
					+ "The request is not in the map, pId: " + pId);
		} else {
			Map<AlloyProcessingParam, Integer> map = incompleteMessages.get(pId);
			if (Configuration.IsInDeubbungMode)
				logger.log(Level.INFO, "[" + Thread.currentThread().getName() + "] "
						+ "Removing " + map.size() + " undone messages from PID:" + pId);
			synchronized (map) {
				if (Configuration.IsInDeubbungMode)
					logger.log(Level.INFO,
							"[" + Thread.currentThread().getName() + "] "
									+ "Starting to remove " + map.size() + " messages from PID:"
									+ pId);
				Iterable<AlloyProcessingParam> itr = incompleteMessages.get(pId)
						.keySet();
				if (Configuration.IsInDeubbungMode)
					logger.log(Level.INFO,
							"[" + Thread.currentThread().getName() + "] "
									+ "Starting to push back "
									+ incompleteMessages.get(pId).keySet().size()
									+ " messages from PID:" + pId);
				pushUndoneRequests(pId, itr);
				if (Configuration.IsInDeubbungMode)
					logger.log(Level.INFO,
							"[" + Thread.currentThread().getName() + "] "
									+ incompleteMessages.get(pId).keySet().size()
									+ " messages are pushed back from PID:" + pId);
				if (Configuration.IsInDeubbungMode)
					logger.log(Level.INFO,
							"[" + Thread.currentThread().getName() + "] " + "The process: "
									+ pId
									+ " is going to be removed form the incompleteMessages: "
									+ incompleteMessages.keySet());
				incompleteMessages.remove(pId);
				if (Configuration.IsInDeubbungMode)
					logger.log(Level.INFO,
							"[" + Thread.currentThread().getName() + "] " + "The process: "
									+ pId + " is removed form the incompleteMessages: "
									+ incompleteMessages.keySet());
			}
		}
	}

	/*
	 * public void removeDoneRequest(final int pId, final AlloyProcessingParam
	 * param){ if( ! incompleteMessages.containsKey(pId) ){
	 * logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+
	 * "The request of PId:"+pId+" is not in the map: "+param); }else{
	 * incompleteMessages.get(pId).remove(param); } }
	 */

	protected void monitorTimeouts() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.currentThread().sleep(monitorInterval);
				for (AlloyProcess ap : manager.getTimedoutProcess(monitorInterval)) {
					logger.log(Level.WARNING,
							"[" + Thread.currentThread().getName() + "] "
									+ "The processes is timedout and will be killed:"
									+ ap.getPId());
					manager.changeStatus(ap.getPId(), Status.KILLING);
					logger.log(Level.WARNING, "[" + Thread.currentThread().getName()
							+ "] " + "Removing undone requests:" + ap.getPId());
					removeAndPushUndoneRequests(ap.getPId());
					logger.log(Level.WARNING,
							"[" + Thread.currentThread().getName() + "] "
									+ "Requests are removed for the killing process:"
									+ ap.getPId());
					manager.killAndReplaceProcess(ap.getPId());
				}

				for (InetSocketAddress pId : findOrphanProcessTasks(manager)) {
					logger.log(Level.WARNING,
							"[" + Thread.currentThread().getName() + "] "
									+ "Orphan processes are to be removed from the process: "
									+ pId);
					removeAndPushUndoneRequests(pId);
					logger.log(Level.WARNING, "[" + Thread.currentThread().getName()
							+ "] " + "Orphan processes are are removed process: " + pId);
				}

			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
						+ "The time-out monitor is interrupted.", e);
			}

		}
	}

	public synchronized Set<InetSocketAddress> findOrphanProcessTasks(
			ProcessesManager manager) {
		Set<InetSocketAddress> result = new HashSet<InetSocketAddress>(
				incompleteMessages.keySet());
		if (Configuration.IsInDeubbungMode)
			logger.log(Level.INFO, "[" + Thread.currentThread().getName() + "] "
					+ "registered incmplemete processes are: " + result);
		if (Configuration.IsInDeubbungMode)
			logger.log(Level.INFO, "[" + Thread.currentThread().getName() + "] "
					+ "Active processes are: " + manager.getLiveProcessIDs());
		result.removeAll(manager.getLiveProcessIDs());
		if (Configuration.IsInDeubbungMode)
			logger.log(Level.INFO, "[" + Thread.currentThread().getName() + "] "
					+ "orphan process are: " + result);
		return Collections.unmodifiableSet(result);
	}

	/**
	 * This method is called by AlloyProcessed, once a message is received from
	 * the process as a task is done.
	 * 
	 * @param result
	 */
	public void processResponded(AlloyProcessedResult result,
			InetSocketAddress PID) {

		if (manager.getAlloyProcess(PID) == null) {
			logger.severe("[" + Thread.currentThread().getName() + "] "
					+ " No Such a PID found: " + PID + " and the current PIDs are:\n\t"
					+ manager.getAllRegisteredPIDs());
			return;
		}

		// regardless of the message status, it should not be created again.
		if (doneChecks.contains(result.params.alloyCoder.getPredName())) {
			if (Configuration.IsInDeubbungMode)
				logger.warning("[" + Thread.currentThread().getName() + "] "
						+ "The check was done before: pID= " + PID + " param="
						+ result.params);
		}
		doneChecks.add(result.params.alloyCoder.getPredName());

		if (result.isTimedout() || result.isFailed()) {
			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "] "
						+ "The process is timed out and is pushed back to be retried later: pID= "
						+ PID + " param=" + result.params);
			removeAndPushUndoneRequest(PID, result.params);
			manager.decreaseMessageCounter(PID);
		} else if (result.isInferred()) {
			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "] "
						+ "The process is inferred: pID= " + PID + " param="
						+ result.params);
			manager.increaseInferredMessageCounter(PID);
		} else {
			removeMessage(PID, result.params);
			manager.decreaseMessageCounter(PID);
		}

		manager.changeStatus(PID, Status.WORKING);
		manager.changeLastLiveTimeReported(PID, System.currentTimeMillis());
		// Add the next level to be processed.

	}

	/**
	 * The property is checked, and its result is sat or not. So, the implied
	 * properties should be processed next.
	 * 
	 * @param result
	 */
	public void checkNextProperties(AlloyProcessedResult result) {
		try {

			Set<String> nextProperties = new HashSet<>(
					result.params.alloyCoder.getToBeCheckedProperties(result.sat));

			if (!nextProperties.isEmpty())
				ExpressionPropertyGenerator.Builder.getInstance()
						.create((GeneratedStorage<AlloyProcessingParam>) feeder,
								nextProperties, doneChecks)
						.startThread();
			else
				logger.log(Level.INFO, "[" + Thread.currentThread().getName() + "] "
						+ "The next properties are empty for:" + result);
		} catch (Err | IOException e) {
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
					+ "Next properties failed to be added.", e);
			e.printStackTrace();
		}
	}

	public void startThread() {
		monitor.start();
		timeoutMonitor.start();
	}

	public void cancelThread() {
		monitor.interrupt();
		timeoutMonitor.interrupt();
	}

	public void changePriority(final int newPriority) {
		timeoutMonitor.setPriority(newPriority);
		timeoutMonitor.setPriority(newPriority);
	}

	@Override
	public void run() {
		listening();
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
