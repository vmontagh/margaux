package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.util.Map;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcessLogger;

public abstract class RequestMessage extends RemoteMessage {

	private static final long serialVersionUID = -9179129766458690826L;
	public transient final RemoteProcessLogger processesLogger;
	// TODO replace AlloyProcessingParam with the refactored one
	// public final AlloyProcessingParam param;

	public RequestMessage(RemoteProcess process,
			RemoteProcessLogger processesLogger) {
		super(process);
		this.processesLogger = processesLogger;
	}

	public RequestMessage(RemoteProcess process, long creationTime,
			RemoteProcessLogger processesLogger) {
		super(process, creationTime);
		this.processesLogger = processesLogger;
	}

	@Override
	public abstract void onAction(Map<Class, Object> context)
			throws InvalidParameterException;

	protected void afterSend(RemoteProcess process) {
		processesLogger.IncreaseSentTasks(process);
	}

	public void prepareThenSend(RemoteProcess process) {
		try {
			// Encoding the param.
			// if(Configuration.IsInDeubbungMode)
			// logger.info("["+Thread.currentThread().getName()+"] " + "Sending a
			// message: "+param);

			// final AlloyProcessingParam param =
			// this.param.prepareToSend().changeDBConnectionInfo(AnalyzerRunner.getDefaultConnectionInfo());
			// (new RequestMessage(param, this.processesManager)
			// ).sendMe(remoteAddres);
			// if(Configuration.IsInDeubbungMode)
			// logger.info("["+Thread.currentThread().getName()+"] " + "prepared and
			// sent: "+param);
		} catch (Exception e) {
			// logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " +
			// "Failed on prepare or send the message: "+ this.param, e);
			e.printStackTrace();
		}

	}

}
