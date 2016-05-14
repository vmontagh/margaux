package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.debugger.pattern.PatternsAnalyzer;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.GeneratedStorage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.AnalyzeExternalReady;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ProcessRemoteMonitor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ProcessSelfMonitor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadDelayToBeMonitored;

@Deprecated
public class ExpressionAnalyzerRunner_ extends AnalyzerRunner {

	final static int proccessNumber = Integer
			.parseInt(Configuration.getProp("processes_number"));
	final static int RemoteMonitorInterval = Integer
			.parseInt(Configuration.getProp("remote_monitor_interval"));
	final public int SelfMonitorInterval = Integer
			.parseInt(Configuration.getProp("self_monitor_interval"));
	final static String ProcessLoggerConfig = Configuration
			.getProp("process_logger_config");
	final static int SubMemory = Integer
			.parseInt(Configuration.getProp("sub_memory"));
	final static int SubStack = Integer
			.parseInt(Configuration.getProp("sub_stak"));
	final static int AlloyFeederBufferSize = Integer
			.parseInt(Configuration.getProp("alloy_feeder_buffer_size"));
	final static int AlloyFeederBackLogBufferSize = Integer
			.parseInt(Configuration.getProp("alloy_feeder_backlog_buffer_size"));
	// final public static String ToBeAnalyzedFilePath =
	// Configuration.getProp("tobe_analyzed_file_path");

	public final InetSocketAddress localSocket;
	public final InetSocketAddress remoteSocket;

	protected final static Logger logger = Logger
			.getLogger(ExpressionAnalyzerRunner_.class.getName() + "--"
					+ Thread.currentThread().getName());

	ProcessesManager manager;
	ThreadDelayToBeMonitored feeder;
	ThreadDelayToBeMonitored monitor;
	// ThreadDelayToBeMonitored propGenerator;
	ThreadDelayToBeMonitored analyzerFrontLinstener;

	List<ThreadDelayToBeMonitored> monitoredThreads = new LinkedList<>();

	ProcessSelfMonitor selfMonitor;

	// Thread feederThread;
	// Thread monitorThread;
	// Thread timeoutMonitorThread;

	private static ExpressionAnalyzerRunner_ self = null;

	private ExpressionAnalyzerRunner_(final InetSocketAddress localSocket,
			final InetSocketAddress remoteSocket) {
		this.localSocket = localSocket;
		this.remoteSocket = remoteSocket;
	}

	public static ExpressionAnalyzerRunner_ initiate(
			final InetSocketAddress localSocket,
			final InetSocketAddress remoteSocket) {
		if (self != null)
			throw new RuntimeException(
					"ExpressionAnalyzerRunner cannot be initilized twice!");
		self = new ExpressionAnalyzerRunner_(localSocket, remoteSocket);
		return self;
	}

	public final static ExpressionAnalyzerRunner_ getInstance() {
		if (self == null)
			throw new RuntimeException(
					"ExpressionAnalyzerRunner has to be initilized once!");
		return self;
	}

	@SuppressWarnings("unchecked")
	public void start() throws Exception {

		selfMonitor = new ProcessSelfMonitor(/* SelfMonitorInterval */ 1 * 1000, 0);

		manager = new ProcessesManager(proccessNumber, null, SubMemory, SubStack,
				"", ProcessLoggerConfig);

		feeder = new AlloyFeeder(manager, AlloyFeederBufferSize,
				AlloyFeederBackLogBufferSize);

		monitor = new ProcessRemoteMonitor(RemoteMonitorInterval,
				(AlloyFeeder) feeder, manager,
				manager.getProcessRemoteMonitorAddress());

		// Start the checking from the sources in the lattice
		// propGenerator = new
		// ExpressionPropertyChecker((GeneratedStorage<AlloyProcessingParam>)
		// feeder, new File(ToBeAnalyzedFilePath) );
		// property generator is starts by an asynchronous message.

		monitoredThreads.add(feeder);
		monitoredThreads.add(monitor);
		// monitoredThreads.add(propGenerator);

		monitor.startThread();

		manager.addAllProcesses();
		System.out.println("manager.processes->" + manager.processes);
		// manager.activateAllProesses();

		feeder.startThread();
		feeder.changePriority(Thread.MAX_PRIORITY);

		analyzerFrontLinstener = new PatternsAnalyzer(this.localSocket,
				this.remoteSocket, (GeneratedStorage<AlloyProcessingParam>) feeder);

		monitoredThreads.add(analyzerFrontLinstener);

		analyzerFrontLinstener.startThread();
		selfMonitor.addThreadToBeMonitored(analyzerFrontLinstener);

		selfMonitor.startThreads();

		// Everything looks to be set. So send a ready message to the
		// remote listener.
		if (remoteSocket != null) {
			(new AnalyzeExternalReady()).sendMe(remoteSocket);
		}

	}

	public static void main(String[] args) throws Exception {

		if (args.length < 4)
			throw new RuntimeException("Enter the port number");

		if (args.length > 4)
			throw new RuntimeException(
					"Inappropriate number of inputs. Only enter the remote port number as an interger.");

		int localPort;
		int remotePort;
		InetAddress localIP;
		InetAddress remoteIP;

		try {
			localPort = Integer.parseInt(args[0]);
			localIP = InetAddress.getByName(args[1]);
			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "] "
						+ "The port is assigned to this process: " + localPort
						+ " and the IP is: " + localIP);

			remotePort = Integer.parseInt(args[2]);
			remoteIP = InetAddress.getByName(args[3]);
			;
			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "] "
						+ "The remote port is: " + remotePort + " and the IP is: "
						+ remoteIP);

		} catch (NumberFormatException nfe) {
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "]"
					+ "The passed port is not acceptable: ", nfe.getMessage());
			throw new RuntimeException("The port number is not an integer: " + nfe);
		} catch (UnknownHostException uhe) {
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "]"
					+ "The passed IP is not acceptable: ", uhe.getMessage());
			throw new RuntimeException("The IP address is not acceptable: " + uhe);
		}

		final InetSocketAddress localSocket = new InetSocketAddress(localIP,
				localPort);
		final InetSocketAddress remoteSocket = new InetSocketAddress(remoteIP,
				remotePort);

		System.out.println("local::" + localSocket);
		System.out.println("remoteSocket::" + remoteSocket);

		ExpressionAnalyzerRunner_.initiate(localSocket, remoteSocket).start();

		// busywait
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		final StringBuilder sb = new StringBuilder();
		while (true) {
			Thread.sleep(10000);
			sb.append(
					"[" + Thread.currentThread().getName() + "]" + "Main is alive....\n");

			for (ThreadDelayToBeMonitored t : ExpressionAnalyzerRunner_
					.getInstance().monitoredThreads) {
				System.out.println("t->" + t);
				System.out.println("t.getStatus()" + t.getStatus());
				sb.append(t.getStatus()).append("\n");
			}

			// ExpressionAnalyzerRunner.getInstance().monitoredThreads.stream()
			// .forEach(m -> sb.append(m != null ? m.getStatus(): "").append("\n"));

			System.out.println(sb);
			// System.out.println("Approximation--------->"
			// + Approximator.getInstance().getDirectImpliedApproximation());
			logger.warning(sb.toString());
			sb.delete(0, sb.length() - 1);

			Thread.currentThread().yield();
			System.gc();
		}
	}

}
