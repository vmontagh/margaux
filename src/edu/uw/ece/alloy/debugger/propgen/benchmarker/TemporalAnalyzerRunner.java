package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.gen.BenchmarkRunner;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ProcessRemoteMonitor;

public class TemporalAnalyzerRunner {

	final static int  proccessNumber = 1;
	
	protected final static Logger logger = Logger.getLogger(TemporalAnalyzerRunner.class.getName()+"--"+Thread.currentThread().getName());
	
	ProcessesManager manager;
	AlloyFeeder feeder;
	ProcessRemoteMonitor monitor;
	ProcessRemoteMonitor.MonitorTimedoutProcesses timeoutMonitor;
	TemporalPropertiesAnalyzer analyzer = new TemporalPropertiesAnalyzer();
	
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
		manager = new ProcessesManager(proccessNumber, null, BenchmarkRunner.SubMemory, BenchmarkRunner.SubStack, "", "java.logger.remote.config");

		 feeder = new AlloyFeeder(manager);

		 monitor = new ProcessRemoteMonitor(20000, feeder, manager, manager.getProcessRemoteMonitorAddress());
		timeoutMonitor = monitor.new MonitorTimedoutProcesses();

		analyzer = new TemporalPropertiesAnalyzer();

		 feederThread = new Thread(feeder); 
		 monitorThread = new Thread(monitor);
		 timeoutMonitorThread = new Thread(timeoutMonitor);

		monitorThread.start();
		timeoutMonitorThread.start();

		manager.addAllProcesses();
		//manager.activateAllProesses();

		feederThread.start();
		feederThread.setPriority(Thread.MAX_PRIORITY);

		List<AlloyProcessingParam>  aps;
		try {
			aps = analyzer.generateRemoteFiles();
		} catch (Err | IOException e1) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Unable to generate alloy files: ", e1);
			throw e1;
		}

		int i = 0;
		for(AlloyProcessingParam ap: aps){
			if(++i > 201) break;
			try {
				feeder.addProcessTask(ap);
				logger.info("["+Thread.currentThread().getName()+"]" + "a process is added");
			} catch (Exception e) {
				logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Unable to add alloy files: ", e);
			}
		}
		

	}
	

	public static void main(String[] args) throws Exception {
		
		TemporalAnalyzerRunner.getInstance().start();		
		
		//busywait
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		while(true){
			Thread.sleep(100000);
			logger.info("["+Thread.currentThread().getName()+"]" + "Main is alive.... ");
			Thread.currentThread().yield();
		}
	}

}
