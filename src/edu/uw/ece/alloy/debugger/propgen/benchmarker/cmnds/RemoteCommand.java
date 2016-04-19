package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.GeneratedStorage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AgentExecuter;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyExecuter;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyProcessedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.FrontAlloyProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ProcessesManager;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.RemoteProcessMonitor;
import edu.uw.ece.alloy.util.Utils;

public abstract class RemoteCommand implements Serializable {
    
    private static final long serialVersionUID = 131434234232434L;
    
    final static Logger logger = Logger.getLogger(RemoteCommand.class.getName() + "--" + Thread.currentThread().getName());;
    
    public RemoteCommand() {
    }
    
    public static void sendACommand(final InetSocketAddress remoteAddres, Object command) throws InterruptedException {
        
        final Logger logger = Logger.getAnonymousLogger();
        
        AsynchronousSocketChannel clientSocketChannel = null;
        ObjectOutputStream oos = null;
        try {
            clientSocketChannel = AsynchronousSocketChannel.open();
            
            Future<Void> connectFuture = clientSocketChannel.connect(remoteAddres);
            connectFuture.get(); // Wait until connection is done.
            OutputStream os = Channels.newOutputStream(clientSocketChannel);
            oos = new ObjectOutputStream(os);
            oos.writeObject(command);
        }
        catch (IOException | ExecutionException e) {
            logger.log(Level.SEVERE, Utils.threadName() + "Failed on sending the done message " + command + " TO =" + remoteAddres, e);
            throw new RuntimeException("Unseccesful sending message " + e.getMessage());
        }
        catch (InterruptedException e) {
            logger.log(Level.SEVERE, Utils.threadName() + "Sending the done message is interrupted: " + command + " TO =" + remoteAddres, e);
            throw e;
        }
        finally {
            if (oos != null) {
                try {
                    oos.close();
                }
                catch (IOException e) {
                    logger.log(Level.SEVERE, Utils.threadName() + "Failed to close the output stream" + command + " TO =" + remoteAddres, e);
                }
            }
            
            if (clientSocketChannel != null && clientSocketChannel.isOpen()) {
                try {
                    clientSocketChannel.close();
                }
                catch (IOException e) {
                    logger.log(Level.SEVERE, Utils.threadName() + "Failed to close the socket" + command + " TO =" + remoteAddres, e);
                }
            }
        }
    }
    
    @SuppressWarnings("static-access")
    public void sendMe(final InetSocketAddress remoteAddres) throws InterruptedException {
        
        this.sendACommand(remoteAddres, this);
    }
    
    public void findRemoteAddress(FrontAlloyProcess front) {
        
        if (Configuration.IsInDeubbungMode)
            logger.finer(Utils.threadName() + "Inappropriate call for findRemoteAddress");
    }
    
    public void terminate(final AsynchronousSocketChannel param) {
        
        if (Configuration.IsInDeubbungMode)
            logger.finer(Utils.threadName() + "Inappropriate call for terminate");
    }
    
    public void process(AgentExecuter executer, File tmpDirectory) {
        
        if (Configuration.IsInDeubbungMode)
            logger.finer(Utils.threadName() + "Inappropriate call for process");
    }
    
    public void killProcess(ProcessesManager manager) {
        
        if (Configuration.IsInDeubbungMode)
            logger.finer(Utils.threadName() + "Inappropriate call for process");
    }
    
    public void updatePorcessorLiveness(final ProcessesManager manager) {
        
        if (Configuration.IsInDeubbungMode)
            logger.finer(Utils.threadName() + "Inappropriate call for process");
    }
    
    public void processDone(final RemoteProcessMonitor monitor) {
        
        if (Configuration.IsInDeubbungMode)
            logger.finer(Utils.threadName() + "Inappropriate call for process");
    }
    
    public void holaProcessDone(RemoteProcessMonitor monitor, final Deque<Object> queue) {
        
        if (Configuration.IsInDeubbungMode)
            logger.finer(Utils.threadName() + "Inappropriate call for process");
    }
    
    public void activateMe(ProcessesManager manager) {
        
        if (Configuration.IsInDeubbungMode)
            logger.finer(Utils.threadName() + "Inappropriate call for process");
    }
    
    public void doAnalyze(final GeneratedStorage<ProcessingParam> generatedStorage) {
        
        if (Configuration.IsInDeubbungMode)
            logger.finer(Utils.threadName() + "Inappropriate call for process");
    }
    
    public void patternExtractionDone(Object lock) {
        
        if (Configuration.IsInDeubbungMode)
            logger.finer(Utils.threadName() + "Inappropriate call for process");
    }
    
    public void storeResult(final Queue<AlloyProcessedResult> queue) {
        
        if (Configuration.IsInDeubbungMode)
            logger.finer(Utils.threadName() + "Inappropriate call for process");
    }
    
    public void readyToUse(final Queue<AlloyProcessedResult> queue) {
        
        if (Configuration.IsInDeubbungMode)
            logger.finer(Utils.threadName() + "Inappropriate call for process");
    }
    
    public boolean processResult(final Deque<Object> queue) {
        
        if (Configuration.IsInDeubbungMode)
            logger.finer(Utils.threadName() + "Inappropriate call for process");
        return true;
    }
}
