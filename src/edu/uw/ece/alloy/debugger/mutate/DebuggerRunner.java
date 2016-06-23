package edu.uw.ece.alloy.debugger.mutate;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.debugger.infrastructure.Runner;
import edu.uw.ece.alloy.debugger.onborder.ExampleFinderByAlloy;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ExpressionAnalyzerRunner;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcessManager;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.DoneMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.InvalidParameterException;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.LivenessMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ReadyMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy.AlloySetupMessage;
import edu.uw.ece.alloy.util.LazyFile;
import edu.uw.ece.alloy.util.ServerSocketInterface;
import edu.uw.ece.alloy.util.events.MessageEventListener;
import edu.uw.ece.alloy.util.events.MessageReceivedEventArgs;
import edu.uw.ece.hola.agent.OnBorderAnalyzerRunner;

/**
 * @author vajih
 *
 */
public class DebuggerRunner extends Runner {

	protected final static Logger logger = Logger
			.getLogger(DebuggerRunner.class.getName() + "--" + Thread.currentThread().getName());

	final public static File TmpDirectoryRoot = new File(Configuration.getProp("temporary_directory"));
	final static int AnalyzerProccessNumber = Integer.parseInt(Configuration.getProp("analyzer_processes_number"));
	// TODO(Fikayo): The number of example finder runner and its agents have to be
	// configurable in debugger.experiment.config. HolaExampleFinerProccessNumber is 
	// for the number of center runner. Apparently the number of agent has
	// been set to '2'. The sources are not attached to the jar file so that
	// I am not able to confirm it. Please remove such constant numbers
	// and put them in the resource file
	final static int HolaExampleFinerProccessNumber = Integer
			.parseInt(Configuration.getProp("hola_example_finder_processes_number"));

	final public static File RelationalPropModule = new File(Configuration.getProp("relational_properties_tagged"));
	final public static File TemporalPropModule = new File(Configuration.getProp("temporal_properties_tagged"));

	protected final File toBeAnalyzedCode;
	protected final File correctModel;
	protected List<File> dependentFiles;
	protected final File tmpLocalDirectory;

	/* Randomly assign a new socket */
	protected final InetSocketAddress distributorSocket;

	protected ServerSocketInterface distributerInterface;
	protected RemoteProcessManager analyzerProcessManager;

	// All the communication interface and socket to connect with
	// the Hola examplefinder.
	protected final InetSocketAddress exampleFinderSocket;
	protected ServerSocketInterface exampleFinderInterface;
	protected RemoteProcessManager exampleFinderProcessManager;

	protected Approximator approximator;
	protected Oracle oracle;
	protected ExampleFinder exampleFinder;
	protected DebuggerAlgorithm debuggerAlgorithm;
	final protected DebuggerAlgorithm debuggerAlgorithmCreator;

	protected DebuggerRunner(final File toBeAnalyzedCode, final File correctModel, List<File> dependentFiles,
			File tmpLocalDirectory, InetSocketAddress distributorSocket, InetSocketAddress exampleFinderSocket,
			DebuggerAlgorithm debuggerAlgorithmCreator) {
		this.toBeAnalyzedCode = toBeAnalyzedCode;
		this.correctModel = correctModel;
		this.distributorSocket = distributorSocket;
		this.exampleFinderSocket = exampleFinderSocket;
		this.dependentFiles = dependentFiles;
		this.tmpLocalDirectory = tmpLocalDirectory;
		this.debuggerAlgorithmCreator = debuggerAlgorithmCreator;
		initiate();
	}

	protected DebuggerRunner(final File toBeAnalyzedCode, final File correctModel, List<File> dependentFiles,
			InetSocketAddress distributorSocket, InetSocketAddress exampleFinderSocket,
			DebuggerAlgorithm debuggerAlgorithmCreator) {
		this(toBeAnalyzedCode, correctModel, Arrays.asList(RelationalPropModule, TemporalPropModule), TmpDirectoryRoot,
				distributorSocket, exampleFinderSocket, debuggerAlgorithmCreator);
	}

	protected Consumer<RemoteProcess> processIsReady = (RemoteProcess process) -> {
		distributerInterface
				.sendMessage(
						new AlloySetupMessage(distributerInterface.getHostProcess(), dependentFiles.stream()
								.map(f -> (new LazyFile(f.getAbsolutePath())).load()).collect(Collectors.toList())),
						process);
		analyzerProcessManager.changeStatusToSETUP(process);
	};

	protected Consumer<RemoteProcess> processIsSetup = (RemoteProcess process) -> {
		analyzerProcessManager.changeStatusToIDLE(process);
	};

	@Override
	protected void initiate() {

		distributerInterface = new ServerSocketInterface(distributorSocket);
		analyzerProcessManager = new RemoteProcessManager(distributorSocket, ExpressionAnalyzerRunner.class,
				AnalyzerProccessNumber);
		// Livensess or readyness message message is received from a remote
		// process.
		distributerInterface.MessageReceived.addListener(new MessageEventListener<MessageReceivedEventArgs>() {
			@Override
			public void actionOn(LivenessMessage livenessMessage, MessageReceivedEventArgs event) {
				System.out.println("livenessMessage---->" + livenessMessage);
				final Map<String, Object> context = new HashMap<>();
				context.put("RemoteProcessLogger", analyzerProcessManager);
				try {
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
					System.out.println("DebuggerRunner actionOn " + readyMessage);
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

		exampleFinderInterface = new ServerSocketInterface(exampleFinderSocket);
		exampleFinderProcessManager = new RemoteProcessManager(exampleFinderSocket, OnBorderAnalyzerRunner.class,
				HolaExampleFinerProccessNumber);
		// Livensess or readyness message message is received from a remote
		// process.
		exampleFinderInterface.MessageReceived.addListener(new MessageEventListener<MessageReceivedEventArgs>() {

			// TODO(Fikayo): OnBorerAnalyzerRunner should periodically
			// send a liveness message to DebuggerRunner.
			// Once the message is received the appropriate process manager
			// updates the process's status. See the analyzer example above
			// as an example and follow the same pattern.
			// Note: the actual function is called in the related message.
			@Override
			public void actionOn(LivenessMessage livenessMessage, MessageReceivedEventArgs event) {
				System.out.println("Hola livenessMessage---->" + livenessMessage);
			}

			// TODO(Fikayo): Implement the readyness message like the one in
			// distributerInterface.
			@Override
			public void actionOn(ReadyMessage readyMessage, MessageReceivedEventArgs event) {
			}

			// TODO(Fikayo): Implement the setup message to send the library
			// file before starting the remote process.
			// The flow between DebuggerRunner(DR) and
			// OnBorderAnalyzerRunner(OAR) is like:
			// DR boots a new OAR, OAR sends ready message, DE sends a setup
			// message, and OAR sends back a Done message.
			// Now OAR is ready to go.
			@Override
			public void actionOn(DoneMessage doneMessage, MessageReceivedEventArgs messageArgs) {
			}

		});

		approximator = new Approximator(distributerInterface, analyzerProcessManager, tmpLocalDirectory,
				toBeAnalyzedCode, dependentFiles);

		// TODO(Fikayo): Once the all tests are passed, instantiate from
		// ExampleFinderByHola
		exampleFinder = new ExampleFinderByAlloy();

		oracle = new CorrectModelOracle(correctModel);

		debuggerAlgorithm = debuggerAlgorithmCreator.createIt(toBeAnalyzedCode, tmpLocalDirectory, approximator, oracle,
				exampleFinder);

	}

	@Override
	public void start() {
		distributerInterface.startThread();
		analyzerProcessManager.addAllProcesses();

		exampleFinderInterface.startThread();
		exampleFinderProcessManager.addAllProcesses();
	}

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {

	}

}
