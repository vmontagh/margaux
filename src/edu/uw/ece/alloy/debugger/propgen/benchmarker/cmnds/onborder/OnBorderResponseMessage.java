package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.onborder;


import java.util.Map;
import java.util.function.Consumer;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.InvalidParameterException;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ResponseMessage;

/**
 * @author ooodunay
 *
 */
public class OnBorderResponseMessage extends ResponseMessage {

	private static final long serialVersionUID = 8868195031032030618L;

	public OnBorderResponseMessage(OnBorderProcessedResult result,
			RemoteProcess process) {
		super(result, process);
	}

	public OnBorderResponseMessage(RemoteProcess process, long creationTime,
			OnBorderProcessedResult result) {
		super(process, creationTime, result);
	}

	@Override
	public OnBorderProcessedResult getResult() {
		return (OnBorderProcessedResult) result;
	}

	@Override
	public void onAction(Map<String, Object> context)
			throws InvalidParameterException {
		Consumer<OnBorderResponseMessage> sessionDone = (Consumer<OnBorderResponseMessage>) context.get("sessionDone");
		try {
			sessionDone.accept(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "OnBorderResponseMessage [result=" + result + ", process=" + process
				+ ", creationTime=" + creationTime + "]";
	}
}
