package edu.uw.ece.alloy.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;
import edu.uw.ece.alloy.util.events.Event;
import edu.uw.ece.alloy.util.events.EventArgs;
import edu.uw.ece.alloy.util.events.MessageReceivedEventArgs;
import edu.uw.ece.alloy.util.events.MessageSentEventArgs;

public class ServerSocketInterface
		implements Runnable, ThreadToBeMonitored, SendOnServerSocketInterface {

	protected final static Logger logger = Logger
			.getLogger(ServerSocketInterface.class.getName() + "--"
					+ Thread.currentThread().getName());

	protected final Thread listeningThread;
	protected final RemoteProcess hostProcess;
	protected final Optional<RemoteProcess> remoteProcess;

	public final Event<EventArgs> ListeningStarted;
	public final Event<MessageSentEventArgs> MessageSent;
	public final Event<MessageSentEventArgs> MessageAttempt;
	public final Event<MessageSentEventArgs> MessageFailed;
	public final Event<MessageReceivedEventArgs> MessageReceived;

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
		this(new RemoteProcess(hostAddress),
				Optional.ofNullable(new RemoteProcess(remoteAddress)));
	}

	public ServerSocketInterface(final InetSocketAddress hostAddress) {
		this(new RemoteProcess(hostAddress), Optional.empty());
	}

	public ServerSocketInterface(final RemoteProcess hostProcess,
			final Optional<RemoteProcess> remoteProcess) {
		this.hostProcess = hostProcess;
		this.remoteProcess = remoteProcess;
		this.listeningThread = new Thread(this);

		this.ListeningStarted = new Event<>();
		this.MessageSent = new Event<>();
		this.MessageAttempt = new Event<>();
		this.MessageFailed = new Event<>();
		this.MessageReceived = new Event<>();
	}

	public RemoteProcess getHostProcess() {
		return this.hostProcess;
	}

	public Optional<RemoteProcess> getRemoteProcess() {
		return this.remoteProcess;
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
	}

	public void sendMessage(RemoteMessage message) {
		this.sendMessage(message, this.getRemoteProcess().orElseThrow(
				() -> new RuntimeException("Remote address should be set.")));
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
			this.onMessageAttempt(eventArgs);
			message.sendMe(remoteProcess);
			this.onMessageSent(eventArgs);
		} catch (Exception e) {
			logger.log(Level.SEVERE,
					Utils.threadName() + "Failed to send command: " + message, e);
			this.onMessageFailed(eventArgs);
		}
	}

	protected final Thread getThread() {
		return this.listeningThread;
	}

	protected final void startListening() {

		AsynchronousServerSocketChannel serverSocketChannel = null;
		try {
			if (Configuration.IsInDeubbungMode) {
				logger.info(Utils.threadName()
						+ "Starting listening fornt runner for pId: " + hostProcess);
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
						logger.info(
								Utils.threadName() + "waiting for a request: " + hostProcess);
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
						
						System.out.println(ois);
						this.onMessageReceived(new MessageReceivedEventArgs(
								(RemoteMessage) ois.readObject(), new RemoteProcess(
										(InetSocketAddress) clientSocket.getRemoteAddress())));
					}

				} catch (IOException | InterruptedException | ExecutionException
						| ClassNotFoundException e) {
					logger.log(Level.SEVERE,
							Utils.threadName() + "Error while listening for request: ", e);
					System.out.println("this.hostProcess="+this.hostProcess);
					System.out.println("this.remoteProcess="+this.remoteProcess);
					e.printStackTrace();
					e.fillInStackTrace();
					e.printStackTrace();
					throw new RuntimeException(e);
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
			throw new RuntimeException(t);

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

	// protected abstract void onReceivedMessage(final RemoteMessage message);

	protected void onListeningStarted() {
		Event<EventArgs> event = this.ListeningStarted;
		if (event.hasListeners()) {
			event.invokeListeners(this, EventArgs.empty());
		}
	}

	protected void onMessageSent(MessageSentEventArgs e) {
		Event<MessageSentEventArgs> event = this.MessageSent;
		if (event.hasListeners()) {
			event.invokeListeners(this, e);
		}
	}

	protected void onMessageAttempt(MessageSentEventArgs e) {
		Event<MessageSentEventArgs> event = this.MessageAttempt;
		if (event.hasListeners()) {
			event.invokeListeners(this, e);
		}
	}

	protected void onMessageFailed(MessageSentEventArgs e) {
		Event<MessageSentEventArgs> event = this.MessageFailed;
		if (event.hasListeners()) {
			event.invokeListeners(this, e);
		}
	}

	protected void onMessageReceived(MessageReceivedEventArgs e) {
		Event<MessageReceivedEventArgs> event = this.MessageReceived;
		if (event.hasListeners()) {
			event.invokeListeners(this, e);
		}
	}

	@Override
	public int triesOnStuck() {
		return 0;
	}

	@Override
	public long isDelayed() {
		return 0;
	}

	@Override
	public void changePriority(int newPriority) {

	}

	@Override
	public String amIStuck() {
		return "NONE";
	}

	@Override
	public void actionOnStuck() {
	}
}
