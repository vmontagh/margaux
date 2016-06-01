package edu.uw.ece.alloy.debugger.onborder;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Pair;
import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.debugger.mutate.ExampleFinder;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessorUtil;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ExpressionAnalyzerRunner;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcessManager;
import edu.uw.ece.alloy.util.LazyFile;
import edu.uw.ece.alloy.util.ServerSocketInterface;
import edu.uw.ece.hola.agent.OnBorderAnalyzerRunner;

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
		File tmpLocalDirectory = new File("tmp/");
		File toBeAnalyzedCode = new File(parentDir,	"bare_linked_list.als");
		
		ServerSocketInterface interfacE = new ServerSocketInterface(testingHost);
		RemoteProcessManager onborderProcessManager = new RemoteProcessManager(testingHost, OnBorderAnalyzerRunner.class, ProccessNumber);
		onborderProcessManager.addAllProcesses();
		
		ExampleFinder finder = new ExampleFinderByHola(interfacE, onborderProcessManager, tmpLocalDirectory);
		System.out.println("\n======= Going In =======\n");
		Pair<Optional<String>, Optional<String>> result = finder.findOnBorderExamples(toBeAnalyzedCode, "", "");
		System.out.println("\n======= I'm out =======\n");
		System.out.println("result: " + result);
	}

}
