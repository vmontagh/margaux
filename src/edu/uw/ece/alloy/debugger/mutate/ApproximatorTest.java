/**
 * 
 */
package edu.uw.ece.alloy.debugger.mutate;

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.Files;

import edu.mit.csail.sdg.alloy4.Util;
import edu.uw.ece.alloy.debugger.knowledgebase.BinaryImplicationLattic;
import edu.uw.ece.alloy.debugger.knowledgebase.ImplicationLattic;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessorUtil;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ExpressionAnalyzerRunner;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ExpressionAnalyzerRunnerTest.NotifiableInteger;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ProcessDistributer;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcessRecord;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ReadyMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ResponseMessage;
import edu.uw.ece.alloy.util.LazyFile;
import edu.uw.ece.alloy.util.ServerSocketInterface;
import edu.uw.ece.alloy.util.events.MessageEventListener;
import edu.uw.ece.alloy.util.events.MessageReceivedEventArgs;

/**
 * The class is used for testing Approximator and its methods
 * 
 * @author vajih
 *
 */
public class ApproximatorTest {

	public class NotifiableInteger {
		int val = 0;
	}

	ServerSocketInterface testingInterface;
	InetSocketAddress testingHost, runnerHost;
	final NotifiableInteger readynessReceived = new NotifiableInteger();
	final File tmpLocalDirectory = new File("tmp/testing");
	final File testingFile = new File(tmpLocalDirectory,
			"approximation_test.als");

	final long startTime = System.currentTimeMillis();

	public final void print(String... args) {
		final long current = System.currentTimeMillis() - startTime;
		System.out.print(current + " - ");
		for (String arg : args)
			System.out.print(arg + " ");
		System.out.println();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// setup an interface.
		testingHost = ProcessorUtil.findEmptyLocalSocket();
		runnerHost = ProcessorUtil.findEmptyLocalSocket();
		testingInterface = new ServerSocketInterface(testingHost, runnerHost);
		testingInterface.startThread();
		readynessReceived.val = 0;
		Files.copy(Approximator.RelationalPropModule, new File(tmpLocalDirectory,
				Approximator.RelationalPropModule.getName()));

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		testingFile.deleteOnExit();
		new File(tmpLocalDirectory, Approximator.RelationalPropModule.getName())
				.deleteOnExit();
	}

	/**
	 * Given the file, all other required parameters for creating an approximator
	 * object is created.
	 * 
	 * @param toBeAnalyzedCode
	 * @return
	 */
	protected Approximator prepareApproximator(File toBeAnalyzedCode,
			File tmpLocalDirectory) throws Exception {
		// creating an instance of Expression runner using reflection.
		Class fooClazz = Class.forName(
				"edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ExpressionAnalyzerRunner");
		Constructor<ExpressionAnalyzerRunner> constructor = fooClazz
				.getDeclaredConstructor(InetSocketAddress.class,
						InetSocketAddress.class);
		constructor.setAccessible(true);
		ExpressionAnalyzerRunner runner = constructor.newInstance(runnerHost,
				testingHost);

		testingInterface.MessageReceived
				.addListener(new MessageEventListener<MessageReceivedEventArgs>() {
					@Override
					public void actionOn(ReadyMessage readyMessage,
							MessageReceivedEventArgs messageArgs) {
						++readynessReceived.val;
						synchronized (readynessReceived) {
							readynessReceived.notify();
						}
					}
				});

		runner.start();

		synchronized (readynessReceived) {
			try {
				readynessReceived.wait();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				fail();
			}
		}

		assertTrue(readynessReceived.val > 0);

		ProcessDistributer mockedProcessDistributer = new ProcessDistributer() {

			@Override
			public RemoteProcess getRandomProcess() {
				return testingInterface.getRemoteProcess().get();
			}

			@Override
			public RemoteProcess getActiveRandomeProcess() {
				return testingInterface.getRemoteProcess().get();
			}
		};

		return new Approximator(testingInterface, mockedProcessDistributer,
				tmpLocalDirectory, toBeAnalyzedCode, Collections.emptyList());
	}

	@Test
	public void testStrongestConsistentApproximation() throws Exception {

		File toBeAnalyzedCode = testingFile;

		String testContent = "open relational_properties_tagged\n"
				+ "sig Node{nxt:  set  Node}\n"
				+ "pred A{ !totalOrder[nxt, Node, Node] }\n" + "pred C{}\n" + "run {}";

		Files.copy(Approximator.RelationalPropModule, new File(tmpLocalDirectory,
				Approximator.RelationalPropModule.getName()));

		Util.writeAll(toBeAnalyzedCode.getAbsolutePath(), testContent);

		Approximator approximator = prepareApproximator(toBeAnalyzedCode,
				tmpLocalDirectory);

		Set<String> strongetConsistentWithA = approximator
				.strongestConsistentApproximation("A", "nxt", "").stream().map(a -> a.a)
				.collect(Collectors.toSet());
		// It should contains all the sources
		Set<String> strongetConsistentWithC = approximator
				.strongestConsistentApproximation("C", "nxt", "").stream().map(a -> a.a)
				.collect(Collectors.toSet());

		// The weaker patterns that are particular to A and weaker than sources.
		// Whatever in the A_C is weaker than sources
		Set<String> A_C = new HashSet<>(strongetConsistentWithA);
		A_C.removeAll(strongetConsistentWithC);
		print("strongetConsistentWithA:",Arrays.asList( strongetConsistentWithA.toArray()).toString());
		print("strongetConsistentWithC:",Arrays.asList( strongetConsistentWithC.toArray()).toString());
		print("A_C:",Arrays.asList( A_C.toArray()).toString());
		for (String pattern : A_C) {
			print("pattern:", pattern);
			assertEquals(Arrays.asList("totalOrder"),
					approximator.strongerPatterns(pattern));
		}
	}

	@Test
	public void testWeakestInconsistentApproximation() throws Exception {

		File toBeAnalyzedCode = testingFile;

		String testContent = 
				  "open relational_properties_tagged\n"
				+ "sig Node{nxt:  set  Node}\n"
				+ "pred A{ !function[nxt, Node] }\n"
				+ "pred C{some nxt and no nxt}\n" 
				+ "run {}";

		Util.writeAll(toBeAnalyzedCode.getAbsolutePath(), testContent);

		Approximator approximator = prepareApproximator(toBeAnalyzedCode,
				tmpLocalDirectory);

		Set<String> weakestInconsistentWithA = approximator
				.weakestInconsistentApproximation("A", "nxt", "").stream().map(a -> a.a)
				.collect(Collectors.toSet());
		print("weakestInconsistentWithA:",Arrays.asList( weakestInconsistentWithA.toArray()).toString());
		
		Set<String> weakestInconsistentWithC = approximator
				.weakestInconsistentApproximation("C", "nxt", "").stream().map(a -> a.a)
				.collect(Collectors.toSet());
		print("weakestInconsistentWithC:",Arrays.asList( weakestInconsistentWithC.toArray()).toString());
		
		Set<String> A_C = new HashSet<>(weakestInconsistentWithA);
		A_C.removeAll(weakestInconsistentWithA);
		print("A_C:", Arrays.asList(A_C.toArray()).toString());
		for (String pattern : A_C) {
			print("pattern in A_C:", pattern);
			assertEquals(Arrays.asList("function"),
					approximator.weakerPatterns(pattern));
		}
	}

}
