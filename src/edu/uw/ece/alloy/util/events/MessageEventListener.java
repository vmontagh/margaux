/**
 * 
 */
package edu.uw.ece.alloy.util.events;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.DiedMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.DoneMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.LivenessMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.MessageListenerAction;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ReadyMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RequestMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ResponseMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.TerminateMessage;

/**
 * Any implementation of MessageEventListener can react subclasses of Messages
 * type.
 * 
 * @author vajih
 *
 */
public class MessageEventListener<T extends MessageEventArgs>
		implements EventListener<T>, MessageListenerAction<T> {

	@Override
	public void onEvent(Object sender, T e) {
		e.getMessage().onEvent(this, e);
	}

	@Override
	public void actionOn(RequestMessage requestMessage,
			MessageEventArgs messageArgs) {
	}

	@Override
	public void actionOn(ResponseMessage responsetMessage,
			MessageEventArgs messageArgs) {
	}

	@Override
	public void actionOn(TerminateMessage terminateMessage,
			MessageEventArgs messageArgs) {
	}

	@Override
	public void actionOn(LivenessMessage livenessMessage,
			MessageEventArgs messageArgs) {
	}

	@Override
	public void actionOn(ReadyMessage readyMessage,
			MessageEventArgs messageArgs) {
	}

	@Override
	public void actionOn(DoneMessage doneMessage, MessageEventArgs messageArgs) {
	}

	@Override
	public void actionOn(DiedMessage diedMessage, MessageEventArgs messageArgs) {
	}

}
