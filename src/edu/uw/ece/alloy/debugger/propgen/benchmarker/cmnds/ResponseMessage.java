/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.util.Map;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.ProcessedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.util.events.MessageEventArgs;

/**
 * @author vajih
 *
 */
public abstract class ResponseMessage extends RemoteMessage {

	private static final long serialVersionUID = -2659549170674865030L;
	// TODO add a new Result
	// public final AlloyProcessedResult result;

	protected final ProcessedResult result;

	public ResponseMessage(RemoteProcess process, long creationTime, final ProcessedResult result) {
		super(process, creationTime);
		this.result = result;
	}

	public ResponseMessage(final ProcessedResult result, RemoteProcess process) {
		super(process);
		this.result = result;
	}

	public abstract ProcessedResult getResult();

	@Override
	public abstract void onAction(Map<String, Object> context) throws InvalidParameterException;

	@Override
	public void onEvent(MessageListenerAction listener, MessageEventArgs args) {
		listener.actionOn(this, args);
	}

	public static ResponseMessage createEmptyResponseMessage() {
		return new ResponseMessage(null, 0, null) {
			@Override
			public void onAction(Map<String, Object> context) throws InvalidParameterException {
			}

			@Override
			public ProcessedResult getResult() {
				return null;
			}
		};
	}

	public boolean isEmptyResponseMessage() {
		return process == null && creationTime == 0 && result == null;
	}

}
