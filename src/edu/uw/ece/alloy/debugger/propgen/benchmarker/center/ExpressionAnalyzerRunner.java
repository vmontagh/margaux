package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.mutate.Approximator;
import edu.uw.ece.alloy.debugger.pattern.PatternsAnalyzer;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.GeneratedStorage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.AnalyzeExternalReady;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ProcessRemoteMonitor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadMonitor;

public class ExpressionAnalyzerRunner extends AnalyzerRunner {

	protected final static Logger logger = Logger
			.getLogger(ExpressionAnalyzerRunner.class.getName() + "--"
					+ Thread.currentThread().getName());

	// ThreadToBeMonitored propGenerator;
	ThreadToBeMonitored analyzerFrontLinstener;

	// Thread feederThread;
	// Thread monitorThread;
	// Thread timeoutMonitorThread;

	private static AnalyzerRunner self = null;

	private ExpressionAnalyzerRunner(final InetSocketAddress localSocket,
			final InetSocketAddress remoteSocket) {
		super(localSocket, remoteSocket);
	}

	public static AnalyzerRunner initiate(final InetSocketAddress localSocket,
			final InetSocketAddress remoteSocket) {
		if (self != null) {
			throw new RuntimeException(
					"ExpressionAnalyzerRunner cannot be initilized twice!");
		}

		self = new ExpressionAnalyzerRunner(localSocket, remoteSocket);
		return self;
	}

	public final static AnalyzerRunner getInstance() {
		if (self == null)
			throw new RuntimeException(
					"ExpressionAnalyzerRunner has to be initilized once!");
		return self;
	}

	@SuppressWarnings("unchecked")
	public void start() throws Exception {

		super.start();

		// Start the checking from the sources in the lattice
		// propGenerator = new
		// ExpressionPropertyChecker((GeneratedStorage<AlloyProcessingParam>)
		// feeder, new File(ToBeAnalyzedFilePath) );
		// property generator is starts by an asynchronous message.

		// monitoredThreads.add(propGenerator);

		analyzerFrontLinstener = new PatternsAnalyzer(this.localSocket,
				this.remoteSocket, (GeneratedStorage<AlloyProcessingParam>) feeder);

		monitoredThreads.add(analyzerFrontLinstener);

		analyzerFrontLinstener.startThread();
		this.addThreadToBeMonitored(analyzerFrontLinstener);

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

		ExpressionAnalyzerRunner.initiate(localSocket, remoteSocket).start();

		// busywait
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		final StringBuilder sb = new StringBuilder();
		while (true) {
			Thread.sleep(10000);
			sb.append(
					"[" + Thread.currentThread().getName() + "]" + "Main is alive....\n");

			for (ThreadToBeMonitored t : ExpressionAnalyzerRunner
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
