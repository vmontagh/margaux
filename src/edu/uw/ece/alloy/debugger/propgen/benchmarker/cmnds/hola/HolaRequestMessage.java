package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.hola;

import java.util.Map;
import java.util.function.Consumer;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.InvalidParameterException;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RequestMessage;

/**
 * A request message for processing
 * 
 * @author ooodunay
 *
 */
public final class HolaRequestMessage extends RequestMessage {

	private static final long serialVersionUID = 4648657748662242448L;

	public HolaRequestMessage(final RemoteProcess process,
			final long creationTime, final HolaProcessingParam param) {
		super(process, creationTime, param);
	}

	public HolaRequestMessage(RemoteProcess process,
			HolaProcessingParam param) {
		super(process, param);
	}

	@Override
	protected RequestMessage changeParam(ProcessingParam param) {
		return new HolaRequestMessage(this.process, this.creationTime,
				(HolaProcessingParam) param);
	}

	@Override
	public HolaProcessingParam getProcessingParam() {
		return (HolaProcessingParam) param;
	}

	@Override
	public void onAction(Map<String, Object> context)
			throws InvalidParameterException {
		@SuppressWarnings("unchecked")
		Consumer<HolaProcessingParam> addNewParamInQueue = (Consumer<HolaProcessingParam>) context
				.get("addNewParamInQueue");
		try {
			addNewParamInQueue.accept(getProcessingParam());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
