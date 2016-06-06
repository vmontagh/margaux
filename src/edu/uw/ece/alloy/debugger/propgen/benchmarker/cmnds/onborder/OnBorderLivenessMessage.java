package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.onborder;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.LivenessMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.MessageListenerAction;
import edu.uw.ece.alloy.util.events.MessageEventArgs;

public class OnBorderLivenessMessage extends LivenessMessage {

	public OnBorderLivenessMessage(RemoteProcess process, long creationTime,
			int processed, int toBeProcessed) {
		super(process, creationTime, processed, toBeProcessed);
		// TODO Auto-generated constructor stub
	}

	public OnBorderLivenessMessage(RemoteProcess process, int processed,
			int toBeProcessed) {
		super(process, processed, toBeProcessed);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onEvent(MessageListenerAction listener, MessageEventArgs args) {
		listener.actionOn(this, args);
	}

}
