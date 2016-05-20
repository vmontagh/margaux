package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.IfPropertyToAlloyCode;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessorUtil;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.LivenessMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ReadyMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ResponseMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger.PatternProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger.PatternRequestMessage;
import edu.uw.ece.alloy.util.LazyFile;
import edu.uw.ece.alloy.util.ServerSocketInterface;
import edu.uw.ece.alloy.util.events.MessageEventListener;
import edu.uw.ece.alloy.util.events.MessageReceivedEventArgs;

public class ExpressionAnalyzerRunnerTest {

	public class NotifiableInteger {
		int val = 0;
	}

	ServerSocketInterface testingInterface;
	InetSocketAddress testingHost, runnerHost;

	final NotifiableInteger livenessReceived = new NotifiableInteger();
	final NotifiableInteger readynessReceived = new NotifiableInteger();
	final NotifiableInteger responseReceived = new NotifiableInteger();

	final long startTime = System.currentTimeMillis();

	public final void print(String... args) {
		final long current = System.currentTimeMillis() - startTime;
		System.out.print(current + " - ");
		for (String arg : args)
			System.out.print(arg + " ");
		System.out.println();
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		// setup an interface.
		testingHost = ProcessorUtil.findEmptyLocalSocket();
		runnerHost = ProcessorUtil.findEmptyLocalSocket();
		testingInterface = new ServerSocketInterface(testingHost, runnerHost);
		testingInterface.startThread();
		livenessReceived.val = 0;
		readynessReceived.val = 0;
		responseReceived.val = 0;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testStartAlloyReadyGetReadyness() {
		ExpressionAnalyzerRunner runner = new ExpressionAnalyzerRunner(runnerHost,
				testingHost);
		runner.initiate();

		testingInterface.MessageReceived
				.addListener(new MessageEventListener<MessageReceivedEventArgs>() {
					@Override
					public void actionOn(ReadyMessage readyMessage,
							MessageReceivedEventArgs messageArgs) {
						print("a readyness message is received", readyMessage + "");
						++readynessReceived.val;
						synchronized (readynessReceived) {
							readynessReceived.notify();
						}
					}
				});

		runner.start();

		try {
			synchronized (readynessReceived) {
				readynessReceived.wait(2000);
			}
			assertTrue(readynessReceived.val > 0);
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testStartAlloyReadyGetLiveness() {

		// Start an AlloyRunner and wait till a readyness message is received.
		ExpressionAnalyzerRunner runner = new ExpressionAnalyzerRunner(runnerHost,
				testingHost);
		runner.initiate();

		testingInterface.MessageReceived
				.addListener(new MessageEventListener<MessageReceivedEventArgs>() {
					@Override
					public void actionOn(LivenessMessage livenessMessage,
							MessageReceivedEventArgs messageArgs) {
						print("a liveness message is received", livenessMessage + "");
						++livenessReceived.val;
						synchronized (livenessReceived) {
							livenessReceived.notify();
						}
					}
				});

		runner.start();

		try {
			synchronized (livenessReceived) {
				livenessReceived.wait(2000);
			}
			assertTrue(0 < livenessReceived.val);
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testExecutionOnePattern() throws Err {
		testExecution(/*
									 * "pred bijective[r: univ->univ, right: set univ] {\n" +
									 * "\tall x: right | one r.x\n" + "}\n" +
									 * "pred acyclic[r: univ->univ, left: set univ] {\n" +
									 * "\tall x: left | x !in x.^r" + "}\n" +
									 */ "pred rootedOne [r: univ->univ, left:univ, right: univ]{"
				+ "\tone root:left | right in root.*r" + "}"
				// + "fun max [es: set univ, next: univ->univ ]: lone univ { es -
				// es.^(~(next)) }\n"
				// + "fun min [es: set univ, next: univ->univ ]: lone univ { es -
				// es.^(next) }"
				+ "");
	}

	@Test
	public void testExecutionTwoPatterns() throws Err {
		testExecution(/*
									 * "pred bijective[r: univ->univ, right: set univ] {\n" +
									 * "\tall x: right | one r.x\n" + "}\n" +
									 * "pred acyclic[r: univ->univ, left: set univ] {\n" +
									 * "\tall x: left | x !in x.^r" + "}\n" +
									 */

				"pred weaklyConnected [ r :univ->univ,  left:univ, right:univ ] {\n"
						+ "\tall d: right | all g: left - d  | d in g.^(r + ~r)\n" + "}\n"
						+ "pred rootedOne [r: univ->univ, left:univ, right: univ]{"
						+ "\tone root:left | right in root.*r" + "}"
		// + "fun max [es: set univ, next: univ->univ ]: lone univ { es -
		// es.^(~(next)) }\n"
		// + "fun min [es: set univ, next: univ->univ ]: lone univ { es -
		// es.^(next) }"
		);

	}

	public void testExecution(String patterns) throws Err {
		ExpressionAnalyzerRunner runner = new ExpressionAnalyzerRunner(runnerHost,
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

					@Override
					public void actionOn(ResponseMessage responseMessage,
							MessageReceivedEventArgs messageArgs) {
						print(responseMessage + "");
						++responseReceived.val;
						synchronized (responseReceived) {
							responseReceived.notify();
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

		File tmpLocalDirectory = new File("tmp/testing");
		// toBeAnalyzedCode
		LazyFile toBeAnalyzedCode = new LazyFile(tmpLocalDirectory,
				"toBeAnalyzedCode.als");
		Util.writeAll(toBeAnalyzedCode.getAbsolutePath(),
				"sig A{r: A}\n pred p[]{ no ~r & r} run{}");

		LazyFile relationalPropModuleOriginal = new LazyFile(tmpLocalDirectory,
				"relationalPropModuleOriginal.als");
		Util.writeAll(relationalPropModuleOriginal.getAbsolutePath(), patterns);

		LazyFile temporalPropModuleOriginal = new LazyFile(tmpLocalDirectory,
				"temporalPropModuleOriginal.als");
		Util.writeAll(temporalPropModuleOriginal.getAbsolutePath(),
				"open relational_lib as relational_properties\n"
						+ "pred OrdDcrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptNon_[r: univ->univ->univ, left, middle, right: univ, left_first: univ, left_next: univ->univ, right_first: univ, right_next: univ->univ]{\n"
						+ "\tall left': left - relational_properties/last[left,left_next] |let left'' = left'.left_next |\n"
						+ "\tlet c = middle.(left'.r) |let c' = middle.(left''.r) | let delta = c' - c |  (c in c' ) and   (c' !in c ) and   (some delta implies(some c implies relational_properties/lt[relational_properties/max[delta,right_next],relational_properties/min[c,right_next],right_next] ) )\n"
						+ "}");

		LazyFile relationalLib = new LazyFile(tmpLocalDirectory,
				"relational_lib.als");
		Util.writeAll(relationalLib.getAbsolutePath(), "module relational_lib\n"
				+ "fun last[elem: univ, next:univ->univ]: one univ { elem - (next.elem) }\n"
				+ "fun max [es: set univ, next: univ->univ ]: lone univ { es - es.^(~(next)) }\n"
				+ "fun min [es: set univ, next: univ->univ ]: lone univ { es - es.^(next) }\n"
				+ "pred lt [e1, e2: univ, next:univ->univ ] { e1 in this/prevs[e2, next] }\n"
				+ "fun prevs [e: univ, next:univ->univ ]: set univ { e.^(~(next)) }\n");

		Map<String, LazyFile> files = new HashMap<>();

		files.put("toBeAnalyzedCode", toBeAnalyzedCode);
		files.put("relationalPropModuleOriginal", relationalPropModuleOriginal);
		files.put("temporalPropModuleOriginal", temporalPropModuleOriginal);
		files.put("relationalLib", relationalLib);

		PatternProcessingParam param = new PatternProcessingParam(0,
				tmpLocalDirectory, UUID.randomUUID(), Long.MAX_VALUE, "r",
				IfPropertyToAlloyCode.EMPTY_CONVERTOR, "p", " for 5", files);

		PatternRequestMessage request = new PatternRequestMessage(
				testingInterface.getHostProcess(), param);

		testingInterface.sendMessage(request);

		synchronized (responseReceived) {
			try {
				responseReceived.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
				fail();
			} finally {
				toBeAnalyzedCode.delete();
				relationalPropModuleOriginal.delete();
				temporalPropModuleOriginal.delete();
				relationalLib.delete();
			}
		}

		assertTrue(responseReceived.val > 0);
	}

}
