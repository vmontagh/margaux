package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.LivenessMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;
import edu.uw.ece.alloy.util.ServerSocketInterface;
import edu.uw.ece.alloy.util.events.MessageEventListener;
import edu.uw.ece.alloy.util.events.MessageSentEventArgs;

/**
 * Create new anonymous liveness messages and send to the receivers
 * 
 * @author vajih
 *
 */
public abstract class ReportLiveness<T extends LivenessMessage> implements Runnable, ThreadToBeMonitored, UpdateLivenessStatus {

	protected final RemoteProcess localProcess;
	protected final RemoteProcess remoteProcess;
	protected int processed;
	protected int tobeProcessed;
	final long sendInterval;
	final Set<T> sentMessages;
	final int maxFailed;
	final ServerSocketInterface interfacE;
	final Thread thread;

	/**
	 * Creating an anonymous livenessmessage to a remote receiver.
	 * 
	 * @param process
	 * @param processed
	 * @param tobeProcessed
	 */
	public ReportLiveness(final RemoteProcess localProcess,
			final RemoteProcess remoteProcess, final int processed,
			final int tobeProcessed, final long sendInterval, final int maxFailed,
			final ServerSocketInterface interfacE) {
		this.localProcess = localProcess;
		this.remoteProcess = remoteProcess;
		this.processed = processed;
		this.tobeProcessed = tobeProcessed;
		this.sendInterval = sendInterval;
		this.maxFailed = maxFailed;
		this.interfacE = interfacE;
		this.sentMessages = new HashSet<>();
		// register the liveness listener failures
		this.interfacE.MessageFailed
				.addListener(new MessageEventListener<MessageSentEventArgs>() {
					@Override
					public void actionOn(LivenessMessage livenessMessage,
							MessageSentEventArgs messageArgs) {
						if (sentMessages.size() >= maxFailed) {
							// halt the JVM
							Runtime.getRuntime().halt(0);
						}
					}
				});
		// register the liveness listener success
		this.interfacE.MessageSent
				.addListener(new MessageEventListener<MessageSentEventArgs>() {
					@Override
					public void actionOn(LivenessMessage livenessMessage,
							MessageSentEventArgs messageArgs) {
						sentMessages.remove(livenessMessage);
					}
				});

		thread = new Thread(this);
	}

	public ReportLiveness(final InetSocketAddress localProcess,
			final InetSocketAddress remoteProcess, final int processed,
			final int tobeProcessed, final long sendInterval, final int maxFailed,
			final ServerSocketInterface interfacE) {
		this(new RemoteProcess(localProcess), new RemoteProcess(remoteProcess),
				processed, tobeProcessed, sendInterval, maxFailed, interfacE);
	}

	public void setProcessed(int processed) {
		this.processed = processed;
	}

	public void setTobeProcessed(int tobeProcessed) {
		this.tobeProcessed = tobeProcessed;
	}
	
	protected abstract T createLivenessMessage();

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(sendInterval);
				T message = createLivenessMessage();
				
				sentMessages.add(message);
				interfacE.sendMessage(message, remoteProcess);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void startThread() {
		thread.start();
	}

	@Override
	public void cancelThread() {
	}

	@Override
	public void changePriority(int newPriority) {
		thread.setPriority(newPriority);
	}

	@Override
	public void actionOnNotStuck() {
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
		return "";
	}

	@Override
	public long isDelayed() {
		return 0;
	}
}
