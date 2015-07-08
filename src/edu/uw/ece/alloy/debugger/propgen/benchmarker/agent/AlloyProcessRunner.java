package edu.uw.ece.alloy.debugger.propgen.benchmarker.agent;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ProcessSelfMonitor;

public class AlloyProcessRunner {

	final static int  SelfMonitorInterval = Integer.valueOf(Configuration.getProp("self_monitor_interval"));
	//final static int  SelfMonitorRetryAttempt = Integer.valueOf(Configuration.getProp("self_monitor_retry_attempt"));
	//final static int  SelfMonitorDoneRatio = Integer.valueOf(Configuration.getProp("self_monitor_done_ratio"));

	
	//The PID is as the port number that the processor is listening to.
	public final InetSocketAddress PID;
	public final InetSocketAddress remotePort;
	
	protected final static Logger logger = Logger.getLogger(AlloyProcessRunner.class.getName()+"--"+Thread.currentThread().getName());

	private Thread frontThread;
	private Thread executerThread;
	private Thread fileThread;
	private Thread socketThread;
	private Thread dbThread;
	private Thread watchdogThread; 
	
	private FrontAlloyProcess front;
	private AlloyExecuter executer;
	private PostProcess.FileWrite fileWriter;
	private PostProcess.SocketWriter socketWriter;
	private PostProcess.DBWriter dbWriter;
	private ProcessSelfMonitor watchdog;
	
	private static AlloyProcessRunner self = null;

	
	public static AlloyProcessRunner getInstance(final InetSocketAddress localPort, final InetSocketAddress remotePort){
		if(self != null)
			throw new RuntimeException("Alloy Processoer cannot be changed.");
		self = new AlloyProcessRunner(localPort, remotePort);
		return self;
	}

	public static AlloyProcessRunner getInstance(){
		if(self == null)
			throw new RuntimeException("The remote port is initialized.");
		return self;
	}
	
	private AlloyProcessRunner(final InetSocketAddress localPort, final InetSocketAddress remotePort) {
		PID = localPort;
		this.remotePort = remotePort; 
	}
	
	public void resetExecuterThread(){
		if(executerThread != null && executerThread.isAlive()){
			
			if(executer.isSpilledTimeout()){
				logger.info("["+Thread.currentThread().getName()+"]" + " The AlloyExecuter thread is interrupted again and again. Replace the thread now. ");
				executer.stop();
				executerThread.interrupt();
				executerThread = new Thread(executer);
				executerThread.start();
			}else{
				logger.info("["+Thread.currentThread().getName()+"]" + " Interrupt the AlloyExecuter thread. ");
				executerThread.interrupt();
			}
			
		}
	}
	
	public void start(){
		
		
		logger.info("["+Thread.currentThread().getName()+"] "+" Starting to create Alloy Processing objects");
		
		executer = AlloyExecuter.getInstance();
		front = new FrontAlloyProcess(PID,remotePort,executer);
		frontThread = new Thread(front);		
		frontThread.start();

		
		dbWriter = new PostProcess.DBWriter();
		socketWriter = new PostProcess.SocketWriter(/*dbWriter,*/ front.getRemoteAddress());
		fileWriter = new PostProcess.FileWrite(/*socketWriter*/);

		executer.resgisterPostProcess(fileWriter);
		executer.resgisterPostProcess(socketWriter);
		executer.resgisterPostProcess(dbWriter);
		
		executerThread = new Thread(executer);
		fileThread = new Thread(fileWriter);
		socketThread = new Thread(socketWriter);
		dbThread = new Thread(dbWriter);
		
		executerThread.start();

		fileThread.start();

		socketThread.start();

		dbThread.start();

		
		watchdog = new ProcessSelfMonitor(SelfMonitorInterval, 3, 1, this);
		watchdogThread = new Thread(watchdog);
		watchdogThread.start();

		
	}
	
	public FrontAlloyProcess getFront() {
		return front;
	}

	public AlloyExecuter getExecuter() {
		return executer;
	}

	public PostProcess.FileWrite getFileWriter() {
		return fileWriter;
	}

	public PostProcess.SocketWriter getSocketWriter() {
		return socketWriter;
	}

	public static void main(String[] args) {
		
		logger.info("["+Thread.currentThread().getName()+"] "+"The process is started.");
		
		if(args.length < 4)
			throw new RuntimeException("Enter the port number");
		
		if(args.length > 4)
			throw new RuntimeException("Inappropriate number of inputs. Only enter the remote port number as an interger.");
		

		
		int localPort;
		int remotePort;
		InetAddress localIP;
		InetAddress remoteIP;
		
		try{
			localPort = Integer.parseInt(args[0]);
			localIP   = InetAddress.getByName(args[1]);
			logger.info("["+Thread.currentThread().getName()+"] "+"The port is assigned to this process: "+localPort+ " and the IP is: "+ localIP);
			
			remotePort = Integer.parseInt(args[2]);
			remoteIP   = InetAddress.getByName(args[3]);;
			logger.info("["+Thread.currentThread().getName()+"] "+"The remote port is: "+remotePort + " and the IP is: "+ remoteIP);
		
		}catch(NumberFormatException nfe){
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]" + "The passed port is not acceptable: ", nfe.getMessage());
			throw new RuntimeException("The port number is not an integer: "+nfe);
		}catch(UnknownHostException uhe){
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]" + "The passed IP is not acceptable: ", uhe.getMessage());
			throw new RuntimeException("The IP address is not acceptable: "+uhe);
		}
		
		
		final InetSocketAddress  localSocket  = new InetSocketAddress(localIP, localPort);
		final InetSocketAddress  remoteSocket = new InetSocketAddress(remoteIP, remotePort);
		
		AlloyProcessRunner.getInstance(localSocket, remoteSocket).start();
		
		//busywait
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		while(true){
			try {
				Thread.sleep(100000);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]" + "Main loop is interrupted ", e);
				break;
			}
			logger.info("["+Thread.currentThread().getName()+"]" + "Main is alive.... ");
			Thread.currentThread().yield();
		}
		
	}

	
	
	
}
