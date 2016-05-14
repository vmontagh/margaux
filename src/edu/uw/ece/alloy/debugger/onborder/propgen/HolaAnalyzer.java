package edu.uw.ece.alloy.debugger.onborder.propgen;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Util;
import edu.uw.ece.alloy.util.Utils;
import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessorUtil;

public class HolaAnalyzer {
	
protected final static Logger logger = Logger.getLogger(HolaAnalyzer.class.getName()+"--"+Thread.currentThread().getName());
	
	private final static String ProcessLoggerConfig = Configuration.getProp("process_logger_config");
	private final static int SubMemory = Integer.parseInt(Configuration.getProp("sub_memory"));
	private final static int SubStack = Integer.parseInt(Configuration.getProp("sub_stak"));
	private final static File tmpDirectoryRoot = new File(Configuration.getProp("temporary_directory"));
	
	private String filePath;
	private HolaProcess subProcess;
	private HolaWatchDog watchdog;
	private Thread subManager;
	
	public HolaAnalyzer(String filePath) {
		this.filePath = filePath;
	}
	
	public void createInstanceFinder() throws UnknownHostException {
		
		this.subProcess = this.createProcess(ProcessorUtil.findEmptyLocalSocket());
		
		// Set up thread for IPC
		subManager = new Thread(new Runnable() {
      public void run() {
      	
         ObjectInputStream sub2main = null;
         ObjectOutputStream main2sub = null;
         
         try {
        	 
            main2sub = new ObjectOutputStream(Utils.wrap(subProcess.getOutputStream()));            
            sub2main = new ObjectInputStream(Utils.wrap(subProcess.getInputStream()));
            
         } catch(Throwable ex) {
        	 
            ex.printStackTrace();
            subProcess.destroyProcess();
            Util.close(main2sub); 
            Util.close(sub2main);
            
         }
         
         while(true) {
        	 
            Object x;
            try {
            	
               x = sub2main.readObject();
               
            } catch(Throwable ex) {
            	
               ex.printStackTrace();
               subProcess.destroyProcess(); 
               Util.close(sub2main);                              
            }
            
         }
      }
   });
		
   this.subManager.start();
   
   // Start watchdog timer
   this.watchdog = new HolaWatchDog();
   this.watchdog.setInterval(12000);
   this.watchdog.startTimer(new Runnable() {
			
			@Override
			public void run() {
				subProcess.destroyProcess();
			}
		});
   
	}
	
	private synchronized HolaProcess createProcess(final InetSocketAddress address) {

		Process sub = null;
		final String jni = "";
		final String java = "java";
		final String classPath = System.getProperty("java.class.path");
		final String debug = Boolean.parseBoolean(System.getProperty("debug")) ? "yes" : "no";

		try {

//			jni = (jniPath != null && jniPath.length() > 0) ? "-Djava.library.path=" + jniPath : "";			
			String[] commands = {
					java,
					"-Xmx" + SubMemory + "m",
					"-Xss" + SubStack + "k",
  				"-Djava.util.logging.config.file=" + ProcessLoggerConfig,
  				jni,
  				"-Ddebug=" + debug,
  				"-cp", classPath, OnBorderInstanceFinder.class.getName(),
//  				"" + this.PID.getPort(),
//  				"" + this.PID.getAddress().getHostAddress(),
  				"" + address.getPort(),
  				"" + address.getAddress().getHostAddress(),
  				"" + this.filePath,
  				"" + Configuration.properties_path
			};
			
			sub = Utils.createProcess(commands);

		} catch (IOException e) {
			logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"]"+ "Not able to create a new process on port: "+address, e);
		}

		HolaProcess result = new HolaProcess(address, sub);
		return result;
	}
}
