package edu.uw.ece.alloy.debugger.onborder.propgen;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.PostProcess.SocketWriter;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteCommand;
import edu.uw.ece.alloy.util.ServerSocketListener;
import edu.uw.ece.alloy.util.Utils;
import edu.uw.ece.hola.agent.HolaRunner;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessorUtil;

/**
 * @author Fikayo Odunayo
 *
 */
public class OnBorderInstanceFinder extends ServerSocketListener {

	protected final static Logger logger = Logger.getLogger(OnBorderInstanceFinder.class.getName()+"--"+Thread.currentThread().getName());
	
	private final static String ProcessLoggerConfig = Configuration.getProp("process_logger_config");
	private final static int SubMemory = Integer.parseInt(Configuration.getProp("sub_memory"));
	private final static int SubStack = Integer.parseInt(Configuration.getProp("sub_stak"));
	private final static File tmpDirectoryRoot = new File(Configuration.getProp("temporary_directory"));
	
	/** The PID is as the port number that the processor is listening to. */
	private final String filePathArgs;
	private final String propertiesFile;
	private final InetSocketAddress PID;
	private final InetSocketAddress remotePort;	
	private final File tmpDirectory;
	
	private HolaProcess subProcess;
	private Thread subManager;
	private HolaWatchDog watchdog;
	private Deque<Object> results = new ArrayDeque<>();
	
	public OnBorderInstanceFinder(final InetSocketAddress localPort, final InetSocketAddress remotePort, final String filePathArgs, final String propertiesFile) {
		
		// The local port is the port we are listening to (We are listening to our own port)
		// For now, set the remote port to be null, it will be set after the process has been created
		super(localPort, null);
		
		this.PID = localPort;
		this.remotePort = remotePort;
		this.filePathArgs = filePathArgs;
		this.propertiesFile = propertiesFile;
		
		// Set up thread for IPC between processes
		this.subManager = new Thread(this);
		
		// Set the local tmpDirectory
		this.tmpDirectory = new File(tmpDirectoryRoot,String.valueOf(PID.getPort()));
		setUpFolders();		
	}
		
	public void execute() throws UnknownHostException {		
				
		// Create Hola Process
		InetSocketAddress address = ProcessorUtil.findEmptyLocalSocket();

		System.out.println("My Address: " + this.PID);
		System.out.println("Sub Address: " + address);
		
		// Change remote address to be the address of the process
		this.changeRemoteAddress(address);
		
		subProcess = this.createProcess(address);
		if(!subProcess.isAlive()) {
			throw new RuntimeException("Sub Process not alive");
		}
		
		// Set up watchdog timer.
		// This process must stop before the interval is finished or it will be terminated.
		this.watchdog = new HolaWatchDog();
		this.watchdog.setInterval(10000);
		
		// Open the IPC channel and start the timer
		this.startThread();
		this.watchdog.startTimer(new Runnable() {
			
			@Override
			public void run() {
				
				logger.info(Utils.threadName() + "Watchdog timer terminated. Running timer callback");
				
				// Get final candidate in the deque.
				HolaResult finalResult = (HolaResult) results.peekLast();
				
				if(finalResult != null) {
  				// Destroy the Hola JVM
  				subProcess.destroyProcess();
  								
  				// Send the result back to the Hola Analyzer
  				SocketWriter writer = new SocketWriter(remotePort);
  				try {
  					writer.doAction(finalResult);
  				} catch (InterruptedException e) {
  					logger.log(Level.SEVERE, Utils.threadName() + "Interrupted while sending final result to remote. Remote Address: " + remotePort, e);
  				}
  				
  				writer.startThread();
				}
			}
		});
		
	}
	
	protected void processCommand(final RemoteCommand command) {
		// Supposed to be a registering call;
		System.out.println("Commands is received in " + OnBorderInstanceFinder.class.getSimpleName() + ": "  + command);
		boolean last = command.processResult(this.results);
		if(last) {
			this.watchdog.stopTimer();
		}
	}

	@Override
	protected Thread getThread() {
		return this.subManager;
	}
	
	@Override
	public void cancelThread() {
		this.subManager.interrupt();
		this.subManager = null;
		
		this.stopRunning();
	}

	@Override
	protected void onStartingListening() throws InterruptedException{
		if(Configuration.IsInDeubbungMode) logger.info(Utils.threadName() + " The process is started on port : " + this.getHostAddress().getPort());
		System.out.println("onStartingListening");
	}
	
	@Override
	public void changePriority(int newPriority) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionOnNotStuck() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int triesOnStuck() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void actionOnStuck() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String amIStuck() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long isDelayed() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private synchronized HolaProcess createProcess(final InetSocketAddress address) {

		Process sub = null;
		final String java = "java";
		final String jarPath = new java.io.File(HolaRunner.class.getProtectionDomain()
                              			  .getCodeSource()
                              			  .getLocation()
                              			  .getPath())
                              			.getAbsolutePath();
		final String debug = Boolean.parseBoolean(System.getProperty("debug")) ? "yes" : "no";

		try {
			
			String[] commands = {
				java,
				"-jar",
				jarPath,
				"" + this.PID.getPort(),
				"" + this.PID.getAddress().getHostAddress(),
				"" + address.getPort(),
				"" + address.getAddress().getHostAddress(),
				"" + this.filePathArgs,
				"" + this.propertiesFile
			};
			
			sub = Utils.createProcess(commands);
			
		} catch (IOException e) {
			logger.log(Level.SEVERE, Utils.threadName() + "Not able to create a new process on port: "+address, e);
		}

		HolaProcess result = new HolaProcess(address, sub);
		return result;
	}
	
	private void setUpFolders(){

		if( this.tmpDirectory.exists() ){
			try {
				if(Configuration.IsInDeubbungMode) logger.info(Utils.threadName() + " exists and has to be recreated." +tmpDirectory.getCanonicalPath());
				Utils.deleteRecursivly(this.tmpDirectory);
			} catch (IOException e) {
				logger.log(Level.SEVERE, Utils.threadName() + "Unable to delete the previous files.", e);
			}
		}

		// After deleting the temp directory create a new one.
		if (!this.tmpDirectory.mkdirs())
			throw new RuntimeException("Can not create a new directory");

	}
	
	public static void main(String[] args) {
		
		if(Configuration.IsInDeubbungMode) logger.info(Utils.threadName() + "The '" + OnBorderInstanceFinder.class.getSimpleName() + "' process has started.");

		if(args.length < 5)
			throw new RuntimeException("Invalid number of arguments");
		
		int localPort;
		int remotePort;
		InetAddress localIP;
		InetAddress remoteIP;
		
		String filePathArgs;
		String propertiesFile;

		try{
			localPort = Integer.parseInt(args[0]);
			localIP   = InetAddress.getByName(args[1]);
			
			if(Configuration.IsInDeubbungMode) logger.info(Utils.threadName() + "The port is assigned to this process: " + localPort + " and the IP is: " + localIP);

			remotePort = Integer.parseInt(args[2]);
			remoteIP   = InetAddress.getByName(args[3]);
			
			if(Configuration.IsInDeubbungMode) logger.info(Utils.threadName() + "The remote port is: " + remotePort + " and the IP is: " + remoteIP);
			
			filePathArgs = args[4];
			propertiesFile = args[5];

		}
		catch(NumberFormatException nfe) {
			logger.log(Level.SEVERE, Utils.threadName() + "The passed port is not acceptable: ", nfe.getMessage());
			throw new RuntimeException("The port number is not an integer: " + nfe);
		}
		catch(UnknownHostException uhe) {
			logger.log(Level.SEVERE, Utils.threadName() + "The passed IP is not acceptable: ", uhe.getMessage());
			throw new RuntimeException("The IP address is not acceptable: " + uhe);
		}

		final InetSocketAddress  localSocket  = new InetSocketAddress(localIP, localPort);
		final InetSocketAddress  remoteSocket = new InetSocketAddress(remoteIP, remotePort);
		
		OnBorderInstanceFinder finder = new OnBorderInstanceFinder(localSocket, remoteSocket, filePathArgs, propertiesFile);
		
		try {
			finder.execute();
		} catch (UnknownHostException e) {
			logger.log(Level.SEVERE, Utils.threadName() + "Unknown Host exception", e);
			e.printStackTrace();
		}
		
		// busy wait
//		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
//		while(true){
//			
//			try {
//				Thread.sleep(100000);
//			} catch (InterruptedException e) {
//				logger.log(Level.SEVERE, Utils.threadName() + "Main loop is interrupted ", e);
//				break;
//			}
//			
//			if(Configuration.IsInDeubbungMode) logger.info(Utils.threadName() + "Main is alive.... ");
//			Thread.yield();
//		}
		
		logger.info(Utils.threadName() + OnBorderInstanceFinder.class.getSimpleName() + " exiting.");
	}
}
