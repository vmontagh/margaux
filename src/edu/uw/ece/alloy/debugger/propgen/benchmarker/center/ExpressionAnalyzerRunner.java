package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.debugger.AnalyzingSession;
import edu.uw.ece.alloy.debugger.infrastructure.Runner;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ExpressionPropertyGenerator;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ExpressionPropertyGenerator.Builder;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessorUtil;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyRunner;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.communication.Publisher;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.communication.Queue;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.DiedMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.DoneMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.InvalidParameterException;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.LivenessMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ReadyMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RequestMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ResponseMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.SetupMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy.AlloyProcessedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy.AlloyRequestMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy.AlloyResponseMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy.AlloySetupMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger.PatternLivenessMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger.PatternProcessedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger.PatternProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger.PatternReadyMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger.PatternResponseMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger.PatternSetupDoneMesssage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.RemoteProcessMonitor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadMonitor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;
import edu.uw.ece.alloy.util.LazyFile;
import edu.uw.ece.alloy.util.SendOnServerSocketInterface;
import edu.uw.ece.alloy.util.ServerSocketInterface;
import edu.uw.ece.alloy.util.Utils;
import edu.uw.ece.alloy.util.events.MessageEventListener;
import edu.uw.ece.alloy.util.events.MessageReceivedEventArgs;
import edu.uw.ece.alloy.util.events.MessageSentEventArgs;

/**
 * The class is for run the interface of analyzing any expressions. It has two
 * interfaces: input and distributer. The input interface takes a a request from
 * a client. The distributer interface dispatch the analyzing parts into
 * solvers. The result will be sent back to through a response message.
 * 
 * The class is singleton.
 * 
 * @author vajih
 *
 */
public final class ExpressionAnalyzerRunner extends Runner {

	public class ExpressionAnalyzingSession implements AnalyzingSession {

		final long creationTime;
		final UUID id;
		/* After timeout millisecond, the waitUntilDone is called. */
		final long timeout;
		final PatternProcessingParam param;
		final ExpressionPropertyGenerator.Builder expressionGeneratorBuilder;
		/* Generated and sent properties */
		final Set<String> generatedProperties;
		/* Queue for caching the responses */
		final Queue<ResponseMessage> responseQueue = new Queue<>();
		/*
		 * The task that is processing the responses. It is shared to end the
		 * task.
		 */
		final Future<?> processingResponseTask;
		final ExecutorService threadExecutor;
		final SendOnServerSocketInterface interfacE;
		/*
		 * A set containing Valid result done by analysis. They encapsulated in
		 * the PatternProcessedREsult and returned later.
		 */
		final Set<AlloyProcessedResult> validResults;
		/*
		 * A thread sleep for a given time, then wakes up and finishes the
		 * session.
		 */
		final Thread timeoutThread;

		public ExpressionAnalyzingSession(final PatternProcessingParam param, final long alloyProcessingTime)
				throws Exception {
			this(param, alloyProcessingTime, tmpLocalDirectory, sessionThreadExecutor, inputInterface);
		}

		public ExpressionAnalyzingSession(final PatternProcessingParam param, final long alloyProcessingTime,
				final File tmpLocalDirectory, ExecutorService threadExecutor, SendOnServerSocketInterface interfacE)
				throws Exception {
			this.creationTime = System.currentTimeMillis();
			this.id = param.getAnalyzingSessionID().get();
			this.timeout = param.getTimeout().orElse(Long.MAX_VALUE);
			this.param = param.changeTmpLocalDirectory(tmpLocalDirectory).prepareToUse();

			this.generatedProperties = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

			expressionGeneratorBuilder = new Builder(id, alloyProcessingTime,
					param.getFile("toBeAnalyzedCode").orElseThrow(RuntimeException::new),
					dependentFiles.stream().filter(f -> f.getName().startsWith("relational_properties_tagged"))
							.findFirst().orElseThrow(RuntimeException::new),
					dependentFiles.stream().filter(f -> f.getName().startsWith("temporal_properties_tagged"))
							.findFirst().orElseThrow(RuntimeException::new),
					param.getFieldName().orElseGet(String::new),
					param.getPropertyToAlloyCode().orElseThrow(RuntimeException::new),
					param.getExpression().orElseThrow(RuntimeException::new),
					param.getScope().orElseThrow(RuntimeException::new),
					param.getFiles().orElseThrow(RuntimeException::new));
			this.validResults = new HashSet<>();
			this.interfacE = interfacE;
			this.threadExecutor = threadExecutor;
			processingResponseTask = sessionThreadExecutor.submit(() -> {
				processResponseMessaes();
			});

			timeoutThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(timeout);
						if (Configuration.IsInDeubbungMode)
							logger.warning(Utils.threadName() + " session " + getSessionID() + " is timed out after "
									+ (System.currentTimeMillis() - creationTime) + " millisecond");
						doneOnWait();
					} catch (InterruptedException e) {
						logger.warning(
								Utils.threadName() + "timeout thread is interrupted in Expression Analyzer!" + e);
					}
				}
			});
			System.out.println("Session is created for " + param.getExpression().get() + " over field="
					+ param.getFieldName().get() + " coder=" + param.getPropertyToAlloyCode().get());
		}

		public void addGeneratedProperties(String propertyName) {
			generatedProperties.add(propertyName);
		}

		@Override
		public long getSessionCreationTime() {
			return creationTime;
		}

		@Override
		public UUID getSessionID() {
			return id;
		}

		public Optional<Future<?>> getProcessingTask() {
			return Optional.ofNullable(processingResponseTask);
		}

		@Override
		public void start() {
			try {
				timeoutThread.start();
				// Start generating Alloy processing params
				System.out.println("Start session............");
				expressionGeneratorBuilder.create(feedingQueue).startThread();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		/**
		 * Processing the following response. A queue is shared between the
		 * session and response follow up action.
		 */
		void processResponseMessaes() {
			while (true) {
				ResponseMessage message;
				try {
					int generatedPropsCount = 0;
					message = responseQueue.take();

					if (message.isEmptyResponseMessage()) {
						if (responseQueue.isEmpty()) {
							done();
							break;
						}
						continue;
					}

					AlloyProcessingParam param = (AlloyProcessingParam) message.getResult().getParam();

					AlloyProcessedResult result = (AlloyProcessedResult) message.getResult();

					if (message.getResult().isNormal()
							&& result.getParam().getAlloyCoder()
									.orElseThrow(
											() -> new RuntimeException("Alloy Coder cannot be Null in a response."))
									.isDesiredSAT(result.sat)
							&& (expressionGeneratorBuilder.inPatterns(param.getAlloyCoder().get().predNameA)
									|| expressionGeneratorBuilder.inPatterns(param.getAlloyCoder().get().predNameB))) {
						validResults.add(result);
					}

					// second: find out whether more properties are required to
					// be checked.
					try {

						Set<String> nextProperties = new HashSet<>(param.getAlloyCoder().get().createItself()
								.getToBeCheckedProperties(message.getResult().sat));
						if (!nextProperties.isEmpty()) {
							generatedPropsCount = expressionGeneratorBuilder
									.createWithHistory(feedingQueue, nextProperties, generatedProperties)
									.generatePatternCheckers();
						} else {
							logger.log(Level.INFO,
									Utils.threadName() + "The next properties are empty for:" + message.getResult());
						}
					} catch (Err | IOException e) {
						logger.log(Level.SEVERE, Utils.threadName() + "Next properties failed to be added.", e);
						e.printStackTrace();
					}

				} catch (InterruptedException e1) {
					logger.log(Level.SEVERE, Utils.threadName() + "The thread is interrupted.", e1);
				}
			}
		}

		@Override
		public void followUp(ResponseMessage message) {
			try {
				responseQueue.put(message);
				if (monitor.sessionIsDone(getSessionID())) {
					responseQueue.put(ResponseMessage.createEmptyResponseMessage());
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				logger.log(Level.SEVERE, Utils.threadName() + "Fail on putting a new response message: <" + message
						+ "> in the Processing queue", e);
			}
		}

		/**
		 * create a new response message and send to the pattern analyzer
		 */
		protected void sendResult() {
			PatternProcessedResult result = new PatternProcessedResult(param,
					Collections.unmodifiableSet(validResults));
			PatternResponseMessage message = new PatternResponseMessage(result, interfacE.getHostProcess());
			interfacE.sendMessage(message);
		}

		@Override
		public void done() {
			sendResult();
			getProcessingTask().orElseThrow(() -> new RuntimeException("The Processing Task should not be NULL!"))
					.cancel(true);
			// finish the timeout thread.
			timeoutThread.interrupt();
		}

		@Override
		public void doneOnWait() {
			getProcessingTask().orElseThrow(() -> new RuntimeException("The Processing Task should not be NULL!"))
					.cancel(true);
			sendResult();
		}

	}

	// Configuration parameters
	final public static long PriodicalMonitoringThreadsReportInMS = Long
			.parseLong(Configuration.getProp("monitoring_report_period"));
	final public static long LivenessIntervalInMS = Long.parseLong(Configuration.getProp("liveness_interval"));
	final public static int MaxLivenessFailTry = Integer.parseInt(Configuration.getProp("liveness_max_fail"));
	final public static long SelfMonitorInterval = Long.parseLong(Configuration.getProp("self_monitor_interval"));
	final public static String TemporaryLocalDirectory = Configuration.getProp("temporary_directory");
	final static int ProccessNumber = Integer.parseInt(Configuration.getProp("alloy_processes_number"));
	final static long AlloyProcessingTime = Long.valueOf(Configuration.getProp("alloy_prcessing_timeout"));

	protected final static Logger logger = Logger
			.getLogger(ExpressionAnalyzerRunner.class.getName() + "--" + Thread.currentThread().getName());

	/* Given socket from input */
	protected final InetSocketAddress localSocket;
	/* Given socket from input */
	protected final InetSocketAddress remoteSocket;
	/* Randomly assign a new socket */
	protected final InetSocketAddress distributorSocket;

	protected ThreadMonitor localThreadsMonitor;

	final protected Map<UUID, ExpressionAnalyzingSession> analyzingSessions = new ConcurrentHashMap<>();
	final protected ExecutorService sessionThreadExecutor;

	protected RemoteProcessMonitor monitor;
	protected ThreadToBeMonitored feeder;
	protected ReportLiveness<PatternLivenessMessage> liveness;
	protected ServerSocketInterface inputInterface, distributerInterface;
	protected Queue<AlloyProcessingParam> feedingQueue, backlogFeedingQueue;
	protected RemoteProcessManager processManager;

	/*
	 * A local directory that stores the required files such as alloy file or
	 * libraries.
	 */
	protected File tmpLocalDirectory;
	/*
	 * Dependency file path is relative to the Alloy source file. Combined with
	 * tmpLocalDirectory, the local path is determined.
	 */
	protected List<LazyFile> dependentFiles;
	protected final long reportInterval;
	protected final long threadMonitoringInterval;
	protected final long livenessInterval;
	protected final int maxLivenessFailed;
	protected long alloyProcessingTime;

	/*
	 * All monitorable threads have to be registered in the list. A periodical
	 * report is made from the list
	 */
	List<ThreadToBeMonitored> monitoredThreads = new LinkedList<>();
	protected Thread reporterThread;

	protected ExpressionAnalyzerRunner(final InetSocketAddress localSocket, final InetSocketAddress remoteSocket,
			final InetSocketAddress distributorSocket, final File tmpLocalDirectory,
			final List<LazyFile> dependencyFiles, final long reportInterval, final long threadMonitoringInterval,
			final long livenessInterval, final int maxLivenessFailed, final long alloyProcessingTime,
			final ExecutorService sessionThreadExecutor) {
		this.localSocket = localSocket;
		this.remoteSocket = remoteSocket;
		this.distributorSocket = distributorSocket;
		this.tmpLocalDirectory = tmpLocalDirectory;
		this.reportInterval = reportInterval;
		this.threadMonitoringInterval = threadMonitoringInterval;
		this.livenessInterval = livenessInterval;
		this.maxLivenessFailed = maxLivenessFailed;
		this.sessionThreadExecutor = sessionThreadExecutor;
		this.dependentFiles = dependencyFiles;
		this.alloyProcessingTime = alloyProcessingTime;
		initiate();
	}

	protected ExpressionAnalyzerRunner(InetSocketAddress localSocket, InetSocketAddress remoteSocket) {
		this(localSocket, remoteSocket, ProcessorUtil.findEmptyLocalSocket(localSocket.getPort()),
				new File(TemporaryLocalDirectory), new LinkedList<>(), PriodicalMonitoringThreadsReportInMS,
				SelfMonitorInterval, LivenessIntervalInMS, MaxLivenessFailTry, AlloyProcessingTime,
				Executors.newFixedThreadPool(10));
	}

	/**
	 * create a new session object and add it to the session list. It helps to
	 * delegate the session creation task to the request message
	 */
	protected Function<PatternProcessingParam, Optional<ExpressionAnalyzingSession>> createNewSession = (
			PatternProcessingParam param) -> {
		Optional<ExpressionAnalyzingSession> session = Optional.empty();

		processManager.replaceAllProcesses();

		while (processManager.allProcessesIDLE()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		try {
			session = Optional.of(new ExpressionAnalyzingSession(param, alloyProcessingTime));
			analyzingSessions.put(param.getAnalyzingSessionID().get(), session.get());
			// for the sake of reporting
			liveness.tobeProcessed++;
		} catch (Exception e) {
			logger.severe(Utils.threadName() + "A seesion cannot be created for: " + param);
			e.printStackTrace();
			throw new RuntimeException();
		}
		return session;
	};

	protected Consumer<PatternResponseMessage> sessionDone = (PatternResponseMessage message) -> {
		liveness.tobeProcessed--;
		liveness.processed++;
	};

	protected Consumer<RemoteProcess> processIsSetup = (RemoteProcess process) -> {
		processManager.changeStatusToIDLE(process);
		if (processManager.allProcessesIDLE())
			inputInterface.sendMessage(new PatternSetupDoneMesssage(inputInterface.getHostProcess()));
	};

	protected Consumer<RemoteProcess> processIsReady = (RemoteProcess process) -> {
		distributerInterface.sendMessage(new AlloySetupMessage(distributerInterface.getHostProcess(),
				dependentFiles.stream().map(f -> f.load()).collect(Collectors.toList())), process);
		processManager.changeStatusToSETUP(process);
	};

	protected Consumer<List<LazyFile>> copyDependecyFiles = (List<LazyFile> files) -> {
		files.stream().forEach(file -> {
			file.unload(tmpLocalDirectory);
			// Only the file name is stored.
			dependentFiles.add(new LazyFile(tmpLocalDirectory, file.getName()));
		});
		processManager.addAllProcesses();
	};

	/**
	 * Retrieve a session from the cached sessions given the session ID.
	 */
	protected Function<AlloyProcessingParam, Optional<ExpressionAnalyzingSession>> getSession = (
			AlloyProcessingParam param) -> {
		return Optional.ofNullable(analyzingSessions.get(param.getAnalyzingSessionID().get()));
	};

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void initiate() {
		inputInterface = new ServerSocketInterface(this.localSocket, this.remoteSocket);

		inputInterface.MessageReceived.addListener(new MessageEventListener<MessageReceivedEventArgs>() {
			@Override
			public void actionOn(RequestMessage requestMessage, MessageReceivedEventArgs messageArgs) {
				final Map<String, Object> context = new HashMap<>();
				// it is expected to see a request message. The request creates
				// a new session and puts it in the analyzingSessions
				context.put("createNewSession", createNewSession);
				try {
					requestMessage.onAction(context);
				} catch (InvalidParameterException e) {
					logger.severe(Utils.threadName() + "request cannot be processed:\n" + e.getStackTrace());
				}
			}
		});

		inputInterface.MessageReceived.addListener(new MessageEventListener<MessageReceivedEventArgs>() {
			@Override
			public void actionOn(SetupMessage setupMessage, MessageReceivedEventArgs messageArgs) {
				final Map<String, Object> context = new HashMap<>();
				context.put("copyDependecyFiles", copyDependecyFiles);
				try {
					setupMessage.onAction(context);
				} catch (InvalidParameterException e) {
					logger.severe(Utils.threadName() + "request cannot be processed:\n" + e.getStackTrace());
				}
			}
		});

		inputInterface.MessageSent.addListener(new MessageEventListener<MessageSentEventArgs>() {
			@Override
			public void actionOn(ResponseMessage responseMessage, MessageSentEventArgs messageArgs) {
				final Map<String, Object> context = new HashMap<>();
				context.put("sessionDone", sessionDone);
				super.actionOn(responseMessage, messageArgs);
				try {
				    responseMessage.onAction(context);
				} catch (InvalidParameterException e) {
					logger.severe(Utils.threadName() + "reponse cannot be processed:\n" + e.getStackTrace());
				}
			}
		});

		// interface for distributing tasks among alloy executers.
		distributerInterface = new ServerSocketInterface(this.distributorSocket);

		// Any request that is sent should be registered to be monitored and
		// recorded to prevent duplication
		distributerInterface.MessageSent.addListener(new MessageEventListener<MessageSentEventArgs>() {
			@Override
			public void actionOn(RequestMessage requestMessage, MessageSentEventArgs event) {
				AlloyRequestMessage alloyRequestMessage = (AlloyRequestMessage) requestMessage;
				AlloyProcessingParam alloyProcessingParam = (AlloyProcessingParam) alloyRequestMessage
						.getProcessingParam();
				String predName = alloyProcessingParam.getAlloyCoder().get().getPredName();
				UUID sessionID = alloyProcessingParam.getAnalyzingSessionID().get();
				analyzingSessions.get(sessionID).addGeneratedProperties(predName);

				// register the message in the monitor
				monitor.addMessage(event.getRemoteProcess(), requestMessage.getProcessingParam());
			}
		});

		// continue the session
		distributerInterface.MessageReceived.addListener(new MessageEventListener<MessageReceivedEventArgs>() {
			@Override
			public void actionOn(ResponseMessage responseMessage, MessageReceivedEventArgs event) {
				// monitor has to process a result.
				// System.out.println("resonse
				// dm->"+((AlloyProcessingParam)responseMessage.getResult().getParam()).getAlloyCoder().get().srcName());
				monitor.processResponded(responseMessage.getResult(), responseMessage.process);
				// System.out.println("resonse after
				// dm->"+((AlloyProcessingParam)responseMessage.getResult().getParam()).getAlloyCoder().get().srcName());
				final Map<String, Object> context = new HashMap<>();
				context.put("getSession", getSession);
				try {
					responseMessage.onAction(context);
				} catch (InvalidParameterException e) {
					logger.severe(Utils.threadName() + "response cannot be processed:\n" + e.getStackTrace());
				}
			}

			@Override
			public void actionOn(LivenessMessage livenessMessage, MessageReceivedEventArgs event) {
				final Map<String, Object> context = new HashMap<>();
				context.put("RemoteProcessLogger", processManager);
				try {
					System.out.println("livenessMessage-E--->" + livenessMessage);
					livenessMessage.onAction(context);
				} catch (InvalidParameterException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void actionOn(ReadyMessage readyMessage, MessageReceivedEventArgs event) {
				final Map<String, Object> context = new HashMap<>();
				context.put("processIsReady", processIsReady);
				try {
					readyMessage.onAction(context);
				} catch (InvalidParameterException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void actionOn(DoneMessage doneMessage, MessageReceivedEventArgs messageArgs) {
				final Map<String, Object> context = new HashMap<>();
				context.put("processIsSetup", processIsSetup);
				try {
					doneMessage.onAction(context);
				} catch (InvalidParameterException e) {
					e.printStackTrace();
				}
			}
		});

		// A remote process was killed itself
		distributerInterface.MessageReceived.addListener(new MessageEventListener<MessageReceivedEventArgs>() {
			@Override
			public void actionOn(DiedMessage diedMessage, MessageReceivedEventArgs event) {
				final Map<String, Object> context = new HashMap<>();
				context.put("RemoteProcessLogger", processManager);
				try {
					diedMessage.onAction(context);
				} catch (InvalidParameterException e) {
					e.printStackTrace();
				}
			}
		});

		// Queue that are shared between feeder and generator
		feedingQueue = new Queue<>(20);
		// Queue that is shared between monitor and feeder
		backlogFeedingQueue = new Queue<>(100);

		processManager = new RemoteProcessManager(distributerInterface.getHostProcess().address, AlloyRunner.class,
				ProccessNumber);
		feeder = new Feeder<AlloyProcessingParam>(processManager, distributerInterface, feedingQueue,
				backlogFeedingQueue) {
			@Override
			protected RequestMessage createRequestMessage(RemoteProcess process, AlloyProcessingParam param) {
				return new AlloyRequestMessage(process, param);
			}
		};

		// liveness messages are sent to the initiator
		liveness = new ReportLiveness<PatternLivenessMessage>(localSocket, remoteSocket, 0, 0, livenessInterval,
				maxLivenessFailed, inputInterface) {
			@Override
			protected PatternLivenessMessage createLivenessMessage() {
				return new PatternLivenessMessage(super.localProcess, super.processed, super.tobeProcessed);
			}
		};

		monitor = new RemoteProcessMonitor((Publisher) feedingQueue, (Publisher) backlogFeedingQueue, processManager);
		localThreadsMonitor = new ThreadMonitor(threadMonitoringInterval, 0);

		reporterThread = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				final StringBuilder sb = new StringBuilder();
				while (true) {
					try {
						Thread.sleep(reportInterval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					sb.append(Utils.threadName() + "Runner is alive....\n");

					for (ThreadToBeMonitored t : monitoredThreads) {
						sb.append(t.getStatus()).append("\n");
					}
					// System.out.println(sb);
					logger.info(sb.toString());
					sb.delete(0, sb.length() - 1);

					Thread.currentThread().yield();
					System.gc();
				}
			}
		});
	}

	public void start() {
		inputInterface.startThread();
		distributerInterface.startThread();
		monitor.startThread();
		localThreadsMonitor.startMonitoring();
		feeder.startThread();
		liveness.startThread();
		reporterThread.start();

		// Everything should be ready now, so the booter should be notified.
		// send a readyness message
		inputInterface.sendMessage(new PatternReadyMessage(inputInterface.getHostProcess()));

	}

	public static void main(String[] args) throws Exception {

		Pair<InetSocketAddress, InetSocketAddress> ports = extractPortsfromCommand(args);

		System.out.println("local::" + ports.a);
		System.out.println("remoteSocket::" + ports.b);
		ExpressionAnalyzerRunner runner = new ExpressionAnalyzerRunner(ports.a, ports.b);
		runner.start();
		// create(ports.a, ports.b).start();

	}

}
