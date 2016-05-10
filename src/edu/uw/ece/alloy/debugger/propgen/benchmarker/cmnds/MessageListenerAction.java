package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import edu.uw.ece.alloy.util.events.MessageEventArgs;

/**
 * The interface expresses the Visitor interface of the 
 * visitor pattern. Once an event is fired, an implementation of
 * this interface is passed to the message object.
 * @author vajih
 *
 */
public interface MessageListenerAction<T extends MessageEventArgs> {
	void actionOn(RequestMessage requestMessage, T messageArgs);
	void actionOn(ResponseMessage responsetMessage, T messageArgs);
	void actionOn(TerminateMessage terminateMessage, T messageArgs);
	void actionOn(LivenessMessage livenessMessage, T messageArgs);
	void actionOn(ReadyMessage readyMessage, T messageArgs);
	void actionOn(DoneMessage doneMessage, T messageArgs);
	void actionOn(DiedMessage diedMessage, T messageArgs);
}
