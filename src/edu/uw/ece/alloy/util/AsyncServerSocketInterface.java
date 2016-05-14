package edu.uw.ece.alloy.util;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteMessage;
import edu.uw.ece.alloy.util.events.MessageReceivedEventArgs;

//TODO(fikayo) Fix the issues or remove the class.

public class AsyncServerSocketInterface extends ServerSocketInterface {

	private final static Logger logger = Logger
			.getLogger(AsyncServerSocketInterface.class.getName() + "--"
					+ Thread.currentThread().getName());

	private Thread receivingThread;
	private MessageHandler handler;

	public AsyncServerSocketInterface(final int hostPort, final int remotePort) {
		this(new InetSocketAddress(hostPort), new InetSocketAddress(remotePort));
	}

	public AsyncServerSocketInterface(final String hostName, final int hostPort,
			final String remoteName, final int remotePort) {
		this(new InetSocketAddress(hostName, hostPort),
				new InetSocketAddress(remoteName, remotePort));
	}

	public AsyncServerSocketInterface(final InetSocketAddress hostAddress,
			final InetSocketAddress remoteAddress) {
		super(hostAddress, remoteAddress);
		this.handler = new MessageHandler(this.queue);
		this.receivingThread = new Thread(this.handler);
	}

	@Override
	public void startThread() {

		super.startThread();
		this.receivingThread.start();
	}

	@Override
	public void changePriority(int newPriority) {

	}

	@Override
	public int triesOnStuck() {

		return 0;
	}

	@Override
	public void actionOnStuck() {

	}

	@Override
	public String amIStuck() {

		return null;
	}

	@Override
	public long isDelayed() {

		return 0;
	}

	@Override
	protected void onReceivedMessage(RemoteMessage message) {
		try {
			queue.put(message);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE,
					Utils.threadName() + "Interuppted while queuing received command", e);
		}
	}

	private void processMessage(RemoteMessage command) {
		if (Configuration.IsInDeubbungMode)
			logger.info(Utils.threadName() + "Invoking event upon receiving command: "
					+ command);
		this.onCommandReceived(new MessageReceivedEventArgs(command));
	}

	private class MessageHandler implements Runnable {

		private final Logger logger = Logger
				.getLogger(MessageHandler.class.getName() + "--"
						+ Thread.currentThread().getName());

		private final BlockingQueue<RemoteMessage> queue;

		public MessageHandler(BlockingQueue<RemoteMessage> queue) {
			this.queue = queue;
		}

		@Override
		public void run() {

			waitForMessages();
		}

		private void waitForMessages() {
			RemoteMessage message;
			try {
				message = this.queue.take();
				processMessage(message);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, Utils.threadName()
						+ "Interuppted while retreiving command from queue", e);
			}
		}

	}
}
