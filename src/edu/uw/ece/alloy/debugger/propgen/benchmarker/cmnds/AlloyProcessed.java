package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.net.InetSocketAddress;
import java.util.logging.Level;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyProcessedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.AlloyProcess.Status;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ProcessesManager;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ProcessRemoteMonitor;

public class AlloyProcessed extends RemoteCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7812337897314648833L;
	public final InetSocketAddress PID;
	public final AlloyProcessedResult result;

	public AlloyProcessed(final InetSocketAddress pID, final AlloyProcessedResult result) {
		super();
		PID = pID;
		this.result = result;
	}

	public void processDone(ProcessRemoteMonitor monitor, final ProcessesManager manager){
		
		if(manager.getAlloyProcess(PID) == null){
			logger.info("["+Thread.currentThread().getName()+"] "+ " No Such a PID found: "+ PID+" "+this);
			return;
		}
		
		
		AlloyProcessingParam param;
		AlloyProcessedResult result = this.result;
		try {
			param = this.result.params.prepareToUse();
			result = this.result.changeParams(param);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Failed on prepare or send the message: "+ this.result, e);
			e.printStackTrace();
		}
		
		logger.info("["+Thread.currentThread().getName()+"] " + "Done and reported: pID= "+PID +" param="+result.params);
		if(result.isTimedout() || result.isFailed()){
			logger.info("["+Thread.currentThread().getName()+"] " + "The process is timed out and is pushed back to be retried later: pID= "+PID +" param="+result.params);
			monitor.removeAndPushUndoneRequest(PID, result.params);
		}else{
			monitor.removeMessage(PID, result.params);
		}
		
		manager.changeStatus(PID, Status.WORKING);
		manager.changeLastLiveTimeReported(PID, System.currentTimeMillis());
		manager.decreaseMessageCounter(PID);
	}


	@Override
	public String toString() {
		return "AlloyProcessed [PID=" + PID + ", param=" + result.params + "]";
	}

	
	public  void send(final InetSocketAddress remoteAddres) throws InterruptedException{
		
		//super.sendMe(remoteAddres);
		try {
			final AlloyProcessingParam param = this.result.params.removeContent().prepareToSend();
			//System.out.println("The file stored in? "+this.result.params.srcPath.exists());
			(new AlloyProcessed(PID, this.result.changeParams(param)) ).sendMe(remoteAddres);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Failed on prepare or send the message: "+ this.result, e);
			e.printStackTrace();
		}
		
	}


}
