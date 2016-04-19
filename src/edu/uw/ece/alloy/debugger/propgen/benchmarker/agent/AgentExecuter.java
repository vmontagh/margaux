/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.agent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.IamAlive;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteCommand;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.Suicided;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;
import edu.uw.ece.alloy.util.ServerSocketInterface;
import edu.uw.ece.hola.agent.Utils;

/**
 * @author fikayo
 *         
 */
public abstract class AgentExecuter implements Runnable, ThreadToBeMonitored {
    
    public final static int MaxExecuterInterrupts = Integer.valueOf(Configuration.getProp("max_alloy_executer_intterupts"));
    private final static Logger logger = Logger.getLogger(AgentExecuter.class.getName() + "--" + Thread.currentThread().getName());
    
    public final int SelfMonitorInterval = Integer.parseInt(Configuration.getProp("self_monitor_interval"));
    
    protected final int maxInterrupt;
    protected final BlockingQueue<ProcessingParam> processingQueue;
    protected final List<PostProcess> postProcesses;
    protected final ServerSocketInterface socketInterface;
    
    protected volatile AtomicInteger processed = new AtomicInteger(0);
    protected volatile AtomicInteger shadowProcessed = new AtomicInteger(-1);
    protected volatile ProcessingParam lastProccessing;
    
    protected int interruptCount = 0;
    protected Thread executerThread;
    protected boolean killToken = false;
    
    public AgentExecuter(final int maxInterrupt, final ServerSocketInterface socketInterface) {
        this.maxInterrupt = maxInterrupt;
        this.socketInterface = socketInterface;
        
        this.executerThread = new Thread(this);
        this.processingQueue = new LinkedBlockingQueue<>();
        this.postProcesses = Collections.synchronizedList(new LinkedList<PostProcess>());
        this.lastProccessing = this.getEmptyParam();
    }
    
    public final boolean isEmpty() {
        
        return this.processingQueue.isEmpty();
    }
    
    public final int size() {
        
        return this.processingQueue.size();
    }
    
    public void process(final ProcessingParam p) {
        
        this.processingQueue.add(p);
    }
    
    public void registerPostProcess(PostProcess e) {
        
        this.postProcesses.add(e);
    }
    
    @Override
    public void startThread() {
        
        if (!this.executerThread.isAlive()) {
            executerThread.start();
        }
    }
    
    public void stopMe() {
        
        this.killToken = true;
    }
    
    @Override
    public void actionOnStuck() {
        
        if (executerThread.isAlive()) {
            
            String className = this.getClass().getSimpleName();
            
            if (this.isSpilledTimeout()) {
                
                if (Configuration.IsInDeubbungMode) {
                    logger.info(Utils.threadName() + " The " + className + " thread is interrupted again and again. Replace the thread now. ");
                }
                
                this.stopMe();
                executerThread.interrupt();
                executerThread = new Thread(this);
                executerThread.start();
            }
            else {
                logger.severe(Utils.threadName() + " Interrupt the " + className + " thread. ");
                executerThread.interrupt();
            }
        }
        
    }
    
    @Override
    public void actionOnNotStuck() {
        
        this.socketInterface.sendLivenessMessage(this.processed.get(), this.size());
        int maxRetryAttepmpts = SelfMonitorInterval;
        int livenessFailed = this.socketInterface.getLivenessFailed();
        
        // Recovery was not enough, the whole processes has to be shut-down
        if (livenessFailed > maxRetryAttepmpts) {
            logger.severe(Utils.threadName() + "After " + livenessFailed + " liveness message, attempts, the executer in PID: " + this.socketInterface.getHostAddress() + " does not prceeed, So the process is exited.");
            
            try {
                RemoteCommand command = new Suicided(this.socketInterface.getHostAddress(), System.currentTimeMillis());
                this.socketInterface.sendMessage(command);
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, Utils.threadName() + "Failed to send a Suicide signal on PID: " + this.socketInterface.getHostAddress(), e);
            }
            
            Runtime.getRuntime().halt(0);
        }
    }
    
    /**
     * This method is called by the self monitor or any external entity to send
     * and record a timeout.
     */
    public final synchronized void recordATimeout() {
        
        synchronized (lastProccessing) {
            if (lastProccessing.isEmptyParam()) {
                return;
            }
            
            try {
                if (Configuration.IsInDeubbungMode)
                    logger.info("[" + Thread.currentThread().getName() + "] " + "The timeout is recorded for " + lastProccessing);
                    
                this.runPostProcessesForCurrentResult();
                this.lastProccessing = this.getEmptyParam();
            }
            catch (InterruptedException e) {
                logger.severe("[" + Thread.currentThread().getName() + "] " + "The thread is interuupted while recording a timeout message.");
            }
        }
    }
    
    @Override
    public void run() {
        
        // If something stuck and gets timeout, a timeout message has to be
        // sent.
        recordATimeout();
        killToken = false;
        interruptCount = 0;
        
        while (!killToken && !Thread.currentThread().isInterrupted()) {
            
            if (this.interruptCount == maxInterrupt) {
                throw new RuntimeException("Constantly interrupted.");
            }
            
            try {
                this.runAgent();
                this.interruptCount = 0;
            }
            catch (InterruptedException e) {
                
                logger.severe(Utils.threadName() + "Processing a result is interrupted after processed " + processed + " requests.");
                recordATimeout();
                ++this.interruptCount;
            }
        }
    }
    
    @Override
    public int triesOnStuck() {
        
        // return recoveryAttempts.get();
        return 0;
    }
    
    @Override
    public long isDelayed() {
        
        long result = 0;
        
        // monitor the socket
        if (this.shadowProcessed.get() == this.processed.get() && !isEmpty()) {
            // The executer does not proceeded.
            result = this.processed.intValue();
            // TODO manage to reset the socket thread
        }
        else {
            // The executer proceeded
            this.shadowProcessed.set(this.processed.get());
        }
        
        return result;
    }
    
    @Override
    public void cancelThread() {
        
        executerThread.interrupt();
    }
    
    @Override
    public void changePriority(int newPriority) {
        
        executerThread.setPriority(newPriority);
        
    }
    
    protected abstract void runAgent() throws InterruptedException;
    
    protected abstract void runPostProcessesForCurrentResult() throws InterruptedException;
    
    protected abstract ProcessingParam getEmptyParam();
    
    /**
     * Returns true if the number of interruptions that have occurred equals
     * maxInterrupt/2
     * 
     * @return
     */
    private boolean isSpilledTimeout() {
        
        return interruptCount == maxInterrupt / 2;
    }
    
}