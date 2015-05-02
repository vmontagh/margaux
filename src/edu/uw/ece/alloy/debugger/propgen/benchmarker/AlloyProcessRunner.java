package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ProcessSelfMonitor;

public class AlloyProcessRunner {

	//The PID is as the port number that the processor is listening to.
	public final int PID;
	public final int remotePort;
	
	protected final static Logger logger = Logger.getLogger(AlloyProcessRunner.class.getName()+"--"+Thread.currentThread().getName());

	private Thread frontThread;
	private Thread executerThread;
	private Thread fileThread;
	private Thread socketThread;
	private Thread watchdogThread; 
	
	private FrontAlloyProcess front;
	private AlloyExecuter executer;
	private PostProcess.FileWrite fileWriter;
	private PostProcess.SocketWriter socketWriter;
	private ProcessSelfMonitor watchdog;
	
	private static AlloyProcessRunner self = null;

	
	public static AlloyProcessRunner getInstance(final int localPort, final int remotePort){
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
	
	private AlloyProcessRunner(int localPort, final int remotePort) {
		PID = localPort;
		this.remotePort = remotePort; 
	}
	
	public void resetExecuterThread(){
		if(executerThread != null && executerThread.isAlive()){
			executerThread.stop();
			executerThread = new Thread(executer);
			executerThread.start();
		}
	}
	
	public void start(){
		
		logger.info("["+Thread.currentThread().getName()+"] "+" Starting to create Alloy Processing objects");
		
		executer = AlloyExecuter.getInstance();
		front = new FrontAlloyProcess(PID,remotePort,executer);
		frontThread = new Thread(front);		
		frontThread.start();
		
		socketWriter = new PostProcess.SocketWriter(front.getRemoteAddress());
		fileWriter = new PostProcess.FileWrite(socketWriter);
		
		executer.resgisterPostProcess(fileWriter);
		//executer.resgisterPostProcess(socketWriter);
		
		executerThread = new Thread(executer);
		fileThread = new Thread(fileWriter);
		socketThread = new Thread(socketWriter);
		
		executerThread.start();
		fileThread.start();
		socketThread.start();
		
		watchdog = new ProcessSelfMonitor(10000, 3, 1, this);
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
		
		if(args.length < 2)
			throw new RuntimeException("Enter the port number");
		
		if(args.length > 2)
			throw new RuntimeException("Inappropriate number of inputs. Only enter the remote port number as an interger.");
		
		int localPort;
		int remotePort;
		
		try{
			localPort = Integer.parseInt(args[0]);
			logger.info("["+Thread.currentThread().getName()+"] "+"The port is assigned to this process: "+localPort);
			remotePort = Integer.parseInt(args[1]);
			logger.info("["+Thread.currentThread().getName()+"] "+"The remote port is: "+remotePort);
		}catch(NumberFormatException nfe){
			throw new RuntimeException("The port number is not an integer: "+args[0]);
		}
		
		AlloyProcessRunner.getInstance(localPort, remotePort).start();
		
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
