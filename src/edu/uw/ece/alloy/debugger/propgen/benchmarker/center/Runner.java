/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.net.InetSocketAddress;
import java.util.List;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadMonitor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;
import edu.uw.ece.alloy.util.AsyncServerSocketInterface;
import edu.uw.ece.alloy.util.ServerSocketInterface;

/**
 * @author fikayo
 *        
 */
public abstract class Runner {
    
    public final int SelfMonitorInterval = Integer.parseInt(Configuration.getProp("self_monitor_interval"));
    
    protected final InetSocketAddress localSocket;
    protected final InetSocketAddress remoteSocket;
    
    protected ThreadMonitor selfMonitor;
    protected ServerSocketInterface inputInterface;
    
    public Runner(final InetSocketAddress localSocket, final InetSocketAddress remoteSocket) {
        this.localSocket = localSocket;
        this.remoteSocket = remoteSocket;
        
        this.init();
    }
    
    public abstract void start();
    
    protected List<ThreadToBeMonitored> getMonitoredThreads() {
        
        return this.selfMonitor.getMonitoredThreads();
    }
    
    protected ThreadMonitor getSelfMonitor() {
        
        return new ThreadMonitor(/* SelfMonitorInterval */ 1 * 1000, 0);
    }
    
    protected void addThreadToBeMonitored(ThreadToBeMonitored thread) {
        
        selfMonitor.addThreadToBeMonitored(thread);
    }
    
    protected void init() {
        
        this.selfMonitor = this.getSelfMonitor();
        
        // Create socket interface for whomever created this process
        this.inputInterface = new AsyncServerSocketInterface(this.localSocket, this.remoteSocket);
        this.addThreadToBeMonitored(this.inputInterface);
    }
}