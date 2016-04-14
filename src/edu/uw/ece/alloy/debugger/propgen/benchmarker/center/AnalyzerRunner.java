package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.GeneratedStorage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.TemporalPropertiesGenerator;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.RemoteProcessMonitor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadMonitor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;
import edu.uw.ece.alloy.util.AsyncServerSocketInterface;
import edu.uw.ece.alloy.Compressor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBConnectionInfo;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBLogger;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.MySQLDBConnectionPool;

public abstract class AnalyzerRunner {
	
	public final int SelfMonitorInterval = Integer.parseInt(Configuration.getProp("self_monitor_interval"));
	
	public static final int ProccessNumber = Integer.parseInt(Configuration.getProp("processes_number"));
	public static final int RemoteMonitorInterval = Integer.parseInt(Configuration.getProp("remote_monitor_interval"));
	public static final String ProcessLoggerConfig = Configuration.getProp("process_logger_config");
	public static final int SubMemory = Integer.parseInt(Configuration.getProp("sub_memory"));
	public static final int SubStack = Integer.parseInt(Configuration.getProp("sub_stak"));
	public static final int AlloyFeederBufferSize = Integer.parseInt(Configuration.getProp("alloy_feeder_buffer_size"));
	public static final int AlloyFeederBackLogBufferSize = Integer.parseInt(Configuration.getProp("alloy_feeder_backlog_buffer_size"));

	protected static DBConnectionInfo dbConnectionInfo = null;
	
	public final InetSocketAddress localSocket;
	public final InetSocketAddress remoteSocket;
	
	protected ProcessesManager manager;
	protected ThreadToBeMonitored feeder;
	protected ThreadToBeMonitored monitor;
	protected ThreadToBeMonitored propGenerator;
	protected final List<ThreadToBeMonitored> monitoredThreads;
	
	protected ThreadMonitor selfMonitor;
	protected AsyncServerSocketInterface analyzerInterface;
	
	public AnalyzerRunner(final InetSocketAddress localSocket, final InetSocketAddress remoteSocket) {
		this.localSocket = localSocket;
		this.remoteSocket = remoteSocket;		
		this.monitoredThreads = new LinkedList<>();
	}
	
	public static DBConnectionInfo getDefaultConnectionInfo() throws SQLException{
		dbConnectionInfo = dbConnectionInfo != null ? dbConnectionInfo : DBLogger.createConfiguredSetuperObject(new MySQLDBConnectionPool(Compressor.EMPTY_DBCONNECTION)).makeNewConfiguredDatabaseLog();
		return dbConnectionInfo ;
	}
	
	@SuppressWarnings("unchecked")
	public void start() throws Exception {
		
		this.selfMonitor = new ThreadMonitor(/*SelfMonitorInterval*/ 1 * 1000, 0);		
		this.manager = new ProcessesManager(ProccessNumber, null, SubMemory, SubStack, "", ProcessLoggerConfig);	
		this.feeder = new AlloyFeeder(this.manager, AlloyFeederBufferSize, AlloyFeederBackLogBufferSize);
		this.monitor = new RemoteProcessMonitor(RemoteMonitorInterval, (AlloyFeeder) this.feeder, this.manager, this.manager.getProcessRemoteMonitorAddress());	
		this.propGenerator = new TemporalPropertiesGenerator((GeneratedStorage<AlloyProcessingParam>) this.feeder);
		this.analyzerInterface = new AsyncServerSocketInterface(this.localSocket, this.remoteSocket);
		
		this.addThreadToBeMonitored(this.analyzerInterface);
		this.addThreadToBeMonitored(this.feeder);
		this.addThreadToBeMonitored(this.monitor);
		this.addThreadToBeMonitored(this.propGenerator);

		this.feeder.changePriority(Thread.MAX_PRIORITY);
		this.feeder.startThread();
		this.monitor.startThread();	
		this.propGenerator.startThread();		
		this.analyzerInterface.startThread();
		
		this.manager.addAllProcesses();	
		this.selfMonitor.startThreads();
	}
	
	protected void addThreadToBeMonitored(ThreadToBeMonitored thread) {
		selfMonitor.addThreadToBeMonitored(thread);
	}
	

}
