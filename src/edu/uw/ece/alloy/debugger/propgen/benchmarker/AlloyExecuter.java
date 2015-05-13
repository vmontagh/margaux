package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.uw.ece.alloy.debugger.exec.A4CommandExecuter;

public class AlloyExecuter implements Runnable {

	private final static AlloyExecuter self = new AlloyExecuter();

	private final BlockingQueue<AlloyProcessingParam> queue = new LinkedBlockingQueue<>();
	private final List<PostProcess> postProcesses = Collections
			.synchronizedList(new LinkedList<>());

	public volatile AtomicInteger processed = new AtomicInteger(0);
	private volatile AlloyProcessingParam lastProccessing;

	protected final static Logger logger = Logger.getLogger(AlloyExecuter.class.getName()+"--"+Thread.currentThread().getName());

	private AlloyExecuter() {
	}

	public static AlloyExecuter getInstance() {
		return self;
	}

	public void process(final AlloyProcessingParam p) {
		logger.info("["+Thread.currentThread().getName()+"]" + "Message recieved "+p);
		queue.add(p);
	}

	public void resgisterPostProcess(PostProcess e) {
		postProcesses.add(e);
	}

	private void runPostProcesses(AlloyProcessedResult result)
			throws InterruptedException {
		for (PostProcess e : postProcesses) {
			try {
				e.doAction(result);
				
			} catch (InterruptedException e1) {
				logger.info("["+Thread.currentThread().getName()+"] " +"The post processing action <" + e
						+ "> is interrupted on: " + result);
				throw e1;
			}
		}
	}

	/**
	 * This method is called by the self monitor or any external entity to send and record a timeout.
	 */
	public void recordATimeout() {
		try {
			if(lastProccessing == null ) return;
			logger.info("["+Thread.currentThread().getName()+"] " +"The timeout is recorded for " + lastProccessing);
			runPostProcesses(new AlloyProcessedResult.TimeoutResult(
					lastProccessing));
			lastProccessing = null;
		} catch (InterruptedException e) {
			logger.info("["+Thread.currentThread().getName()+"] " +"The thread is interuupted while recording a timeout message.");
		}
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}

	public int size() {
		return queue.size();
	}

	private void runAlloy() throws InterruptedException {

		lastProccessing = queue.take();// wait here.
		long time = System.currentTimeMillis();
		logger.info("["+Thread.currentThread().getName()+"]" + " Start processing "+lastProccessing);
		AlloyProcessedResult rep = new AlloyProcessedResult(lastProccessing);
		try {
			A4CommandExecuter.getInstance().run(
					new String[] { lastProccessing.srcPath.getAbsolutePath() },
					rep);
			logger.info("["+Thread.currentThread().getName()+"]" + " Prcessing "+lastProccessing+" took "+(System.currentTimeMillis()-time)+" sec and result is: "+rep);
			runPostProcesses(rep);
			processed.incrementAndGet();
		} catch (Err e) {
			runPostProcesses(new AlloyProcessedResult.FailedResult(
					lastProccessing));
			logger.severe("["+Thread.currentThread().getName()+"] " +"The Alloy processor failed on processing: "
					+ lastProccessing);
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " +e.getMessage(), e);
		}

	}

	@Override
	public void run() {
		
		int i = 0;
		final int maxInterrupt = 1000;  
		while (!Thread.currentThread().isInterrupted()){
			if( i == maxInterrupt) throw new RuntimeException("Constantly interrupted.");
			try {
				runAlloy();
				i = 0;
			} catch (InterruptedException e) {
				logger.info("["+Thread.currentThread().getName()+"] " +"Processing a result is interrupted after processed "
						+ processed + " requests.");
				i++;
			}
		}

	}

	public void cancel() {
		Thread.currentThread().interrupt();
	}

}
