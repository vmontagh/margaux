package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AlloyProcessingParamLazyCompressing and AlloyProcessingParamLazyDecompressing
 * are a pair of Compressing. Compressing returns the other one and vice versa.
 * 
 * @author vajih
 *
 */
public class AlloyProcessingParamLazyCompressing
		extends AlloyProcessingParamLazy {

	final static Logger logger = Logger
			.getLogger(AlloyProcessingParamLazyCompressing.class.getName() + "--"
					+ Thread.currentThread().getName());

	private static final long serialVersionUID = -7212397055597409504L;

	final public static AlloyProcessingParamLazyCompressing EMPTY_PARAM = new AlloyProcessingParamLazyCompressing();

	protected AlloyProcessingParamLazyCompressing(UUID analyzingSessionID,
			PropertyToAlloyCode alloyCoder, int priority, File tmpDirectory,
			DBConnectionInfo dBConnectionInfo) {
		super(analyzingSessionID, priority, tmpDirectory, alloyCoder,
				dBConnectionInfo);
	}

	protected AlloyProcessingParamLazyCompressing(UUID analyzingSessionID,
			final PropertyToAlloyCode alloyCoder, int priority, File tmpDirectory) {
		super(analyzingSessionID, alloyCoder, priority, tmpDirectory);
	}

	protected AlloyProcessingParamLazyCompressing(UUID analyzingSessionID,
			final PropertyToAlloyCode alloyCoder, int priority) {
		super(analyzingSessionID, alloyCoder, priority);
	}

	protected AlloyProcessingParamLazyCompressing() {
		super();
	}

	protected AlloyProcessingParam createIt(UUID analyzingSessionID,
			final PropertyToAlloyCode alloyCoder, int priority, File tmpDirectory,
			DBConnectionInfo dBConnectionInfo) {
		return new AlloyProcessingParamLazyCompressing(analyzingSessionID,
				alloyCoder, priority, tmpDirectory, dBConnectionInfo);
	}

	protected AlloyProcessingParam createIt(UUID analyzingSessionID,
			final PropertyToAlloyCode alloyCoder, int priority, File tmpDirectory) {
		return new AlloyProcessingParamLazyCompressing(analyzingSessionID,
				alloyCoder, priority, tmpDirectory);
	}

	public AlloyProcessingParam createIt(UUID analyzingSessionID,
			final PropertyToAlloyCode alloyCoder, int priority) {
		return new AlloyProcessingParamLazyCompressing(analyzingSessionID,
				alloyCoder, priority);
	}

	public AlloyProcessingParam createIt(
			AlloyProcessingParamLazyCompressing param) {
		return new AlloyProcessingParamLazyCompressing(
				param.getAnalyzingSessionID().get(), param.getAlloyCoder().get(),
				param.getPriority().get(), param.getTmpLocalDirectory().get(),
				param.dBConnectionInfo);
	}

	public AlloyProcessingParam prepareToSend() {
		try {
			return AlloyProcessingParamLazyDecompressing.EMPTY_PARAM.createIt(
					this.analyzingSessionID, alloyCoder.compress(), this.priority,
					this.getTmpLocalDirectory().get(), this.dBConnectionInfo);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
					+ "Unable to compress the object: " + this, e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public AlloyProcessingParam prepareToUse() {
		return this;
	}

}
