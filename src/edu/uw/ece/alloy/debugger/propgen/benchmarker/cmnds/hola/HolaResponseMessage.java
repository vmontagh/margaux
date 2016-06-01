package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.hola;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ExpressionAnalyzerRunner.ExpressionAnalyzingSession;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.InvalidParameterException;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ResponseMessage;

/**
 * @author ooodunay
 *
 */
public class HolaResponseMessage extends ResponseMessage {

	private static final long serialVersionUID = 1260287508826972724L;

	protected HolaResponseMessage(RemoteProcess process, long creationTime,
			HolaProcessedResult result) {
		super(process, creationTime, result);
	}

	public HolaResponseMessage(HolaProcessedResult result, RemoteProcess process) {
		super(result, process);
	}

	public void onAction(Map<String, Object> context)
			throws InvalidParameterException {
		@SuppressWarnings("unchecked")
		Function<HolaProcessingParam, Optional<ExpressionAnalyzingSession>> getSession = (Function<HolaProcessingParam, Optional<ExpressionAnalyzingSession>>) context
				.get("getSession");
		try {
			getSession.apply(this.getResult().getParam()).get().followUp(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public HolaProcessedResult getResult() {
		return (HolaProcessedResult) result;
	}

}
