/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessorUtil;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcessRecord.Status;
import edu.uw.ece.alloy.util.Utils;

/**
 * It is an abstract class t create purchases on another JVMs. It can track the
 * number of sent Request messages and response messages.
 * 
 * It checks the status of remote process It kills a remote process by either
 * sending a suicide message or ?! It takes Liveness messages and update the
 * processes status
 * 
 * T is the type of class that should be created on a ne JVM.
 * 
 * 
 * @author vajih
 *
 */
public class RemoteProcessManager<T /*
																		 * TODO it should only run Runner objects
																		 * "extends [Something]Runner"
																		 */> implements RemoteProcessLogger{

	protected final static Logger logger = Logger.getLogger(
			RemoteProcessManager.class.getName() + "--" + Utils.threadName());;

	final static int SubMemory = Integer
			.parseInt(Configuration.getProp("sub_memory"));
	final static int SubStack = Integer
			.parseInt(Configuration.getProp("sub_stak"));
	final static String ProcessLoggerConfig = Configuration
			.getProp("process_logger_config");

	final public InetSocketAddress localSocket;
	final public int maxActiveProcessNumbers;
	final public int maxDoingTasks;
	
	final public Optional<Class<T>> remoteRunnerClass;
	/*
	 * How many message are sent to a remote process and waiting for the response
	 */
	final ConcurrentMap<RemoteProcess, AtomicLong> waitingMaessgesCount = new ConcurrentHashMap<>();
	/* How many messages are sent and responded */
	final ConcurrentMap<RemoteProcess, AtomicLong> allMaessgesCount = new ConcurrentHashMap<>();

	/* Active process */
	final ConcurrentMap<RemoteProcess, RemoteProcessRecord> activeProcesses = new ConcurrentHashMap<>();
	/* Process that */
	final ConcurrentMap<RemoteProcess, RemoteProcessRecord> deadProcesses = new ConcurrentHashMap<>();

	public RemoteProcessManager(InetSocketAddress localSocket,
			int maxActiveProcessNumbers, int maxDoingTasks, Class<T> remoteRunnerClass) {
		// InetSocketAddress is immutable.
		this.localSocket = localSocket;
		this.maxActiveProcessNumbers = maxActiveProcessNumbers;
		this.maxDoingTasks = maxDoingTasks;
		this.remoteRunnerClass = Optional.ofNullable(remoteRunnerClass);
	}
	
	public RemoteProcessManager(InetSocketAddress localSocket,
			int maxActiveProcessNumbers, int maxDoingTasks, Optional<Class<T>> remoteRunnerClass) {
		// InetSocketAddress is immutable.
		this.localSocket = localSocket;
		this.maxActiveProcessNumbers = maxActiveProcessNumbers;
		this.maxDoingTasks = maxDoingTasks;
		this.remoteRunnerClass = remoteRunnerClass;
	}
	
	public RemoteProcessManager(InetSocketAddress localSocket,
			int maxActiveProcessNumbers, int maxDoingTasks) {
		this(localSocket, maxActiveProcessNumbers, maxDoingTasks, Optional.empty());
	}

	final Process bootProcess(RemoteProcess remoteSocket, Class<T> clazz)
			throws IOException {
		return ProcessorUtil.<T> createNewJVM(SubMemory, SubStack,
				ProcessLoggerConfig, remoteSocket.address, localSocket, clazz);
	}

	final Process bootProcess(RemoteProcess remoteSocket) throws IOException {
		if (this.remoteRunnerClass.isPresent()){
			return bootProcess(remoteSocket, remoteRunnerClass.get() );
		}
		return bootProcess(remoteSocket, getGenericTypeAsClass());
	}

	/**
	 * Change the status of a live process
	 * 
	 * @param process
	 * @param status
	 */
	public void changeStatus(final RemoteProcess process, Status status) {
		if (Configuration.IsInDeubbungMode)
			logger.log(Level.INFO, "[" + Thread.currentThread().getName() + "] "
					+ "Changing the status of PID:" + process + " to: " + status);
		synchronized (activeProcesses) {
			if (activeProcesses.containsKey(process)) {
				activeProcesses.replace(process,
						activeProcesses.get(process).changeStatus(status));
				if (Configuration.IsInDeubbungMode)
					logger.log(Level.INFO, "[" + Thread.currentThread().getName() + "] "
							+ "The status is chanaged PID:" + process + " to: " + status);
			} else {
				logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "]"
						+ "The process is not found to be changed its status. ");
				throw new RuntimeException("The process is not found: " + process);
			}
		}
	}
	
	@Override
	public void changeStatusToIDLE(final RemoteProcess process) {
		changeStatus(process, Status.IDLE);
	}
	
	@Override
	public void changeStatusToWORKING(final RemoteProcess process) {
		changeStatus(process, Status.WORKING);
	}
	
	@Override
	public void changeStatusToKILLING(final RemoteProcess process) {
		changeStatus(process, Status.KILLING);
	}

	@Override
	public void changeStatusToNOANSWER(final RemoteProcess process) {
		changeStatus(process, Status.NOANSWER);
	}
	
	/**
	 * Give a process, the record is replaced. RemoteProcessRecord is immutable.
	 * 
	 * @param process
	 * @param newRecord
	 */
	protected void changeRecord(final RemoteProcess process,
			RemoteProcessRecord newRecord) {
		if (activeProcesses.containsKey(process)) {
			activeProcesses.replace(process, newRecord);
		} else {
			throw new RuntimeException(
					"The process is not found: " + activeProcesses);
		}
	}

	@Override
	public void changeSentTasks(final RemoteProcess process, int sentTasks) {
		synchronized (activeProcesses) {
			changeRecord(process,
					activeProcesses.get(process).changeSentTasks(sentTasks));
		}
	}

	@Override
	public void IncreaseSentTasks(final RemoteProcess process, int sentTasks) {
		changeSentTasks(process,
				activeProcesses.get(process).sentTasks + sentTasks);
	}

	@Override
	public void IncreaseSentTasks(final RemoteProcess process) {
		IncreaseSentTasks(process, 1);
	}

	
	@Override
	public void changeDoingTasks(final RemoteProcess process, int doingTasks) {
		synchronized (activeProcesses) {
			changeRecord(process,
					activeProcesses.get(process).changeDoingTasks(doingTasks));
		}
	}

	@Override
	public void IncreaseDoingTasks(final RemoteProcess process, int doingTasks) {
		changeDoingTasks(process,
				activeProcesses.get(process).doingTasks + doingTasks);
	}

	@Override
	public void IncreaseDoingTasks(final RemoteProcess process) {
		IncreaseDoingTasks(process, 1);
	}

	@Override
	public void DecreaseDoingTasks(final RemoteProcess process, int doingTasks) {
		changeDoingTasks(process,
				activeProcesses.get(process).doingTasks - doingTasks);
	}

	@Override
	public void DecreaseDoingTasks(final RemoteProcess process) {
		DecreaseDoingTasks(process, 1);
	}

	@Override
	public void changeDoneTasks(final RemoteProcess process, int doneTasks) {
		synchronized (activeProcesses) {
			changeRecord(process,
					activeProcesses.get(process).changeDoneTasks(doneTasks));
		}
	}

	@Override
	public void IncreaseDoneTasks(final RemoteProcess process, int doneTasks) {
		changeDoneTasks(process,
				activeProcesses.get(process).doingTasks + doneTasks);
	}

	@Override
	public void IncreaseDoneTasks(final RemoteProcess process) {
		IncreaseDoneTasks(process, 1);
	}

	@Override
	public void changeLastLiveTimeReported(final RemoteProcess process,
			long lastLiveTimeReported) {
		synchronized (activeProcesses) {
			changeRecord(process, activeProcesses.get(process)
					.changeLastLiveTimeReported(lastLiveTimeReported));
		}
	}

	@Override
	public void changeLastLiveTimeReported(final RemoteProcess process) {
		changeLastLiveTimeReported(process, System.currentTimeMillis());
	}

	@Override
	public void changeLastLiveTimeRecieved(final RemoteProcess process,
			long lastLiveTimeRecieved) {
		synchronized (activeProcesses) {
			changeRecord(process, activeProcesses.get(process)
					.changeLastLiveTimeRecieved(lastLiveTimeRecieved));
		}
	}

	@Override
	public void changeLastLiveTimeRecieved(final RemoteProcess process) {
		changeLastLiveTimeRecieved(process, System.currentTimeMillis());
	}

	/**
	 * retrieve the record of a process that currently is active.
	 * @param process
	 * @return
	 */
	@Override
	public RemoteProcessRecord getRemoteProcessRecord(
			final RemoteProcess process) {
		return activeProcesses.get(process);
	}

	public Set<RemoteProcess> getAllRegisteredRemoteProcesses() {
		return activeProcesses.keySet();
	}

	/**
	 * Find which processors are timed out.
	 * 
	 * @param threshold
	 *          in milliseconds
	 * @return
	 */
	public List<RemoteProcess> getTimedoutProcess(int threshold) {
		List<RemoteProcess> result = Collections
				.synchronizedList(new LinkedList<>());
		// No need to synchronized. The time is loose.
		long currentMoment = System.currentTimeMillis();
		for (RemoteProcess process : activeProcesses.keySet()) {
			RemoteProcessRecord record = activeProcesses.get(process);
			if (currentMoment - Math.max(record.lastLiveTimeReported,
					record.lastLiveTimeRecieved) > threshold)
				result.add(process);
		}
		return Collections.unmodifiableList(result);
	}

	public void finalize() {
		for (RemoteProcess process : activeProcesses.keySet())
			killProcess(process);
	}

	public Set<RemoteProcess> getLiveProcessIDs() {
		return Collections.unmodifiableSet(activeProcesses.keySet());

	}

	public boolean allProcessesNotWorking() {
		synchronized (activeProcesses) {
			for (RemoteProcess process : activeProcesses.keySet()) {
				if (getRemoteProcessStatus(process).equals(Status.WORKING)) {
					return false;
				}
			}
			return true;
		}
	}

	public boolean allProcessesWorking() {
		synchronized (activeProcesses) {
			for (RemoteProcess process : activeProcesses.keySet()) {
				if (!getRemoteProcessStatus(process).equals(Status.WORKING)) {
					return false;
				}
			}
			return true;
		}
	}

	public boolean SomeProcessesWorking() {
		synchronized (activeProcesses) {
			for (RemoteProcess process : activeProcesses.keySet()) {
				if (getRemoteProcessStatus(process).equals(Status.WORKING)) {
					return true;
				}
			}
			return false;
		}
	}

	public Status getRemoteProcessStatus(RemoteProcess process) {
		return activeProcesses.get(process).status;
	}

	public String getStatus() {
		final StringBuilder result = new StringBuilder();
		final SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss.SSS");
		int sent = 0, done = 0, doing = 0, doneForProcess = 0, doingForProcess = 0,
				sentForProcess = 0;
		for (RemoteProcess process : activeProcesses.keySet()) {

			doneForProcess = activeProcesses.get(process).doneTasks;
			done += doneForProcess;
			result.append("Current reported DONE Meessages for PID=<" + process
					+ "> is:" + done).append("\n");

			doingForProcess = activeProcesses.get(process).doingTasks;
			doing += doingForProcess;
			result.append("Current reported DOING");

			sentForProcess = activeProcesses.get(process).doingTasks;
			sent += sentForProcess;
			result.append("Current reported Sent Meessages for PID=<" + process
					+ "> is:" + sentForProcess).append("\n");

			result
					.append("Current last message was recieved from PID=<" + process
							+ "> was at:"
							+ sdf.format(activeProcesses.get(process).lastLiveTimeRecieved))
					.append("\n");
			result
					.append("Current last message was reported from PID=<" + process
							+ "> was at:"
							+ sdf.format(activeProcesses.get(process).lastLiveTimeReported))
					.append("\n");
			result.append("Current reported Sent Meessages for PID=<" + process
					+ "> is:" + activeProcesses.get(process).sentTasks).append("\n");
		}

		result.append("Total not responded:").append(doing).append("\n")
				.append("The current total sent: ").append(sent).append("\n")
				.append("The current total Done: ").append(done).append("\n");

		return result.toString();
	}

	/**
	 * Type T has to be set in the subclass.
	 * 
	 * @return
	 */
	protected Class getGenericTypeAsClass() {
		return (Class<?>)((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
	}

	public void addProcess(RemoteProcess remoteProcess) throws IOException {
		RemoteProcessRecord record = new RemoteProcessRecord(remoteProcess, bootProcess(remoteProcess));
		activeProcesses.putIfAbsent(remoteProcess, record);
		if (Configuration.IsInDeubbungMode)
			logger.log(Level.INFO, Utils.threadName() + "A process:" + remoteProcess
					+ " is added to the process list " + activeProcesses);
	}
	
	/**
	 * Add a random process to the activeProcess list
	 * @throws IOException
	 */
	public void addProcess() throws IOException {
		addProcess( new RemoteProcess(ProcessorUtil.findEmptyLocalSocket()));
	}

	/**
	 * Not thread safe.
	 */
	public void addAllProcesses() {
		if (Configuration.IsInDeubbungMode)
			logger.log(Level.INFO, Utils.threadName() + "Starting to add processes");
		// No other thread can add a process to the map.
		synchronized (activeProcesses) {
			int i = activeProcesses.size();
			int maxAttempts = i + 100;
			while (i < maxAttempts) {

				if (activeProcesses.size() == maxActiveProcessNumbers)
					break;
				if (i > maxActiveProcessNumbers)
					throw new RuntimeException("Invalid state: i=" + i
							+ " Should not be more than ProcessNumbers="
							+ maxActiveProcessNumbers);
				try {
					addProcess();
					++i;
					logger.log(Level.WARNING,
							"[" + Thread.currentThread().getName() + "] "
									+ "A process is added to the processes list:"
									+ activeProcesses);
				} catch (IOException e) {
					logger.severe("[" + Thread.currentThread().getName() + "]"
							+ "Processes cannot be created in setUpAllProcesses");
				} finally {
					--maxAttempts;
				}
			}

			if (i != maxActiveProcessNumbers)
				throw new RuntimeException("Cannot create all processes: "
						+ maxActiveProcessNumbers + " after " + maxAttempts + " attempts.");
		}

	}

	/**
	 * Precondition. The process has to be in the Killing state
	 * 
	 * @param port
	 */
	public boolean killProcess(final RemoteProcess process) {
		if (Configuration.IsInDeubbungMode)
			logger.log(Level.INFO,
					Utils.threadName() + "Kill a remote Process: " + process);
		boolean result = false;
		synchronized (activeProcesses) {
			if (!activeProcesses.containsKey(process)) {
				logger.log(Level.WARNING, "[" + Thread.currentThread().getName() + "]"
						+ "The process is not found to be active: " + process);
			} else if (activeProcesses
					.get(process).status != RemoteProcessRecord.Status.KILLING) {
				logger.log(Level.WARNING,
						"[" + Thread.currentThread().getName() + "]" + "The process: "
								+ process
								+ " is not in the killing state and cannot be killed: "
								+ getRemoteProcessStatus(process));
			} else {
				logger.log(Level.WARNING, "[" + Thread.currentThread().getName() + "] "
						+ "Killing a process:" + process);
				logger.log(Level.WARNING, "[" + Thread.currentThread().getName() + "] "
						+ "Entering a lock for killing a process:" + process);
				activeProcesses.get(process).process.destroyForcibly();
				deadProcesses.putIfAbsent(process, activeProcesses.get(process));
				activeProcesses.remove(process);
				logger.log(Level.WARNING,
						"[" + Thread.currentThread().getName() + "] " + "A process:"
								+ process + " is killed to the process list "
								+ activeProcesses);
				result = true;
			}
		}
		return result;
	}

	/**
	 * Not thread safe Precondition. The process has to be in the Killing state
	 * 
	 * @param port
	 */
	public void killAndReplaceProcess(final RemoteProcess process) {
		// synchronized (processes) {
		if (killProcess(process)) {
			addAllProcesses();
		}
		// }
	}

	public RemoteProcess getRandomProcess() {
		synchronized (activeProcesses) {
			@SuppressWarnings("rawtypes")
			List<RemoteProcess> randomArray = new ArrayList<RemoteProcess>(
					activeProcesses.keySet());
			final int max = randomArray.size();
			final int randomIndex = (new Random()).nextInt(max);
			return randomArray.get(randomIndex);
		}
	}

	/**
	 * Return the most idle process.
	 * 
	 * @return
	 */
	public RemoteProcess getIdlerProcess() {
		// TODO
		throw new RuntimeException("Unimplemented");
	}

	protected boolean isAccepting(final RemoteProcess process) {

		if (!activeProcesses.containsKey(process)) {
			logger.log(Level.SEVERE, Utils.threadName() + "Process " + process
					+ " not found in activeProcesses list: " + activeProcesses);
			throw new RuntimeException("Not working process was found.");
		}

		RemoteProcessRecord record = activeProcesses.get(process);

		if (!record.isActive())
			return false;

		if (record.status.equals(Status.WORKING)
				&& record.doingTasks > maxDoingTasks) {
			return false;
		}
		return true;
	}

	public RemoteProcess getActiveRandomeProcess() {

		RemoteProcess result;
		int retry = 10;
		int maxRetry = 10000;
		do {
			if (retry > maxRetry) {
				logger.log(Level.SEVERE,
						Utils.threadName()
								+ "Not able to find a random working process after atempting: "
								+ retry);
				throw new RuntimeException("Not working process was found.");
			}
			result = getRandomProcess();
			++retry;
			try {
				Thread.sleep(retry);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, Utils.threadName()
						+ "Interrupted while waiting for an active process. ");
			}

		} while (!isAccepting(result));

		return result;
	}

}
