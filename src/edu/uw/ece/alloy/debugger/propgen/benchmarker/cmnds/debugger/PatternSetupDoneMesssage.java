package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger;

import java.util.Map;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.DoneMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.InvalidParameterException;

public class PatternSetupDoneMesssage extends DoneMessage {

	private static final long serialVersionUID = 7557612838282041241L;

	public PatternSetupDoneMesssage(RemoteProcess process, long creationTime) {
		super(process, creationTime);
		// TODO Auto-generated constructor stub
	}

	public PatternSetupDoneMesssage(RemoteProcess process) {
		super(process);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onAction(Map<String, Object> context) throws InvalidParameterException {
		// TODO Auto-generated method stub

	}

}
