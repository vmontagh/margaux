package edu.uw.ece.alloy.debugger.propgen.benchmarker.agent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.exec.A4CommandExecuter;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.PropertyToAlloyCode;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.ProcessedResult.Status;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.UpdateLivenessStatus;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.communication.Subscriber;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy.AlloyDiedMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy.AlloyProcessedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;
import edu.uw.ece.alloy.util.SendOnServerSocketInterface;
import edu.uw.ece.hola.agent.Utils;

public class AlloyExecuter implements Runnable, ThreadToBeMonitored {

	// Configurations
	final static public int MaxInterrupt = Integer
			.parseInt(Configuration.getProp("max_alloy_executer_intterupts"));
	final static public int MaxRetryOnFail = Integer
			.parseInt(Configuration.getProp("self_monitor_retry_attempt"));

	private Thread executerThread = new Thread(this);
	Thread timeoutThread;

	protected Subscriber<AlloyProcessingParam> queue;
	private final List<PostProcess> postProcesses = Collections
			.synchronizedList(new LinkedList<PostProcess>());

	protected final UpdateLivenessStatus livenessStatus;
	protected final SendOnServerSocketInterface interfacE;

	protected volatile AtomicInteger processed = new AtomicInteger(0);
	protected volatile AtomicInteger shadowProcessed = new AtomicInteger(-1);
	protected volatile AtomicInteger livenessFailed = new AtomicInteger(0);
	protected volatile AtomicInteger recoveryAttempts = new AtomicInteger(0);

	// private volatile AlloyProcessingParam lastProccessing =
	// AlloyProcessingParam.EMPTY_PARAM;

	protected final static Logger logger = Logger.getLogger(
			AlloyExecuter.class.getName() + "--" + Thread.currentThread().getName());

	protected int iInterrupt = 0;
	protected final int maxInterrupt, maxRetryAttempt;
	protected boolean killToken = false;

	protected final File tmpLocalDirectory;

	public AlloyExecuter(final int maxInterrupt, final int maxRetryAttempt,
			Subscriber<AlloyProcessingParam> queue,
			final UpdateLivenessStatus livenessStatus,
			final SendOnServerSocketInterface interfacE, File tmpLocalDirectory) {
		this.maxInterrupt = maxInterrupt;
		this.queue = queue;
		this.livenessStatus = livenessStatus;
		this.maxRetryAttempt = maxRetryAttempt;
		this.interfacE = interfacE;
		this.tmpLocalDirectory = tmpLocalDirectory;
	}

	public AlloyExecuter(final Subscriber<AlloyProcessingParam> queue,
			final UpdateLivenessStatus livenessStatus,
			final SendOnServerSocketInterface interfacE, File tmpLocalDirectory) {
		this(MaxInterrupt, MaxRetryOnFail, queue, livenessStatus, interfacE,
				tmpLocalDirectory);
	}

	public AlloyExecuter(final Subscriber<AlloyProcessingParam> queue,
			final SendOnServerSocketInterface interfacE, File tmpLocalDirectory) {
		this(MaxInterrupt, MaxRetryOnFail, queue, new UpdateLivenessStatus() {
			long processed, tobeProcessed;

			@Override
			public void setTobeProcessed(int tobeProcessed) {
				this.tobeProcessed = tobeProcessed;
			}

			@Override
			public void setProcessed(int processed) {
				this.processed = processed;
			}
		}, interfacE, tmpLocalDirectory);
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
				logger.severe("[" + Thread.currentThread().getName() + "] "
						+ "The post processing action <" + e + "> is interrupted on: "
						+ result);
				throw e1;
			}
		}
	}

	private List<AlloyProcessedResult> inferProperties(
			AlloyProcessedResult result) {
		List<AlloyProcessedResult> ret = new ArrayList<>();

		// If coders is not empty, then something is inferred.
		for (PropertyToAlloyCode inferedCoder : result.getParam().getAlloyCoder()
				.get().createItself().getInferedPropertiesCoder(result.sat)) {
			ret.add(new AlloyProcessedResult(result.getParam().createIt(inferedCoder),
					Status.INFERRED));
		}

		return Collections.unmodifiableList(ret);
	}

	/**
	 * This method is called by the self monitor or any external entity to send
	 * and record a timeout.
	 */
	public synchronized void recordATimeout(
			AlloyProcessingParam lastProccessing) {

		if (lastProccessing.equals(lastProccessing.EMPTY_PARAM))
			return;
		try {
			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "] "
						+ "The timeout is recorded for " + lastProccessing);
			runPostProcesses(
					new AlloyProcessedResult(lastProccessing, Status.TIMEOUT));
			lastProccessing = lastProccessing.EMPTY_PARAM;
		} catch (InterruptedException e) {
			logger.severe("[" + Thread.currentThread().getName() + "] "
					+ "The thread is interuupted while recording a timeout message.");
		}

	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}

	public int size() {
		return queue.size();
	}

	/**
	 * The function has to be synchronized in case more than one thread calls it
	 * and access to lastProccessing
	 * 
	 * @throws InterruptedException
	 */
	private synchronized void runAlloy() throws InterruptedException {

		AlloyProcessingParam lastProccessing = queue.take();
		final AlloyProcessingParam originalLastProcessing = lastProccessing;
		try {
			lastProccessing = originalLastProcessing
					.changeTmpLocalDirectory(tmpLocalDirectory).prepareToUse();
		} catch (Exception e1) {
			logger.severe(Utils.threadName() + " The param: " + lastProccessing
					+ " cannot be localized");
			e1.printStackTrace();
		}

		if (lastProccessing.equals(lastProccessing.EMPTY_PARAM)) {
			logger.severe(Utils.threadName() + "Why empty?!!!");
			return;
		}

		long time = System.currentTimeMillis();
		if (Configuration.IsInDeubbungMode)
			logger.info("[" + Thread.currentThread().getName() + "]"
					+ " Start processing " + lastProccessing);

		AlloyProcessedResult rep = new AlloyProcessedResult(originalLastProcessing);
		try {

			timeoutThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(originalLastProcessing.getTimeout().get());
						if (Configuration.IsInDeubbungMode)
							logger.warning(Utils.threadName() + originalLastProcessing.getAlloyCoder().get().getPredName()
									+ " is timed out after "
									+ (System.currentTimeMillis() - time)
									+ " millisecond");
						recordATimeout(originalLastProcessing);
					} catch (InterruptedException e) {
						logger.warning(
								Utils.threadName() + "timeout thread is interrupted!" + e);
					}
				}
			});

			A4CommandExecuter.getInstance()
					.run(
							lastProccessing.getSrcPath().orElseThrow(RuntimeException::new)
									.getAbsolutePath(),
							rep, PropertyToAlloyCode.COMMAND_BLOCK_NAME);

			// stop the timeout timer
			timeoutThread.interrupt();

			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "]" + " Prcessing "
						+ lastProccessing + " took " + (System.currentTimeMillis() - time)
						+ " sec and result is: " + rep);

			runPostProcesses(rep);
			processed.incrementAndGet();
			livenessStatus.setProcessed(processed.get());
			livenessStatus.setTobeProcessed(queue.size());
			// The inferred result should be added into the logs. The log goes into
			// file, socket, and DB.
			for (AlloyProcessedResult inferredResult : inferProperties(rep)) {
				runPostProcesses(inferredResult);
			}

		} catch (Err e) {
			e.printStackTrace();
			runPostProcesses(
					new AlloyProcessedResult(lastProccessing, Status.FAILED));
			logger.severe("[" + Thread.currentThread().getName() + "] "
					+ " The Alloy processor failed on processing: " + lastProccessing);
			if (Configuration.IsInDeubbungMode)
				logger.log(Level.SEVERE,
						"[" + Thread.currentThread().getName() + "] " + e.getMessage(), e);
		}

	}

	/**
	 * There was maxInterrupt/2 number of interrupt happened
	 * 
	 * @return
	 */
	public boolean isSpilledTimeout() {
		return iInterrupt == maxInterrupt / 2;
	}

	public void stopMe() {
		killToken = true;
	}

	@Override
	public void run() {

		// if something stuck and gets timeout, a timeout message has to be sent.
		killToken = false;
		iInterrupt = 0;
		while (!killToken && !Thread.currentThread().isInterrupted()) {
			if (iInterrupt == maxInterrupt)
				throw new RuntimeException("Constantly interrupted.");
			try {
				runAlloy();
				iInterrupt = 0;
			} catch (InterruptedException e) {
				logger.severe("[" + Thread.currentThread().getName() + "] "
						+ "Processing a result is interrupted after processed " + processed
						+ " requests.");
				++iInterrupt;
			}
		}
	}

	@Override
	public int triesOnStuck() {
		return recoveryAttempts.get();
	}

	@Override
	public void actionOnStuck() {

	}

	protected void restartThread() {
		if (executerThread.isAlive()) {
			if (this.isSpilledTimeout()) {
				if (Configuration.IsInDeubbungMode)
					logger.info("[" + Thread.currentThread().getName() + "]"
							+ " The AlloyExecuter thread is interrupted again and again. Replace the thread now. ");
				this.stopMe();
				executerThread.interrupt();
				timeoutThread.interrupt();
				executerThread = new Thread(this);
				executerThread.start();
			} else {
				logger.severe("[" + Thread.currentThread().getName() + "]"
						+ " Interrupt the AlloyExecuter thread. ");
				executerThread.interrupt();
				timeoutThread.interrupt();
			}
			recoveryAttempts.incrementAndGet();
		}
	}

	protected void haltIfCantProceed() {
		// recovery was not enough, the whole processes has to be shut-down
		if (recoveryAttempts.get() > maxRetryAttempt) {
			logger.severe("[" + Thread.currentThread().getName() + "]"
					+ "After recovery " + recoveryAttempts + " times "
					+ " attempts, the executer in PID:" + interfacE.getHostProcess()
					+ " does not prceeed, So the process is exited.");

			try {
				AlloyDiedMessage message = new AlloyDiedMessage(
						interfacE.getHostProcess());
				interfacE.sendMessage(message);
			} catch (Exception e) {
				logger.log(Level.SEVERE,
						"[" + Thread.currentThread().getName() + "]"
								+ "Failed to send a Suicide signal on PID: "
								+ interfacE.getHostProcess(),
						e);
			}
			Runtime.getRuntime().halt(0);
		}
	}

	@Override
	public void actionOnNotStuck() {
		haltIfCantProceed();
	}

	@Override
	public String amIStuck() {
		return isDelayed() == 0 ? ""
				: "Processing PostProess" + getClass().getSimpleName()
						+ " is stuck after processing " + processed + " messages.";
	}

	@Override
	public long isDelayed() {
		long result = 0;
		// monitor the socket
		if (shadowProcessed.get() == processed.get() && !isEmpty()) {
			// The executer does not proceeded.
			result = processed.intValue();
			// TODO manage to reset the socket thread
		} else {
			// The executer proceeded
			shadowProcessed.set(processed.get());
		}
		return result;
	}

	@Override
	public void startThread() {
		if (!executerThread.isAlive())
			executerThread.start();
	}

	@Override
	public void cancelThread() {
		executerThread.interrupt();
	}

	@Override
	public void changePriority(int newPriority) {
		executerThread.setPriority(newPriority);
	}

}
