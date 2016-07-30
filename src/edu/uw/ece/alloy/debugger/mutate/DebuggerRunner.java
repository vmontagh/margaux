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

/**
 * @author vajih
 *
 */
public class DebuggerRunner extends Runner {

	protected final static Logger logger = Logger
			.getLogger(DebuggerRunner.class.getName() + "--" + Thread.currentThread().getName());

	final public static File TmpDirectoryRoot = new File(Configuration.getProp("temporary_directory"));
	final static int ProccessNumber = Integer.parseInt(Configuration.getProp("analyzer_processes_number"));

	final public static File RelationalPropModule = new File(Configuration.getProp("relational_properties_tagged"));
	final public static File TemporalPropModule = new File(Configuration.getProp("temporal_properties_tagged"));

	protected final File toBeAnalyzedCode;
	protected final File correctModel;
	protected List<File> dependentFiles;
	protected final File tmpLocalDirectory;
	protected final File reviewedExamples, newReviewedExamples, skipTerms;

	/* Randomly assign a new socket */
	protected final InetSocketAddress distributorSocket;

	protected ServerSocketInterface distributerInterface;
	protected RemoteProcessManager analyzerProcessManager;
	protected Approximator approximator;
	protected Oracle oracle;
	protected ExampleFinder exampleFinder;
	protected DebuggerAlgorithm debuggerAlgorithm;
	final protected DebuggerAlgorithm debuggerAlgorithmCreator;

	protected DebuggerRunner(final File toBeAnalyzedCode, final File correctModel, List<File> dependentFiles,
			File tmpLocalDirectory, InetSocketAddress distributorSocket, DebuggerAlgorithm debuggerAlgorithmCreator, final File reviewedExamples,
			final File newReviewedExamples, final File skipTerms) {
		this.toBeAnalyzedCode = toBeAnalyzedCode;
		this.correctModel = correctModel;
		this.distributorSocket = distributorSocket;
		this.dependentFiles = dependentFiles;
		this.tmpLocalDirectory = tmpLocalDirectory;
		this.debuggerAlgorithmCreator = debuggerAlgorithmCreator;
		this.reviewedExamples = reviewedExamples;
		this.newReviewedExamples = newReviewedExamples;
		this.skipTerms = skipTerms;
		initiate();
	}

	protected DebuggerRunner(final File toBeAnalyzedCode, final File correctModel,
			InetSocketAddress distributorSocket, DebuggerAlgorithm debuggerAlgorithmCreator, final File reviewedExamples,
			final File newReviewedExamples, final File skipTerms) {
		this(toBeAnalyzedCode, correctModel, Arrays.asList(RelationalPropModule, TemporalPropModule), TmpDirectoryRoot,
				distributorSocket, debuggerAlgorithmCreator, reviewedExamples, newReviewedExamples, skipTerms);
	}
	
	protected DebuggerRunner(final File toBeAnalyzedCode, final File correctModel,
			InetSocketAddress distributorSocket, DebuggerAlgorithm debuggerAlgorithmCreator,
			final File newReviewedExamples) {
		this(toBeAnalyzedCode, correctModel, Arrays.asList(RelationalPropModule, TemporalPropModule), TmpDirectoryRoot,
				distributorSocket, debuggerAlgorithmCreator, new File("!~@#"), newReviewedExamples, new File("!~@#"));
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
				ProccessNumber);
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

		approximator = new Approximator(distributerInterface, analyzerProcessManager, tmpLocalDirectory,
				toBeAnalyzedCode, dependentFiles);

		exampleFinder = new ExampleFinderByAlloy();
		oracle = new CorrectModelOracle(correctModel);

		debuggerAlgorithm = debuggerAlgorithmCreator.createIt(toBeAnalyzedCode, tmpLocalDirectory, approximator, oracle,
				exampleFinder, reviewedExamples,newReviewedExamples, skipTerms);

	}

	@Override
	public void start() {
		distributerInterface.startThread();
		analyzerProcessManager.addAllProcesses();
	}

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {

	}

}
