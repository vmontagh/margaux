/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ExpressionAnalyzerRunner.ExpressionAnalyzingSession;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.InvalidParameterException;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ResponseMessage;

/**
 * @author vajih
 *
 */
public class AlloyResponseMessage extends ResponseMessage {

	private static final long serialVersionUID = -3938160835973911177L;

	protected AlloyResponseMessage(RemoteProcess process, long creationTime,
			AlloyProcessedResult result) {
		super(process, creationTime, result);
	}

	public AlloyResponseMessage(AlloyProcessedResult result,
			RemoteProcess process) {
		super(result, process);
	}

	public void onAction(Map<String, Object> context)
			throws InvalidParameterException {
		@SuppressWarnings("unchecked")
		Function<AlloyProcessingParam, Optional<ExpressionAnalyzingSession>> getSession = (Function<AlloyProcessingParam, Optional<ExpressionAnalyzingSession>>) context
				.get("getSession");
		try {
			getSession.apply(this.getResult().getParam()).get().followUp(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public AlloyProcessedResult getResult() {
		return (AlloyProcessedResult) result;
	}

}
