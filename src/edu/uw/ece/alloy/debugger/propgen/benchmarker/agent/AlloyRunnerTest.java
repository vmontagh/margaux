package edu.uw.ece.alloy.debugger.propgen.benchmarker.agent;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.Dependency;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.IfPropertyToAlloyCode;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessorUtil;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.LivenessMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ReadyMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ResponseMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy.AlloyRequestMessage;
import edu.uw.ece.alloy.util.ServerSocketInterface;
import edu.uw.ece.alloy.util.Utils;
import edu.uw.ece.alloy.util.events.MessageEventListener;
import edu.uw.ece.alloy.util.events.MessageReceivedEventArgs;

/**
 * @author vajih
 *
 */
public class AlloyRunnerTest {

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
		// testingInterface.cancelThread();
	}

	@Test
	public void testStartAlloyReadyGetReadyness() {

		// Start an AlloyRunner and wait till a readyness message is received.
		AlloyRunner runner = new AlloyRunner(runnerHost, testingHost);
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
		AlloyRunner runner = new AlloyRunner(runnerHost, testingHost);
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
			assertTrue(0 < readynessReceived.val);
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testExecution() {
		AlloyRunner runner = new AlloyRunner(runnerHost, testingHost);
		runner.initiate();

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
						print("a resonse is recieved", responseMessage + "");
						++responseReceived.val;
						synchronized (responseReceived) {
							responseReceived.notify();
						}
					}
				});

		runner.start();

		synchronized (readynessReceived) {
			try {
				readynessReceived.wait(2000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				fail();
			}
		}

		File depFile1 = new File("tmp/testing", "dep1.als");
		File depFile2 = new File("tmp/testing", "dep2.als");

		try {
			Util.writeAll(depFile1.getAbsolutePath(), "dep1 content");
			Util.writeAll(depFile2.getAbsolutePath(), "dep2 content");
		} catch (Err e) {
			e.printStackTrace();
			fail();
		}

		Dependency dep1 = Dependency.EMPTY_DEPENDENCY.createIt(depFile1,
				Utils.readFile(depFile1.getAbsolutePath()));
		Dependency dep2 = Dependency.EMPTY_DEPENDENCY.createIt(depFile2,
				Utils.readFile(depFile2.getAbsolutePath()));

		IfPropertyToAlloyCode coder = (IfPropertyToAlloyCode) IfPropertyToAlloyCode.EMPTY_CONVERTOR
				.createIt("pred pred_A[]{some A}", "pred pred_B[]{some B}", "pred_A[]",
						"pred_B[]", "pred_A", "pred_B",
						Arrays.asList(new Dependency[] { dep1, dep2 }),
						/*AlloyProcessingParam.EMPTY_PARAM,*/ "sig A{}\nsig B{}", " for 5",
						"field");
		/*AlloyProcessingParam param = coder.generate(UUID.randomUUID());*/
		AlloyProcessingParam param = new AlloyProcessingParam(UUID.randomUUID(), 0, coder );

		AlloyRequestMessage request = new AlloyRequestMessage(
				testingInterface.getHostProcess(), param);

		testingInterface.sendMessage(request);

		synchronized (responseReceived) {
			try {
				responseReceived.wait(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				fail();
			} finally {
				depFile1.delete();
				depFile2.delete();
			}
		}
	}

}
