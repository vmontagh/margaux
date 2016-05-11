package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AlloyProcessingParamLazyDecompressing
		extends AlloyProcessingParamLazyCompressing {

	private static final long serialVersionUID = -7212397055597409504L;
	final static Logger logger = Logger
			.getLogger(AlloyProcessingParamLazyDecompressing.class.getName() + "--"
					+ Thread.currentThread().getName());

	final public static AlloyProcessingParamLazyDecompressing EMPTY_PARAM = new AlloyProcessingParamLazyDecompressing();

	protected AlloyProcessingParamLazyDecompressing(UUID analyzingSessionID,
			int priority, File tmpDirectory, PropertyToAlloyCode alloyCoder,
			DBConnectionInfo dBConnectionInfo) {
		super(analyzingSessionID, alloyCoder, priority, tmpDirectory,
				dBConnectionInfo);
	}

	protected AlloyProcessingParamLazyDecompressing(UUID analyzingSessionID,
			final PropertyToAlloyCode alloyCoder, int priority, File tmpDirectory) {
		super(analyzingSessionID, alloyCoder, priority, tmpDirectory);
	}

	protected AlloyProcessingParamLazyDecompressing(UUID analyzingSessionID,
			final PropertyToAlloyCode alloyCoder, int priority) {
		super(analyzingSessionID, alloyCoder, priority);
	}

	protected AlloyProcessingParamLazyDecompressing() {
		super();
	}

	protected AlloyProcessingParam createIt(UUID analyzingSessionID,
			final PropertyToAlloyCode alloyCoder, int priority, File tmpDirectory,
			DBConnectionInfo dBConnectionInfo) {
		return new AlloyProcessingParamLazyDecompressing(analyzingSessionID,
				priority, tmpDirectory, alloyCoder, dBConnectionInfo);
	}

	protected AlloyProcessingParam createIt(UUID analyzingSessionID,
			final PropertyToAlloyCode alloyCoder, int priority, File tmpDirectory) {
		return new AlloyProcessingParamLazyDecompressing(analyzingSessionID,
				alloyCoder, priority, tmpDirectory);
	}

	public AlloyProcessingParam createIt(UUID analyzingSessionID,
			final PropertyToAlloyCode alloyCoder, int priority) {
		return new AlloyProcessingParamLazyDecompressing(analyzingSessionID,
				alloyCoder, priority);
	}

	public AlloyProcessingParam createIt(
			AlloyProcessingParamLazyDecompressing param) {
		return new AlloyProcessingParamLazyDecompressing(
				param.getAnalyzingSessionID().get(), param.priority,
				param.getTmpLocalDirectory().get(), param.getAlloyCoder().get(),
				param.dBConnectionInfo);
	}

	public AlloyProcessingParam prepareToSend() {
		return this;
	}

	public AlloyProcessingParam prepareToUse() {
		try {
			return AlloyProcessingParamLazyCompressing.EMPTY_PARAM.createIt(
					this.analyzingSessionID, this.alloyCoder.deCompress(), this.priority,
					this.tmpLocalDirectory, this.dBConnectionInfo);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
					+ "Unable to compress the object: " + this, e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
