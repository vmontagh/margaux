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
	
	public final InetSocketAddress localSocket;
	public final InetSocketAddress remoteSocket;
	
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
		
		this.selfMonitor = new ThreadMonitor(/*SelfMonitorInterval*/ 1 * 1000, 0);		
		this.manager = new ProcessesManager(ProccessNumber, null, SubMemory, SubStack, "", ProcessLoggerConfig);	
		this.feeder = new AlloyFeeder(this.manager, AlloyFeederBufferSize, AlloyFeederBackLogBufferSize);
		this.monitor = new ProcessRemoteMonitor(RemoteMonitorInterval, (AlloyFeeder) this.feeder, this.manager, this.manager.getProcessRemoteMonitorAddress());	
		this.propGenerator = new TemporalPropertiesGenerator((GeneratedStorage<AlloyProcessingParam>) this.feeder);
	
		this.addThreadToBeMonitored(this.feeder);
		this.addThreadToBeMonitored(this.monitor);
		this.addThreadToBeMonitored(this.propGenerator);
	
		this.monitor.openInterface();	
		this.manager.addAllProcesses();
	
		this.feeder.changePriority(Thread.MAX_PRIORITY);
		this.feeder.openInterface();
	
		this.propGenerator.openInterface();		

		this.selfMonitor.startThreads();
	}
	
	protected void addThreadToBeMonitored(ThreadToBeMonitored thread) {
		selfMonitor.addThreadToBeMonitored(thread);
	}
	

}
