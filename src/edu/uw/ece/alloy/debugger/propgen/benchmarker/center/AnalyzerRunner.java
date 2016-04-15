package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.GeneratedStorage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.TemporalPropertiesGenerator;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteCommand;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.RemoteProcessMonitor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadMonitor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;
import edu.uw.ece.alloy.util.AsyncServerSocketInterface;
import edu.uw.ece.alloy.util.ServerSocketInterface;
import edu.uw.ece.alloy.util.events.CommandReceivedEventArgs;
import edu.uw.ece.alloy.util.events.EventArgs;
import edu.uw.ece.alloy.util.events.EventListener;
import edu.uw.ece.hola.agent.Program;
import edu.uw.ece.hola.agent.Utils;
import edu.uw.ece.alloy.Compressor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBConnectionInfo;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBLogger;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.MySQLDBConnectionPool;

public abstract class AnalyzerRunner implements EventListener<CommandReceivedEventArgs> {
    
    private final static Logger logger = Logger.getLogger(AnalyzerRunner.class.getName() + "--" + Thread.currentThread().getName());
    
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
    protected AlloyFeeder feeder;
    protected RemoteProcessMonitor monitor;
    protected ThreadToBeMonitored propGenerator;
    protected final List<ThreadToBeMonitored> monitoredThreads;
    
    protected ThreadMonitor selfMonitor;
    protected ServerSocketInterface agentInterface;
    protected ServerSocketInterface analyzerInterface;
    
    public AnalyzerRunner(final InetSocketAddress localSocket, final InetSocketAddress remoteSocket) {
        this.localSocket = localSocket;
        this.remoteSocket = remoteSocket;
        this.monitoredThreads = new LinkedList<>();
    }
    
    public static DBConnectionInfo getDefaultConnectionInfo() throws SQLException {
        
        dbConnectionInfo = dbConnectionInfo != null ? dbConnectionInfo : DBLogger.createConfiguredSetuperObject(new MySQLDBConnectionPool(Compressor.EMPTY_DBCONNECTION)).makeNewConfiguredDatabaseLog();
        return dbConnectionInfo;
    }
    
    public void start() throws Exception {
                
        this.manager = new ProcessesManager(ProccessNumber, null, SubMemory, SubStack, "", ProcessLoggerConfig);
        this.feeder = new AlloyFeeder(this.manager, AlloyFeederBufferSize, AlloyFeederBackLogBufferSize);
        this.monitor = new RemoteProcessMonitor(RemoteMonitorInterval, this.feeder, this.manager);
        this.propGenerator = new TemporalPropertiesGenerator(this.feeder);
        
        // Get socket interface for remote agents from the remote monitor
        this.agentInterface = this.monitor.getSocketInterface();
        this.agentInterface.CommandReceived.addListener(this);
        
        // Set the socket interface for the feeder to be the same as that for the remote agents
        this.feeder.setSocketInterface(this.agentInterface);
        this.feeder.changePriority(Thread.MAX_PRIORITY);
        
        // Create socket interface for the analyzer that created this process
        this.analyzerInterface = new AsyncServerSocketInterface(this.localSocket, this.remoteSocket);

        // Initialize self monitor
        this.selfMonitor = new ThreadMonitor(/* SelfMonitorInterval */ 1 * 1000, 0);
        
        // Add all threads to be monitored
        this.addThreadToBeMonitored(this.feeder);
        this.addThreadToBeMonitored(this.monitor);
        this.addThreadToBeMonitored(this.propGenerator);
        this.addThreadToBeMonitored(this.agentInterface);
        this.addThreadToBeMonitored(this.analyzerInterface);
        
        // Start all the threads
        this.feeder.startThread();
        this.monitor.startThread();
        this.propGenerator.startThread();
        this.agentInterface.startThread();
        this.analyzerInterface.startThread();
        
        this.manager.addAllProcesses();

        // Start monitoring all threads
        this.selfMonitor.startMonitoring();
    }
    
    @Override
    public void onEvent(Object sender, CommandReceivedEventArgs e) {
        
        RemoteCommand command = e.getCommand();
        
        if (Configuration.IsInDeubbungMode)
            logger.info(Utils.threadName() + "processCommand Enter:" + command);
        
        command.killProcess(this.manager);
        command.updatePorcessorLiveness(this.manager);
        command.processDone(this.monitor);
        command.activateMe(this.manager);
        
        if (Configuration.IsInDeubbungMode)
            logger.info(Utils.threadName() + "processCommand Exit:" + command);
    }
    
    protected void addThreadToBeMonitored(ThreadToBeMonitored thread) {
        
        selfMonitor.addThreadToBeMonitored(thread);
    }
    
}
