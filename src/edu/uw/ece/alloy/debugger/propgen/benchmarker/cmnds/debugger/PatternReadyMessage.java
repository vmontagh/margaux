package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger;

import java.util.Map;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcessLogger;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.InvalidParameterException;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ReadyMessage;

public class PatternReadyMessage extends ReadyMessage {

	private static final long serialVersionUID = 3439223445192210745L;

	public PatternReadyMessage(RemoteProcess process, long creationTime) {
		super(process, creationTime);
	}

	public PatternReadyMessage(RemoteProcess process) {
		super(process);
	}

	@Override
	public void onAction(Map<String, Object> context)
			throws InvalidParameterException {
		RemoteProcessLogger manager = (RemoteProcessLogger) context
				.get("RemoteProcessLogger");
		manager.changeStatusToIDLE(process);
	}
}
