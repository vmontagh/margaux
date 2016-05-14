/**
 * 
 */
package edu.uw.ece.alloy.debugger.infrastructure;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Pair;
import edu.uw.ece.alloy.Configuration;

/**
 * @author vajih
 *
 */
public abstract class Runner {

	protected final static Logger logger = Logger.getLogger(
			Runner.class.getName() + "--" + Thread.currentThread().getName());

	/**
	 * getting the passed arguments from command, a pair of socket addresses will
	 * be returned. pair.a is the local address and pair.b is remote address
	 * 
	 * @param args
	 * @return
	 */
	public static Pair<InetSocketAddress, InetSocketAddress> extractPortsfromCommand(
			String... args) {
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

		return new Pair<InetSocketAddress, InetSocketAddress>(localSocket,
				remoteSocket);
	}

	protected abstract void initiate();

	public abstract void start();
}
