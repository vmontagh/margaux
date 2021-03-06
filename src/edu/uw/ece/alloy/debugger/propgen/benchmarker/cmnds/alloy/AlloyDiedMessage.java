/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy;

import java.util.Map;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcessLogger;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.DiedMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.InvalidParameterException;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.MessageListenerAction;
import edu.uw.ece.alloy.util.events.MessageEventArgs;

/**
 * @author vajih
 *
 */
public class AlloyDiedMessage extends DiedMessage {

	private static final long serialVersionUID = 6683662764100743934L;

	public AlloyDiedMessage(RemoteProcess process, long creationTime) {
		super(process, creationTime);
	}

	public AlloyDiedMessage(RemoteProcess process) {
		super(process);
	}

	@Override
	public void onAction(Map<String, Object> context)
			throws InvalidParameterException {
		RemoteProcessLogger manager = (RemoteProcessLogger) context
				.get("RemoteProcessLogger");
		manager.changeStatusToKILLING(process);
	}

	@Override
	public void onEvent(MessageListenerAction listener, MessageEventArgs args) {
		listener.actionOn(this, args);
	}

}
