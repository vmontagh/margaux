/**
 * 
 */
package edu.uw.ece.alloy.util.events;

import java.net.InetSocketAddress;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteCommand;

/**
 * @author fikayo
 *
 */
public class CommandSentEventArgs extends EventArgs {

    private RemoteCommand command;
    private InetSocketAddress address;
    private AlloyProcessingParam param;
    
    public CommandSentEventArgs(RemoteCommand command) {
        this(command, null);
    }
    
    public CommandSentEventArgs(RemoteCommand command, InetSocketAddress address) {
        this(command, address, null);
    }
    
    public CommandSentEventArgs(RemoteCommand command, InetSocketAddress address, AlloyProcessingParam param) {
        this.command = command;
        this.address = address;
        this.param = param;
    }
    
    public RemoteCommand getCommand() {
        
        return this.command;
    }
    
    public InetSocketAddress getAddress() {
        
        return this.address;
    }
    
    public AlloyProcessingParam getParam() {
        
        return this.param;
    }
}
