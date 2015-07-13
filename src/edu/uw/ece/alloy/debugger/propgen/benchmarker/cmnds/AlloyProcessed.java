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

	@Override
	public void processDone(ProcessRemoteMonitor monitor){
		
		logger.fine("["+Thread.currentThread().getName()+"] " + "Processeing the response: pID= "+PID +" param="+this.result.params);
		
		AlloyProcessingParam param = this.result.params;
		AlloyProcessedResult result = this.result;
		try {
			param = this.result.params.prepareToUse();
			result = this.result.changeParams(param);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Failed on prepare or send the message: PID="+PID+", "+ this.result, e);
			e.printStackTrace();
		}
		
		logger.fine("["+Thread.currentThread().getName()+"] " + "Done and reported: pID= "+PID +" param="+param);
		
		monitor.processResponded(result, PID);
	}


	@Override
	public String toString() {
		return "AlloyProcessed [PID=" + PID + ", param=" + result.params + "]";
	}

	
	public  void send(final InetSocketAddress remoteAddres) throws InterruptedException{
		
		logger.fine("["+Thread.currentThread().getName()+"] " + "Sending a response: pID= "+PID +" param="+result.params);
		//super.sendMe(remoteAddres);
		try {
			AlloyProcessingParam param = this.result.params.prepareToSend();
			param = param.resetToEmptyTmpDirectory();
			//System.out.println("The file stored in? "+this.result.params.srcPath.exists());
			(new AlloyProcessed(PID, this.result.changeParams(param)) ).sendMe(remoteAddres);
			logger.fine("["+Thread.currentThread().getName()+"] " + "Response is sent: pID= "+PID +" param="+param);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Failed on prepare or send the message: "+ this.result, e);
			e.printStackTrace();
		}
		
	}


}
