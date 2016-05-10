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
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ProcessIt;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteCommand;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.RemoteProcessMonitor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadMonitor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;
import edu.uw.ece.alloy.util.AsyncServerSocketInterface;
import edu.uw.ece.alloy.util.ServerSocketInterface;
import edu.uw.ece.alloy.util.events.CommandReceivedEventArgs;
import edu.uw.ece.alloy.util.events.CommandSentEventArgs;
import edu.uw.ece.alloy.util.events.EventArgs;
import edu.uw.ece.alloy.util.events.EventListener;
import edu.uw.ece.alloy.util.events.MessageReceivedEventArgs;
import edu.uw.ece.alloy.Compressor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBConnectionInfo;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBLogger;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.MySQLDBConnectionPool;

public abstract class DistributedRunner extends Runner_ implements EventListener<MessageReceivedEventArgs> {
    
    private final static Logger logger = Logger.getLogger(DistributedRunner.class.getName() + "--" + Thread.currentThread().getName());
    
    public static final int ProccessNumber = Integer.parseInt(Configuration.getProp("processes_number"));
    public static final int RemoteMonitorInterval = Integer.parseInt(Configuration.getProp("remote_monitor_interval"));
    public static final String ProcessLoggerConfig = Configuration.getProp("process_logger_config");
    public static final int SubMemory = Integer.parseInt(Configuration.getProp("sub_memory"));
    public static final int SubStack = Integer.parseInt(Configuration.getProp("sub_stak"));
    public static final int AlloyFeederBufferSize = Integer.parseInt(Configuration.getProp("alloy_feeder_buffer_size"));
    public static final int AlloyFeederBackLogBufferSize = Integer.parseInt(Configuration.getProp("alloy_feeder_backlog_buffer_size"));
    
    protected static DBConnectionInfo dbConnectionInfo = null;
    
    protected ProcessesManager manager;
    protected AlloyFeeder feeder;
    protected RemoteProcessMonitor taskMonitor;
    protected ServerSocketInterface distributedInterface;
    
    public DistributedRunner(final InetSocketAddress localSocket, final InetSocketAddress remoteSocket) {
        super(localSocket, remoteSocket);
    }
    
    public static DBConnectionInfo getDefaultConnectionInfo() throws SQLException {
        
        dbConnectionInfo = dbConnectionInfo != null ? dbConnectionInfo : DBLogger.createConfiguredSetuperObject(new MySQLDBConnectionPool(Compressor.EMPTY_DBCONNECTION)).makeNewConfiguredDatabaseLog();
        return dbConnectionInfo;
    }
    
    @Override
    public void start() {
        
        // Add all threads to be monitored
        this.addThreadToBeMonitored(this.feeder);
        this.addThreadToBeMonitored(this.taskMonitor);
        this.addThreadToBeMonitored(this.distributedInterface);
        
        // Start all the threads
        this.feeder.startThread();
        this.taskMonitor.startThread();
        this.distributedInterface.startThread();
        this.inputInterface.startThread();
        
        this.manager.addAllProcesses();
        
        // Start monitoring all threads
        this.selfMonitor.startMonitoring();
    }
    
    @Override
    public final void onEvent(Object sender, CommandReceivedEventArgs e) {
        
        RemoteCommand command = e.getCommand();
        
        if (Configuration.IsInDeubbungMode)
            logger.info(Utils.threadName() + "processCommand Enter:" + command);
            
        this.processAgentCommand(command);
        
        if (Configuration.IsInDeubbungMode)
            logger.info(Utils.threadName() + "processCommand Exit:" + command);
    }
    
    protected abstract void processAgentCommand(RemoteCommand command);
    
    @Override
    protected void init() {
        
        super.init();
        
        this.distributedInterface = new AsyncServerSocketInterface(manager.getProcessRemoteMonitorAddress(), null);
        this.distributedInterface.CommandReceived.addListener(this);        
        this.distributedInterface.CommandAttempt.addListener(new EventListener<CommandSentEventArgs>() {
            
            @Override
            public void onEvent(Object sender, CommandSentEventArgs e) {
                
                RemoteCommand command = e.getCommand();
                if (command instanceof ProcessIt) {
                    ProcessIt cmd = (ProcessIt) command;
                    taskMonitor.addMessage(e.getAddress(), cmd.param);
                }
            }
        });        
        this.distributedInterface.CommandFailed.addListener(new EventListener<CommandSentEventArgs>() {
            
            @Override
            public void onEvent(Object sender, CommandSentEventArgs e) {
                
                RemoteCommand command = e.getCommand();
                if (command instanceof ProcessIt) {
                    ProcessIt cmd = (ProcessIt) command;
                    taskMonitor.removeMessage(e.getAddress(), cmd.param);
                }
            }
        });
        
        this.manager = new ProcessesManager(ProccessNumber, null, SubMemory, SubStack, "", ProcessLoggerConfig);
        this.feeder = new AlloyFeeder(this.manager, this.distributedInterface, AlloyFeederBufferSize, AlloyFeederBackLogBufferSize);
        this.feeder.changePriority(Thread.MAX_PRIORITY);
        
        this.taskMonitor = new RemoteProcessMonitor(RemoteMonitorInterval, this.feeder, this.manager);        
    }
    
}
