package edu.uw.ece.alloy.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.IamAlive;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ProcessReady;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteCommand;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;

public abstract class ServerSocketInterfaceBase	implements Runnable, ThreadToBeMonitored {

	protected final static Logger logger = Logger.getLogger(ServerSocketInterfaceBase.class.getName() + "--" + Thread.currentThread().getName());

	public final int SelfMonitorInterval = Integer.parseInt(Configuration.getProp("self_monitor_interval"));

	protected final Thread listeningThread;
	protected final InetSocketAddress hostAddress;
	protected final InetSocketAddress remoteAddress;
	protected final BlockingQueue<RemoteCommand> queue;
	
	protected volatile AtomicInteger livenessFailed = new AtomicInteger(0);

	public ServerSocketInterfaceBase(final int hostPort, final int remotePort) {
		this(new InetSocketAddress(hostPort), new InetSocketAddress(remotePort));
	}

	public ServerSocketInterfaceBase(final String hostName, final int hostPort,
			final String remoteName, final int remotePort) {
		this(new InetSocketAddress(hostName, hostPort),
				new InetSocketAddress(remoteName, remotePort));
	}

	public ServerSocketInterfaceBase(final InetSocketAddress hostAddress,	final InetSocketAddress remoteAddress) {
		this.hostAddress = hostAddress;
		this.remoteAddress = remoteAddress;
		this.listeningThread = new Thread(this);
		this.queue = new LinkedBlockingQueue<>();
	}

	public InetSocketAddress getHostAddress() {
		return this.hostAddress;
	}

	public InetSocketAddress getRemoteAddress() {
		return this.remoteAddress;
	}

	public void startThread() {
		getThread().start();
	}
	
	@Override
	public void run() {
		startListening();
	}

	@Override
	public void cancelThread() {
		this.getThread().interrupt();
	}

	@Override
	public void actionOnNotStuck() {
		this.sendLivenessMessage();
		this.haltIfCantProceed(/* SelfMonitorRetryAttempt */ 1);
	}

	protected final Thread getThread() {
		return this.listeningThread;
	}

	private void startListening() {

		AsynchronousServerSocketChannel serverSocketChannel = null;

		try {
			if (Configuration.IsInDeubbungMode) {
				logger.info(Utils.threadName() + "Starting listening fornt runner for pId: " + hostAddress.getPort());
			}
								
			// Open socket for listening
			serverSocketChannel = AsynchronousServerSocketChannel.open().bind(hostAddress);

			this.onStartListening();

			Future<AsynchronousSocketChannel> serverFuture = null;
			AsynchronousSocketChannel clientSocket = null;
			InputStream connectionInputStream = null;
			ObjectInputStream ois = null;

			while (!Thread.currentThread().isInterrupted()) {
				try {
					
					if (Configuration.IsInDeubbungMode) {
						logger.info(Utils.threadName() + "waiting for a request: "+ hostAddress.getPort());
					}
					
					serverFuture = serverSocketChannel.accept();
					
					if (Configuration.IsInDeubbungMode) {
						logger.info(Utils.threadName() + "a message is received: " + hostAddress.getPort());
					}
					
					clientSocket = serverFuture.get();

					if ((clientSocket != null) && (clientSocket.isOpen())) {
						
						connectionInputStream = Channels.newInputStream(clientSocket);
						ois = new ObjectInputStream(connectionInputStream);
						queue.put((RemoteCommand) ois.readObject());
//						onReceivedMessage((RemoteCommand) ois.readObject());
					}
					
				} catch (IOException | InterruptedException | ExecutionException | ClassNotFoundException e) {
					logger.log(Level.SEVERE, Utils.threadName() + "Error while listening for request: ", e);
				} finally {
					
					if (ois != null) {
						try {
							ois.close();
						} catch (IOException e) {
							logger.log(Level.SEVERE, Utils.threadName()	+ "Error while closing InputOutputstream: ", e);
						}
					}
					
					if (connectionInputStream != null) {
						try {
							connectionInputStream.close();
						} catch (IOException e) {
							logger.log(Level.SEVERE, Utils.threadName() + "Error while closing Connection Inputputstream: ", e);
						}
					}
					
					if (clientSocket != null && clientSocket.isOpen()) {
						try {
							clientSocket.close();
						} catch (IOException e) {
							logger.log(Level.SEVERE, Utils.threadName() + "Error while closing Client socket: ", e);
						}
					}
				}
			}

		} catch (Throwable t) {
			logger.log(Level.SEVERE, Utils.threadName()	+ "A serious error breaks the Front Processor listener: ", t);
		} finally {
			
			if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
				try {
					serverSocketChannel.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE, Utils.threadName()	+ "Error while closing AsynchronousServerSocketChannel socket: ",	e);
				}
			}
		}

	}

	protected void onStartListening() {	}
	
	protected void sendMessage(RemoteCommand command) {
		
		if(command == null) {
			return;
		}
		
		try {
			command.sendMe(this.getRemoteAddress());
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, Utils.threadName() + "Failed to send command: " + command, e);
		}
		
	}
	
	protected void sendLivenessMessage() {

		try {

			IamAlive iamAlive = new IamAlive(this.getHostAddress(),	System.currentTimeMillis(), -1, -1);
			iamAlive.sendMe(this.getRemoteAddress());
			livenessFailed.set(0);

			if (Configuration.IsInDeubbungMode)	logger.info(Utils.threadName() + "A live message" + iamAlive);
		} catch (Exception e) {
			logger.log(Level.SEVERE, Utils.threadName() + "Failed to send a live signal. " + livenessFailed + " attempts made.",e);
			livenessFailed.incrementAndGet();
		}
	}

	protected void sendReadynessMessage() {
		
		if (Configuration.IsInDeubbungMode)
			logger.info(Utils.threadName() + "Sending a readyness message pId: "	+ hostAddress.getPort());
		
		ProcessReady command = new ProcessReady(hostAddress);
		try {
			command.sendMe(remoteAddress);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, Utils.threadName() + "Failed to send Ready signal.", e);
		}
	}

	protected void onReceivedMessage(final RemoteCommand command) {
		
		// Supposed to be a registering call;
		if (Configuration.IsInDeubbungMode)	logger.info(Utils.threadName() + "Recieved message: " + command);

	}

	protected void haltIfCantProceed(final int maxRetryAttepmpts) {

		// Recovery was not enough, the whole processes has to be shut-down
		if (livenessFailed.get() > maxRetryAttepmpts) {
			logger.severe(Utils.threadName() + livenessFailed	+ " liveness message attempts does not prceeed, So the process is exited.");
			Runtime.getRuntime().halt(0);
		}
	}

}
