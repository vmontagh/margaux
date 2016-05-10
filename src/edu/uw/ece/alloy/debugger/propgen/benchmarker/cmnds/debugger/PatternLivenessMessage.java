package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.LivenessMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.MessageListenerAction;
import edu.uw.ece.alloy.util.events.MessageEventArgs;

public class PatternLivenessMessage extends LivenessMessage {

	private static final long serialVersionUID = 1L;

	public PatternLivenessMessage(RemoteProcess process, int processed,
			int toBeProcessed) {
		super(process, processed, toBeProcessed);
	}

	@Override
	public void onEvent(MessageListenerAction listener, MessageEventArgs args) {
		listener.actionOn(this, args);
	}
}
