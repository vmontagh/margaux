package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.GeneratedStorage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ProcessIt;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteCommand;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.RemoteProcessMonitor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;
import edu.uw.ece.alloy.util.RetryingThread;
import edu.uw.ece.alloy.util.ServerSocketInterface;
import edu.uw.ece.alloy.util.Utils;

public class AlloyFeeder extends GeneratedStorage<ProcessingParam> implements Runnable, ThreadToBeMonitored {
    
    final static Logger logger = Logger.getLogger(AlloyFeeder.class.getName() + "--" + Thread.currentThread().getName());
    
    final BlockingQueue<ProcessingParam> queue;
    // This buffer stores as a second buffer. The content is eventually merged
    // into the main 'queue'.
    // In case of any fault happening and the request is going to be restocked,
    // the request is
    // added to 'backLog' then merged into 'queue'. The 'backLog' could be
    // either limit or unlimited.
    final BlockingQueue<ProcessingParam> backLog;
    
    private final ProcessesManager processesManager;
    private final ServerSocketInterface distributedInterface;
    
    private final Thread sender = new RetryingThread(this, 100);
    
    private final Thread merger = new RetryingThread(new Runnable() {
        
        public void run() {
            
            try {
                merge();
            }
            catch (InterruptedException e) {
                logger.log(Level.SEVERE, Utils.threadName() + "The thread is interuppted.", e);
                throw new RuntimeException(e);
            }
        }
    }, 100);
    
    public AlloyFeeder(final ProcessesManager processesManager, final ServerSocketInterface distributedInterface, final int bufferSize, final int backLogBufferSize) {
        
        super();
        
        this.processesManager = processesManager;
        this.distributedInterface = distributedInterface;
        
        if (bufferSize <= 0) {
            queue = new LinkedBlockingQueue<>();
        }
        else {
            queue = new LinkedBlockingQueue<>(bufferSize);
        }
        
        if (backLogBufferSize <= 0) {
            backLog = new LinkedBlockingQueue<>();
        }
        else {
            backLog = new LinkedBlockingQueue<>(backLogBufferSize);
        }
    }
    
    public ServerSocketInterface getSocketInterface() {
        
        return this.distributedInterface;
    }
    
    public void addProcessTask(final ProcessingParam p) throws InterruptedException {
        
        if (Configuration.IsInDeubbungMode)
            logger.log(Level.INFO, Utils.threadName() + " a request is added to be sent:" + p);
        // If the message has to be compressed, it will be compressed next.
        try {
            queue.put(p);
        }
        catch (InterruptedException e) {
            logger.log(Level.SEVERE, Utils.threadName() + "a new Alloy process message cannot be added to the queue:" + p, e);
            throw e;
        }
        if (Configuration.IsInDeubbungMode)
            logger.log(Level.INFO, Utils.threadName() + "a request is added to be sent and the queue size is:" + queue.size());
    }
    
    public void clear() {
        
        super.size = 0;
        queue.clear();
    }
    
    public void addGeneratedProp(final ProcessingParam item) {
        
        try {
            super.size++;
            this.addProcessTask(item);
        }
        catch (InterruptedException e) {
            logger.log(Level.SEVERE, Utils.threadName() + "Cannot add a new Alloy procesing param:" + item, e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    public long getSize() {
        
        return super.size;
    }
    
    public void addProcessTaskToBacklog(final ProcessingParam p) throws InterruptedException {
        
        if (Configuration.IsInDeubbungMode)
            logger.log(Level.INFO, Utils.threadName() + " a request is added to be merged:" + p);
        try {
            backLog.put(p);
        }
        catch (InterruptedException e) {
            logger.log(Level.SEVERE, Utils.threadName() + "a new Alloy process message cannot be added to the backlog:" + p, e);
            throw e;
        }
        if (Configuration.IsInDeubbungMode)
            logger.log(Level.INFO, Utils.threadName() + "a request is added to be merged and the backlog size is:" + backLog.size());
    }
    
    /**
     * Pick a request from the queue and send it to a process.
     * 
     * @throws InterruptedException
     */
    private void sendCommand() throws InterruptedException {
        
        if (Configuration.IsInDeubbungMode)
            logger.info(Utils.threadName() + "Queue size is: " + queue.size());
            
        ProcessingParam e;
        try {
            if (Configuration.IsInDeubbungMode)
                logger.info(Utils.threadName() + "Queue is: " + queue);
            // take a request, if something is in the queue. Otherwise the
            // thread parks here.
            e = queue.take();
            
            if (Configuration.IsInDeubbungMode)
                logger.info(Utils.threadName() + "Queue object: " + e);
                
        }
        catch (InterruptedException e1) {
            logger.log(Level.SEVERE, Utils.threadName() + "The command queue is interrupted.", e1);
            throw e1;
        }
        if (Configuration.IsInDeubbungMode)
            logger.info(Utils.threadName() + "Message is taken " + e);
            
        // Find a processor to process the request.
        AlloyProcess process = processesManager.getActiveRandomeProcess();
        
        if (Configuration.IsInDeubbungMode)
            logger.info(Utils.threadName() + "got a process " + e);
        try {
            
            RemoteCommand command = new ProcessIt(e, processesManager);
            this.distributedInterface.sendMessage(command, process.address);
            if (Configuration.IsInDeubbungMode)
                logger.info(Utils.threadName() + "Message sent to " + process.address);
        }
        catch (Throwable t) {
            logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] " + "The command cannot be sent.", t);
            // Put it back
            // queue.put(e);
            addProcessTaskToBacklog(e);
            throw t;
        }
        
    }
    
    private void merge() throws InterruptedException {
        
        if (Configuration.IsInDeubbungMode)
            logger.info(Utils.threadName() + "Backlog Queue size is: " + backLog.size());
            
        ProcessingParam e;
        try {
            // take a request, if something is in the queue. Otherwise the
            // thread parks here.
            e = backLog.take();
        }
        catch (InterruptedException e1) {
            logger.log(Level.SEVERE, Utils.threadName() + "The backlog queue is interrupted.", e1);
            throw e1;
        }
        if (Configuration.IsInDeubbungMode)
            logger.info(Utils.threadName() + "Message is taken " + e + " and backLog size is:" + backLog.size());
            
        addProcessTask(e);
        
        if (Configuration.IsInDeubbungMode)
            logger.info(Utils.threadName() + "The message is added to the main queue " + e + " and queue size is:" + queue.size() + " and the backlogqueue size is: " + backLog.size());
            
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
        
        return (new StringBuilder()).append("New messages added=").append(size).append("\nMessages to be sent=").append(queue.size()).append("\nMessages to be merged=").append(backLog.size()).toString();
    }
    
    @Override
    public void run() {
        
        try {
            sendCommand();
        }
        catch (InterruptedException e) {
            logger.log(Level.SEVERE, Utils.threadName() + "The thread is interuppted.", e);
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void actionOnNotStuck() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public int triesOnStuck() {
        
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public void actionOnStuck() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public String amIStuck() {
        
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public long isDelayed() {
        
        // TODO Auto-generated method stub
        return 0;
    }
    
}
