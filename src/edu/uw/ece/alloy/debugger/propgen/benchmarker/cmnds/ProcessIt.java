package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AgentExecuter;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyExecuter;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyProcessRunner;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.AlloyFeeder;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.AnalyzerRunner;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.DistributedRunner;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ProcessesManager;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.TemporalAnalyzerRunner;

public class ProcessIt extends RemoteCommand {
    
    /**
     * 
     */
    private static final long serialVersionUID = 8188777849612332517L;
    final static Logger logger = Logger.getLogger(ProcessIt.class.getName() + "--" + Thread.currentThread().getName());
    
    public final ProcessingParam param;
    public transient final ProcessesManager processesManager;
    
    public ProcessIt(ProcessingParam param, final ProcessesManager processesManager) {
        super();
        this.param = param;
        this.processesManager = processesManager;
    }
    
    @Override
    public void process(AgentExecuter executer, File tmpDirectory) {
        
        try {
            if (Configuration.IsInDeubbungMode)
                logger.fine("[" + Thread.currentThread().getName() + "] " + "Received a message: " + param);
                
            this.param = this.param.prepareToUse();
            
            // tmpDirectory is sent at the client side.
            this.param = this.param.changeTmpDirectory(tmpDirectory);
            
            if (this.param instanceof AlloyProcessingParam) {
                AlloyProcessingParam alloyParam = (AlloyProcessingParam) this.param;
                this.param = alloyParam.dumpAll();
            }
            
            executer.process(this.param);
            
            if (Configuration.IsInDeubbungMode) {
                logger.fine("[" + Thread.currentThread().getName() + "] " + "Prepared and queued: " + param);
            }
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] " + "Failed on prepare or execute the message: " + this.param, e);
            e.printStackTrace();
        }
    }
    
    @Override
    public String toString() {
        
        ProcessingParam param = this.param;
        /*
         * try { param = this.param; } catch (Exception e) {
         * logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " +
         * "Failed on decompressing the message: "+ param, e);
         * e.printStackTrace(); }
         */
        return "ProcessIt [param=" + param + "]";
    }
    
    /**
     * To be called by the broker
     * 
     * @param remoteAddres
     * @throws InterruptedException
     */
    public void send(final InetSocketAddress remoteAddres) throws InterruptedException {
        
        try {
            // Encoding the param.
            if (Configuration.IsInDeubbungMode)
                logger.info("[" + Thread.currentThread().getName() + "] " + "Sending a message: " + param);
                
            ProcessingParam param = this.param.prepareToSend();
            if (this.param instanceof AlloyProcessingParam) {
                AlloyProcessingParam alloyParam = (AlloyProcessingParam) this.param;
                param = alloyParam.changeDBConnectionInfo(DistributedRunner.getDefaultConnectionInfo());
            }
            
            RemoteCommand command = new ProcessIt(param, this.processesManager);
            command.sendMe(remoteAddres);
            
            if (Configuration.IsInDeubbungMode)
                logger.info("[" + Thread.currentThread().getName() + "] " + "prepared and sent: " + param);
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] " + "Failed on prepare or send the message: " + this.param, e);
            e.printStackTrace();
        }
        
        processesManager.recordAMessageSentCounter(remoteAddres);
        processesManager.IncreaseSentTasks(remoteAddres, 1);
        
    }
    
}
