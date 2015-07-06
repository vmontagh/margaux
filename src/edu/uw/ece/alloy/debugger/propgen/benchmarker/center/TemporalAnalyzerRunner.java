package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.gen.BenchmarkRunner;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.TemporalPropertiesGenerator;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ProcessRemoteMonitor;

public class TemporalAnalyzerRunner {

	final static int  proccessNumber = Integer.valueOf(Configuration.getProp("processes_number"));
	final static int  RemoteMonitorInterval = Integer.valueOf(Configuration.getProp("remote_monitor_interval"));
	final static String ProcessLoggerConfig = Configuration.getProp("process_logger_config");
	final static  int SubMemory = Integer.valueOf(Configuration.getProp("sub_memory"));
	final static  int SubStack = Integer.valueOf(Configuration.getProp("sub_stak"));

	protected final static Logger logger = Logger.getLogger(TemporalAnalyzerRunner.class.getName()+"--"+Thread.currentThread().getName());

	ProcessesManager manager;
	AlloyFeeder feeder;
	ProcessRemoteMonitor monitor;
	ProcessRemoteMonitor.MonitorTimedoutProcesses timeoutMonitor;
	TemporalPropertiesGenerator analyzer = new TemporalPropertiesGenerator();

	Thread feederThread;
	Thread monitorThread;
	Thread timeoutMonitorThread;

	private final static TemporalAnalyzerRunner self = new TemporalAnalyzerRunner();

	private TemporalAnalyzerRunner() {

	}

	public final static  TemporalAnalyzerRunner getInstance(){
		return self;
	}

	public void start() throws Exception{
		manager = new ProcessesManager(proccessNumber, null, SubMemory, SubStack, "", ProcessLoggerConfig);

		feeder = new AlloyFeeder(manager);

		monitor = new ProcessRemoteMonitor(RemoteMonitorInterval, feeder, manager, manager.getProcessRemoteMonitorAddress());
		timeoutMonitor = monitor.new MonitorTimedoutProcesses();

		analyzer = new TemporalPropertiesGenerator();

		feederThread = new Thread(feeder); 
		monitorThread = new Thread(monitor);
		timeoutMonitorThread = new Thread(timeoutMonitor);

		monitorThread.start();
		timeoutMonitorThread.start();

		manager.addAllProcesses();
		//manager.activateAllProesses();

		feederThread.start();
		feederThread.setPriority(Thread.MAX_PRIORITY);

		//List<AlloyProcessingParam>  aps = Collections.EMPTY_LIST;
		try {
			//aps = analyzer.generateRemoteFiles();
			//The files are generated while fed into the feeder
			analyzer.generateAlloyProcessingParams(feeder);
		} catch (Err | IOException e1) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Unable to generate alloy files: ", e1);
			throw e1;
		}


		/*for(AlloyProcessingParam ap: aps){
			try {
				feeder.addProcessTask(ap);
				logger.info("["+Thread.currentThread().getName()+"]" + "a process is added");
			} catch (Exception e) {
				logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Unable to add alloy files: ", e);
			}
		}*/


	}


	public static void main(String[] args) throws Exception {


		TemporalAnalyzerRunner.getInstance().start();		

		//busywait
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		while(true){
			Thread.sleep(20000);
			logger.info("["+Thread.currentThread().getName()+"]" + "Main is alive.... ");
			logger.info("["+Thread.currentThread().getName()+"]" + "\n"+TemporalAnalyzerRunner.getInstance().manager.getStatus());
			logger.info("["+Thread.currentThread().getName()+"]" + "\n"+TemporalAnalyzerRunner.getInstance().monitor.getStatus());
			logger.info("["+Thread.currentThread().getName()+"]" + "\n"+TemporalAnalyzerRunner.getInstance().feeder.getStatus());
			Thread.currentThread().yield();
			System.gc();
		}
	}

}
