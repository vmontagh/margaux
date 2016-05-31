package edu.uw.ece.alloy.debugger.onborder;

import java.io.File;
import java.net.InetSocketAddress;

import org.junit.Before;
import org.junit.Test;

import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.debugger.mutate.ExampleFinder;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessorUtil;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ExpressionAnalyzerRunner;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcessManager;
import edu.uw.ece.alloy.util.LazyFile;
import edu.uw.ece.alloy.util.ServerSocketInterface;

public class ExampleFinderByHolaTest {

	private InetSocketAddress testingHost;

	final long startTime = System.currentTimeMillis();
	final static int ProccessNumber = Integer.parseInt(Configuration.getProp("analyzer_processes_number"));
	
	public final void print(String... args) {
		final long current = System.currentTimeMillis() - startTime;
		System.out.print(current + " - ");
		for (String arg : args)
			System.out.print(arg + " ");
		System.out.println();
	}
	
	@Before
	public void setUp() throws Exception {
		testingHost = ProcessorUtil.findEmptyLocalSocket();
	}
	
	@Test
	public void testExampleFinder() {
		
		String parentDir = "/home/ooodunay/workspace/alloy4/models/debugger/min_dist/";
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new File(parentDir,	"bare_linked_list.als");
		
		ServerSocketInterface interfacE = new ServerSocketInterface(testingHost);
		RemoteProcessManager onborderProcessManager = new RemoteProcessManager(testingHost, OnBorderAnalyzerRunner.class, ProccessNumber);
		ExampleFinder finder = new ExampleFinderByHola(interfacE, onborderProcessManager);
	}

}
