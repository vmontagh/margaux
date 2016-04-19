/**
 * 
 */
package edu.uw.ece.alloy.debugger.onborder.propgen;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.DistributedRunner;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.AnalyzeExternalReady;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteCommand;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;
import edu.uw.ece.alloy.util.events.CommandReceivedEventArgs;
import edu.uw.ece.alloy.util.events.EventListener;
import edu.uw.ece.alloy.util.Utils;

/**
 * @author fikayo
 *         
 */
public class OnBorderRunner extends DistributedRunner {
    
    protected final static Logger logger = Logger.getLogger(OnBorderRunner.class.getName() + "--" + Thread.currentThread().getName());
    
    private Deque<Object> results = new ArrayDeque<>();
    
    /**
     * @param localSocket
     * @param remoteSocket
     */
    public OnBorderRunner(InetSocketAddress localSocket, InetSocketAddress remoteSocket) {
        super(localSocket, remoteSocket);
    }
    
    @Override
    public void start() {
        
        super.start();
        
        this.inputInterface.CommandReceived.addListener(new EventListener<CommandReceivedEventArgs>() {
            
            @Override
            public void onEvent(Object sender, CommandReceivedEventArgs e) {
                
                RemoteCommand command = e.getCommand();
                command.doAnalyze(feeder); // OnBorderRequest Command
            }
        });
        
        // Everything looks to be set. So send a ready message to the
        // remote listener.
        this.inputInterface.sendMessage(new AnalyzeExternalReady());
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.uw.ece.alloy.debugger.propgen.benchmarker.center.DistributedRunner#
     * processAgentCommand(edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.
     * RemoteCommand)
     */
    @Override
    protected void processAgentCommand(RemoteCommand command) {
        
        boolean last = command.processResult(this.results); // HolaCompleteCommand
                                                            // Command
        if (last) {
            // Remove command from remote task monitor
            command.holaProcessDone(this.taskMonitor, this.results);
            
            // Get final candidate in the deque.
            HolaProcessedResult finalResult = (HolaProcessedResult) results.peekLast();
            // TODO: Send done command back to debugger
        }
    }
    
    public static void main(String[] args) throws Exception {
        
        if (args.length < 4)
            throw new RuntimeException("Enter the port number");
            
        if (args.length > 4)
            throw new RuntimeException("Inappropriate number of inputs. Only enter the remote port number as an interger.");
            
        int localPort;
        int remotePort;
        InetAddress localIP;
        InetAddress remoteIP;
        
        try {
            localPort = Integer.parseInt(args[0]);
            localIP = InetAddress.getByName(args[1]);
            
            if (Configuration.IsInDeubbungMode)
                logger.info(Utils.threadName() + "The port is assigned to this process: " + localPort + " and the IP is: " + localIP);
                
            remotePort = Integer.parseInt(args[2]);
            remoteIP = InetAddress.getByName(args[3]);
            
            if (Configuration.IsInDeubbungMode)
                logger.info(Utils.threadName() + "The remote port is: " + remotePort + " and the IP is: " + remoteIP);
                
        }
        catch (NumberFormatException nfe) {
            logger.log(Level.SEVERE, Utils.threadName() + "The passed port is not acceptable: ", nfe.getMessage());
            throw new RuntimeException("The port number is not an integer: " + nfe);
        }
        catch (UnknownHostException uhe) {
            logger.log(Level.SEVERE, Utils.threadName() + "The passed IP is not acceptable: ", uhe.getMessage());
            throw new RuntimeException("The IP address is not acceptable: " + uhe);
        }
        
        final InetSocketAddress localSocket = new InetSocketAddress(localIP, localPort);
        final InetSocketAddress remoteSocket = new InetSocketAddress(remoteIP, remotePort);
        
        System.out.println("local::" + localSocket);
        System.out.println("remoteSocket::" + remoteSocket);
        
        OnBorderRunner runner = new OnBorderRunner(localSocket, remoteSocket);
        runner.start();
        
        // busy wait
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        final StringBuilder sb = new StringBuilder();
        while (true) {
            
            Thread.sleep(10000);
            sb.append(Utils.threadName() + "Main is alive....\n");
            
            for (ThreadToBeMonitored t : runner.getMonitoredThreads()) {
                System.out.println("t->" + t);
                System.out.println("t.getStatus()" + t.getStatus());
                sb.append(t.getStatus()).append("\n");
            }
            
            System.out.println(sb);
            
            // System.out.println("Approximation--------->"
            // + Approximator.getInstance().getDirectImpliedApproximation());
            logger.warning(sb.toString());
            sb.delete(0, sb.length() - 1);
            
            Thread.yield();
            System.gc();
        }
    }
    
}
