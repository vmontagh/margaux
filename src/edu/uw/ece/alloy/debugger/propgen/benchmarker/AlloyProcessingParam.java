package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.Compressor;
import edu.uw.ece.alloy.util.Utils;

public class AlloyProcessingParam extends ProcessingParam {

	final public static boolean reWrite = Boolean
			.valueOf(Configuration.getProp("is_rewrite_enable_on_agent"));
	final static Logger logger = Logger
			.getLogger(AlloyProcessingParam.class.getName() + "--"
					+ Thread.currentThread().getName());
	/**
	 * 
	 */
	private static final long serialVersionUID = 2055342494359782938L;

	final public static AlloyProcessingParam EMPTY_PARAM = new AlloyProcessingParam();

	protected final PropertyToAlloyCode alloyCoder;
	protected final DBConnectionInfo dBConnectionInfo;

	protected AlloyProcessingParam() {
		this(UUID.randomUUID());
	}

	protected AlloyProcessingParam(UUID analyzingSessionID, Integer priority,
			File tmpLocalDirectory, PropertyToAlloyCode alloyCoder,
			DBConnectionInfo dBConnectionInfo) {
		super(priority, tmpLocalDirectory, analyzingSessionID);
		this.alloyCoder = alloyCoder;
		this.dBConnectionInfo = dBConnectionInfo;
	}

	protected AlloyProcessingParam(UUID analyzingSessionID, int priority,
			PropertyToAlloyCode alloyCoder) {
		this(analyzingSessionID, priority, Compressor.EMPTY_FILE,
				PropertyToAlloyCode.EMPTY_CONVERTOR,
				DBConnectionInfo.EMPTY_DBCONNECTIONINFO);
	}

	protected AlloyProcessingParam(UUID analyzingSessionID) {
		this(analyzingSessionID, Integer.MIN_VALUE, Compressor.EMPTY_FILE,
				PropertyToAlloyCode.EMPTY_CONVERTOR,
				DBConnectionInfo.EMPTY_DBCONNECTIONINFO);
	}

	/**
	 * The following create methods are added in order to make an instance of the
	 * object itself. It will be used for composition. The subclasses also have
	 * such methods and their functionality will be composed at runtime with the
	 * property generators.
	 */

	protected AlloyProcessingParam createIt(UUID analyzingSessionID,
			final PropertyToAlloyCode alloyCoder, int priority,
			File tmpLocalDirectory, DBConnectionInfo dBConnectionInfo) {
		return new AlloyProcessingParam(analyzingSessionID, priority,
				tmpLocalDirectory, alloyCoder, dBConnectionInfo);
	}

	protected AlloyProcessingParam createIt(UUID analyzingSessionID,
			PropertyToAlloyCode alloyCoder, Integer priority, File tmpLocalDirectory,
			DBConnectionInfo dBConnectionInfo) {
		return new AlloyProcessingParam(analyzingSessionID, priority,
				tmpLocalDirectory, alloyCoder, dBConnectionInfo);
	}

	protected AlloyProcessingParam createIt(UUID analyzingSessionID,
			final PropertyToAlloyCode alloyCoder, int priority,
			File tmpLocalDirectory) {
		return new AlloyProcessingParam(analyzingSessionID, priority,
				tmpLocalDirectory, alloyCoder, DBConnectionInfo.EMPTY_DBCONNECTIONINFO);
	}

	public AlloyProcessingParam createIt(UUID analyzingSessionID,
			final PropertyToAlloyCode alloyCoder, int priority) {
		return new AlloyProcessingParam(analyzingSessionID, priority, alloyCoder);
	}

	public AlloyProcessingParam createIt(final PropertyToAlloyCode alloyCoder) {
		return new AlloyProcessingParam(this.analyzingSessionID, Integer.MIN_VALUE,
				alloyCoder);
	}

	public AlloyProcessingParam createIt(AlloyProcessingParam param) {
		return createIt(param.analyzingSessionID, param.alloyCoder, param.priority,
				param.tmpLocalDirectory, param.dBConnectionInfo);
	}

	public AlloyProcessingParam createIt(UUID analyzingSessionID,
			final PropertyToAlloyCode alloyCoder) {
		return createIt(analyzingSessionID, PropertyToAlloyCode.EMPTY_CONVERTOR,
				this.priority, this.tmpLocalDirectory, this.dBConnectionInfo);
	}

	public AlloyProcessingParam createItself() {
		return createIt(this);
	}

	public Optional<String> content() {
		return this.alloyCoder.equals(EMPTY_PARAM.alloyCoder) ? Optional.empty()
				: Optional.of(this.alloyCoder.generateAlloyCode());
	}

	public Optional<File> getSrcPath() {
		return !this.tmpLocalDirectory.equals(EMPTY_PARAM.tmpLocalDirectory)
				&& !this.alloyCoder.equals(EMPTY_PARAM.alloyCoder)
						? Optional
								.of(new File(tmpLocalDirectory, this.alloyCoder.srcPath()))
						: Optional.empty();
	}

	public Optional<File> getDestPath() {
		return !this.tmpLocalDirectory.equals(EMPTY_PARAM.tmpLocalDirectory)
				&& !this.alloyCoder.equals(EMPTY_PARAM.alloyCoder)
						? Optional
								.of(new File(tmpLocalDirectory, this.alloyCoder.destPath()))
						: Optional.empty();
	}

	public Optional<PropertyToAlloyCode> getAlloyCoder() {
		return Optional.ofNullable(alloyCoder);
	}

	public Optional<DBConnectionInfo> getDBConnectionInfo() {
		return Optional.ofNullable(dBConnectionInfo);
	}

	public List<Dependency> dependencies() {

		// Attach to the tmpDirectory
		final List<Dependency> result = new LinkedList<Dependency>();

		for (Dependency dependency : this.alloyCoder.dependencies) {
			result.add(dependency.createIt(
					new File(tmpLocalDirectory, dependency.path.getPath()),
					dependency.content));
		}

		return Collections.unmodifiableList(result);
	}

	public void dumpContent() {
		dumpFile(getSrcPath().orElseThrow(RuntimeException::new),
				content().orElseThrow(RuntimeException::new));
	}

	/**
	 * Write the content of the dependency on a disk. If the path exists, it does
	 * not overwrite it.
	 **/
	public void dumpDependecies() throws IOException {
		for (Dependency dependency : dependencies()) {
			if (dependency.path.exists())
				continue;
			// create relative directories
			if (!dependency.path.getParentFile().exists())
				dependency.path.getParentFile().mkdirs();
			dumpFile(dependency.path, dependency.content);
			// A hack: Instead of copying the content, just the path is copied.
			// Files.copy(new File(dependency.content).toPath(),
			// dependency.path.toPath(),StandardCopyOption.REPLACE_EXISTING);
		}
	}

	public AlloyProcessingParam dumpAll() throws IOException {
		dumpDependecies();
		dumpContent();
		return this;
	}

	public synchronized AlloyProcessingParam removeContent() {
		try {
			if (getSrcPath().orElseThrow(RuntimeException::new).exists())
				Files.delete(getSrcPath().orElseThrow(RuntimeException::new).toPath());
		} catch (IOException e) {
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
					+ "Unable to remove the file.", e);
			e.printStackTrace();
		}
		return this;
	}

	public synchronized AlloyProcessingParam removeDependecies() {
		for (Dependency dependency : dependencies()) {
			try {
				if (dependency.path.exists())
					Utils.deleteRecursivly(dependency.path);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
						+ "Unable to remove the file.", e);
				e.printStackTrace();
			}
		}
		return this;
	}

	public AlloyProcessingParam removeAll() {
		removeContent();
		removeDependecies();
		return this;
	}

	public AlloyProcessingParam changeTmpLocalDirectory(final File tmpDirectory) {
		return createIt(this.analyzingSessionID, this.alloyCoder, this.priority,
				tmpDirectory, this.dBConnectionInfo);
	}

	public AlloyProcessingParam changeDBConnectionInfo(
			final DBConnectionInfo dBConnectionIno) {
		return createIt(this.analyzingSessionID, this.alloyCoder, this.priority,
				this.tmpLocalDirectory, dBConnectionIno);
	}

	public AlloyProcessingParam resetToEmptyTmpLocalDirectory() {
		return createIt(this.analyzingSessionID, this.alloyCoder, this.priority,
				Compressor.EMPTY_FILE, this.dBConnectionInfo);
	}

	/**
	 * compress and decompress the content for sending over socket. The subClass
	 * have to override the compressions.
	 * 
	 * @return
	 * @throws Exception
	 */
	/*
	 * public AlloyProcessingParam compress() throws Exception{ return this; }
	 * 
	 * public AlloyProcessingParam decompress() throws Exception{ return this; }
	 */

	protected File dumpFile(final File path, final String content) {
		if (!reWrite && path.exists()) {
			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "] "
						+ "The file is already exists and the configuration does not let to be replaced: "
						+ path.getAbsolutePath());
			return path;
		}
		try {
			Util.writeAll(path.getAbsolutePath(), content);
		} catch (Err e) {
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
					+ "Unable to dump the file.", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return path;
	}

	// The default behavior is to eagerly store the file on the disk
	public AlloyProcessingParam prepareToUse() throws Exception {
		dumpAll();
		// final File srcParent = new File(srcPath.getParent());
		return this;// prepareToUse(srcParent);
	}

	// The default behavior is to eagerly store the file on the disk
	public AlloyProcessingParam prepareToSend() throws Exception {
		// dumpAll();
		// final File srcParent = new File(srcPath.getParent());
		return this;// prepareToUse(srcParent);
	}

	/*
	 * public AlloyProcessingParam prepareToUse(final File destFolder) throws
	 * Exception{ return this; }
	 */

	public String getSourceFileName() {
		return alloyCoder.srcName().replace(".als", "");
	}

	@Override
	public int compareTo(ProcessingParam o) {
		if (o == null)
			return -1;
		if (o.priority == this.priority)
			return 0;
		if (o.priority < this.priority)
			return 1;
		return -1;
	}

	@Override
	public boolean isEmptyParam() {
		return priority.equals(Integer.MIN_VALUE)
				&& tmpLocalDirectory.equals(EMPTY_PARAM.tmpLocalDirectory)
				&& alloyCoder.equals(PropertyToAlloyCode.EMPTY_CONVERTOR)
				&& !dBConnectionInfo.equals(DBConnectionInfo.EMPTY_DBCONNECTIONINFO);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((alloyCoder == null) ? 0 : alloyCoder.hashCode());
		result = prime * result
				+ ((dBConnectionInfo == null) ? 0 : dBConnectionInfo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AlloyProcessingParam other = (AlloyProcessingParam) obj;
		if (alloyCoder == null) {
			if (other.alloyCoder != null) {
				return false;
			}
		} else if (!alloyCoder.equals(other.alloyCoder)) {
			return false;
		}
		if (dBConnectionInfo == null) {
			if (other.dBConnectionInfo != null) {
				return false;
			}
		} else if (!dBConnectionInfo.equals(other.dBConnectionInfo)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "AlloyProcessingParam [alloyCoder=" + alloyCoder.toString()
				+ ", dBConnectionInfo=" + dBConnectionInfo + ", priority=" + priority
				+ ", tmpLocalDirectory=" + tmpLocalDirectory + ", analyzingSessionID="
				+ analyzingSessionID + "]";
	}
}
