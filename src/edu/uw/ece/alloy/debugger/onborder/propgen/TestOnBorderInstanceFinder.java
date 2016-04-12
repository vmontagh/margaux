package edu.uw.ece.alloy.debugger.onborder.propgen;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.junit.Test;

import edu.uw.ece.alloy.util.Utils;

public class TestOnBorderInstanceFinder {

	@Test
	public void testMain() throws UnknownHostException {
		
		String file = "/home/fikayo/workspace/OnBorderInstanceFinder/models/binary_tree.als";
		
		InetAddress localAddress = InetAddress.getByName(InetAddress.getLocalHost().getHostAddress());
		InetSocketAddress address1 = Utils.findEmptyLocalSocket(localAddress);
		InetSocketAddress address2 = Utils.findEmptyLocalSocket(localAddress);
		
		String[] args = new String[6];
		args[0] = "" + address1.getPort();
		args[1] = "" + address1.getAddress().getHostAddress();		
		args[2] = "" + address2.getPort();
		args[3] = "" + address2.getAddress().getHostAddress();		
		args[4] = file;
		args[5] = "resources/debugger.expriment.config";
		
		OnBorderInstanceFinder.main(args);
	}
}
