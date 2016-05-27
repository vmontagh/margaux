package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.util.Utils;

public class ProcessorUtil {

	protected final static Logger logger = Logger.getLogger(
			ProcessorUtil.class.getName() + "--" + Thread.currentThread().getName());;

	public static class EmptySocketFinder {

		public final static int MaxPortNumber = Integer
				.valueOf(Configuration.getProp("max_port"));
		public final static int MinPortNumberOffset = Integer
				.valueOf(Configuration.getProp("min_port_offset"));
		public final static int MinPortNumber = Integer
				.valueOf(Configuration.getProp("min_port"));
		public final static int MaxTryPort = 10000000;

		protected final int minPort;
		protected final int maxPort;

		protected int lastFoundPort = Integer.MIN_VALUE;

		protected EmptySocketFinder(int startingPort, int minPortOffset,
				int maxPort) {
			this.minPort = startingPort
					+ (startingPort % minPortOffset) * minPortOffset;
			this.maxPort = maxPort;
			this.lastFoundPort = this.minPort;
		}

		public boolean available(int port) {
			if (port < minPort || port > maxPort) {
				throw new IllegalArgumentException("Invalid start port: " + port);
			}

			AsynchronousServerSocketChannel channel = null;
			try {
				channel = AsynchronousServerSocketChannel.open().bind(
						new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(),
								port));
				return true;
			} catch (IOException e) {
				logger.info(
						Utils.threadName() + "Port cannot be used: " + e.getMessage());
			} finally {
				if (channel != null) {
					try {
						channel.close();
					} catch (IOException e) {
						/* should not be thrown */
						logger.severe(Utils.threadName() + "Open port '" + port
								+ "' cannot be closed: " + e.getMessage());
					}
				}
			}

			return false;
		}

		/**
		 * This function may return already used ports. So the history in
		 * sentMessagesCounter should be cleaned.
		 * 
		 * @return
		 */
		public synchronized InetSocketAddress findEmptyLocalSocket(
				final InetAddress localAddress) {
			int port = lastFoundPort;
			int tmpPort = lastFoundPort - minPort + 1;

			int findPortTriesMax = 1;

			while (++findPortTriesMax < MaxTryPort) {
				tmpPort = (tmpPort + 1) % (maxPort
						- minPort);/*
											  * The range is an odd number so the second round it
											  * iterates the other sent of numbers.
											  */
				int actualport = tmpPort + minPort;

				if (available(actualport)) {
					port = actualport;
					break;
				}
			}

			if (port == lastFoundPort) {
				throw new RuntimeException("No port available");
			}

			lastFoundPort = port;
			return new InetSocketAddress(localAddress, lastFoundPort);
		}
	}

	protected static EmptySocketFinder socketFinder = null;

	/**
	 * Only the first initialization is considered. The rest will be ignored.
	 * 
	 * @param initialPort
	 * @return
	 */
	public static synchronized InetSocketAddress findEmptyLocalSocket(
			int initialPort) {

		if (null == socketFinder) {
			socketFinder = new EmptySocketFinder(initialPort,
					EmptySocketFinder.MinPortNumberOffset,
					EmptySocketFinder.MaxPortNumber);
		}

		try {
			return socketFinder.findEmptyLocalSocket(
					InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()));
		} catch (UnknownHostException e) {
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
					+ "The Local IP address cannot be found ", e);
			e.printStackTrace();
			throw new RuntimeException("The Local IP address cannot be found " + e);
		}
	}

	public static InetSocketAddress findEmptyLocalSocket() {
		return findEmptyLocalSocket(EmptySocketFinder.MinPortNumber);
	}

	/**
	 * Create a process on another JVM.
	 * 
	 * @param SubMemory
	 * @param SubStack
	 * @param ProcessLoggerConfig
	 * @param remoteSocket
	 *          The port that clazz on the new JVM is listening to.
	 * @param localSocket
	 *          The port that current JVM is listening to.
	 * @param clazz
	 *          The class contains main on another JVM
	 * @return
	 * @throws IOException
	 */
	public static Process createNewJVM(int SubMemory, int SubStack,
			String ProcessLoggerConfig, InetSocketAddress remoteSocket,
			InetSocketAddress localSocket, Class<?> clazz) throws IOException {

		final String java = "java";
		final String debug = Boolean.parseBoolean(System.getProperty("debug"))
				? "yes" : "no";

		try {
			ProcessBuilder pb = new ProcessBuilder(java, "-Xmx" + SubMemory + "m",
					"-Xss" + SubStack + "k", "-Ddebug=" + debug,
					"-Djava.util.logging.config.file=" + ProcessLoggerConfig, "-cp",
					System.getProperty("java.class.path"), clazz.getName(),
					"" + remoteSocket.getPort(),
					"" + remoteSocket.getAddress().getHostAddress(),
					"" + localSocket.getPort(),
					"" + localSocket.getAddress().getHostAddress());

			pb.redirectOutput(Redirect.INHERIT);
			pb.redirectError(Redirect.INHERIT);
			return pb.start();

		} catch (IOException e) {
			logger
					.log(Level.SEVERE,
							"[" + Thread.currentThread().getName() + "]"
									+ "Not able to create a new process on port: " + remoteSocket,
							e);
			throw e;
		}
	}

}
