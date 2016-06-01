package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.hola;

import java.util.Map;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcessLogger;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.InvalidParameterException;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ReadyMessage;

public class HolaReadyMessage extends ReadyMessage {

	private static final long serialVersionUID = 1176177100747733805L;

	public HolaReadyMessage(RemoteProcess process) {
		super(process);
	}

	public HolaReadyMessage(RemoteProcess process, long creationTime) {
		super(process, creationTime);
	}

	@Override
	public void onAction(Map<String, Object> context)
			throws InvalidParameterException {

		System.out.println("--Hola ready message--");
		RemoteProcessLogger manager = (RemoteProcessLogger) context.get("RemoteProcessLogger");
		manager.changeStatusToIDLE(process);
	}

}
