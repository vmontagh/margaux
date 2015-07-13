package edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.h2.mvstore.ConcurrentArrayList;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyExecuter;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyProcessRunner;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.PostProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.PostProcess.FileWrite;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.PostProcess.SocketWriter;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.IamAlive;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.Suicided;

public class ProcessSelfMonitor implements Runnable {

	public final int monitorInterval;
	public final int RecoveryAttemtps;

	protected final static Logger logger = Logger.getLogger(ProcessSelfMonitor.class.getName()+"--"+Thread.currentThread().getName());

	List<ThreadDelayToBeMonitored> monitoredThreads = new LinkedList<>();

	public ProcessSelfMonitor(int monitorInterval, int RecoveryAttemtps
		 ) {
		super();
		this.monitorInterval = monitorInterval;
		this.RecoveryAttemtps = RecoveryAttemtps;

	}

	public void addThreadToBeMonitored(ThreadDelayToBeMonitored thread){
		monitoredThreads.add(thread);
	}

	@Override
	public void run() {

		int processedSoFar = -1;
		int filedSoFar = -1;
		int sentSoFar = -1;

		int recoveryAttempts = 0;
		int livenessFailed = 0;
		while(!Thread.currentThread().isInterrupted()){

			try{
				Thread.sleep(monitorInterval/2);

				for(ThreadDelayToBeMonitored thread: monitoredThreads){
					try{
						if(thread.isDelayed() != 0){
							logger.info("["+Thread.currentThread().getName()+"]"+ thread.amIStuck());
							thread.actionOnStuck();
						}else{
							thread.actionOnNotStuck();
						}
					}catch(Throwable tr){
						logger.severe("["+Thread.currentThread().getName()+"]"+ "Watchdog main loop is BADLY faced an exception!");					
					}
				}
				/*try {




				logger.info("["+Thread.currentThread().getName()+"]"+ "Monitor takes: processedSoFar:"+processedSoFar+" currently_processed="+alloyProcessRunner.getExecuter().processed+" isEmpty="+alloyProcessRunner.getExecuter().isEmpty());
				//monitor the executer
				if(processedSoFar == alloyProcessRunner.getExecuter().processed.intValue() && !alloyProcessRunner.getExecuter().isEmpty()){
					//The executer does not proceeded.
					logger.info("["+Thread.currentThread().getName()+"]"+ "A request is timed out to be executed");
					alloyProcessRunner.resetExecuterThread();
					//alloyProcessRunner.getExecuter().recordATimeout();//It is called internally once the thread is restarted again.
					logger.info("["+Thread.currentThread().getName()+"]"+ "The executer thread is restarted and i ready to go.");
					recoveryAttempts++;
				}else{
					//The executer proceeded
					processedSoFar = alloyProcessRunner.getExecuter().processed.intValue();
					recoveryAttempts = 0;
					try{

						IamAlive iamAlive =  new IamAlive(alloyProcessRunner.PID, System.currentTimeMillis(), 
								processedSoFar, alloyProcessRunner.getExecuter().size());

						iamAlive.sendMe(alloyProcessRunner.getFront().getRemoteAddress());
						livenessFailed = 0;
						logger.info("["+Thread.currentThread().getName()+"]"+ "A live message is sent from pId: "+alloyProcessRunner.PID +" >"+iamAlive);
					}catch(Exception e){
						logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]"+ "Failed to send a live signal on PID: "+ alloyProcessRunner.PID+" this is "+livenessFailed+" attempt", e);
						livenessFailed++;

					}

				} 

				//monitor the socket
				if(sentSoFar == alloyProcessRunner.getSocketWriter().processed.longValue() && !alloyProcessRunner.getSocketWriter().isEmpty()){
					//The executer does not proceeded.
					logger.severe("["+Thread.currentThread().getName()+"]"+ "A request is timed out to be sent over socket");
					//TODO manage to reset the socket thread
				}else{
					//The executer proceeded
					sentSoFar = alloyProcessRunner.getSocketWriter().processed.intValue();
				}

				//monitor the file
				if(filedSoFar == alloyProcessRunner.getFileWriter().processed.longValue() && !alloyProcessRunner.getFileWriter().isEmpty()){
					//The executer does not proceeded.
					logger.severe("["+Thread.currentThread().getName()+"]"+ "A request is timed out to be written on a file");
					//TODO manage to reset the socket thread
				}else{
					//The executer proceeded
					filedSoFar = alloyProcessRunner.getFileWriter().processed.intValue();
				}

				//recovery was not enough, the whole processes has to be shut-down
				if(recoveryAttempts > this.RecoveryAttemtps || livenessFailed > this.RecoveryAttemtps){
					logger.severe("["+Thread.currentThread().getName()+"]"+ "After recovery "+ recoveryAttempts+ " times or " + livenessFailed +" liveness message, attempts, the executer in PID:"+ 
							alloyProcessRunner.PID +" does not prceeed, So the process is exited.");

					try{
						new Suicided(alloyProcessRunner.PID, System.currentTimeMillis()).
						sendMe(alloyProcessRunner.getFront().getRemoteAddress());
					}catch(Exception e){
						logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"]"+ "Failed to send a Suicide signal on PID: "+ alloyProcessRunner.PID, e);
					}

					Runtime.getRuntime().halt(0);
				}*/
				System.gc();

				Thread.sleep(monitorInterval/2);
			} catch (InterruptedException e) {
				logger.info("["+Thread.currentThread().getName()+"]"+ "Watchdog main loop is interrpted.");

			}	
		}
	}

	public void cancel() {
		Thread.currentThread().interrupt();
	}


}
