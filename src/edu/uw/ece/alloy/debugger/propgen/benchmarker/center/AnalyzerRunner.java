package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.GeneratedStorage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.TemporalPropertiesGenerator;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ProcessRemoteMonitor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadMonitor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;

public abstract class AnalyzerRunner {
	
	public final int SelfMonitorInterval = Integer.parseInt(Configuration.getProp("self_monitor_interval"));
	
	public static final int ProccessNumber = Integer.parseInt(Configuration.getProp("processes_number"));
	public static final int RemoteMonitorInterval = Integer.parseInt(Configuration.getProp("remote_monitor_interval"));
	public static final String ProcessLoggerConfig = Configuration.getProp("process_logger_config");
	public static final int SubMemory = Integer.parseInt(Configuration.getProp("sub_memory"));
	public static final int SubStack = Integer.parseInt(Configuration.getProp("sub_stak"));
	public static final int AlloyFeederBufferSize = Integer.parseInt(Configuration.getProp("alloy_feeder_buffer_size"));
	public static final int AlloyFeederBackLogBufferSize = Integer.parseInt(Configuration.getProp("alloy_feeder_backlog_buffer_size"));
	
	protected final InetSocketAddress localSocket;
	protected final InetSocketAddress remoteSocket;
	
	protected ProcessesManager manager;
	protected ThreadToBeMonitored feeder;
	protected ThreadToBeMonitored monitor;
	protected ThreadToBeMonitored propGenerator;
	protected final List<ThreadToBeMonitored> monitoredThreads;
	
	protected ThreadMonitor selfMonitor;
	
	public AnalyzerRunner(final InetSocketAddress localSocket, final InetSocketAddress remoteSocket) {
		this.localSocket = localSocket;
		this.remoteSocket = remoteSocket;		
		this.monitoredThreads = new LinkedList<>();
	}
	
	@SuppressWarnings("unchecked")
	public void start() throws Exception {
		
		selfMonitor = new ThreadMonitor(/*SelfMonitorInterval*/ 1 * 1000, 0);
		
		manager = new ProcessesManager(ProccessNumber, null, SubMemory, SubStack, "", ProcessLoggerConfig);
	
		feeder = new AlloyFeeder(manager, AlloyFeederBufferSize, AlloyFeederBackLogBufferSize);
	
		monitor = new ProcessRemoteMonitor(RemoteMonitorInterval, (AlloyFeeder) feeder, manager, manager.getProcessRemoteMonitorAddress());
	
		propGenerator = new TemporalPropertiesGenerator((GeneratedStorage<AlloyProcessingParam>) feeder);
	
		monitoredThreads.add(feeder);
		monitoredThreads.add(monitor);
		monitoredThreads.add(propGenerator);
	
		monitor.startThread();
	
		manager.addAllProcesses();
		System.out.println("manager.processes->"+manager.processes);
		//manager.activateAllProesses();
	
		feeder.startThread();
		feeder.changePriority(Thread.MAX_PRIORITY);
	
		propGenerator.startThread();		

		selfMonitor.startThreads();
	}
	
	protected void addThreadToBeMonitored(ThreadToBeMonitored thread) {
		selfMonitor.addThreadToBeMonitored(thread);
	}
	

}
