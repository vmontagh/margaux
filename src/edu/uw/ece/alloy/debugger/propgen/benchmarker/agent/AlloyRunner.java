/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.agent;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Pair;
import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.debugger.infrastructure.Runner;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ReportLiveness;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.communication.Queue;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.InvalidParameterException;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RequestMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy.AlloyLivenessMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy.AlloyReadyMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadMonitor;
import edu.uw.ece.alloy.util.ServerSocketInterface;
import edu.uw.ece.alloy.util.Utils;
import edu.uw.ece.alloy.util.events.MessageEventListener;
import edu.uw.ece.alloy.util.events.MessageReceivedEventArgs;

/**
 * The class Alloy runner is the main for executing an Alloy program.
 * 
 * @author vajih
 *
 */
public final class AlloyRunner extends Runner {
	// Configuration parameters
	final public static long SelfMonitorInterval = Long
			.parseLong(Configuration.getProp("self_monitor_interval"));
	final public static int SelfMonitorRetryAttempt = Integer
			.valueOf(Configuration.getProp("self_monitor_retry_attempt"));
	final public static File TmpDirectoryRoot = new File(
			Configuration.getProp("temporary_directory"));
	final public static long LivenessIntervalInMS = Long
			.parseLong(Configuration.getProp("liveness_interval"));
	final public static int MaxLivenessFailTry = Integer
			.parseInt(Configuration.getProp("liveness_max_fail"));

	final public static boolean RemoveSourceAfter = Boolean
			.parseBoolean(Configuration.getProp("do_clean_source_after_computation"));
	final public static boolean DoLogOnFile = Boolean
			.parseBoolean(Configuration.getProp("do_log_on_file"));
	final public static boolean DoLogOnDB = Boolean
			.parseBoolean(Configuration.getProp("do_log_on_db"));

	protected final static Logger logger = Logger.getLogger(
			AlloyRunner.class.getName() + "--" + Thread.currentThread().getName());

	public final InetSocketAddress localSocket;
	public final InetSocketAddress remoteSocket;

	// Directory contains all the temporary files. It is a sub directory in the
	// temp root directory.
	protected File tmpLocalDirectory;
	protected final long threadMonitoringInterval;
	protected final long livenessInterval;
	protected final int maxLivenessFailed;
	protected final boolean logOnFile, logOnDB, removeContent;

	protected ServerSocketInterface inputInterface;
	protected Queue<AlloyProcessingParam> feedingQueue;
	protected AlloyExecuter executer;
	protected PostProcess.FileWrite fileWriter;
	protected PostProcess.SocketWriter socketWriter;
	protected PostProcess.DBWriter dbWriter;
	protected PostProcess.CleanAfterProccessed cleanAfterProcessed;
	protected ReportLiveness<AlloyLivenessMessage> liveness;
	protected ThreadMonitor localThreadsMonitor;
	protected int tobeProcessed;

	/**
	 * Retrieve a session from the cached sessions given the session ID.
	 */
	protected Consumer<AlloyProcessingParam> addNewParamInQueue = (
			AlloyProcessingParam param) -> {
		try {
			feedingQueue.put(param);
		} catch (Exception ie) {
			logger
					.severe(Utils.threadName() + " Cannot add a new task to the queue.");
			throw new RuntimeException(ie);
		}
	};

	protected AlloyRunner(InetSocketAddress localSocket,
			InetSocketAddress remoteSocket, File tmpLocalDirectory,
			long threadMonitoringInterval, long livenessInterval,
			int maxLivenessFailed, final boolean logOnFile, final boolean logOnDB,
			final boolean removeContent) {
		this.localSocket = localSocket;
		this.remoteSocket = remoteSocket;
		this.tmpLocalDirectory = tmpLocalDirectory;
		this.threadMonitoringInterval = threadMonitoringInterval;
		this.livenessInterval = livenessInterval;
		this.maxLivenessFailed = maxLivenessFailed;
		this.logOnFile = logOnFile;
		this.logOnDB = logOnDB;
		this.removeContent = removeContent;
		this.tobeProcessed = 0;
		initiate();
	}

	protected AlloyRunner(InetSocketAddress localSocket,
			InetSocketAddress remoteSocket) {
		this(localSocket, remoteSocket,
				new File(TmpDirectoryRoot, String.valueOf(localSocket.getPort())),
				SelfMonitorInterval, LivenessIntervalInMS, MaxLivenessFailTry,
				DoLogOnFile, DoLogOnDB, RemoveSourceAfter);
	}

	protected void initiate() {
		// setting up the temporary folder
		// remote the previous contents
		if (tmpLocalDirectory.exists()) {
			try {
				if (Configuration.IsInDeubbungMode)
					logger.info("[" + Thread.currentThread().getName() + "] "
							+ " exists and has to be recreated."
							+ tmpLocalDirectory.getCanonicalPath());
				Utils.deleteRecursivly(tmpLocalDirectory);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
						+ "Unable to delete the previous files.", e);
			}
		}
		// After deleting the temp directory create a new one.
		if (!tmpLocalDirectory.mkdirs())
			throw new RuntimeException("Can not create a new directory");

		localThreadsMonitor = new ThreadMonitor(threadMonitoringInterval, 0);

		// Setup the input interface.
		inputInterface = new ServerSocketInterface(this.localSocket,
				this.remoteSocket);

		// liveness messages are sent to the initiator
		liveness = new ReportLiveness<AlloyLivenessMessage>(localSocket,
				remoteSocket, 0, 0, livenessInterval, maxLivenessFailed,
				inputInterface) {
			@Override
			protected AlloyLivenessMessage createLivenessMessage() {
				return new AlloyLivenessMessage(super.localProcess, super.processed,
						super.tobeProcessed);
			}
		};

		// Queue that are shared between inputinterface and
		feedingQueue = new Queue<>();

		executer = new AlloyExecuter(feedingQueue, liveness, inputInterface,
				tmpLocalDirectory);
		localThreadsMonitor.addThreadToBeMonitored(executer);

		inputInterface.MessageReceived
				.addListener(new MessageEventListener<MessageReceivedEventArgs>() {
					@Override
					public void actionOn(RequestMessage requestMessage,
							MessageReceivedEventArgs messageArgs) {
						final Map<String, Object> context = new HashMap<>();
						context.put("addNewParamInQueue", addNewParamInQueue);
						try {
							requestMessage.onAction(context);
						} catch (InvalidParameterException e) {
							logger.severe(Utils.threadName()
									+ "request cannot be processed:\n" + e.getStackTrace());
						}
					}
				});

		fileWriter = new PostProcess.FileWrite();
		socketWriter = new PostProcess.SocketWriter(inputInterface);
		dbWriter = new PostProcess.DBWriter(inputInterface.getHostProcess());
		cleanAfterProcessed = new PostProcess.CleanAfterProccessed();

		// Sending back a response is mandatory.
		executer.resgisterPostProcess(socketWriter);
		localThreadsMonitor.addThreadToBeMonitored(executer);
		if (logOnFile) {
			executer.resgisterPostProcess(fileWriter);
			localThreadsMonitor.addThreadToBeMonitored(fileWriter);
		}
		if (logOnDB) {
			executer.resgisterPostProcess(dbWriter);
			localThreadsMonitor.addThreadToBeMonitored(dbWriter);
		}
		if (removeContent) {
			executer.resgisterPostProcess(cleanAfterProcessed);
			localThreadsMonitor.addThreadToBeMonitored(cleanAfterProcessed);
		}

	}

	public void start() {
		executer.startThread();
		socketWriter.startThread();
		inputInterface.startThread();

		if (logOnFile) {
			fileWriter.startThread();
		}
		if (logOnDB) {
			dbWriter.startThread();
		}
		if (removeContent) {
			cleanAfterProcessed.startThread();
		}

		localThreadsMonitor.startMonitoring();

		liveness.startThread();

		// Everything should be ready now, so the booter should be notified.
		// send a readyness message
		inputInterface
				.sendMessage(new AlloyReadyMessage(inputInterface.getHostProcess()));
	}

	public static void main(String[] args) throws Exception {

		Pair<InetSocketAddress, InetSocketAddress> ports = extractPortsfromCommand(
				args);
		AlloyRunner runner = new AlloyRunner(ports.a, ports.b);
		runner.start();
	}
}
