package edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs;

import static org.junit.Assert.*;

import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.AlloyFeeder;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ProcessesManager;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ProcessReady;

public class ProcessRemoteMonitorTester {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test_monitor_willnot_stop_incaseof_error() {

		ProcessesManager pm = new ProcessesManager(1, "", 1, 2, "", "");
		AlloyFeeder af = new AlloyFeeder(pm, 0, 0);

		RemoteProcessMonitor pmr =  new RemoteProcessMonitor(1000, af, pm, 4000);

		pmr.startThread();

		assertTrue(pmr.getStatus().contains("Monitor is working? false"));

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertTrue(pmr.getStatus().contains("Monitor is working? true"));

		for(int i = 1; i <= 99; ++i){

			ProcessReady pReady = new ProcessReady(new InetSocketAddress(300));

			try {
				pReady.sendMe(new InetSocketAddress(4000));
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			
			
			//assertTrue(pmr.getStatus().contains("Monitor is working? false"));

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			assertTrue(pmr.getStatus().contains("Monitor is working? true"));
		}
		assertTrue(pmr.getStatus().contains("Monitor is working? true"));
	}


}
