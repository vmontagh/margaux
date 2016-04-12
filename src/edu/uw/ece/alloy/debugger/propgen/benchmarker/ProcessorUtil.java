package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;

public class ProcessorUtil {

	protected final static Logger logger = Logger.getLogger(
			ProcessorUtil.class.getName() + "--" + Thread.currentThread().getName());;

	public final static int MaxPortNumber = Integer
			.valueOf(Configuration.getProp("max_port"));
	public final static int MinPortNumber = Integer
			.valueOf(Configuration.getProp("min_port"));
	public final static int MaxTryPort = 10000000;

	static int lastFoundPort = MinPortNumber;

	public ProcessorUtil() {
		// TODO Auto-generated constructor stub
	}

	public static InetSocketAddress findEmptyLocalSocket() {
		try {
			return findEmptyLocalSocket(
					InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()));
		} catch (UnknownHostException e) {
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
					+ "The Local IP address cannot be found ", e);
			e.printStackTrace();
			throw new RuntimeException("The Local IP address cannot be found " + e);
		}
	}

	public static boolean available(int port) {
		if (port < MinPortNumber || port > MaxPortNumber) {
			throw new IllegalArgumentException("Invalid start port: " + port);
		}

		ServerSocket ss = null;
		DatagramSocket ds = null;
		AsynchronousServerSocketChannel channel = null;
		try {
			channel = AsynchronousServerSocketChannel
					.open().bind(new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(),port));
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
		} finally {
			if (channel != null){
				try {
					channel.close();
				} catch (IOException e) {
					/* should not be thrown */
				}
			}
			if (ds != null) {
				ds.close();
			}

			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					/* should not be thrown */
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
	public synchronized static InetSocketAddress findEmptyLocalSocket(
			final InetAddress localAddress) {
		int port = lastFoundPort;
		int tmpPort = lastFoundPort - MinPortNumber + 1;

		int findPortTriesMax = 1;

		while (++findPortTriesMax < MaxTryPort) {
			tmpPort = (tmpPort + 2) % (MaxPortNumber
					- MinPortNumber);/*
													  * The range is an odd number so the second round it
													  * iterates the other sent of numbers.
													  */
			int actualport = tmpPort + MinPortNumber;

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

	public static void main(String... args) throws UnknownHostException {
		InetSocketAddress s = findEmptyLocalSocket();
		System.out.println(s);
		System.out.println(InetAddress.getLocalHost().getHostAddress());
		try {
			new ServerSocket(s.getPort());
			Thread.sleep(10 * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
