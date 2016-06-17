package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger;

import java.util.Map;
import java.util.function.Consumer;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
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
	public void onAction(Map<String, Object> context) throws InvalidParameterException {
		@SuppressWarnings("unchecked")
		Consumer<RemoteProcess> processIsReady = (Consumer<RemoteProcess>) context.get("processIsReady");
		try {
			processIsReady.accept(process);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
