/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ExpressionAnalyzerRunner.ExpressionAnalyzingSession;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.InvalidParameterException;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RequestMessage;

/**
 * The message class carries a request from debugger to expression analyzer.
 * 
 * @author vajih
 *
 */
public class PatternRequestMessage extends RequestMessage {

	private static final long serialVersionUID = 613569577999219478L;

	/**
	 * @param process
	 * @param param
	 */
	public PatternRequestMessage(RemoteProcess process,
			PatternProcessingParam param) {
		super(process, param);
	}

	public PatternProcessingParam getProcessingParam() {
		return (PatternProcessingParam) this.param;
	}

	@Override
	protected PatternRequestMessage changeParam(ProcessingParam param) {
		return new PatternRequestMessage(this.process,
				(PatternProcessingParam) param);
	}

	@Override
	public void onAction(Map<String, Object> context)
			throws InvalidParameterException {
		@SuppressWarnings("unchecked")
		Function<PatternProcessingParam, Optional<ExpressionAnalyzingSession>> createNewSession = (Function<PatternProcessingParam, Optional<ExpressionAnalyzingSession>>) context
				.get("createNewSession");
		try {
			createNewSession.apply(getProcessingParam()).get().start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
