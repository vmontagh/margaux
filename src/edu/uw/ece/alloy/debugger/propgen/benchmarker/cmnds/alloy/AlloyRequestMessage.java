/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy;

import java.util.Map;
import java.util.function.Consumer;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.InvalidParameterException;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RequestMessage;

/**
 * A request message for processing
 * 
 * @author vajih
 *
 */
public final class AlloyRequestMessage extends RequestMessage {

	private static final long serialVersionUID = -8897105757528949775L;

	public AlloyRequestMessage(final RemoteProcess process,
			final long creationTime, final AlloyProcessingParam param) {
		super(process, creationTime, param);
	}

	public AlloyRequestMessage(RemoteProcess process,
			AlloyProcessingParam param) {
		super(process, param);
	}

	@Override
	protected RequestMessage changeParam(ProcessingParam param) {
		return new AlloyRequestMessage(this.process, this.creationTime,
				(AlloyProcessingParam) param);
	}

	@Override
	public AlloyProcessingParam getProcessingParam() {
		return (AlloyProcessingParam) param;
	}

	@Override
	public void onAction(Map<String, Object> context)
			throws InvalidParameterException {
		@SuppressWarnings("unchecked")
		Consumer<AlloyProcessingParam> addNewParamInQueue = (Consumer<AlloyProcessingParam>) context
				.get("addNewParamInQueue");
		try {
			addNewParamInQueue.accept(getProcessingParam());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
