package edu.uw.ece.alloy.debugger.propgen.benchmarker.agent;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBConnectionInfo;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBConnectionPool;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBLogger;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.MySQLDBConnectionPool;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.AlloyProcessed;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;
import edu.uw.ece.alloy.util.Utils;

public abstract class PostProcess implements Runnable, ThreadToBeMonitored {
    
    public final static boolean doCompress = Boolean.valueOf(Configuration.getProp("doCompressAlloyParams"));
    protected final static Logger logger = Logger.getLogger(PostProcess.class.getName() + "--" + Thread.currentThread().getName());
    
    private final PostProcess nextAction;
    protected final BlockingQueue<ProcessedResult> results = new LinkedBlockingQueue<>();
    
    protected volatile AtomicInteger processed = new AtomicInteger(0);
    protected volatile AtomicInteger shadowProcessed = new AtomicInteger(-1);
    protected volatile AtomicInteger recoveredTry = new AtomicInteger(0);
    
    private Thread postProcessThread = new Thread(this);;
    
    public PostProcess() {
        this(null);
    }
    
    public PostProcess(PostProcess nextAction) {
        super();
        this.nextAction = nextAction;
        
    }
    
    public boolean isEmpty() {
        
        return results.isEmpty();
    }
    
    public String amIStuck() {
        
        return isDelayed() == 0 ? "" : "Processing PostProess" + getClass().getSimpleName() + " is stuck after processing " + processed + " messages.";
    }
    
    @Override
    public void cancelThread() {
        
        postProcessThread.interrupt();
    }
    
    @Override
    public void changePriority(int newPriority) {
        
        postProcessThread.setPriority(newPriority);
    }
    
    /**
     * If the delay condition is true, then it returns a message how many
     * messages are stuck in the queue. This function is useful for monitoring
     * the threads.
     * 
     * @return Non-empty message means a delay is recorded Empty message
     */
    public synchronized long isDelayed() {
        
        long result = 0;
        // monitor the socket
        if (shadowProcessed.get() == processed.get() && !isEmpty()) {
            // The executer does not proceeded.
            result = processed.longValue();
            // TODO manage to reset the socket thread
        }
        else {
            // The executer proceeded
            shadowProcessed.set(processed.get());
        }
        return result;
    }
    
    public int triesOnStuck() {
        
        return recoveredTry.get();
    }
    
    protected void doActionOnStuck() {
        
        if (Configuration.IsInDeubbungMode)
            logger.finer(Utils.threadName() + "");
    }
    
    public void actionOnStuck() {
        
        triesOnStuck();
    }
    
    public void startThread() {
        
        postProcessThread.start();
    }
    
    public void actionOnNotStuck() {
        
        if (Configuration.IsInDeubbungMode)
            logger.finer(Utils.threadName() + "");
    }
    
    public void doAction(final ProcessedResult result) throws InterruptedException {
        
        try {
            if (Configuration.IsInDeubbungMode)
                logger.info(Utils.threadName() + "Post process: " + result);
            results.put(result);
        }
        catch (InterruptedException e) {
            if (Configuration.IsInDeubbungMode)
                logger.info(Utils.threadName() + "Processing a new result is interupted: " + result);
            throw e;
        }
    }
    
    protected abstract void action(final ProcessedResult result) throws InterruptedException;
    
    protected void actionAndIncreament(final ProcessedResult result) throws InterruptedException {
        
        action(result);
        processed.incrementAndGet();
    }
    
    protected void doSerialAction(final ProcessedResult result) throws InterruptedException {
        
        this.actionAndIncreament(result);
        
        if (nextAction != null)
            nextAction.doSerialAction(result);
            
    }
    
    @Override
    public void run() {
        
        ProcessedResult result = null;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                
                result = results.take();
                if (Configuration.IsInDeubbungMode)
                    logger.info(Utils.threadName() + "Start a Post process: " + result);
                    
                doSerialAction(result);
                
            }
            
        }
        catch (InterruptedException e) {
            logger.severe(Utils.threadName() + "Processing a result is interrupted: " + result + " after processed " + processed + " results.");
        }
    }
    
    public void cancel() {
        
        Thread.currentThread().interrupt();
    }
    
    public static class FileWriter extends PostProcess {
        
        public FileWriter() {
            super();
        }
        
        public FileWriter(PostProcess socketWriter) {
            super(socketWriter);
        }
        
        @Override
        protected void action(ProcessedResult result) {
            
            String content = result.asRecordHeader() + ",propa,propb,op,sat" + "\n" + result.asRecord() + "," + result.params.alloyCoder.predNameA + "," + result.params.alloyCoder.predNameB + "," + result.params.alloyCoder.commandOperator() + "," + DBLogger.convertSATResult(result);
            
            if (Configuration.IsInDeubbungMode)
                logger.info(Utils.threadName() + "Start wirint on file: " + result + "   " + content);
                
            try {
                Util.writeAll(result.params.destPath().getAbsolutePath(), content);
                if (Configuration.IsInDeubbungMode)
                    logger.info(Utils.threadName() + "result is written in: " + result.params.destPath().getAbsolutePath());
            }
            catch (Err e) {
                logger.log(Level.SEVERE, Utils.threadName() + "Failed on storing the result: " + result, e);
            }
        }
        
    }
    
    public static class SocketWriter extends PostProcess {
        
        private final InetSocketAddress localAddress;
        private final InetSocketAddress remoteAddres;
        
        public SocketWriter(final InetSocketAddress localAddress, final InetSocketAddress remoteAddress) {
            this(null, localAddress, remoteAddress);
        }
        
        public SocketWriter(final PostProcess nextAction, final InetSocketAddress localAddress, final InetSocketAddress remoteAddress) {
            super(nextAction);
            this.localAddress = localAddress;
            this.remoteAddres = remoteAddress;
        }
        
        @Override
        protected void action(ProcessedResult result) throws InterruptedException {
            
            ProcessedResult updatedResult = result;
            
            AlloyProcessed command = new AlloyProcessed(this.localAddress, updatedResult);
            if (Configuration.IsInDeubbungMode)
                logger.info(Utils.threadName() + "Start sending a done message: " + command + " as the result is:" + result + " TO: " + remoteAddres);
                
            try {
                command.send(remoteAddres);
                if (Configuration.IsInDeubbungMode)
                    logger.info(Utils.threadName() + "message sent: " + command);
            }
            catch (InterruptedException e) {
                logger.severe(Utils.threadName() + "Sending the result is interrupted: " + result + " TO: " + remoteAddres);
                throw e;
            }
            
        }
        
    }
    
    public static class DBWriter extends PostProcess {
        
        // A map from url, that sent from server, to a connection pool.
        private final Map<DBConnectionInfo, DBConnectionPool> connections = new HashMap<>();
        
        private final InetSocketAddress localAddress;
        
        public DBWriter(final InetSocketAddress localAddress) {
            this.localAddress = localAddress;
        }
        
        protected DBConnectionPool getConnection(final DBConnectionInfo dBConnectionInfo) throws SQLException {
            
            if (!connections.containsKey(dBConnectionInfo)) {
                connections.put(dBConnectionInfo, new MySQLDBConnectionPool(dBConnectionInfo));
            }
            
            return connections.get(dBConnectionInfo);
        }
        
        @Override
        protected void action(ProcessedResult result) {
            
            try {
                (DBLogger.createDatabaseOperationsObject(getConnection(result.params.dBConnectionInfo))).insertResult(result, this.localAddress.toString());
            }
            catch (SQLException e) {
                logger.severe(Utils.threadName() + " Error happened in insertin the result into the database." + e);
            }
        }
    }
    
    public static class CleanAfterProccessed extends PostProcess {
        
        public CleanAfterProccessed() {
            super();
        }
        
        @Override
        protected void action(ProcessedResult result) {
            
            if (Configuration.IsInDeubbungMode)
                logger.info(Utils.threadName() + "The conent of the param is removed from the disk: " + result.params);
            result.params.removeContent();
            
        }
        
    }
    
}
