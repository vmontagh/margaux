package edu.uw.ece.alloy.debugger.onborder;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Pair;
import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.debugger.mutate.ExampleFinder;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessorUtil;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ProcessDistributer;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ReadyMessage;
import edu.uw.ece.alloy.util.ServerSocketInterface;
import edu.uw.ece.alloy.util.events.MessageEventListener;
import edu.uw.ece.alloy.util.events.MessageReceivedEventArgs;
import edu.uw.ece.hola.agent.OnBorderAnalyzerRunner;

public class ExampleFinderByHolaTest {

    public class NotifiableInteger {

        int val = 0;
    }

    private ServerSocketInterface testingInterface;
    private InetSocketAddress testingHost;
    private InetSocketAddress runnerHost;
    private final File tmpLocalDirectory = new File("./tmp/");
    private final NotifiableInteger readynessReceived = new NotifiableInteger();

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
        runnerHost = ProcessorUtil.findEmptyLocalSocket();
        testingInterface = new ServerSocketInterface(testingHost, runnerHost);
        testingInterface.startThread();
        readynessReceived.val = 0;
    }
    
    /**
     * Given the file, all other required parameters for creating an
     * ExampleFinder object is created.
     * 
     * @param toBeAnalyzedCode
     * @return
     */
    protected ExampleFinder prepareExampleFinder(File toBeAnalyzedCode, File tmpLocalDirectory) throws Exception {

        // creating an instance of OnBorder runner using reflection.
        Class fooClazz = Class.forName("edu.uw.ece.hola.agent.OnBorderAnalyzerRunner");
        Constructor<OnBorderAnalyzerRunner> constructor = fooClazz.getDeclaredConstructor(InetSocketAddress.class, InetSocketAddress.class);
        constructor.setAccessible(true);
        OnBorderAnalyzerRunner runner = constructor.newInstance(runnerHost, testingHost);

        testingInterface.MessageReceived.addListener(new MessageEventListener<MessageReceivedEventArgs>() {

            @Override
            public void actionOn(ReadyMessage readyMessage, MessageReceivedEventArgs messageArgs) {

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

        return new ExampleFinderByHola(testingInterface, mockedProcessDistributer, tmpLocalDirectory);
    }

    @Test
    public void testExampleFinder() throws Exception {

    	String parentDir = "./models/debugger/";
//        File toBeAnalyzedCode = new File(parentDir, "bare_linked_list.als");
//        String parentDir = "./models/examples/toys/";
        File toBeAnalyzedCode = new File(parentDir, "dijkstra.als");

        System.out.println("\n======= Going In =======\n");
        ExampleFinder finder = prepareExampleFinder(toBeAnalyzedCode, tmpLocalDirectory);        
        Pair<Optional<String>, Optional<String>> result = finder.findOnBorderExamples(toBeAnalyzedCode, "ShowDijkstra", "not ShowDijkstra");
        System.out.println("\n======= I'm out =======\n");
        System.out.println("result: " + result);
    }

}
