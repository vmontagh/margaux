package edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.mchange.v1.util.SimpleMapEntry;

import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.ProcessedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcessLogger;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.communication.Publisher;
import edu.uw.ece.alloy.util.Utils;

public class RemoteProcessMonitor implements ThreadToBeMonitored, Runnable {

	protected final static Logger logger = Logger
			.getLogger(RemoteProcessMonitor.class.getName() + "--" + Thread.currentThread().getName());
	final static int MAX_TIMEOUT_RETRY = Integer.valueOf(Configuration.getProp("remote_timeout_retry"));
	final static int RemoteMonitorInterval = Integer.parseInt(Configuration.getProp("remote_monitor_interval"));

	protected final int maxTimeoutRetry;

	protected final Publisher<ProcessingParam> feeder;
	protected final Publisher<ProcessingParam> backLogQueueFeeder;
	// protected final AlloyFeeder feeder;
	protected final RemoteProcessLogger processLogger;
	protected final int monitorInterval;

	/*
	 * A map from each each remote processor to the messages that sent, the sent
	 * time, and number of tries
	 */
	protected final Map<RemoteProcess, Map<ProcessingParam, Map.Entry<Long, Integer>/*
	 * The
	 * number
	 * of
	 * duplications
	 */>> incompleteMessages = new ConcurrentHashMap<>();
	/*
	 * Once a processing param is sent in a session, the param will be added to
	 * the session. Once the message is responded, it will be removed from the
	 * set. A session is not start or registered, if
	 * !unresponsededParamInSession.containsKey(sessionId) A session is ended,
	 * if unresponsededParamInSession.get(sessionId).isEmpty() A session is
	 * under process, if !unresponsededParamInSession.get(sessionId).isEmpty()
	 */
	protected final Map<UUID, Set<ProcessingParam>> unresponsededParamInSession = new ConcurrentHashMap<>();

	/*
	 * [FOR DEBUGGING] Map from a message to a list of remote processors that
	 * have already be sent
	 */
	protected final Map<ProcessingParam, List<RemoteProcess>> sentMessages = new ConcurrentHashMap<>();
	/* How many times a processing param is retried */
	protected final Map<ProcessingParam, Integer> timeoutRetry = new ConcurrentHashMap<>();
	/*
	 * How many messages are received from a remote processor. Once a message is
	 * removed from incompleteMessages, its value is increased.
	 */
	protected final Map<RemoteProcess, AtomicInteger> receivedMessagesNumber = new ConcurrentHashMap<>();

	// The checks is done and no need to recreate it again.
	// private final Set<String> doneChecks = Collections
	// .newSetFromMap(new ConcurrentHashMap<String, Boolean>());

	/* A thread finds out what messages are timed out */
	protected final Thread timeoutMonitor;

	protected volatile boolean monitorWorking = false;

	public RemoteProcessMonitor(int monitorInterval, Publisher<ProcessingParam> feeder,
			Publisher<ProcessingParam> backLogQueueFeeder, RemoteProcessLogger processLogger, int maxTimeoutRetry) {
		super();
		this.feeder = feeder;
		this.backLogQueueFeeder = backLogQueueFeeder;
		this.processLogger = processLogger;
		this.monitorInterval = monitorInterval;
		this.maxTimeoutRetry = maxTimeoutRetry;

		this.timeoutMonitor = new Thread(this);
	}

	public RemoteProcessMonitor(Publisher<ProcessingParam> feeder, Publisher<ProcessingParam> backLogQueueFeeder,
			RemoteProcessLogger processLogger) {
		this(RemoteMonitorInterval, feeder, backLogQueueFeeder, processLogger, MAX_TIMEOUT_RETRY);
	}

	public void addMessage(final RemoteProcess process, final ProcessingParam param) {
		// TODO message: All message the AlloyProcessingParam objects are
		// compressed
		// already. to read them, they have to be decompressed.
		if (!incompleteMessages.containsKey(process)) {
			incompleteMessages.put(process, new ConcurrentHashMap<>());
		}

		if (!unresponsededParamInSession.containsKey(param.getAnalyzingSessionID()
				.orElseThrow(() -> new RuntimeException("The session Id could not be null")))) {
			unresponsededParamInSession.put(
					param.getAnalyzingSessionID()
					.orElseThrow(() -> new RuntimeException("The session Id could not be null")),
					Collections.newSetFromMap(new ConcurrentHashMap<ProcessingParam, Boolean>()));
		}

		synchronized (incompleteMessages) {
			Map<ProcessingParam, Map.Entry<Long, Integer>> mapValue = incompleteMessages.get(process);
			if (mapValue.containsKey(param)) {
				logger.severe(Utils.threadName() + "Message duplication for " + param + " of process: " + process);
			}

			Map.Entry<Long, Integer> times = new SimpleMapEntry(System.currentTimeMillis(), 1);

			if (mapValue.containsKey(param)) {
				times.setValue(mapValue.get(param).getValue() + 1);
			}

			mapValue.put(param, times);
			if (Boolean.parseBoolean(System.getProperty("debug"))) {
				if (!sentMessages.containsKey(param)) {
					sentMessages.put(param, Collections.synchronizedList(new LinkedList<RemoteProcess>()));
				}
				sentMessages.get(param).add(process);
			}
			synchronized (unresponsededParamInSession) {
				unresponsededParamInSession.get(param.getAnalyzingSessionID().get()).add(param);
				//System.out.println("added->" + param.hashCode());
			}
		}
	}
	
	protected void removeMessageFromProcess(final RemoteProcess process, final ProcessingParam param){
		RemoteProcess bProcess = null;
		for (RemoteProcess rp : incompleteMessages.keySet()) {
			if (incompleteMessages.get(rp).containsKey(param)) {
				bProcess = rp;
				break;
			}
		}

		if (bProcess == null) {
			logger.warning("Surprisssssseeeee!!!! " + param + " does not belong to any pid and the sent pid is wrong:"
					+ process);
		}

		if (!incompleteMessages.containsKey(process)) {
			logger.warning(Utils.threadName() + "No message set is available for process: " + process);
		} else {
			//System.out.println("to be removed2->" + ((AlloyProcessingParam) param).getAlloyCoder().get().srcName());
			synchronized (incompleteMessages) {
				Map<ProcessingParam, Map.Entry<Long, Integer>> mapValue = incompleteMessages.get(process);
				if (!mapValue.containsKey(param)) {
					logger.warning(Utils.threadName() + "Message " + param + " is not found for process: " + process);
				} else {
					mapValue.remove(param);
					recordRemovedMessage(process);
				}
			}
		}		
	}
	
	protected void removeMessageFromSession(final ProcessingParam param){
		synchronized (unresponsededParamInSession) {
			if (!unresponsededParamInSession.get(param.getAnalyzingSessionID().get()).remove(param)) {
				logger.warning(Utils.threadName() + "The param: " + param
						+ " has not been recored under this session: " + param.getAnalyzingSessionID());
			}
			//System.out.println("removed->" + ((AlloyProcessingParam) param).getAlloyCoder().get().srcName());
		}
	}

	public final String getStatusOnSessions() {
		StringBuilder result = new StringBuilder();

		int doneSessions = 0;
		List<Integer> progressingSessions = new ArrayList<>();

		for (UUID sessionId : unresponsededParamInSession.keySet()) {
			if (sessionIsDone(sessionId)) {
				++doneSessions;
			} else {
				if (unresponsededParamInSession.get(sessionId).size() < 5) {
					System.out
					.println(
							"The sessions is->"
									+ unresponsededParamInSession.get(sessionId).stream()
									.map(p -> p.hashCode() + " " + ((AlloyProcessingParam) p)
											.getAlloyCoder().get().srcName())
									.collect(Collectors.toSet()));
				}
				progressingSessions.add(unresponsededParamInSession.get(sessionId).size());
			}

		}

		result.append("Number of done sessions: ").append(doneSessions).append("\n");
		result.append("Progressing in sessions: ");
		for (int n : progressingSessions) {
			result.append(n).append(" ");
		}

		result.append("\n");
		return result.toString();
	}

	public final String getStatus() {

		StringBuilder result = new StringBuilder();
		int waiting = 0;
		for (RemoteProcess process : incompleteMessages.keySet()) {
			int waitingForPId = incompleteMessages.get(process).size();
			waiting += waitingForPId;
			result.append("Unresponded Message for PID<").append(process).append(">=").append(waitingForPId)
			.append("\n");
		}
		int done = 0;
		for (RemoteProcess process : receivedMessagesNumber.keySet()) {
			int doneForPID = receivedMessagesNumber.get(process).intValue();
			done += doneForPID;
			result.append("Responded Message for PID<").append(process).append(">=").append(doneForPID).append("\n");
		}

		result.append("Total waiting: ").append(waiting);
		result.append("\tTotal done: ").append(done);
		result.append("\nMonitor is working? ").append(monitorWorking);
		result.append("\n").append(processLogger.getStatus());
		result.append("\n").append(getStatusOnSessions());

		return result.toString();
	}

	public boolean sessionIsNotRecorded(UUID sessionId) {
		return !unresponsededParamInSession.containsKey(sessionId);
	}

	public boolean sessionIsDone(UUID sessionId) {
		//System.out.println("unresponded size====" + unresponsededParamInSession.get(sessionId).size());
		return !sessionIsNotRecorded(sessionId) && unresponsededParamInSession.get(sessionId).isEmpty();
	}

	/**
	 * In the case of timeout, the process might be crashed. So the message is
	 * resent to the process to be processed again. If after retrying
	 * MaxTimeoutRetry times, still timeout is reported, then something actually
	 * happened.
	 * 
	 * @param process
	 * @param param
	 */
	public void removeAndPushUndoneRequest(final RemoteProcess process, final ProcessingParam param) {

		removeMessageFromProcess(process, param);

		if (!timeoutRetry.containsKey(param)) {
			timeoutRetry.put(param, 1);
		}

		if (timeoutRetry.get(param) <= maxTimeoutRetry) {
			if (Configuration.IsInDeubbungMode)
				logger.info(Utils.threadName() + "The task was timed out on " + process
						+ " but it will be retried for: " + timeoutRetry.get(param) + " time.");
			pushUndoneRequest(process, param);
			timeoutRetry.replace(param, timeoutRetry.get(param) + 1);
		} else{
			removeMessageFromSession(param);
		}
	}

	public boolean isAllProcessesNotWorking() {
		return processLogger.allProcessesNotWorking();
	}

	/**
	 * Remove from the pId from incompleteMessages and push the params into the
	 * feeder
	 * 
	 * @param process
	 */
	public void removeAndPushUndoneRequests(final RemoteProcess process) {

		if (processLogger.getRemoteProcessRecord(process) == null) {
			logger.log(Level.WARNING, Utils.threadName() + "The process is not avaialable: " + process);
			return;
		}

		if (processLogger.getRemoteProcessRecord(process).isActive()) {
			logger.log(Level.WARNING, Utils.threadName() + "The process is still active: " + process);
		}
		if (!incompleteMessages.containsKey(process)) {
			logger.log(Level.SEVERE, Utils.threadName() + "The request is not in the map, pId: " + process);
		} else {
			Map<ProcessingParam, Map.Entry<Long, Integer>> map = incompleteMessages.get(process);
			synchronized (map) {
				Iterable<ProcessingParam> itr = incompleteMessages.get(process).keySet();
				pushUndoneRequests(process, itr);
				incompleteMessages.remove(process);
				// it is not removed from the session
			}
		}
	}

	public synchronized Set<RemoteProcess> findOrphanProcesses(RemoteProcessLogger processLogger) {

		Set<RemoteProcess> result = new HashSet<RemoteProcess>(incompleteMessages.keySet());
		result.removeAll(processLogger.getLiveProcessIDs());	
		return Collections.unmodifiableSet(result);
	}

	/**
	 * Finds the requests, i.e. ProcessingParams, that are timed out.
	 * 
	 * @return
	 */
	protected Map<RemoteProcess, Set<ProcessingParam>> findTimedoutRequests() {
		final Map<RemoteProcess, Set<ProcessingParam>> result = new HashMap<>();
		for (RemoteProcess process : incompleteMessages.keySet()) {
			Map<ProcessingParam, Map.Entry<Long, Integer>> params = incompleteMessages.get(process);
			for (ProcessingParam param : params.keySet()) {
				Map.Entry<Long, Integer> log = params.get(param);
				if ((1.2 * param.getTimeout().get() + log.getKey()) < System.currentTimeMillis()) {
					if (!result.containsKey(process)) {
						result.put(process, new HashSet<>());
					}
					result.get(process).add(param);
				}

			}
		}
		return Collections.unmodifiableMap(result);
	}

	/**
	 * This method is called by response message, once a message is received
	 * from the process as a task is done.
	 * 
	 * @param result
	 */
	public void processResponded(ProcessedResult result, RemoteProcess process) {

		//System.out.println("processResponded removed1->"
		//		+ ((AlloyProcessingParam) result.getParam()).getAlloyCoder().get().srcName());

		if (processLogger.getRemoteProcessRecord(process) == null) {
			logger.severe(Utils.threadName() + " No Such a PID found: " + process + " and the current PIDs are:\n\t"
					+ processLogger.getAllRegisteredProcesses());
			return;
		}

		//System.out.println("processResponded removed2->"
		//		+ ((AlloyProcessingParam) result.getParam()).getAlloyCoder().get().srcName());

		if (result.isTimedout() || result.isFailed()) {
			//System.out.println("processResponded removed3->"
			//		+ ((AlloyProcessingParam) result.getParam()).getAlloyCoder().get().srcName());
			removeAndPushUndoneRequest(process, result.getParam());
			processLogger.DecreaseDoingTasks(process);
		} else if (result.isInferred()) {
			//System.out.println("processResponded removed4->" + result.getParam().hashCode());
			if (Configuration.IsInDeubbungMode)
				logger.info(Utils.threadName() + "The process is inferred: pID= " + process + " param="
						+ result.getParam());
			// manager.increaseInferredMessageCounter(PID);
		} else {
			//System.out.println("processResponded removed5->"
			//		+ ((AlloyProcessingParam) result.getParam()).getAlloyCoder().get().srcName());
			removeMessageFromProcess(process, result.getParam());
			removeMessageFromSession(result.getParam());
			processLogger.DecreaseDoingTasks(process);
			processLogger.IncreaseDoneTasks(process);
		}
		//System.out.println("processResponded removed6->"
		//		+ ((AlloyProcessingParam) result.getParam()).getAlloyCoder().get().srcName());
		processLogger.changeStatusToWORKING(process);
		processLogger.changeLastLiveTimeRecieved(process);
	}

	public void holaProcessResponded(ProcessedResult result, RemoteProcess process) {
		removeMessageFromProcess(process, result.getParam());
		removeMessageFromSession(result.getParam());
		}

	/**
	 * The property is checked, and its result is sat or not. So, the implied
	 * properties should be processed next.
	 * 
	 * @param result
	 */
	/*
	 * public void checkNextProperties(AlloyProcessedResult result) {
	 * 
	 * try {
	 * 
	 * AlloyProcessingParam params = (AlloyProcessingParam) result.params;
	 * Set<String> nextProperties = new HashSet<>(
	 * params.alloyCoder.getToBeCheckedProperties(result.sat == 1));
	 * 
	 * if (!nextProperties.isEmpty())
	 * ExpressionPropertyGenerator.Builder.getInstance()
	 * .create((GeneratedStorage<ProcessingParam>) feeder, nextProperties,
	 * doneChecks) .startThread(); else logger.log(Level.INFO,
	 * Utils.threadName() + "The next properties are empty for:" + result); }
	 * catch (Err | IOException e) { logger.log(Level.SEVERE, Utils.threadName()
	 * + "Next properties failed to be added.", e); e.printStackTrace(); } }
	 */

	public void startThread() {

		timeoutMonitor.start();
	}

	public void cancelThread() {

		timeoutMonitor.interrupt();
	}

	public void changePriority(final int newPriority) {

		timeoutMonitor.setPriority(newPriority);
	}

	@Override
	public void run() {

		monitorTimeouts();
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

	protected int getAllWaitings() {

		int waiting = 0;
		for (RemoteProcess process : incompleteMessages.keySet()) {
			int waitingForPId = incompleteMessages.get(process).size();
			waiting += waitingForPId;
		}

		return waiting;
	}

	protected void monitorTimeouts() {

		while (!Thread.currentThread().isInterrupted()) {
			try {
				//System.out.println("monitorInterval->" + monitorInterval);
				Thread.sleep(monitorInterval);
				for (RemoteProcess process : processLogger.getTimedoutProcess(monitorInterval)) {

					logger.log(Level.WARNING,
							Utils.threadName() + "The processes is timedout and will be killed:" + process);
					processLogger.changeStatusToKILLING(process);

					logger.log(Level.WARNING, Utils.threadName() + "Removing undone requests:" + process);
					removeAndPushUndoneRequests(process);

					logger.log(Level.WARNING,
							Utils.threadName() + "Requests are removed for the killing process:" + process);

					processLogger.killAndReplaceProcess(process);
				}

				System.out.println("check orphan processes");

				for (RemoteProcess process : findOrphanProcesses(processLogger)) {
					System.out.println("orphan process->" + process);
					logger.log(Level.WARNING,
							Utils.threadName() + "Orphan processes are to be removed from the process: " + process);
					removeAndPushUndoneRequests(process);

					logger.log(Level.WARNING,
							Utils.threadName() + "Orphan processes are are removed process: " + process);
				}

				Map<RemoteProcess, Set<ProcessingParam>> timedOut = findTimedoutRequests();
				System.out.println("found the following obsolete processing units:" + timedOut);
				for (RemoteProcess process : timedOut.keySet()) {
					for (ProcessingParam param : timedOut.get(process)) {
						removeAndPushUndoneRequest(process, param);
					}
				}

				System.out.println("get status->" + getStatus());

			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, Utils.threadName() + "The time-out monitor is interrupted.", e);
			}

		}
	}

	private void recordRemovedMessage(final RemoteProcess process) {

		if (!receivedMessagesNumber.containsKey(process)) {
			receivedMessagesNumber.put(process, new AtomicInteger(1));
		} else {
			receivedMessagesNumber.get(process).incrementAndGet();
		}
	}

	private void pushUndoneRequest(final RemoteProcess process, ProcessingParam param) {
		try {
			backLogQueueFeeder.put(param);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, Utils.threadName() + "The request is not queued:" + param + " of pId: " + process,
					e);
		}
	}

	private void pushUndoneRequests(final RemoteProcess process, Iterable<ProcessingParam> itr) {

		for (ProcessingParam param : itr) {
			pushUndoneRequest(process, param);
		}
	}

}
