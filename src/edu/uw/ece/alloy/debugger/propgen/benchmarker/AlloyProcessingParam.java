package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.Compressor;
import edu.uw.ece.alloy.util.Utils;

public class AlloyProcessingParam extends ProcessingParam {
    
    protected final static long CREATION_TIME = System.currentTimeMillis();
    
    final public static boolean reWrite = Boolean.valueOf(Configuration.getProp("is_rewrite_enable_on_agent"));
    
    final static Logger logger = Logger.getLogger(AlloyProcessingParam.class.getName() + "--" + Thread.currentThread().getName());
    
    private static final long serialVersionUID = 2055342494359782938L;
    
    final public static AlloyProcessingParam EMPTY_PARAM = new AlloyProcessingParam();
    
    public final PropertyToAlloyCode alloyCoder;
    public final DBConnectionInfo dBConnectionInfo;
    
    protected AlloyProcessingParam(final PropertyToAlloyCode alloyCoder, int priority, final File tmpDirectory, final DBConnectionInfo dBConnectionInfo) {
        
        super(priority, tmpDirectory);
        
        if (alloyCoder != null && !alloyCoder.equals(PropertyToAlloyCode.EMPTY_CONVERTOR)) {
            this.alloyCoder = alloyCoder.createItself();
        }
        else {
            this.alloyCoder = PropertyToAlloyCode.EMPTY_CONVERTOR;
        }
        
        this.dBConnectionInfo = new DBConnectionInfo(dBConnectionInfo);
    }
    
    protected AlloyProcessingParam(final PropertyToAlloyCode alloyCoder, int priority, final File tmpDirectory) {
        this(alloyCoder, priority, tmpDirectory, Compressor.EMPTY_DBCONNECTION);
    }
    
    protected AlloyProcessingParam(final PropertyToAlloyCode alloyCoder, int priority) {
        this(alloyCoder, priority, Compressor.EMPTY_FILE);
    }
    
    protected AlloyProcessingParam() {
        this(PropertyToAlloyCode.EMPTY_CONVERTOR, Compressor.EMPTY_PRIORITY);
    }
    
    /* (non-Javadoc)
     * @see edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam#isEmptyParam()
     */
    @Override
    public boolean isEmptyParam() {
        
        return this.equals(AlloyProcessingParam.EMPTY_PARAM);
    }
    
    /**
     * The following create methods are added in order to make an instance of
     * the object itself. It will be used for composition. The subclasses also
     * have such methods and their functionality will be composed at runtime
     * with the property generators.
     */
    protected AlloyProcessingParam createIt(final PropertyToAlloyCode alloyCoder, int priority, File tmpDirectory, DBConnectionInfo dBConnectionInfo) {
        
        return new AlloyProcessingParam(alloyCoder, priority, tmpDirectory, dBConnectionInfo);
    }
    
    protected AlloyProcessingParam createIt(final PropertyToAlloyCode alloyCoder, int priority, File tmpDirectory) {
        
        return new AlloyProcessingParam(alloyCoder, priority, tmpDirectory);
    }
    
    public AlloyProcessingParam createIt(final PropertyToAlloyCode alloyCoder, int priority) {
        
        return new AlloyProcessingParam(alloyCoder, priority);
    }
    
    public AlloyProcessingParam createIt(AlloyProcessingParam param) {
        
        return createIt(param.alloyCoder, param.priority, param.tmpDirectory, param.dBConnectionInfo);
    }
    
    public AlloyProcessingParam createIt(final PropertyToAlloyCode alloyCoder) {
        
        return createIt(alloyCoder, this.priority, this.tmpDirectory, this.dBConnectionInfo);
    }
    
    public AlloyProcessingParam createItself() {
        
        return createIt(this);
    }
    
    public String content() {
        
        return this.alloyCoder.generateAlloyCode();
    }
    
    public File srcPath() {
        
        return new File(tmpDirectory, this.alloyCoder.srcPath());
    }
    
    public File destPath() {
        
        return new File(tmpDirectory, this.alloyCoder.destPath());
    }
    
    public int priority() {
        
        return priority;
    }
    
    public List<Dependency> dependencies() {
        
        // Attach to the tmpDirectory
        final List<Dependency> result = new LinkedList<Dependency>();
        
        for (Dependency dependency : this.alloyCoder.dependencies) {
            result.add(dependency.createIt(new File(tmpDirectory, dependency.path.getPath()), dependency.content));
        }
        
        return Collections.unmodifiableList(result);
    }
    
    public void dumpContent() {
        
        dumpFile(srcPath(), content());
    }
    
    public void dumpDependecies() throws IOException {
        
        for (Dependency dependency : dependencies()) {
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
            if (srcPath().exists())
                Files.delete(srcPath().toPath());
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] " + "Unable to remove the file.", e);
            e.printStackTrace();
        }
        return this;
    }
    
    public synchronized AlloyProcessingParam removeDependecies() {
        
        for (Dependency dependency : dependencies()) {
            try {
                if (dependency.path.exists())
                    Utils.deleteRecursivly(dependency.path);
            }
            catch (IOException e) {
                logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] " + "Unable to remove the file.", e);
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
    
    /* (non-Javadoc)
     * @see edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam#changeTmpDirectory(java.io.File)
     */
    @Override
    public AlloyProcessingParam changeTmpDirectory(final File tmpDirectory) {
        
        return createIt(this.alloyCoder, this.priority, tmpDirectory, this.dBConnectionInfo);
    }
    
    public AlloyProcessingParam changeDBConnectionInfo(final DBConnectionInfo dBConnectionIno) {
        
        return createIt(this.alloyCoder, this.priority, this.tmpDirectory, dBConnectionIno);
    }
    
    public AlloyProcessingParam resetToEmptyTmpDirectory() {
        
        return createIt(this.alloyCoder, this.priority);
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
                logger.info("[" + Thread.currentThread().getName() + "] " + "The file is already exists and the configuration does not let to be replaced: " + path.getAbsolutePath());
            return path;
        }
        try {
            Util.writeAll(path.getAbsolutePath(), content);
        }
        catch (Err e) {
            logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] " + "Unable to dump the file.", e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return path;
    }
    
    @Override
    public AlloyProcessingParam prepareToUse() throws Exception {
        
        // The default behavior is to eagerly store the file on the disk
        
        // final File srcParent = new File(srcPath.getParent());
        return this;// prepareToUse(srcParent);
    }
    
    @Override
    public AlloyProcessingParam prepareToSend() throws Exception {

        // The default behavior is to eagerly store the file on the disk
        dumpAll();
        // final File srcParent = new File(srcPath.getParent());
        return this;// prepareToUse(srcParent);
    }
       
    public String getSourceFileName() {
        
        return alloyCoder.srcName().replace(".als", "");
    }
    
    @Override
    public String toString() {
        
        return "AlloyProcessingParam [hashcode=" + hashCode() + ", srcPath=" + srcPath() + ", destPath=" + destPath() +
               /*
                * ", content=" + content() + ", dependencies=" + dependencies()
                * +
                */ ", priority=" + priority + "]";
    }
    
    protected boolean isEqual(AlloyProcessingParam other) {
        
        if (alloyCoder == null) {
            if (other.alloyCoder != null)
                return false;
        }
        else if (!alloyCoder.equals(other.alloyCoder))
            return false;
            
        if (tmpDirectory == null) {
            if (other.tmpDirectory != null)
                return false;
        }
        else if (!tmpDirectory.equals(other.tmpDirectory))
            return false;
            
        if (dBConnectionInfo == null) {
            if (other.dBConnectionInfo != null)
                return false;
        }
        else if (!dBConnectionInfo.equals(other.dBConnectionInfo))
            return false;
            
        if (priority != other.priority)
            return false;
        return true;
    }
    
    @Override
    public int hashCode() {
        
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alloyCoder == null) ? 0 : alloyCoder.hashCode());
        
        result = prime * result + ((tmpDirectory == null) ? 0 : tmpDirectory.hashCode());
        
        result = prime * result + ((dBConnectionInfo == null) ? 0 : dBConnectionInfo.hashCode());
        
        result = prime * result + priority;
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AlloyProcessingParam other = (AlloyProcessingParam) obj;
        return isEqual(other);
    }
    
}
