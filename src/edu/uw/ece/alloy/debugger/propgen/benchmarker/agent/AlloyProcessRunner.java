package edu.uw.ece.alloy.debugger.propgen.benchmarker.agent;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.Runner;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteCommand;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadMonitor;
import edu.uw.ece.alloy.util.Utils;
import edu.uw.ece.alloy.util.events.CommandReceivedEventArgs;
import edu.uw.ece.alloy.util.events.EventListener;

public class AlloyProcessRunner extends Runner {
    
    public final boolean RemoveSourceAfter = Boolean.parseBoolean(Configuration.getProp("do_clean_source_after_computation"));
    public final int SelfMonitorRetryAttempt = Integer.valueOf(Configuration.getProp("self_monitor_retry_attempt"));
    // final static int SelfMonitorDoneRatio =
    // Integer.valueOf(Configuration.getProp("self_monitor_done_ratio"));
    
    public final static File tmpDirectoryRoot = new File(Configuration.getProp("temporary_directory"));
    public final File tmpDirectory;
    
    protected final static Logger logger = Logger.getLogger(AlloyProcessRunner.class.getName() + "--" + Thread.currentThread().getName());
    
    private AlloyExecuter executer;
    private PostProcess.FileWriter fileWriter;
    private PostProcess.SocketWriter socketWriter;
    private PostProcess.DBWriter dbWriter;
    private PostProcess.CleanAfterProccessed cleanAfterProcessed;
    
    private static AlloyProcessRunner self = null;
    
    private AlloyProcessRunner(final InetSocketAddress localPort, final InetSocketAddress remotePort) {
        
        super(localPort, remotePort);
        
        // Set the local tmpDirectory
        this.tmpDirectory = new File(tmpDirectoryRoot, String.valueOf(localPort.getPort()));
        this.setUpFolders();
    }
    
    public static AlloyProcessRunner getInstance(final InetSocketAddress localPort, final InetSocketAddress remotePort) {
        
        if (self != null) {
            throw new RuntimeException("Alloy Processor cannot be changed.");
        }
        
        self = new AlloyProcessRunner(localPort, remotePort);
        return self;
    }
    
    public static AlloyProcessRunner getInstance() {
        
        if (self == null) {
            throw new RuntimeException("The remote port is initialized.");
        }
        
        return self;
    }
    
    /*
     * public void resetExecuterThread(){ if(executerThread != null &&
     * executerThread.isAlive()){
     * 
     * if(executer.isSpilledTimeout()){
     * logger.info("["+Thread.currentThread().getName()+"]" +
     * " The AlloyExecuter thread is interrupted again and again. Replace the thread now. "
     * ); executer.stopMe(); executerThread.interrupt(); executerThread = new
     * Thread(executer); executerThread.start(); }else{
     * logger.info("["+Thread.currentThread().getName()+"]" +
     * " Interrupt the AlloyExecuter thread. "); executerThread.interrupt(); }
     * 
     * } }
     */
    
    @Override
    public void start() {
        
        if (Configuration.IsInDeubbungMode)
            logger.info(Utils.threadName() + " Starting to create Alloy Processing objects");
                    
        // Register threads to be monitored
        this.addThreadToBeMonitored(this.executer);
        this.addThreadToBeMonitored(this.socketWriter);
        this.addThreadToBeMonitored(this.fileWriter);
        this.addThreadToBeMonitored(this.dbWriter);
        
        this.executer.startThread();
        this.socketWriter.startThread();
        this.fileWriter.startThread();
        this.dbWriter.startThread();
        
        this.selfMonitor.startMonitoring();
        
        if (RemoveSourceAfter) {
            this.cleanAfterProcessed = new PostProcess.CleanAfterProccessed();
            this.executer.registerPostProcess(this.cleanAfterProcessed);
            this.addThreadToBeMonitored(this.cleanAfterProcessed);
            this.cleanAfterProcessed.startThread();
        }
        
        this.inputInterface.CommandReceived.addListener(new EventListener<CommandReceivedEventArgs>() {
            
            @Override
            public void onEvent(Object sender, CommandReceivedEventArgs e) {
                
                RemoteCommand command = e.getCommand();
                command.process(executer, tmpDirectory); // AnalyzeExternalRequest Command
            }
        });
        
        this.inputInterface.startThread();
        this.inputInterface.sendReadynessMessage();
    }
    
    @Override
    protected ThreadMonitor getSelfMonitor() {
        
        return new ThreadMonitor(SelfMonitorInterval, 3);
    }
    
    @Override
    protected void init() {
        
        super.init();
        
        this.executer = AlloyExecuter.instantiate(this.inputInterface);
        this.fileWriter = new PostProcess.FileWriter();
        this.socketWriter = new PostProcess.SocketWriter(this.localSocket, this.remoteSocket);
        this.dbWriter = new PostProcess.DBWriter(this.localSocket);
        
        this.executer.registerPostProcess(this.fileWriter);
        this.executer.registerPostProcess(this.socketWriter);
        this.executer.registerPostProcess(this.dbWriter);
    }
    
    private void setUpFolders() {
        
        if (tmpDirectory.exists()) {
            try {
                if (Configuration.IsInDeubbungMode)
                    logger.info(Utils.threadName() + " exists and has to be recreated." + tmpDirectory.getCanonicalPath());
                Utils.deleteRecursivly(tmpDirectory);
            }
            catch (IOException e) {
                logger.log(Level.SEVERE, Utils.threadName() + "Unable to delete the previous files.", e);
            }
        }
        
        // After deleting the temp directory create a new one.
        if (!tmpDirectory.mkdir())
            throw new RuntimeException("Can not create a new directory");
            
    }
    
    public static void main(String[] args) {
        
        if (Configuration.IsInDeubbungMode)
            logger.info(Utils.threadName() + "The alloy process agent has started.");
            
        if (args.length < 4)
            throw new RuntimeException("Enter the port number");
            
        if (args.length > 4)
            throw new RuntimeException("Inappropriate number of inputs. Only enter the remote port number as an interger.");
            
        int localPort;
        int remotePort;
        InetAddress localIP;
        InetAddress remoteIP;
        
        try {
            localPort = Integer.parseInt(args[0]);
            localIP = InetAddress.getByName(args[1]);
            if (Configuration.IsInDeubbungMode)
                logger.info(Utils.threadName() + "The port is assigned to this process: " + localPort + " and the IP is: " + localIP);
                
            remotePort = Integer.parseInt(args[2]);
            remoteIP = InetAddress.getByName(args[3]);;
            if (Configuration.IsInDeubbungMode)
                logger.info(Utils.threadName() + "The remote port is: " + remotePort + " and the IP is: " + remoteIP);
                
        }
        catch (NumberFormatException nfe) {
            logger.log(Level.SEVERE, Utils.threadName() + "The passed port is not acceptable: ", nfe.getMessage());
            throw new RuntimeException("The port number is not an integer: " + nfe);
        }
        catch (UnknownHostException uhe) {
            logger.log(Level.SEVERE, Utils.threadName() + "The passed IP is not acceptable: ", uhe.getMessage());
            throw new RuntimeException("The IP address is not acceptable: " + uhe);
        }
        
        final InetSocketAddress localSocket = new InetSocketAddress(localIP, localPort);
        final InetSocketAddress remoteSocket = new InetSocketAddress(remoteIP, remotePort);
        
        AlloyProcessRunner.getInstance(localSocket, remoteSocket).start();
        
        // busy wait
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        while (true) {
            try {
                Thread.sleep(100000);
            }
            catch (InterruptedException e) {
                logger.log(Level.SEVERE, Utils.threadName() + "Main loop is interrupted ", e);
                break;
            }
            
            if (Configuration.IsInDeubbungMode)
                logger.info(Utils.threadName() + "Main is alive.... ");
            
            Thread.yield();
        }
        
    }
    
}

// package edu.uw.ece.alloy.debugger.propgen.benchmarker.agent;
//
// import java.io.File;
// import java.io.IOException;
// import java.net.InetAddress;
// import java.net.InetSocketAddress;
// import java.net.UnknownHostException;
// import java.util.logging.Level;
// import java.util.logging.Logger;
//
// import edu.mit.csail.sdg.gen.alloy.Configuration;
// import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadMonitor;
// import edu.uw.ece.alloy.util.Utils;
//
// public class AlloyProcessRunner {
//
// final public int SelfMonitorInterval =
// Integer.parseInt(Configuration.getProp("self_monitor_interval"));
// final public boolean RemoveSourceAfter =
// Boolean.parseBoolean(Configuration.getProp("do_clean_source_after_computation"));
// final public int SelfMonitorRetryAttempt =
// Integer.valueOf(Configuration.getProp("self_monitor_retry_attempt"));
// //final static int SelfMonitorDoneRatio =
// Integer.valueOf(Configuration.getProp("self_monitor_done_ratio"));
// final public static File tmpDirectoryRoot = new File(
// Configuration.getProp("temporary_directory") );
// final public File tmpDirectory;
//
// /** The PID is as the port number that the processor is listening to. */
// public final InetSocketAddress PID;
// public final InetSocketAddress remotePort;
//
// protected final static Logger logger =
// Logger.getLogger(AlloyProcessRunner.class.getName()+"--"+Thread.currentThread().getName());
//
// //private Thread frontThread;
// //private Thread executerThread;
// //private Thread fileThread;
// //private Thread socketThread;
// //private Thread dbThread;
// //private Thread cleanerThread;
// //private Thread watchdogThread;
//
// private FrontAlloyProcess front;
// private AlloyExecuter executer;
// private PostProcess.FileWrite fileWriter;
// private PostProcess.SocketWriter socketWriter;
// private PostProcess.DBWriter dbWriter;
// private PostProcess.CleanAfterProccessed cleanAfterProcessed;
// private ThreadMonitor watchdog;
//
// private static AlloyProcessRunner self = null;
//
//
// public static AlloyProcessRunner getInstance(final InetSocketAddress
// localPort, final InetSocketAddress remotePort){
// if(self != null)
// throw new RuntimeException("Alloy Processoer cannot be changed.");
// self = new AlloyProcessRunner(localPort, remotePort);
// return self;
// }
//
// public static AlloyProcessRunner getInstance(){
// if(self == null)
// throw new RuntimeException("The remote port is initialized.");
// return self;
// }
//
// private AlloyProcessRunner(final InetSocketAddress localPort, final
// InetSocketAddress remotePort) {
// PID = localPort;
// this.remotePort = remotePort;
// //Set the local tmpDirectory
// tmpDirectory = new File(tmpDirectoryRoot,String.valueOf(PID.getPort()));
// setUpFolders();
// }
//
// /*public void resetExecuterThread(){
// if(executerThread != null && executerThread.isAlive()){
//
// if(executer.isSpilledTimeout()){
// logger.info("["+Thread.currentThread().getName()+"]" + " The AlloyExecuter
// thread is interrupted again and again. Replace the thread now. ");
// executer.stopMe();
// executerThread.interrupt();
// executerThread = new Thread(executer);
// executerThread.start();
// }else{
// logger.info("["+Thread.currentThread().getName()+"]" + " Interrupt the
// AlloyExecuter thread. ");
// executerThread.interrupt();
// }
//
// }
// }*/
//
// public void startThreads(){
//
//
// if(Configuration.IsInDeubbungMode)
// logger.info("["+Thread.currentThread().getName()+"] "+" Starting to create
// Alloy Processing objects");
//
// executer = AlloyExecuter.getInstance();
//
// fileWriter = new PostProcess.FileWrite();
// executer.resgisterPostProcess(fileWriter);
//
// socketWriter = new PostProcess.SocketWriter( remotePort);
// executer.resgisterPostProcess(socketWriter);
//
// dbWriter = new PostProcess.DBWriter();
// executer.resgisterPostProcess(dbWriter);
//
// watchdog = new ThreadMonitor(SelfMonitorInterval, 3);
// //register threads to be monitored
//
// watchdog.addThreadToBeMonitored(executer);
// watchdog.addThreadToBeMonitored(socketWriter);
// watchdog.addThreadToBeMonitored(fileWriter);
// watchdog.addThreadToBeMonitored(dbWriter);
//
// executer.startThread();
// socketWriter.startThread();
// fileWriter.startThread();
// dbWriter.startThread();
//
// watchdog.startMonitoring();
//
// if(RemoveSourceAfter){
// cleanAfterProcessed = new PostProcess.CleanAfterProccessed();
// executer.resgisterPostProcess(cleanAfterProcessed);
// watchdog.addThreadToBeMonitored(cleanAfterProcessed);
// cleanAfterProcessed.startThread();
// }
//
//
// front = new FrontAlloyProcess(PID,remotePort,executer);
// front.startThreads();
// }
//
//
// private void setUpFolders(){
//
// if( tmpDirectory.exists() ){
// try {
// if(Configuration.IsInDeubbungMode)
// logger.info("["+Thread.currentThread().getName()+"] " +" exists and has to be
// recreated." +tmpDirectory.getCanonicalPath());
// Utils.deleteRecursivly(tmpDirectory);
// } catch (IOException e) {
// logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Unable
// to delete the previous files.", e);
// }
// }
//
// //After deleting the temp directory create a new one.
// if (!tmpDirectory.mkdir())
// throw new RuntimeException("Can not create a new directory");
//
// }
//
//
// public static void main(String[] args) {
//
// if(Configuration.IsInDeubbungMode)
// logger.info("["+Thread.currentThread().getName()+"] "+"The process is
// started.");
//
// if(args.length < 4)
// throw new RuntimeException("Enter the port number");
//
// if(args.length > 4)
// throw new RuntimeException("Inappropriate number of inputs. Only enter the
// remote port number as an interger.");
//
// int localPort;
// int remotePort;
// InetAddress localIP;
// InetAddress remoteIP;
//
// try{
// localPort = Integer.parseInt(args[0]);
// localIP = InetAddress.getByName(args[1]);
// if(Configuration.IsInDeubbungMode)
// logger.info("["+Thread.currentThread().getName()+"] "+"The port is assigned
// to this process: "+localPort+ " and the IP is: "+ localIP);
//
// remotePort = Integer.parseInt(args[2]);
// remoteIP = InetAddress.getByName(args[3]);;
// if(Configuration.IsInDeubbungMode)
// logger.info("["+Thread.currentThread().getName()+"] "+"The remote port is:
// "+remotePort + " and the IP is: "+ remoteIP);
//
// }catch(NumberFormatException nfe){
// logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]" + "The
// passed port is not acceptable: ", nfe.getMessage());
// throw new RuntimeException("The port number is not an integer: "+nfe);
// }catch(UnknownHostException uhe){
// logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]" + "The
// passed IP is not acceptable: ", uhe.getMessage());
// throw new RuntimeException("The IP address is not acceptable: "+uhe);
// }
//
//
// final InetSocketAddress localSocket = new InetSocketAddress(localIP,
// localPort);
// final InetSocketAddress remoteSocket = new InetSocketAddress(remoteIP,
// remotePort);
//
// AlloyProcessRunner.getInstance(localSocket, remoteSocket).startThreads();
//
// //busywait
// Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
// while(true){
// try {
// Thread.sleep(100000);
// } catch (InterruptedException e) {
// logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]" + "Main
// loop is interrupted ", e);
// break;
// }
// if(Configuration.IsInDeubbungMode)
// logger.info("["+Thread.currentThread().getName()+"]" + "Main is alive.... ");
// Thread.currentThread().yield();
// }
//
// }
//
//
// }
