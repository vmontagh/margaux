/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ExpressionAnalyzerRunner.ExpressionAnalyzingSession;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.InvalidParameterException;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ResponseMessage;

/**
 * @author vajih
 *
 */
public class PatternResponseMessage extends ResponseMessage {
	
	private static final long serialVersionUID = 8868195031032030618L;

	public PatternResponseMessage(PatternProcessedResult result, RemoteProcess process) {
		super(result, process);
	}

	public PatternResponseMessage(RemoteProcess process, long creationTime,
			PatternProcessedResult result) {
		super(process, creationTime, result);
	}

	@Override
	public PatternProcessedResult getResult() {
		return (PatternProcessedResult)result;
	}

	@Override
	public void onAction(Map<String, Object> context)
			throws InvalidParameterException {
		Consumer<PatternResponseMessage> sessionDone = (Consumer<PatternResponseMessage>) context
				.get("sessionDone");
		try {
			sessionDone.accept(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "PatternResponseMessage [result=" + result + ", process=" + process
				+ ", creationTime=" + creationTime + "]";
	}
}
