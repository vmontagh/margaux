package edu.uw.ece.alloy.debugger.propgen.benchmarker.agent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.exec.A4CommandExecuter;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParamLazyCompressing;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyProcessedResult.FailedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyProcessedResult.TimeoutResult;

public class AlloyExecuter implements Runnable {

	private final static AlloyExecuter self = new AlloyExecuter( Integer.valueOf(Configuration.getProp("self_correction_interrupt") ) );

	private final BlockingQueue<AlloyProcessingParam> queue = new LinkedBlockingQueue<>();
	private final List<PostProcess> postProcesses = Collections
			.synchronizedList(new LinkedList<PostProcess>());

	public volatile AtomicInteger processed = new AtomicInteger(0);
	private volatile AlloyProcessingParam lastProccessing = AlloyProcessingParam.EMPTY_PARAM;

	protected final static Logger logger = Logger.getLogger(AlloyExecuter.class.getName()+"--"+Thread.currentThread().getName());

	protected int iInterrupt = 0;
	protected final int maxInterrupt ;  
	protected boolean killToken = false;
	
	private AlloyExecuter(final int maxInterrupt) {
		this.maxInterrupt = maxInterrupt;
	}

	public static AlloyExecuter getInstance() {
		return self;
	}

	public void process(final AlloyProcessingParam p) {
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
	public synchronized void recordATimeout() {

		synchronized (lastProccessing) {
			if(lastProccessing.equals(lastProccessing.EMPTY_PARAM) ) return;
			try {
				logger.info("["+Thread.currentThread().getName()+"] " +"The timeout is recorded for " + lastProccessing);
				runPostProcesses(new AlloyProcessedResult.TimeoutResult(
						lastProccessing));
				lastProccessing = lastProccessing.EMPTY_PARAM;
			} catch (InterruptedException e) {
				logger.info("["+Thread.currentThread().getName()+"] " +"The thread is interuupted while recording a timeout message.");
			}
		}
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}

	public int size() {
		return queue.size();
	}

	/**
	 * The function has to be synchronized in case more than one thread calls it and access to lastProccessing
	 * @throws InterruptedException
	 */
	private  synchronized void runAlloy() throws InterruptedException {

		synchronized (lastProccessing) {
			lastProccessing = queue.take();
			
			if(lastProccessing.equals(lastProccessing.EMPTY_PARAM)){
				logger.severe("["+Thread.currentThread().getName()+"] "+"Why null?!!!");
				return;
			}

			long time = System.currentTimeMillis();
			logger.info("["+Thread.currentThread().getName()+"]" + " Start processing "+lastProccessing);

			AlloyProcessedResult rep = new AlloyProcessedResult(lastProccessing);
			try {

				A4CommandExecuter.getInstance().run(
						new String[] { lastProccessing.srcPath().getAbsolutePath() },
						rep);

				logger.info("["+Thread.currentThread().getName()+"]" + " Prcessing "+lastProccessing+" took "+(System.currentTimeMillis()-time)+" sec and result is: "+rep);
				runPostProcesses(rep);
				processed.incrementAndGet();
			} catch (Err e) {
				if(lastProccessing == null){
					logger.severe("["+Thread.currentThread().getName()+"] " +"The parameter is null and no failed message can be sent: "
							+ lastProccessing);
					return;
				}

				runPostProcesses(new AlloyProcessedResult.FailedResult(
						lastProccessing));
				logger.severe("["+Thread.currentThread().getName()+"] " +"The Alloy processor failed on processing: "
						+ lastProccessing);
				logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " +e.getMessage(), e);
			}
		}

	}

	/**
	 * There was maxInterrupt/2 number of interrupt happened
	 * @return
	 */
	public boolean isSpilledTimeout(){
		return iInterrupt == maxInterrupt/2;
	}
	
	public void stop(){
		killToken = true;
	}
	
	@Override
	public void run() {

		//if something stuck and gets timeout, a timeout message has to be sent.
		recordATimeout();
		killToken = false;
		iInterrupt = 0;
		while (!killToken && !Thread.currentThread().isInterrupted()){
			if( iInterrupt == maxInterrupt) throw new RuntimeException("Constantly interrupted.");
			try {
				runAlloy();
				iInterrupt = 0;
			} catch (InterruptedException e) {
				logger.info("["+Thread.currentThread().getName()+"] " +"Processing a result is interrupted after processed "
						+ processed + " requests.");
				recordATimeout();
				++iInterrupt;
			}
		}

	}

}
