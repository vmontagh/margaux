package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import edu.uw.ece.alloy.Compressor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.PropertyToAlloyCode;
import edu.uw.ece.alloy.util.LazyFile;

public class PatternProcessingParam extends ProcessingParam {

	private static final long serialVersionUID = -7728543765538276470L;

	/* from the file name to the File */
	protected final Map<String, LazyFile> files;

	protected final String fieldName;
	/*
	 * An implementation of PropertyToAlloyCode determines the kind of analysis
	 * should be done. For example IfPropertyToAlloyCode makes expression => prop
	 */
	protected final PropertyToAlloyCode propertyToAlloyCode;
	protected final String expression;
	protected final String scope;

	public Optional<File> getFile(String fileName) {
		return Optional.ofNullable(files.get(fileName));
	}

	public Optional<List<File>> getFiles() {
		return Optional.ofNullable(
				Collections.unmodifiableList(new ArrayList<>(files.values())));
	}

	public Optional<String> getFieldName() {
		return Optional.ofNullable(fieldName);
	}

	public Optional<PropertyToAlloyCode> getPropertyToAlloyCode() {
		return Optional.ofNullable(propertyToAlloyCode);
	}

	public Optional<String> getExpression() {
		return Optional.ofNullable(expression);
	}

	public Optional<String> getScope() {
		return Optional.ofNullable(scope);
	}

	public static final PatternProcessingParam EMPTY_PARAM = new PatternProcessingParam(
			Compressor.EMPTY_INTEGER, Compressor.EMPTY_FILE, Compressor.EMPTY_UUID,
			Compressor.EMPTY_LONG, Compressor.EMPTY_STRING,
			PropertyToAlloyCode.EMPTY_CONVERTOR, Compressor.EMPTY_STRING,
			Compressor.EMPTY_STRING, Collections.emptyMap());

	public PatternProcessingParam(Integer priority, File tmpLocalDirectory,
			UUID analyzingSessionID, Long timeout, String fieldName,
			PropertyToAlloyCode propertyToAlloyCode, String expression, String scope,
			Map<String, LazyFile> files) {
		super(priority, tmpLocalDirectory, analyzingSessionID, timeout);
		this.fieldName = fieldName;
		this.propertyToAlloyCode = propertyToAlloyCode;
		this.expression = expression;
		this.scope = scope;
		this.files = Collections.unmodifiableMap(new HashMap<>(files));
	}

	public PatternProcessingParam(Integer priority, File tmpLocalDirectory,
			UUID analyzingSessionID, Long timeout, String fieldName,
			PropertyToAlloyCode propertyToAlloyCode, String expression,
			String scope) {
		this(priority, tmpLocalDirectory, analyzingSessionID, timeout, fieldName,
				propertyToAlloyCode, expression, scope, Collections.emptyMap());
	}

	/**
	 * Create a new request with a new UUID
	 * 
	 * @param priority
	 * @param tmpLocalDirectory
	 * @param timeout
	 * @param fieldName
	 * @param propertyToAlloyCode
	 * @param expression
	 * @param scope
	 */
	public PatternProcessingParam(Integer priority, File tmpLocalDirectory,
			Long timeout, String fieldName, PropertyToAlloyCode propertyToAlloyCode,
			String expression, String scope) {
		this(priority, tmpLocalDirectory, UUID.randomUUID(), timeout, fieldName,
				propertyToAlloyCode, expression, scope);
	}

	@Override
	public boolean isEmptyParam() {
		return this.equals(EMPTY_PARAM);
	}

	@Override
	public PatternProcessingParam createItself() {
		return new PatternProcessingParam(this.priority, this.tmpLocalDirectory,
				this.analyzingSessionID, this.timeout, this.fieldName,
				this.propertyToAlloyCode, this.expression, this.scope, this.files);
	}

	@Override
	public PatternProcessingParam changeTmpLocalDirectory(File tmpDirectory) {
		return new PatternProcessingParam(this.priority, tmpDirectory,
				this.analyzingSessionID, this.timeout, this.fieldName,
				this.propertyToAlloyCode, this.expression, this.scope, this.files);
	}

	@Override
	public PatternProcessingParam prepareToUse() throws Exception {
		return this;
	}

	@Override
	public PatternProcessingParam prepareToSend() throws Exception {
		return this;
	}

	public static class PatternProcessingParamLazy
			extends PatternProcessingParam {

		private static final long serialVersionUID = 4915980895863419341L;

		public PatternProcessingParamLazy(Integer priority, File tmpLocalDirectory,
				Long timeout, String fieldName, PropertyToAlloyCode propertyToAlloyCode,
				String expression, String scope) {
			super(priority, tmpLocalDirectory, timeout, fieldName,
					propertyToAlloyCode, expression, scope);
		}

		public PatternProcessingParamLazy(Integer priority, File tmpLocalDirectory,
				UUID analyzingSessionID, Long timeout, String fieldName,
				PropertyToAlloyCode propertyToAlloyCode, String expression,
				String scope) {
			super(priority, tmpLocalDirectory, analyzingSessionID, timeout, fieldName,
					propertyToAlloyCode, expression, scope);
		}

		public PatternProcessingParamLazy(Integer priority, File tmpLocalDirectory,
				UUID analyzingSessionID, Long timeout, String fieldName,
				PropertyToAlloyCode propertyToAlloyCode, String expression,
				String scope, Map<String, LazyFile> files) {
			super(priority, tmpLocalDirectory, analyzingSessionID, timeout, fieldName,
					propertyToAlloyCode, expression, scope, files);
		}

		@Override
		public PatternProcessingParam prepareToSend() throws Exception {

			Map<String, LazyFile> loadFiles = new HashMap<>();
			files.keySet().stream()
					.forEach(name -> loadFiles.put(name, loadFiles.get(name).load()));

			return new PatternProcessingParam(this.priority, tmpLocalDirectory,
					this.analyzingSessionID, this.timeout, this.fieldName,
					this.propertyToAlloyCode, this.expression, this.scope,
					Collections.unmodifiableMap(loadFiles));
		}

		@Override
		public PatternProcessingParam prepareToUse() throws Exception {

			Map<String, LazyFile> unloadFiles = new HashMap<>();
			files.keySet().stream().forEach(name -> unloadFiles.put(name,
					unloadFiles.get(name).unload(tmpLocalDirectory)));

			return new PatternProcessingParam(this.priority, tmpLocalDirectory,
					this.analyzingSessionID, this.timeout,

					this.fieldName, this.propertyToAlloyCode, this.expression, this.scope,
					Collections.unmodifiableMap(unloadFiles));
		}

		@Override
		public PatternProcessingParamLazy createItself() {
			return new PatternProcessingParamLazy(this.priority,
					this.tmpLocalDirectory, this.analyzingSessionID, this.timeout,
					this.fieldName, this.propertyToAlloyCode, this.expression, this.scope,
					this.files);
		}

		@Override
		public PatternProcessingParamLazy changeTmpLocalDirectory(
				File tmpDirectory) {
			return new PatternProcessingParamLazy(this.priority, tmpDirectory,
					this.analyzingSessionID, this.timeout, this.fieldName,
					this.propertyToAlloyCode, this.expression, this.scope, this.files);
		}

	}

}
