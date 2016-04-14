package edu.uw.ece.alloy.debugger.propgen.benchmarker.agent;

import java.util.ArrayList;
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
import edu.uw.ece.alloy.debugger.propgen.benchmarker.PropertyToAlloyCode;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyProcessedResult.FailedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyProcessedResult.InferredResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyProcessedResult.TimeoutResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.IamAlive;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.Suicided;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;

public class AlloyExecuter implements Runnable, ThreadToBeMonitored  {

	private Thread executerThread = new Thread(this);

	private final static AlloyExecuter self = new AlloyExecuter( Integer.valueOf(Configuration.getProp("max_alloy_executer_intterupts") ) );

	private final BlockingQueue<AlloyProcessingParam> queue = new LinkedBlockingQueue<>();
	private final List<PostProcess> postProcesses = Collections
			.synchronizedList(new LinkedList<PostProcess>());

	protected volatile AtomicInteger processed = new AtomicInteger(0);
	protected volatile AtomicInteger shadowProcessed = new AtomicInteger(-1);
	protected volatile AtomicInteger livenessFailed = new AtomicInteger(0);
	protected volatile AtomicInteger recoveryAttempts = new AtomicInteger(0);

	private volatile AlloyProcessingParam lastProccessing = AlloyProcessingParam.EMPTY_PARAM;

	protected final static Logger logger = Logger.getLogger(AlloyExecuter.class.getName()+"--"+Thread.currentThread().getName());

	protected int iInterrupt = 0;
	protected final int maxInterrupt ;  
	protected boolean killToken = false;

	protected AlloyExecuter(final int maxInterrupt) {
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
				logger.severe("["+Thread.currentThread().getName()+"] " +"The post processing action <" + e
						+ "> is interrupted on: " + result);
				throw e1;
			}
		}
	}

	private List<AlloyProcessedResult> inferProperties(AlloyProcessedResult result){
		List<AlloyProcessedResult> ret = new ArrayList<>();
		
		List<PropertyToAlloyCode> coders = result.params.alloyCoder.getInferedPropertiesCoder(result.sat == 1);
		
		// If coders is not empty, then something is inferred.
		for (PropertyToAlloyCode coder: coders){
			if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"]" + " Start processing "+lastProccessing);
			ret.add(InferredResult.createInferredResult(result, coder, result.sat == 1));
		}
		
		return Collections.unmodifiableList(ret);
	}
	
	/**
	 * This method is called by the self monitor or any external entity to send and record a timeout.
	 */
	public synchronized void recordATimeout() {

		synchronized (lastProccessing) {
			if(lastProccessing.equals(lastProccessing.EMPTY_PARAM) ) return;
			try {
				if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"] " +"The timeout is recorded for " + lastProccessing);
				runPostProcesses(new AlloyProcessedResult.TimeoutResult(
						lastProccessing));
				lastProccessing = lastProccessing.EMPTY_PARAM;
			} catch (InterruptedException e) {
				logger.severe("["+Thread.currentThread().getName()+"] " +"The thread is interuupted while recording a timeout message.");
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
			if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"]" + " Start processing "+lastProccessing);

			AlloyProcessedResult rep = new AlloyProcessedResult(lastProccessing);
			try {
				
				A4CommandExecuter.getInstance().run(
						lastProccessing.srcPath().getAbsolutePath(),
						rep, PropertyToAlloyCode.COMMAND_BLOCK_NAME);
				
				if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"]" + " Prcessing "+lastProccessing+" took "+(System.currentTimeMillis()-time)+" sec and result is: "+rep);
				
				runPostProcesses(rep);				
				processed.incrementAndGet();
				
				// The inferred result should be added into the logs. The log goes into file, socket, and DB.
				for (AlloyProcessedResult inferredResult: inferProperties(rep)){
					
					System.out.println("inferredResult->"+rep.params.alloyCoder.predCallA+"=>"+rep.params.alloyCoder.predCallB+"--->"+
					inferredResult.params.alloyCoder.predCallA+"=>"+inferredResult.params.alloyCoder.predCallB + "  "+rep+" "+rep.getClass());
					
					runPostProcesses(inferredResult);
				}
				
			} catch (Err e) {
				if(lastProccessing == null){
					logger.severe("["+Thread.currentThread().getName()+"] " +
							" The parameter is null and no failed message can be sent: " +
							lastProccessing);
					return;
				}

				runPostProcesses(new AlloyProcessedResult.FailedResult(
						lastProccessing));
				logger.severe("["+Thread.currentThread().getName()+"] " +
						" The Alloy processor failed on processing: " +
						lastProccessing);
				if(Configuration.IsInDeubbungMode) logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " +e.getMessage(), e);
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

	public void stopMe(){
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
				logger.severe("["+Thread.currentThread().getName()+"] " +"Processing a result is interrupted after processed "
						+ processed + " requests.");
				recordATimeout();
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

		if(executerThread.isAlive()){
			if(this.isSpilledTimeout()){
				if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"]" + " The AlloyExecuter thread is interrupted again and again. Replace the thread now. ");
				this.stopMe();
				executerThread.interrupt();
				executerThread = new Thread(this);
				executerThread.start();
			}else{
				logger.severe("["+Thread.currentThread().getName()+"]" + " Interrupt the AlloyExecuter thread. ");
				executerThread.interrupt();
			}
		}

	}

	protected void sendLivenessMessage(){
		//The executer starts to processing, so no need to recover or kill itself. Reset the countr.
		recoveryAttempts.set(0);
		try{

			IamAlive iamAlive =  new IamAlive(AlloyProcessRunner.getInstance().PID, System.currentTimeMillis(), 
					processed.get(), size());

			iamAlive.sendMe(AlloyProcessRunner.getInstance().remotePort);
			livenessFailed.set(0);
			if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"]"+ "A live message is sent from pId: "+AlloyProcessRunner.getInstance().PID +" >"+iamAlive);
		}catch(Exception e){
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]"+ "Failed to send a live signal on PID: "+ AlloyProcessRunner.getInstance().PID+" this is "+livenessFailed+" attempt", e);
			livenessFailed.incrementAndGet();

		}

	}

	protected void haltIfCantProceed(final int maxRetryAttepmpts){
		AlloyProcessRunner.getInstance();
		//recovery was not enough, the whole processes has to be shut-down
		if(recoveryAttempts.get() > maxRetryAttepmpts || 
				livenessFailed.get() > maxRetryAttepmpts ){
			logger.severe("["+Thread.currentThread().getName()+"]"+ "After recovery "+ recoveryAttempts+ " times or " + livenessFailed +" liveness message, attempts, the executer in PID:"+ 
					AlloyProcessRunner.getInstance().PID +" does not prceeed, So the process is exited.");

			try{
				new Suicided(AlloyProcessRunner.getInstance().PID, System.currentTimeMillis()).
				sendMe(AlloyProcessRunner.getInstance().remotePort);
			}catch(Exception e){
				logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]"+ "Failed to send a Suicide signal on PID: "+ AlloyProcessRunner.getInstance().PID, e);
			}

			Runtime.getRuntime().halt(0);
		}
	}


	@Override
	public void actionOnNotStuck() {
		sendLivenessMessage();
		haltIfCantProceed(AlloyProcessRunner.getInstance().SelfMonitorRetryAttempt);
	}

	@Override
	public String amIStuck() {
		return isDelayed() == 0 ? "" :  "Processing PostProess"+getClass().getSimpleName()+" is stuck after processing "+processed+" messages.";
	}

	@Override
	public long isDelayed() {
		long result = 0;
		//monitor the socket
		if(shadowProcessed.get() == processed.get() && !isEmpty()){
			//The executer does not proceeded.
			result = processed.intValue(); 
			//TODO manage to reset the socket thread
		}else{
			//The executer proceeded 
			shadowProcessed.set(processed.get());
		}
		return result;
	}

	@Override
	public void startThread() {
		if(!executerThread.isAlive())
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
