package edu.uw.ece.alloy.util;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteCommand;
import edu.uw.ece.alloy.util.events.CommandReceivedEventArgs;
import edu.uw.ece.alloy.util.events.Event;

public class AsyncServerSocketInterface extends ServerSocketInterfaceBase {

	private Thread receivingThread;
	private MessageHandler handler;

	public final Event<CommandReceivedEventArgs> commandReceived;
	
	public AsyncServerSocketInterface(final int hostPort, final int remotePort) {
		this(new InetSocketAddress(hostPort), new InetSocketAddress(remotePort));
	}

	public AsyncServerSocketInterface(final String hostName, final int hostPort,	final String remoteName, final int remotePort) {
		this(new InetSocketAddress(hostName, hostPort),	new InetSocketAddress(remoteName, remotePort));
	}
	
	public AsyncServerSocketInterface(final InetSocketAddress hostAddress, final InetSocketAddress remoteAddress) {
		super(hostAddress, remoteAddress);		
		this.handler = new MessageHandler(this.queue);
		this.receivingThread = new Thread(this.handler);
		this.commandReceived = new Event<>();
	}

	@Override
	public void startThread() {
		super.startThread();
		this.receivingThread.start();
	}
	
	@Override
	public void changePriority(int newPriority) {	}

	@Override
	public int triesOnStuck() {
		return 0;
	}

	@Override
	public void actionOnStuck() {	}

	@Override
	public String amIStuck() {
		return null;
	}

	@Override
	public long isDelayed() {
		return 0;
	}
	
	@Override
	protected void onReceivedMessage(RemoteCommand command) {
		
		super.onReceivedMessage(command);
		
		Event<CommandReceivedEventArgs> event = this.commandReceived;
		if(event.hasHandlers()) {
			event.invokeListeners(this, new CommandReceivedEventArgs(command));
		}
	}
	
	private class MessageHandler implements Runnable {
		
		private final BlockingQueue<RemoteCommand> queue;
		
		public MessageHandler(BlockingQueue<RemoteCommand> queue) {
			this.queue = queue;
		}
		
		@Override
		public void run() {
			waitForMessages();
		}
		
		private void waitForMessages() {
			
			RemoteCommand command;
			try {
				command = this.queue.take();
				onReceivedMessage(command);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
}
