/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.communication.Queue;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RequestMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;
import edu.uw.ece.alloy.util.RetryingThread;
import edu.uw.ece.alloy.util.ServerSocketInterface;

/**
 * Feeder is the storage that keeps tasks to be sent before distributed to
 * remote processors. In collaboration with processmanager, it distributes the
 * tasks among remote processes
 * 
 * @author vajih
 *
 */
public abstract class Feeder<T extends ProcessingParam>
		implements Runnable, ThreadToBeMonitored {

	final static Logger logger = Logger.getLogger(
			Feeder.class.getName() + "--" + Thread.currentThread().getName());

	final public static boolean PREVENT_DUPLICATION = Boolean
			.parseBoolean(Configuration.getProp("prevent_generation_duplication"));

	final Queue<T> queue;
	/*
	 * This buffer stores as a second buffer. The content is eventually merged
	 * into the main 'queue'. In case of any fault happening and the request is
	 * going to be restocked, the request is added to 'backLog' then merged into
	 * 'queue'. The 'backLog' could be either limit or unlimited.
	 */
	final Queue<T> backLogQueue;

	final ProcessDistributer processes;

	final Thread sender, merger;

	/* Interface to send messages */
	final ServerSocketInterface distributerInterface;

	public Feeder(final ProcessDistributer processes,
			final ServerSocketInterface distributerInterface, final Queue<T> queue,
			final Queue<T> backLogQueue) {
		this.processes = processes;
		this.queue = queue;
		this.backLogQueue = backLogQueue;
		this.distributerInterface = distributerInterface;

		sender = new RetryingThread(this, 100);
		merger = new RetryingThread(new Runnable() {
			public void run() {
				try {
					merge();
				} catch (InterruptedException e) {
					logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "]"
							+ "The thread is interuppted.", e);
					throw new RuntimeException(e);
				}
			}
		}, 100);
	}

	public Feeder(final ProcessDistributer processes,
			final ServerSocketInterface distributerInterface, int bufferSize,
			int backLogBufferSize) {
		this(processes, distributerInterface,
				bufferSize > 0 ? new Queue<>(bufferSize) : new Queue<>(),
				backLogBufferSize > 0 ? new Queue<>(backLogBufferSize) : new Queue<>());
	}

	public Feeder(final ProcessDistributer processes,
			final ServerSocketInterface distributerInterface) {
		this(processes, distributerInterface, 0, 0);
	}

	public void addProcessTask(final T param) throws InterruptedException {
		if (Configuration.IsInDeubbungMode)
			logger.log(Level.INFO, "[" + Thread.currentThread().getName() + "]"
					+ " a request is added to be sent:" + param);
		// If the message has to be compressed, it will be compressed next.
		try {
			queue.put(param);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE,
					"[" + Thread.currentThread().getName() + "]"
							+ "a new Alloy process message cannot be added to the queue:"
							+ param,
					e);
			throw e;
		}
		if (Configuration.IsInDeubbungMode)
			logger.log(Level.INFO,
					"[" + Thread.currentThread().getName() + "]"
							+ "a request is added to be sent and the queue size is:"
							+ queue.size());
	}

	public void clear() {
		queue.clear();
	}

	public long getSize() {
		return queue.size();
	}

	public void addProcessTaskToBacklog(final T param)
			throws InterruptedException {
		if (Configuration.IsInDeubbungMode)
			logger.log(Level.INFO, "[" + Thread.currentThread().getName() + "]"
					+ " a request is added to be merged:" + param);
		try {
			backLogQueue.put(param);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE,
					"[" + Thread.currentThread().getName() + "]"
							+ "a new Alloy process message cannot be added to the backlog:"
							+ param,
					e);
			throw e;
		}
		if (Configuration.IsInDeubbungMode)
			logger.log(Level.INFO,
					"[" + Thread.currentThread().getName() + "]"
							+ "a request is added to be merged and the backlog size is:"
							+ backLogQueue.size());
	}

	protected abstract RequestMessage createRequestMessage(RemoteProcess process,
			T param);

	/**
	 * Pick a request from the queue and send it to a process.
	 * 
	 * @throws InterruptedException
	 */
	protected void sendMessage() throws InterruptedException {
		if (Configuration.IsInDeubbungMode)
			logger.info("[" + Thread.currentThread().getName() + "]"
					+ "Queue size is: " + queue.size());
		T e;
		try {
			// take a request, if something is in the queue. Otherwise the thread
			// parks here.
			e = queue.take();
			//System.out.println("feeder 1->"+e.hashCode());
			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "]"
						+ "Queue object: " + e);

		} catch (InterruptedException e1) {
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "]"
					+ "The command queue is interrupted.", e1);
			throw e1;
		}
		if (Configuration.IsInDeubbungMode)
			logger.info("[" + Thread.currentThread().getName() + "]"
					+ "Message is taken " + e);

		// Find a processor to process the request.
		RemoteProcess process = processes.getActiveRandomeProcess();

		if (Configuration.IsInDeubbungMode)
			logger.info(
					"[" + Thread.currentThread().getName() + "]" + "got a process " + e);
		try {
			//System.out.println("feeder 2->"+e.hashCode());
			// TODO register monitor as a listener to message that are sent
			RequestMessage message = createRequestMessage(process, e);
			distributerInterface.sendMessage(message, process);
			//System.out.println("feeder 3->"+message.getProcessingParam().hashCode());
			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "]"
						+ "Message sent to " + process.address);
		} catch (Throwable t) {
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
					+ "The command cannot be sent.", t);
			addProcessTaskToBacklog(e);
			throw t;
		}

	}

	private void merge() throws InterruptedException {

		if (Configuration.IsInDeubbungMode)
			logger.info("[" + Thread.currentThread().getName() + "]"
					+ "Backlog Queue size is: " + backLogQueue.size());
		T e;
		try {
			// take a request, if something is in the queue. Otherwise the thread
			// parks here.
			e = backLogQueue.take();
		} catch (InterruptedException e1) {
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "]"
					+ "The backlog queue is interrupted.", e1);
			throw e1;
		}
		if (Configuration.IsInDeubbungMode)
			logger.info(
					"[" + Thread.currentThread().getName() + "]" + "Message is taken " + e
							+ " and backLog size is:" + backLogQueue.size());

		addProcessTask(e);

		if (Configuration.IsInDeubbungMode)
			logger.info("[" + Thread.currentThread().getName() + "]"
					+ "The message is added to the main queue " + e
					+ " and queue size is:" + queue.size()
					+ " and the backlogqueue size is: " + backLogQueue.size());

	}

	public void startThread() {
		sender.start();
		merger.start();
	}

	public void cancelThread() {
		sender.interrupt();
		merger.interrupt();
	}

	public void changePriority(final int newPriority) {
		sender.setPriority(newPriority);
		merger.setPriority(newPriority);
	}

	public String getStatus() {
		return (new StringBuilder()).append("New messages added=")
				.append(queue.size()).append("\nMessages to be sent=")
				.append(queue.size()).append("\nMessages to be merged=")
				.append(backLogQueue.size()).toString();
	}

	@Override
	public void run() {
		try {
			while (true)
				sendMessage();
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "]"
					+ "The thread is interuppted.", e);
			//throw new RuntimeException(e);
		}
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
