package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import edu.uw.ece.alloy.Compressor;
import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBConnectionInfo;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBLogger;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.GeneratedStorage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.MySQLDBConnectionPool;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.TemporalPropertiesGenerator;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ProcessRemoteMonitor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadDelayToBeMonitored;

public class TemporalAnalyzerRunner extends AnalyzerRunner {

	final static int  proccessNumber = Integer.parseInt(Configuration.getProp("processes_number"));
	final static int  RemoteMonitorInterval = Integer.parseInt(Configuration.getProp("remote_monitor_interval"));
	final static String ProcessLoggerConfig = Configuration.getProp("process_logger_config");
	final static int SubMemory = Integer.parseInt(Configuration.getProp("sub_memory"));
	final static int SubStack = Integer.parseInt(Configuration.getProp("sub_stak"));
	final static int AlloyFeederBufferSize = Integer.parseInt(Configuration.getProp("alloy_feeder_buffer_size")); 
	final static int AlloyFeederBackLogBufferSize = Integer.parseInt(Configuration.getProp("alloy_feeder_backlog_buffer_size")); 


	protected final static Logger logger = Logger.getLogger(TemporalAnalyzerRunner.class.getName()+"--"+Thread.currentThread().getName());

	ProcessesManager manager;
	ThreadDelayToBeMonitored feeder;
	ThreadDelayToBeMonitored monitor;
	ThreadDelayToBeMonitored propGenerator;
	

	List<ThreadDelayToBeMonitored> monitoredThreads = new LinkedList<>();

	//Thread feederThread;
	//Thread monitorThread;
	//Thread timeoutMonitorThread;

	private final static TemporalAnalyzerRunner self = new TemporalAnalyzerRunner();

	private TemporalAnalyzerRunner() {

	}

	public final static  TemporalAnalyzerRunner getInstance(){
		return self;
	}




	@SuppressWarnings("unchecked")
	public void start() throws Exception{
		manager = new ProcessesManager(proccessNumber, null, SubMemory, SubStack, "", ProcessLoggerConfig);

		feeder = new AlloyFeeder(manager, AlloyFeederBufferSize, AlloyFeederBackLogBufferSize);

		monitor = new ProcessRemoteMonitor(RemoteMonitorInterval, (AlloyFeeder) feeder, manager, manager.getProcessRemoteMonitorAddress());

		propGenerator = new TemporalPropertiesGenerator((GeneratedStorage<AlloyProcessingParam>) feeder);

		monitoredThreads.add(feeder);
		monitoredThreads.add(monitor);
		monitoredThreads.add(propGenerator);

		monitor.startThread();

		manager.addAllProcesses();
		//manager.activateAllProesses();

		feeder.startThread();
		feeder.changePriority(Thread.MAX_PRIORITY);

		propGenerator.startThread();
		
	}


	public static void main(String[] args) throws Exception {


		TemporalAnalyzerRunner.getInstance().start();		

		//busywait
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		final StringBuilder sb = new StringBuilder();
		while(true){
			Thread.sleep(20000);
			sb.append("["+Thread.currentThread().getName()+"]" + "Main is alive....\n");
			TemporalAnalyzerRunner.getInstance().monitoredThreads.stream().forEach(m->sb.append(m.getStatus()).append("\n"));

			System.out.println(sb);
			logger.warning(sb.toString());
			sb.delete(0, sb.length()-1);

			Thread.currentThread().yield();
			System.gc();
		}
	}

}
