package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.net.InetSocketAddress;
import java.util.logging.Level;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyProcessedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ExpressionAnalyzerRunner;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.RemoteProcessMonitor;

public class AlloyProcessed extends RemoteCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7812337897314648833L;
	public final InetSocketAddress PID;
	public final AlloyProcessedResult result;

	public AlloyProcessed(final InetSocketAddress pID,
			final AlloyProcessedResult result) {
		super();
		PID = pID;
		this.result = result;
	}

	@Override
	public void processDone(RemoteProcessMonitor monitor) {

		if (Configuration.IsInDeubbungMode)
			logger.fine("[" + Thread.currentThread().getName() + "] "
					+ "Processeing the response: pID= " + PID + " param="
					+ this.result.params);

		AlloyProcessingParam param = this.result.params;
		AlloyProcessedResult result = this.result;
		try {
			// decode it
			param = this.result.params.prepareToUse();
			result = this.result.changeParams(param);
		} catch (Exception e) {
			logger.log(Level.SEVERE,
					"[" + Thread.currentThread().getName() + "] "
							+ "Failed on prepare or send the message: PID=" + PID + ", "
							+ this.result,
					e);
			e.printStackTrace();
		}

		if (Configuration.IsInDeubbungMode)
			logger.fine("[" + Thread.currentThread().getName() + "] "
					+ "Done and reported: pID= " + PID + " param=" + param);

		// update the predCall for the inferred properties
		System.out.println("before updatePropertyCall->"+result);
		result = result.updatePropertyCall();
		System.out.println("after updatePropertyCall->"+result);
		
		monitor.processResponded(result, PID);
		monitor.checkNextProperties(result);

		//Approximator.getInstance().addDirectImplication(result);

		System.out.println("Result is:" +result.sat +"-------" + result.params.alloyCoder.predBodyA + result.params.alloyCoder.srcNameOperator() + result.params.alloyCoder.predNameB + "   "+result.getClass());
		
		// Send correct result. i.e. no counter-example or sat == 0
		if ((result.sat == 1) == result.params.alloyCoder.desiredSAT()) {
			System.out.println("result on the server is:"+result);
			try {
				(new AnalyzeExternalResult(result))
						.sendMe(ExpressionAnalyzerRunner.getInstance().remoteSocket);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE,
						"[" + Thread.currentThread().getName() + "] "
								+ "Failed on prepare or send n external message: PID=" + PID
								+ ", " + this.result,
						e);
				e.printStackTrace();
			}
		}
	}

	@Override
	public String toString() {
		return "AlloyProcessed [PID=" + PID + ", param=" + result.params + "]";
	}

	public void send(final InetSocketAddress remoteAddres)
			throws InterruptedException {

		if (Configuration.IsInDeubbungMode)
			logger.fine("[" + Thread.currentThread().getName() + "] "
					+ "Sending a response: pID= " + PID + " param=" + result.params);
		// super.sendMe(remoteAddres);
		try {
			AlloyProcessingParam param = this.result.params.prepareToSend();
			param = param.resetToEmptyTmpDirectory();
			// System.out.println("The file stored in?
			// "+this.result.params.srcPath.exists());
			(new AlloyProcessed(PID, this.result.changeParams(param)))
					.sendMe(remoteAddres);
			if (Configuration.IsInDeubbungMode)
				logger.fine("[" + Thread.currentThread().getName() + "] "
						+ "Response is sent: pID= " + PID + " param=" + param);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
					+ "Failed on prepare or send the message: " + this.result, e);
			e.printStackTrace();
		}

	}

}
