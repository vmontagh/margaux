package edu.uw.ece.alloy.debugger.onborder.propgen;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.PostProcess.SocketWriter;
import edu.uw.ece.alloy.util.Utils;

public class OnBorderInstanceFinder {

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
	
	public OnBorderInstanceFinder(final InetSocketAddress localPort, final InetSocketAddress remotePort, final String filePathArgs, final String propertiesFile) {
		this.PID = localPort;
		this.remotePort = remotePort; 
		this.filePathArgs = filePathArgs;
		this.propertiesFile = propertiesFile;
		
		// Set the local tmpDirectory
		this.tmpDirectory = new File(tmpDirectoryRoot,String.valueOf(PID.getPort()));
		setUpFolders();		
	}
		
	public void execute() throws UnknownHostException {		
				
		// Create Hola Process
		InetAddress localAddress = Utils.getLocalAddress();		
		InetSocketAddress address = Utils.findEmptyLocalSocket(localAddress);
		subProcess = this.createProcess(address);

		if(!subProcess.isAlive()) throw new RuntimeException("Sub Process not alive");
		
		BlockingDeque<HolaResult> results = new LinkedBlockingDeque<>();
		
		// Set up watchdog timer.
		// This process must stop before the interval is finished or it will be terminated.
		this.watchdog = new HolaWatchDog();
		this.watchdog.setInterval(10000);
		
		// Set up thread for IPC between processes
		subManager = new Thread(new Runnable() {
			
				public void run() {
	      	
	         ObjectInputStream sub2main = null;
	         
	         try {
	        	           
	            sub2main = new ObjectInputStream(Utils.wrap(subProcess.getInputStream()));
	            
	         } catch(IOException e) {
	        	 
	        	 	logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"]"+ "Error creating input stream: " + e.getMessage(), e);
	            e.printStackTrace();
	            
	            watchdog.stopTimer();
	            Util.close(sub2main);
	         }
	         
	         while(true) {
	        	 
	            HolaResult result = null;
	            try {	            	
	               result = (HolaResult) sub2main.readObject();	
              
	            } catch(IOException | ClassNotFoundException e) {
	            	
	            	logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"]"+ "Error reading from input stream" + e.getMessage(), e);
	            	
	               watchdog.stopTimer();	               
	               Util.close(sub2main); 
	               e.printStackTrace();
	            }            

	            // Add results to the deque
              if(result.isLast()) {              	
              	if(result.getInstance() != null) {
              		results.add(result);
              		
              		System.out.println("=====================================================");
          				System.out.println("Instance Result: \n    " + result.getInstance().toString().replace("\n", "\n" + "    ") + "");
          				System.out.println("=====================================================");
              		
              	}
              	
            		watchdog.stopTimer();
            		
              } else { 
              	try {
									results.put(result);
								} catch (InterruptedException e) {
									logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"]"+ "Interrupted while placing result in deque", e);
								}
              }
	         }
	      }
	   });
			
		// Open the IPC channel and start the timer
//		this.subManager.start();		
		this.watchdog.startTimer(new Runnable() {
			
			@Override
			public void run() {
				
				logger.log(Level.INFO,"["+Thread.currentThread().getName()+"]" + "Watchdog timer terminated. Running timer callback");
				
				// Get final candidate in the deque.
				HolaResult finalResult = results.peekLast();
				
				if(finalResult != null) {
  				// Destroy the Hola JVM
  				subProcess.destroyProcess();
  								
  				// Send the result back to the Hola Analyzer
  				SocketWriter writer = new SocketWriter(remotePort);
  				try {
  					writer.doAction(finalResult);
  				} catch (InterruptedException e) {
  					logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"]"+ "Interrupted while sending final result to remote. Remote Address: " + remotePort, e);
  				}
  				
  				writer.startThread();
				}
			}
		});
		
	}
	
	private synchronized HolaProcess createProcess(final InetSocketAddress address) {

		Process sub = null;
		final String java = "java";
		final String holaJar = "/home/fikayo/workspace/OnBorderInstanceFinder/lib/hola-0.2.jar";
		final String classPath = /*System.getProperty("java.class.path");*/ holaJar + ":" + "/home/fikayo/workspace/OnBorderInstanceFinder/bin/";
		final String debug = Boolean.parseBoolean(System.getProperty("debug")) ? "yes" : "no";

		try {

//			jni = (jniPath != null && jniPath.length() > 0) ? "-Djava.library.path=" + jniPath : "";
			String[] commands = {
					java,
					"-Xmx" + SubMemory + "m",
					"-Xss" + SubStack + "k",
//  				"-Djava.util.logging.config.file=" + ProcessLoggerConfig,
  				"-Ddebug=" + debug,
  				"-cp", classPath, "onborder.agent.HolaRunner",
  				"" + this.PID.getPort(),
  				"" + this.PID.getAddress().getHostAddress(),
  				"" + address.getPort(),
  				"" + address.getAddress().getHostAddress(),
  				"" + this.filePathArgs,
  				"" + this.propertiesFile
			};
			
			sub = Utils.createProcess(commands);

		} catch (IOException e) {
			logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"]"+ "Not able to create a new process on port: "+address, e);
		}

		HolaProcess result = new HolaProcess(address, sub);
		return result;
	}
	
	private void setUpFolders(){

		if( this.tmpDirectory.exists() ){
			try {
				if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"] " +" exists and has to be recreated." +tmpDirectory.getCanonicalPath());
				Utils.deleteRecursivly(this.tmpDirectory);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Unable to delete the previous files.", e);
			}
		}

		// After deleting the temp directory create a new one.
		if (!this.tmpDirectory.mkdirs())
			throw new RuntimeException("Can not create a new directory");

	}
	
	public static void main(String[] args) {
		
		if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"] "+"The '" + OnBorderInstanceFinder.class.getSimpleName() + "' process is started.");

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
			if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"] "+"The port is assigned to this process: " + localPort + " and the IP is: " + localIP);

			remotePort = Integer.parseInt(args[2]);
			remoteIP   = InetAddress.getByName(args[3]);
			if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"] "+"The remote port is: " + remotePort + " and the IP is: " + remoteIP);
			
			filePathArgs = args[4];
			propertiesFile = args[5];

		}
		catch(NumberFormatException nfe) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]" + "The passed port is not acceptable: ", nfe.getMessage());
			throw new RuntimeException("The port number is not an integer: " + nfe);
		}
		catch(UnknownHostException uhe) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]" + "The passed IP is not acceptable: ", uhe.getMessage());
			throw new RuntimeException("The IP address is not acceptable: " + uhe);
		}

		final InetSocketAddress  localSocket  = new InetSocketAddress(localIP, localPort);
		final InetSocketAddress  remoteSocket = new InetSocketAddress(remoteIP, remotePort);
		
		OnBorderInstanceFinder finder = new OnBorderInstanceFinder(localSocket, remoteSocket, filePathArgs, propertiesFile);
		
		try {
			finder.execute();
		} catch (UnknownHostException e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]" + "Unknown Host exception", e);
			e.printStackTrace();
		}
	}

}
