package edu.uw.ece.alloy.debugger.onborder.propgen;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Pair;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessorUtil;
import edu.uw.ece.alloy.util.Utils;

public class HolaProcessManager {

	protected final static Logger logger = Logger.getLogger(HolaProcessManager.class.getName()+"--"+Thread.currentThread().getName());

	private final int newmem;
	private final int newstack;
	private final String jniPath;
	private final String classPath;
	private final InetSocketAddress watchdogAddress;

	private final String processLoggerConfig;
	
	/**
	 * This stores how many messages are sent to the Hola processor to be processed. 
	 * The value is used to stop sent many messages to an Alloy processor.
	 * If the key is 0, means that the processor is IDLE or INIATED
	 * The value stores all the messages sent 
	 */
	private Pair<AtomicInteger, AtomicInteger> sentMessagesCounter;

	public HolaProcessManager(String classPath, int newMem, int newStack, final String jniPath, final String processLogConfig) {
		
		this(classPath, newMem, newStack, jniPath, ProcessorUtil. findEmptyLocalSocket() , processLogConfig);
	}

	public HolaProcessManager(String classPath, int newMem, int newStack, final String jniPath, final InetSocketAddress watchdogAddress, final String processLogConfig) {

		this.jniPath = jniPath;
		this.newmem = newMem;
		this.newstack = newStack;
		this.watchdogAddress = watchdogAddress;
		this.processLoggerConfig = processLogConfig;
		if (classPath==null || classPath.length()==0) 
			this.classPath = System.getProperty("java.class.path");
		else
			this.classPath = classPath;
	}

	public InetSocketAddress getProcessRemoteMonitorAddress(){
		return watchdogAddress;
	}
	
	public synchronized HolaProcess createProcess(final InetSocketAddress address, String filepathParam, String propertiesFile) throws IOException{

		final Process sub;
		final String java = "java";
		final String debug = Boolean.parseBoolean(System.getProperty("debug")) ? "yes" : "no";

		try {
			
			String jni = (jniPath != null && jniPath.length() > 0) ? "-Djava.library.path=" + jniPath : "";			
			String[] commands = {
					java,
					"-Xmx" + newmem + "m",
					"-Xss" + newstack + "k",
  				"-Djava.util.logging.config.file=" + processLoggerConfig,
  				jni,
  				"-Ddebug=" + debug,
  				"-cp", classPath, OnBorderInstanceFinder.class.getName(),
  				"" + address.getPort(),
  				"" + address.getAddress().getHostAddress(),
  				"" + filepathParam,
  				"" + propertiesFile
			};
			
			sub = Utils.createProcess(commands);

		} catch (IOException e) {
			logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"]"+ "Not able to create a new process on port: "+address, e);
			throw e;
		}

		HolaProcess result = new HolaProcess(address, sub);
		return result;
	}


}
