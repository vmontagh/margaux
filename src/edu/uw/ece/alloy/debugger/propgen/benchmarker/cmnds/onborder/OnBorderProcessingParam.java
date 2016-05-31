package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.onborder;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

import edu.uw.ece.alloy.Compressor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;

/**
 * @author ooodunay
 */
public class OnBorderProcessingParam extends ProcessingParam {

	private static final long serialVersionUID = -2184748218300115506L;
	public static final ProcessingParam EMPTY_PARAM = new OnBorderProcessingParam(
			Compressor.EMPTY_INTEGER, Compressor.EMPTY_FILE, Compressor.EMPTY_UUID,
			Compressor.EMPTY_LONG, Compressor.EMPTY_STRING, Compressor.EMPTY_STRING);

	private final String fileName;
	private final String[] predNames;

	public OnBorderProcessingParam(Integer priority, File tmpLocalDirectory,
			UUID analyzingSessionID, Long timeout, String fileName,
			String... predNames) {

		super(priority, tmpLocalDirectory, analyzingSessionID, timeout);
		this.fileName = fileName;
		this.predNames = predNames;
	}

	public Optional<String> getFileName() {
		return Optional.ofNullable(this.fileName);
	}

	public Optional<String[]> getPredNames() {
		return Optional.ofNullable(this.predNames);
	}

	@Override
	public boolean isEmptyParam() {
		return this.equals(EMPTY_PARAM);
	}

	@Override
	public ProcessingParam createItself() {
		return new OnBorderProcessingParam(this.priority, this.tmpLocalDirectory,
				this.analyzingSessionID, this.timeout, this.fileName, this.predNames);
	}

	@Override
	public ProcessingParam changeTmpLocalDirectory(File tmpDirectory) {
		return new OnBorderProcessingParam(this.priority, tmpDirectory,
				this.analyzingSessionID, this.timeout, this.fileName, this.predNames); 
	}

}
