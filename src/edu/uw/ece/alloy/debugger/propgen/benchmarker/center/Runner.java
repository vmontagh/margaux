/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.net.InetSocketAddress;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadMonitor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;
import edu.uw.ece.alloy.util.AsyncServerSocketInterface;
import edu.uw.ece.alloy.util.ServerSocketInterface;

/**
 * @author fikayo
 *
 */
public abstract class Runner {

    protected final InetSocketAddress localSocket;
    protected final InetSocketAddress remoteSocket;
    
    protected ThreadMonitor selfMonitor;
    protected ServerSocketInterface inputInterface;

    public Runner(final InetSocketAddress localSocket, final InetSocketAddress remoteSocket) {
        this.localSocket = localSocket;
        this.remoteSocket = remoteSocket;
        
        this.init();
    }

    private void init() {

        this.selfMonitor = new ThreadMonitor(/* SelfMonitorInterval */ 1 * 1000, 0);
        
        // Create socket interface for whomever created this process
        this.inputInterface = new AsyncServerSocketInterface(this.localSocket, this.remoteSocket);
        this.addThreadToBeMonitored(this.inputInterface);
    }
    
    protected void addThreadToBeMonitored(ThreadToBeMonitored thread) {
        
        selfMonitor.addThreadToBeMonitored(thread);
    }
}