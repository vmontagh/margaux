package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.internal.Finalizer;

import edu.mit.csail.sdg.alloy4.WorkerEngine;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessesManager.AlloyProcess.Status;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RegisterCallback;

public class ProcessesManager {

	protected final static Logger logger = Logger.getLogger(ProcessesManager.class.getName()+"--"+Thread.currentThread().getName());;

	final int ProcessNumbers, newmem, newstack;
	final String jniPath, classPath;
	final InetSocketAddress watchdogAddress;

	//TODO change the key type to Process
	final ConcurrentHashMap<Integer, AlloyProcess> processes = new ConcurrentHashMap<>();

	final String processLoggerConfig;

	static int  lastFoundPort = 49152;;

	public ProcessesManager(int ProcessNumbers, String classPath, int newMem, int newStack, final String jniPath, final String processLogConfig) {
		this(ProcessNumbers, classPath, newMem, newStack, jniPath, new InetSocketAddress(findEmptyLocalSocket()) , processLogConfig);
	}

	public ProcessesManager(int ProcessNumbers, String classPath, int newMem, int newStack, final String jniPath, final InetSocketAddress watchdogAddress, final String processLogConfig) {
		this.ProcessNumbers = ProcessNumbers;	
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

	public static final class AlloyProcess{
		public enum Status{
			INITIATED, IDLE, WORKING, KILLING, NOANSWER
		}
		public final InetSocketAddress address;
		public final int doneTasks;
		public final int doingTasks;
		public final int sentTasks;
		public final Status status;
		public final long lastLiveTimeRecieved;
		public final long lastLiveTimeReported;

		public final Process process;

		public AlloyProcess(InetSocketAddress address, Process process) {

			this( new InetSocketAddress( address.getAddress(), address.getPort()),
					0,
					0,
					0,
					Status.INITIATED,
					process, 0, 0);
		}


		public AlloyProcess(InetSocketAddress address, int doneTasks,
				int doingTasks, int sentTasks,
				Status status, Process process, final long lastLiveTimeReported, final long lastLiveTimeRecieved) {
			super();
			this.address = address;
			this.doneTasks = doneTasks;
			this.doingTasks = doingTasks;
			this.sentTasks = sentTasks;
			this.status = status;
			this.process = process;
			this.lastLiveTimeReported = lastLiveTimeReported;
			this.lastLiveTimeRecieved = lastLiveTimeRecieved;
		}


		public AlloyProcess changeDoneTasks(int i){
			return new AlloyProcess(address, i, doingTasks, sentTasks, status, process, lastLiveTimeReported,lastLiveTimeRecieved);
		}

		public AlloyProcess changeStatus(Status s){
			return new AlloyProcess(address, doneTasks, doingTasks, sentTasks, s, process, lastLiveTimeReported,lastLiveTimeRecieved);
		}

		public AlloyProcess changeDoingTasks(int i){
			return new AlloyProcess(address, doneTasks, i, sentTasks, status, process, lastLiveTimeReported,lastLiveTimeRecieved);
		}

		public AlloyProcess changeSentTasks(int i){
			return new AlloyProcess(address, doneTasks, doingTasks, i, status, process, lastLiveTimeReported,lastLiveTimeRecieved);
		}

		public AlloyProcess changeLastLiveTimeReported(long i){
			return new AlloyProcess(address, doneTasks, doingTasks, sentTasks, status, process, i, lastLiveTimeRecieved);
		}

		public AlloyProcess changeLastLiveTimeRecieved(long i){
			return new AlloyProcess(address, doneTasks, doingTasks, sentTasks, status, process, lastLiveTimeReported, i);
		}

		public int getPId(){return address.getPort();}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((address == null) ? 0 : address.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AlloyProcess other = (AlloyProcess) obj;
			if (address == null) {
				if (other.address != null)
					return false;
			} else if (!address.equals(other.address))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "AlloyProcess [address=" + address + ", doneTasks="
					+ doneTasks + ", doingTasks=" + doingTasks + ", sentTasks="
					+ sentTasks + ", status=" + status
					+ ", lastLiveTimeRecieved=" + lastLiveTimeRecieved
					+ ", lastLiveTimeReported=" + lastLiveTimeReported
					+ ", process=" + process + "]";
		}


		public boolean isActive(){
			return status.equals(Status.IDLE) || status.equals(Status.WORKING); 
		}

	}


	public synchronized static int  findEmptyLocalSocket(){
		int port = lastFoundPort + 1;
		int tmpPort = port;

		while( tmpPort < 65535){
			tmpPort = tmpPort + 10;
			try {
				ServerSocket socket= new ServerSocket(tmpPort);
				port = socket.getLocalPort();
				socket.close();
				break;
			} catch (IOException e) {
				logger.info("The port is not available: "+tmpPort);
			}
		}

		if(port == lastFoundPort){
			throw new RuntimeException("No port available");
		}
		lastFoundPort = port;
		return lastFoundPort;

	}


	public synchronized AlloyProcess createProcess(final InetSocketAddress address) throws IOException{

		final Process sub;
		final String java = "java";
		final String debug = "yes".equals(System.getProperty("debug")) ? "yes" : "no";

		try {
			if (jniPath!=null && jniPath.length()>0)
				sub = Runtime.getRuntime().exec(new String[] {
						java,
						"-Xmx" + newmem + "m",
						"-Xss" + newstack + "k",
						"-Djava.library.path=" + jniPath,
						"-Ddebug=" + debug,
						"-cp", classPath, AlloyProcessRunner.class.getName(),
						""+address.getPort(),
						""+watchdogAddress.getPort()
						
				});

			else
				sub = Runtime.getRuntime().exec(new String[] {
						java,
						"-Xmx" + newmem + "m",
						"-Xss" + newstack + "k",
						"-Ddebug=" + debug,
						"-Djava.util.logging.config.file=" + processLoggerConfig,
						"-cp", classPath, AlloyProcessRunner.class.getName(),
						""+address.getPort(),
						""+watchdogAddress.getPort()
						
				});
		} catch (IOException e) {
			logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"]"+ "Not able to create a new process on port: "+address, e);
			throw e;
		}

		AlloyProcess result = new AlloyProcess(address, sub);
		return result;
	}

/*	public void activateProess(final int pId) throws InterruptedException{
		if(! processes.containsKey(pId)){
			logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"]"+ "Pid does not exits to be activated: "+pId);
			throw new RuntimeException("Pid does not exits to be activated: "+pId);
		}else if(processes.get(pId).status != Status.INITIATED){
			logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"]"+ "Pid is not in Initial state and does not tobe activated: "+pId);
		}else{
			RegisterCallback command = new RegisterCallback(watchdogAddress);
			try {
				command.sendMe(processes.get(pId).address);
				//this look to be inconsistent. The status has to go IDLE to be ready for 
				changeStatus(pId, Status.IDLE);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"]"+ "Command cannot be sent to activate: "+pId, e);
				throw e;
			}
		}
	}

	public void activateAllProesses() {
		//Not need to be thread safe.
		for(Integer pId: processes.keySet()){
			try{
				activateProess(pId);
			}catch(InterruptedException e) {
				logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"]"+ "Cannot be  activated: "+pId, e);

			}
		}
	}*/

	public AlloyProcess createProcess(final int port) throws IOException{
		return createProcess(new InetSocketAddress(port));
	}

	public void addProcess() throws IOException{
		int port = findEmptyLocalSocket();
		processes.putIfAbsent( port , createProcess(port));
	}


	public void addAllProcesses(){
		//No other thread can add a process to the map.
		synchronized (processes) {
			int i = processes.size();
			int maxAttempts = i + 100;
			while(i < maxAttempts){

				if(processes.size() == ProcessNumbers) break;
				if( i > ProcessNumbers) throw new RuntimeException("Invalid state: i="+i+" Should not be more than ProcessNumbers="+ProcessNumbers);
				try {
					addProcess();
					++i;
				} catch (IOException e) {
					logger.info("["+Thread.currentThread().getName()+"]"+"Processes cannot be created in setUpAllProcesses");			
				} finally{
					--maxAttempts;
				}
			}

			if( i !=  ProcessNumbers) throw new RuntimeException("Cannot create all processes: "+ProcessNumbers+" after "+ maxAttempts+" attempts.");	
		}

	}

	public void killProcess(final int port){
		synchronized (processes) {
			processes.get(port).process.destroyForcibly();
			processes.remove(port);
		}
	}

	public void killAndReplaceProcess(final int port){
		synchronized (processes) {
			killProcess(port);
			addAllProcesses();
		}
	}

	public AlloyProcess getRandomProcess(){
		synchronized(processes){
			@SuppressWarnings("rawtypes")
			List<Integer> randomArray = new ArrayList<Integer>(processes.keySet());
			final int max = randomArray.size();
			final int randomIndex = (new Random()).nextInt(max);
			return processes.get(randomArray.get(randomIndex));
		}
	}

	/**
	 * Return the most idle process.
	 * @return
	 */
	public AlloyProcess getIdlerProcess(){
		//TODO
		throw new RuntimeException("Unimplemented");
	}

	public AlloyProcess getActiveRandomeProcess(){

		AlloyProcess result;
		int i = 10;
		int attempts = 1000; 
		do{
			if( i > attempts  ){
				logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"]"+ "Not abale to find a random working process after atempting: "+i);
				throw new RuntimeException("Not working process was found.");
			}
			result = getRandomProcess();
			++i;
			try {
				Thread.sleep(i);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"]"+ "Interrupted while waiting for an active process. ");
			}
			
		}while(!result.isActive());

		return result;
	}



	public void changeStatus(int pId, Status status){
		synchronized(processes){
			if(processes.containsKey(pId)){
				processes.replace(pId, processes.get(pId).changeStatus(status));
			}else{
				throw new RuntimeException("The process is not found: "+pId);
			}
		}
	}


	private void changeNumber(int pId, AlloyProcess newProcess){
		if(processes.containsKey(pId)){
			processes.replace(pId, newProcess );
		}else{
			throw new RuntimeException("The process is not found: "+pId);
		}
	}

	public void changeSentTasks(int pId, int sentTasks){
		synchronized(processes){
			changeNumber(pId, processes.get(pId).changeSentTasks(sentTasks) );
		}
	}

	public void IncreaseSentTasks(int pId, int sentTasks){
		synchronized(processes){
			changeNumber(pId,processes.get(pId).changeDoneTasks(processes.get(pId).sentTasks + sentTasks) );
		}
	}

	public void changeDoingTasks(int pId, int doingTasks){
		synchronized(processes){
			changeNumber(pId, processes.get(pId).changeDoingTasks(doingTasks) );
		}
	}

	public void IncreaseDoingTasks(int pId, int doingTasks){
		synchronized(processes){
			changeNumber(pId, processes.get(pId).changeDoneTasks(processes.get(pId).doingTasks + doingTasks) );
		}
	}

	public void changeDoneTasks(int pId, int doneTasks){
		synchronized(processes){
			changeNumber(pId, processes.get(pId).changeDoneTasks(doneTasks) );
		}
	}

	public void IncreaseDoneTasks(int pId, int doneTasks){
		synchronized(processes){
			changeNumber(pId, processes.get(pId).changeDoneTasks(processes.get(pId).doneTasks + doneTasks) );
		}
	}


	public void changeLastLiveTimeReported(int pId, long lastLiveTimeReported){
		synchronized(processes){
			changeNumber(pId, processes.get(pId).changeLastLiveTimeReported(lastLiveTimeReported) );
		}
	}

	public void changeLastLiveTimeRecieved(int pId, long lastLiveTimeRecieved){
		synchronized(processes){
			changeNumber(pId, processes.get(pId).changeLastLiveTimeRecieved(lastLiveTimeRecieved) );
		}
	}

	public AlloyProcess getAlloyProcess(int pId){
		logger.info("["+Thread.currentThread().getName()+"] "+" The Pid: "+pId+" is in ?"+Arrays.asList(processes.keySet()));
		return processes.get(pId);
	}

	/**
	 * Find which processors are timed out.
	 * @param threshold in milliseconds
	 * @return
	 */
	public List<AlloyProcess> getTimedProcess(int threshold){
		List<AlloyProcess> result =  Collections.synchronizedList(new LinkedList<>());
		//No need to synchronized. The time is loose.
		for(AlloyProcess ap: processes.values()){
			if(System.currentTimeMillis() - ap.lastLiveTimeRecieved > threshold)
				result.add(ap);
		}
		return Collections.unmodifiableList(result);
	}

	public void finalize(){
		for(Integer port: processes.keySet())
			killProcess(port);
	}

}
