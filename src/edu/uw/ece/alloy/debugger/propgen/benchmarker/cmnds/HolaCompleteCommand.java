package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.net.InetSocketAddress;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.onborder.propgen.HolaProcessedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.RemoteProcessMonitor;
import edu.uw.ece.alloy.util.Utils;

public class HolaCompleteCommand extends RemoteCommand {
    
    protected final static Logger logger = Logger.getLogger(HolaCompleteCommand.class.getName() + "--" + Thread.currentThread().getName());
    private static final long serialVersionUID = 7812337897314648833L;
    
    public final InetSocketAddress PID;
    public final HolaProcessedResult result;
    
    public HolaCompleteCommand(final InetSocketAddress pID, final HolaProcessedResult result) {
        super();
        PID = pID;
        this.result = result;
    }
    
    public void send(final InetSocketAddress remoteAddres) throws InterruptedException {
        
        if (Configuration.IsInDeubbungMode)
            logger.fine(Utils.threadName() + "Sending a response: pID= " + PID + " result=" + result.toString());
            
        try {
            
            this.sendMe(remoteAddres);
            
            if (Configuration.IsInDeubbungMode)
                logger.fine(Utils.threadName() + "Response is sent: pID= " + PID + " result=" + result.toString());
                
        }
        catch (Exception e) {
            
            logger.log(Level.SEVERE, Utils.threadName() + "Failed on prepare or send the message: " + this.result, e);
            e.printStackTrace();
        }
        
    }
    
    @Override
    public boolean processResult(final Deque<Object> queue) {
        
        if (result.getInstance() != null) {
            
            queue.push(result);
            
            System.out.println("=====================================================");
            System.out.println("Instance Result: \n    " + result.getInstance().toString().replace("\n", "\n" + "    ") + "");
            System.out.println("=====================================================");
            
        }
        
        return result.isLast();
    }
        
    @Override
    public String toString() {
        
        return "HolaComplete [PID=" + PID + ", result=" + result.toString() + "]";
    }
}
