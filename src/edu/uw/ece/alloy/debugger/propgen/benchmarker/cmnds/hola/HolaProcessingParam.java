package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.hola;

import java.io.File;
import java.util.UUID;

import edu.uw.ece.alloy.Compressor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;

public class HolaProcessingParam extends ProcessingParam {
	
	private static final long serialVersionUID = 7726491611239691624L;
	
	public final static HolaProcessingParam EMPTY_PARAM = new HolaProcessingParam();
	private final String filePath;

	public HolaProcessingParam(Integer priority, File tmpLocalDirectory,
			UUID analyzingSessionID, Long timeout, String filePath) {
		
		super(priority, tmpLocalDirectory, analyzingSessionID, timeout);
		this.filePath = filePath;
	}

	public HolaProcessingParam(UUID analyzingSessionID) {
		this(Integer.MIN_VALUE, Compressor.EMPTY_FILE, analyzingSessionID, Long.MAX_VALUE, Compressor.EMPTY_STRING);
	}
	
	private HolaProcessingParam() {
		this(UUID.randomUUID());
	}
	
	public String getFilePath() {
		return this.filePath;
	}
	
	@Override
	public boolean isEmptyParam() {
		return this.equals(EMPTY_PARAM);
	}

	@Override
	public HolaProcessingParam createItself() {
		return new HolaProcessingParam(priority, tmpLocalDirectory, analyzingSessionID, timeout, filePath);
	}

	@Override
	public HolaProcessingParam changeTmpLocalDirectory(File tmpDirectory) {
		return new HolaProcessingParam(priority, tmpDirectory, analyzingSessionID, timeout, filePath);
	}

	@Override
	public HolaProcessingParam prepareToUse() throws Exception {
		return this;
	}

	@Override
	public HolaProcessingParam prepareToSend() throws Exception {
		return this;
	}
}
