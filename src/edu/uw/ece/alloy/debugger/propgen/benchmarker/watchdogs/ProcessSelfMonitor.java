package edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyExecuter;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessRunner;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.PostProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.PostProcess.FileWrite;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.PostProcess.SocketWriter;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.IamAlive;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.Suicided;

public class ProcessSelfMonitor implements Runnable {

	public final int monitorInterval;
	public final int RecoveryAttemtps;
	public final long doneRatio;

	protected final static Logger logger = Logger.getLogger(ProcessSelfMonitor.class.getName()+"--"+Thread.currentThread().getName());

	public final AlloyProcessRunner alloyProcessRunner;


	public ProcessSelfMonitor(int monitorInterval, int RecoveryAttemtps,
			long doneRatio, AlloyProcessRunner alloyProcessRunner) {
		super();
		this.monitorInterval = monitorInterval;
		this.RecoveryAttemtps = RecoveryAttemtps;
		this.doneRatio = doneRatio;
		this.alloyProcessRunner = alloyProcessRunner;

	}

	@Override
	public void run() {

		int processedSoFar = -1;
		int filedSoFar = -1;
		int sentSoFar = -1;

		int recoveryAttempts = 0;
		int livenessFailed = 0;
		while(!Thread.currentThread().isInterrupted()){
			try {
				logger.info("["+Thread.currentThread().getName()+"]"+ "Monitor takes: processedSoFar:"+processedSoFar+" currently_processed="+alloyProcessRunner.getExecuter().processed+" isEmpty="+alloyProcessRunner.getExecuter().isEmpty());
				//monitor the executer
				if(processedSoFar == alloyProcessRunner.getExecuter().processed.intValue() && !alloyProcessRunner.getExecuter().isEmpty()){
					//The executer does not proceeded.
					logger.info("["+Thread.currentThread().getName()+"]"+ "A request is timed out to be executed");
					alloyProcessRunner.getExecuter().recordATimeout();
					alloyProcessRunner.resetExecuterThread();
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
				}
				System.gc();

				Thread.sleep(monitorInterval);
			} catch (InterruptedException e) {
				logger.info("["+Thread.currentThread().getName()+"]"+ "Watchdog main loop is interrpted.");

			}	
		}
	}

	public void cancel() {
		Thread.currentThread().interrupt();
	}


}
