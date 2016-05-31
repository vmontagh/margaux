/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.onborder;

import java.util.Map;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.InvalidParameterException;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RequestMessage;

/**
 * @author ooodunay
 *
 */
public class OnBorderRequestMessage extends RequestMessage {

	private static final long serialVersionUID = -8789737892395042206L;

	public OnBorderRequestMessage(RemoteProcess process, long creationTime,
			ProcessingParam param) {
		super(process, creationTime, param);
		// TODO Auto-generated constructor stub
	}

	public OnBorderRequestMessage(RemoteProcess process, ProcessingParam param) {
		super(process, param);
	}

	public OnBorderProcessingParam getProcessingParam() {
		return (OnBorderProcessingParam) this.param;
	}

	@Override
	protected RequestMessage changeParam(ProcessingParam param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onAction(Map<String, Object> context)
			throws InvalidParameterException {
		
		System.out.println("I received your action. Hooray!");
	}

}
