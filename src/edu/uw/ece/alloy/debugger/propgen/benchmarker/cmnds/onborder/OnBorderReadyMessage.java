package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.onborder;

import java.util.Map;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcessLogger;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.InvalidParameterException;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ReadyMessage;

public class OnBorderReadyMessage extends ReadyMessage {

	private static final long serialVersionUID = 4437624162872498762L;

	public OnBorderReadyMessage(RemoteProcess process, long creationTime) {
		super(process, creationTime);
		// TODO Auto-generated constructor stub
	}

	public OnBorderReadyMessage(RemoteProcess process) {
		super(process);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onAction(Map<String, Object> context)
			throws InvalidParameterException {
		
		System.out.println("--OnBorder ready message--");
		RemoteProcessLogger manager = (RemoteProcessLogger) context.get("RemoteProcessLogger");
		manager.changeStatusToIDLE(process);
	}

}
