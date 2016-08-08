package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.hola;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uw.ece.alloy.Compressor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;
import edu.uw.ece.alloy.util.LazyFile;
import edu.uw.ece.alloy.util.Utils;

public class HolaProcessingParam extends ProcessingParam {
	
	private static final long serialVersionUID = 7726491611239691624L;
	
    final static Logger logger = Logger.getLogger(HolaProcessingParam.class.getName() + "--" + Thread.currentThread().getName());
	
	public final static HolaProcessingParam EMPTY_PARAM = new HolaProcessingParam();
	private final String filePath;
    private final String[] predNames;
    private final List<LazyFile> dependencies;

    public HolaProcessingParam(UUID analyzingSessionID) {
        this(Integer.MIN_VALUE, Compressor.EMPTY_FILE, analyzingSessionID, Long.MAX_VALUE, Compressor.EMPTY_STRING, new LinkedList<>());
    }
    
    public HolaProcessingParam(int priority, UUID analyzingSessionID, String filePath, List<LazyFile> dependentFiles, String... predNames) {
        this(priority, Compressor.EMPTY_FILE, analyzingSessionID, Long.MAX_VALUE, filePath, dependentFiles, predNames);
    }
    
    public HolaProcessingParam(int priority, File tmpLocalDirectory, UUID analyzingSessionID, String filePath, List<LazyFile> dependentFiles, String... predNames) {
        this(priority, tmpLocalDirectory, analyzingSessionID, Long.MAX_VALUE, filePath, dependentFiles, predNames);
    }
    
	public HolaProcessingParam(Integer priority, File tmpLocalDirectory,
			UUID analyzingSessionID, Long timeout, String filePath, List<LazyFile> dependentFiles, String... predNames) {
		
		super(priority, tmpLocalDirectory, analyzingSessionID, timeout);
		this.filePath = filePath;
		this.predNames = predNames;
		
		// Attach to the tmpDirectory
        final List<LazyFile> result = new LinkedList<LazyFile>();

        for (LazyFile dependency : dependentFiles) {
            result.add(new LazyFile(tmpLocalDirectory, dependency.getPath()));
        }

        this.dependencies = Collections.unmodifiableList(result);
	}
	
	private HolaProcessingParam() {
		this(UUID.randomUUID());
	}
	
	public String getFilePath() {
		return this.filePath;
	}	
	
    public String[] getPredNames() {
        return this.predNames;
    }
	
	@Override
	public boolean isEmptyParam() {
		return this.equals(EMPTY_PARAM);
	}

    /**
     * Write the content of the dependency on a disk. If the path exists, it
     * does not overwrite it.
     **/
    public void dumpDependecies() throws IOException {
        
        for (LazyFile dependency : this.dependencies) {            
            if (!dependency.exists()) {
                dependency.dumpFile();
            }
        }
    }

    public synchronized HolaProcessingParam removeDependecies() {
        
        for (LazyFile dependency : this.dependencies) {
            try {
                if (dependency.exists())
                    Utils.deleteRecursivly(dependency);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] " + "Unable to remove the dependency.", e);
                e.printStackTrace();
            }
        }
        return this;
    }
    
	@Override
	public HolaProcessingParam createItself() {
		return new HolaProcessingParam(priority, tmpLocalDirectory, analyzingSessionID, timeout, filePath, this.dependencies, predNames);
	}

	@Override
	public HolaProcessingParam changeTmpLocalDirectory(File tmpDirectory) {
		return new HolaProcessingParam(priority, tmpDirectory, analyzingSessionID, timeout, filePath, this.dependencies, predNames);
	}

	@Override
	public HolaProcessingParam prepareToUse() throws Exception {

        dumpDependecies();
		return this;
	}

	@Override
	public HolaProcessingParam prepareToSend() throws Exception {
		return this;
	}
	
	@Override
	public String toString() {
	
	   return "HolaProcessingParam [filePath=" + filePath + ", predNames=" + Arrays.toString(predNames) 
	           + ", priority=" + priority
               + ", tmpLocalDirectory=" + tmpLocalDirectory + ", analyzingSessionID="
               + analyzingSessionID + ", timeout=" + timeout + "]";
	}
}
