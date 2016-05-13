package edu.uw.ece.alloy.debugger.mutate;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.infrastructure.Runner;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ExpressionAnalyzerRunner;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcessManager;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.InvalidParameterException;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.LivenessMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ReadyMessage;
import edu.uw.ece.alloy.util.ServerSocketInterface;
import edu.uw.ece.alloy.util.events.MessageEventListener;
import edu.uw.ece.alloy.util.events.MessageReceivedEventArgs;

/**
 * @author vajih
 *
 */
public class DebuggerRunner extends Runner {

	protected final static Logger logger = Logger.getLogger(
			DebuggerRunner.class.getName() + "--" + Thread.currentThread().getName());

	final public static File TmpDirectoryRoot = new File(
			Configuration.getProp("temporary_directory"));

	protected final File toBeAnalyzedCode;
	protected final List<File> dependentFiles;
	protected final File tmpLocalDirectory;

	/* Randomly assign a new socket */
	protected final InetSocketAddress distributorSocket;

	protected ServerSocketInterface distributerInterface;
	protected RemoteProcessManager analyzerProcessManager;
	protected Approximator approximator;
	protected DebuggerAlgorithm debuggerAlgorithm;

	protected DebuggerRunner(final File toBeAnalyzedCode,
			List<File> dependentFiles, File tmpLocalDirectory,
			InetSocketAddress distributorSocket) {
		this.toBeAnalyzedCode = toBeAnalyzedCode;
		this.distributorSocket = distributorSocket;
		this.dependentFiles = dependentFiles;
		this.tmpLocalDirectory = tmpLocalDirectory;
		initiate();
	}

	protected DebuggerRunner(final File toBeAnalyzedCode,
			List<File> dependentFiles, InetSocketAddress distributorSocket) {
		this(toBeAnalyzedCode, dependentFiles, TmpDirectoryRoot, distributorSocket);
	}

	@Override
	protected void initiate() {

		distributerInterface = new ServerSocketInterface(distributorSocket);
		analyzerProcessManager = new RemoteProcessManager(distributorSocket,
				ExpressionAnalyzerRunner.class);
		// Livensess or readyness message message is received from a remote process.
		distributerInterface.MessageReceived
				.addListener(new MessageEventListener<MessageReceivedEventArgs>() {
					@Override
					public void actionOn(LivenessMessage livenessMessage,
							MessageReceivedEventArgs event) {
						System.out.println("livenessMessage---->"+livenessMessage);
						final Map<String, Object> context = new HashMap<>();
						context.put("RemoteProcessLogger", analyzerProcessManager);
						try {
							livenessMessage.onAction(context);
						} catch (InvalidParameterException e) {
							e.printStackTrace();
						}
					}

					@Override
					public void actionOn(ReadyMessage readyMessage,
							MessageReceivedEventArgs event) {
						final Map<String, Object> context = new HashMap<>();
						context.put("RemoteProcessLogger", analyzerProcessManager);
						try {
							System.out.println("DebuggerRunner actionOn " + readyMessage);
							readyMessage.onAction(context);
						} catch (InvalidParameterException e) {
							e.printStackTrace();
						}
					}
				});

		approximator = new Approximator(distributerInterface,
				analyzerProcessManager, tmpLocalDirectory, toBeAnalyzedCode,
				dependentFiles);

		debuggerAlgorithm = new DebuggerAlgorithm(toBeAnalyzedCode, approximator) {
		};

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

		String AlloyTmpTestPath = "tmp/testing.als";

		Debugger deg;
		try {
			deg = new Debugger(AlloyTmpTestPath);
			deg.bootRemoteAnalyzer();
			// Thread.sleep(1000);
			deg.analyzeImpliedPatterns();
		} catch (Err | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
