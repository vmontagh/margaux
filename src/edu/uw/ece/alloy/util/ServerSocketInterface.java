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
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.AnalyzeExternalLiveness;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ReadyMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;
import edu.uw.ece.alloy.util.events.*;

public abstract class ServerSocketInterface
		implements Runnable, ThreadToBeMonitored {

	protected final static Logger logger = Logger
			.getLogger(ServerSocketInterface.class.getName() + "--"
					+ Thread.currentThread().getName());

	protected final Thread listeningThread;
	protected final RemoteProcess hostProcess;
	protected final RemoteProcess remoteProcess;
	protected final BlockingQueue<RemoteMessage> queue;

	public final Event<EventArgs> ListeningStarted;
	public final Event<MessageSentEventArgs> MessageSent;
	public final Event<MessageSentEventArgs> MessageAttempt;
	public final Event<MessageSentEventArgs> MessageFailed;
	public final Event<MessageReceivedEventArgs> MessageReceived;

	protected volatile AtomicInteger livenessFailed = new AtomicInteger(0);

	private int maxRetryAttempts;

	public ServerSocketInterface(final int hostPort, final int remotePort) {
		this(new InetSocketAddress(hostPort), new InetSocketAddress(remotePort));
	}

	public ServerSocketInterface(final String hostName, final int hostPort,
			final String remoteName, final int remotePort) {
		this(new InetSocketAddress(hostName, hostPort),
				new InetSocketAddress(remoteName, remotePort));
	}

	public ServerSocketInterface(final InetSocketAddress hostAddress,
			final InetSocketAddress remoteAddress) {
		this(new RemoteProcess(hostAddress), new RemoteProcess(remoteAddress));
	}

	public ServerSocketInterface(final RemoteProcess hostProcess,
			final RemoteProcess remoteProcess) {
		this.hostProcess = hostProcess;
		this.remoteProcess = remoteProcess;
		this.listeningThread = new Thread(this);
		this.queue = new LinkedBlockingQueue<>();
		this.maxRetryAttempts = 1;

		this.ListeningStarted = new Event<>();
		this.MessageSent = new Event<>();
		this.MessageAttempt = new Event<>();
		this.MessageFailed = new Event<>();
		this.MessageReceived = new Event<>();
	}

	public RemoteProcess getHostProcess() {
		return this.hostProcess;
	}

	public RemoteProcess getRemoteProcess() {
		return this.remoteProcess;
	}

	public int getLivenessFailed() {

		return this.livenessFailed.get();
	}

	public void setMaxRetryAttempts(int maxRetryAttempts) {

		this.maxRetryAttempts = maxRetryAttempts;
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
		this.haltIfCantProceed(this.maxRetryAttempts);
	}

	public void haltIfCantProceed(final int maxRetryAttepmpts) {

		// Recovery was not enough, the whole processes has to be shut-down
		if (livenessFailed.get() > maxRetryAttepmpts) {
			logger.severe(Utils.threadName() + livenessFailed
					+ " liveness message attempts does not prceeed, So the process is exited.");
			Runtime.getRuntime().halt(0);
		}
	}

	public void sendMessage(RemoteMessage message) {
		this.sendMessage(message, this.getRemoteProcess());
	}

	/**
	 * A portal to send a message. Appropriate events are called before or after.
	 * 
	 * @param message
	 * @param remoteProcess
	 */
	public void sendMessage(RemoteMessage message, RemoteProcess remoteProcess) {

		if (message == null) {
			throw new RuntimeException("A null message cannot be sent");
		}

		if (remoteProcess == null) {
			throw new RuntimeException("The reciever message cannot be null");
		}

		MessageSentEventArgs eventArgs = new MessageSentEventArgs(message,
				remoteProcess);
		try {
			this.onCommandAttempt(eventArgs);
			message.sendMe(remoteProcess);
			this.onCommandSent(eventArgs);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE,
					Utils.threadName() + "Failed to send command: " + message, e);
			this.onCommandFailed(eventArgs);
		}
	}

	public void sendLivenessMessage() {
		this.sendLivenessMessage(-1, -1);
	}

	public void sendLivenessMessage(int processed, int toBeProcessed) {

		try {
			AnalyzeExternalLiveness iamAlive = new AnalyzeExternalLiveness(
					this.getHostProcess(), 0, 0);
			iamAlive.sendMe(this.getRemoteProcess());
			livenessFailed.set(0);
			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "]"
						+ "A live message" + iamAlive);
		} catch (Exception e) {
			logger.log(Level.SEVERE,
					"[" + Thread.currentThread().getName() + "]"
							+ "Failed to send a live signal this is " + livenessFailed
							+ " attempt",
					e);
			livenessFailed.incrementAndGet();
		}

	}

	public void sendReadynessMessage() {
		if (Configuration.IsInDeubbungMode)
			logger.info(Utils.threadName() + "Sending a readyness message pId: "
					+ hostProcess);

		ReadyMessage message = new ReadyMessage(hostProcess);
		try {
			message.sendMe(remoteProcess);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE,
					Utils.threadName() + "Failed to send Ready signal.", e);
		}
	}

	protected final Thread getThread() {
		return this.listeningThread;
	}

	protected final void startListening() {

		AsynchronousServerSocketChannel serverSocketChannel = null;
		try {
			if (Configuration.IsInDeubbungMode) {
				logger.info(
						Utils.threadName() + "Starting listening fornt runner for pId: "
								+ hostProcess);
			}

			// Open socket for listening
			serverSocketChannel = AsynchronousServerSocketChannel.open()
					.bind(hostProcess.getAddress());

			this.onListeningStarted();

			Future<AsynchronousSocketChannel> serverFuture = null;
			AsynchronousSocketChannel clientSocket = null;
			InputStream connectionInputStream = null;
			ObjectInputStream ois = null;

			while (!Thread.currentThread().isInterrupted()) {
				try {
					if (Configuration.IsInDeubbungMode) {
						logger.info(Utils.threadName() + "waiting for a request: "
								+ hostProcess);
					}

					serverFuture = serverSocketChannel.accept();
					if (Configuration.IsInDeubbungMode) {
						logger.info(Utils.threadName() + "a message is received on: "
								+ hostProcess);
					}

					clientSocket = serverFuture.get();
					if ((clientSocket != null) && (clientSocket.isOpen())) {
						connectionInputStream = Channels.newInputStream(clientSocket);
						ois = new ObjectInputStream(connectionInputStream);
						onReceivedMessage((RemoteMessage) ois.readObject());
					}

				} catch (IOException | InterruptedException | ExecutionException
						| ClassNotFoundException e) {
					logger.log(Level.SEVERE,
							Utils.threadName() + "Error while listening for request: ", e);
				} finally {

					if (ois != null) {
						try {
							ois.close();
						} catch (IOException e) {
							logger.log(Level.SEVERE, Utils.threadName()
									+ "Error while closing InputOutputstream: ", e);
						}
					}

					if (connectionInputStream != null) {
						try {
							connectionInputStream.close();
						} catch (IOException e) {
							logger
									.log(Level.SEVERE,
											Utils.threadName()
													+ "Error while closing Connection Inputputstream: ",
											e);
						}
					}

					if (clientSocket != null && clientSocket.isOpen()) {
						try {
							clientSocket.close();
						} catch (IOException e) {
							logger.log(Level.SEVERE,
									Utils.threadName() + "Error while closing Client socket: ",
									e);
						}
					}
				}
			}

		} catch (Throwable t) {
			logger.log(Level.SEVERE, Utils.threadName()
					+ "A serious error breaks the Front Processor listener: ", t);
		} finally {

			if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
				try {
					serverSocketChannel.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE,
							Utils.threadName()
									+ "Error while closing AsynchronousServerSocketChannel socket: ",
							e);
				}
			}
		}

	}

	
	protected abstract void onReceivedMessage(final RemoteMessage message);

	protected void onListeningStarted() {
		Event<EventArgs> event = this.ListeningStarted;
		if (event.hasListeners()) {
			event.invokeListeners(this, EventArgs.empty());
		}
	}

	protected void onCommandSent(MessageSentEventArgs e) {
		Event<MessageSentEventArgs> event = this.MessageSent;
		if (event.hasListeners()) {
			event.invokeListeners(this, e);
		}
	}

	protected void onCommandAttempt(MessageSentEventArgs e) {
		Event<MessageSentEventArgs> event = this.MessageAttempt;
		if (event.hasListeners()) {
			event.invokeListeners(this, e);
		}
	}

	protected void onCommandFailed(MessageSentEventArgs e) {
		Event<MessageSentEventArgs> event = this.MessageFailed;
		if (event.hasListeners()) {
			event.invokeListeners(this, e);
		}
	}

	protected void onCommandReceived(MessageReceivedEventArgs e) {
		Event<MessageReceivedEventArgs> event = this.MessageReceived;
		if (event.hasListeners()) {
			event.invokeListeners(this, e);
		}
	}
}
