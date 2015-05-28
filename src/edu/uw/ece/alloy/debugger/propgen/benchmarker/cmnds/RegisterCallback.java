package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.net.InetSocketAddress;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.FrontAlloyProcess;

public class RegisterCallback extends RemoteCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4829776596375928307L;
	final InetSocketAddress hostAddress;
	
	public RegisterCallback(final InetSocketAddress hostAddress) {
		this.hostAddress = hostAddress;
		
	}
	
	public void findRemoteAddress( FrontAlloyProcess front ) {
		front.setRemoteAddress(hostAddress);
	}

	@Override
	public String toString() {
		return "RegisterCallback [hostAddress=" + hostAddress + "]";
	}
	
	
	
}
