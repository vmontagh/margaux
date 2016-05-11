package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.util.UUID;

public class AlloyProcessingParamLazy extends AlloyProcessingParam {

	private static final long serialVersionUID = -4596969763967530052L;

	final public static AlloyProcessingParamLazy EMPTY_PARAM = new AlloyProcessingParamLazy();

	protected AlloyProcessingParamLazy(UUID analyzingSessionID, int priority,
			File tmpDirectory, PropertyToAlloyCode alloyCoder,
			DBConnectionInfo dBConnectionInfo) {
		super(analyzingSessionID, priority, tmpDirectory, alloyCoder,
				dBConnectionInfo);
	}

	protected AlloyProcessingParamLazy(UUID analyzingSessionID,
			final PropertyToAlloyCode alloyCoder, int priority, File tmpDirectory) {
		super(analyzingSessionID, priority, tmpDirectory, alloyCoder);
	}

	protected AlloyProcessingParamLazy(UUID analyzingSessionID,
			final PropertyToAlloyCode alloyCoder, int priority) {
		super(analyzingSessionID, priority, alloyCoder);
	}

	protected AlloyProcessingParamLazy() {
		super();
	}

	/**
	 * The following create methods are added in order to make an instance of the
	 * object itself. It will be used for composition. The subclasses also have
	 * such methods and their functionality will be composed at runtime with the
	 * property generators.
	 */
	protected AlloyProcessingParam createIt(UUID analyzingSessionID,
			final PropertyToAlloyCode alloyCoder, int priority, File tmpDirectory,
			DBConnectionInfo dBConnectionInfo) {
		return new AlloyProcessingParamLazy(analyzingSessionID, priority,
				tmpDirectory, alloyCoder, dBConnectionInfo);
	}

	protected AlloyProcessingParam createIt(UUID analyzingSessionID,
			final PropertyToAlloyCode alloyCoder, int priority, File tmpDirectory) {
		return new AlloyProcessingParamLazy(analyzingSessionID, alloyCoder,
				priority, tmpDirectory);
	}

	public AlloyProcessingParam createIt(UUID analyzingSessionID,
			final PropertyToAlloyCode alloyCoder, int priority) {
		return new AlloyProcessingParamLazy(analyzingSessionID, alloyCoder,
				priority);
	}

	public AlloyProcessingParam createIt(AlloyProcessingParamLazy param) {
		return new AlloyProcessingParamLazy(param.getAnalyzingSessionID().get(),
				param.getPriority().get(), param.getTmpLocalDirectory().get(),
				param.getAlloyCoder().get(), param.getDBConnectionInfo().get());
	}

	public AlloyProcessingParam prepareToSend() throws Exception {
		return this;
	}

	public AlloyProcessingParam prepareToUse() throws Exception {
		return dumpAll();
	}

}
