package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.LivenessMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.MessageListenerAction;
import edu.uw.ece.alloy.util.events.MessageEventArgs;

public class AlloyLivenessMessage extends LivenessMessage {

	private static final long serialVersionUID = 5682869876334839992L;

	public AlloyLivenessMessage(RemoteProcess process, int processed,
			int toBeProcessed) {
		super(process, processed, toBeProcessed);
	}

	public AlloyLivenessMessage(RemoteProcess process, long creationTime,
			int processed, int toBeProcessed) {
		super(process, creationTime, processed, toBeProcessed);
	}

	@Override
	public void onEvent(MessageListenerAction listener, MessageEventArgs args) {
		listener.actionOn(this, args);
	}

}
