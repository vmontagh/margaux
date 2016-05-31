package edu.uw.ece.alloy.debugger.onborder.propgen;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.junit.Test;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessorUtil;

public class OnBorderInstanceFinderTest {

	@Test
	public void testMain() throws UnknownHostException {
		
		String file = "/home/fikayo/workspace/OnBorderInstanceFinder/models/binary_tree.als";
		
		InetSocketAddress address1 = ProcessorUtil.findEmptyLocalSocket();
		InetSocketAddress address2 = ProcessorUtil.findEmptyLocalSocket();
		
		String[] args = new String[6];
		args[0] = "" + address1.getPort();
		args[1] = "" + address1.getAddress().getHostAddress();		
		args[2] = "" + address2.getPort();
		args[3] = "" + address2.getAddress().getHostAddress();		
		args[4] = file;
		args[5] = "resources/debugger.expriment.config";
		
//		OnBorderInstanceFinder.main(args);
	}
}
