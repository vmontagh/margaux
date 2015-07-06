package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.util.Utils;

public class AlloyProcessingParam implements Comparable<AlloyProcessingParam>, Serializable {

	protected final static File EMPTY_FILE = new File( "NONE" );
	protected final static String EMPTY_CONTENT = "";
	protected final static int EMPTY_PRIORITY = -1;

	protected final static long CREATION_TIME = System.currentTimeMillis(); 

	final public static boolean reWrite = Boolean.valueOf(Configuration.getProp("is_rewrite_enable_on_agent"));

	final static Logger logger = Logger.getLogger(AlloyProcessingParam.class.getName()+"--"+Thread.currentThread().getName());
	/**
	 * 
	 */
	private static final long serialVersionUID = 2055342494359782938L;


	final public static AlloyProcessingParam EMPTY_PARAM = new AlloyProcessingParam();

	public final int priority;
	
	public final PropertyToAlloyCode alloyCoder; 
	
	protected AlloyProcessingParam(final PropertyToAlloyCode alloyCoder, int priority) {
		this.alloyCoder = alloyCoder != null ? alloyCoder.createItself() : PropertyToAlloyCode.EMPTY_CONVERTOR;
		this.priority = priority;
	}
	
	protected AlloyProcessingParam() {
		this.alloyCoder = PropertyToAlloyCode.EMPTY_CONVERTOR;
		this.priority = EMPTY_PRIORITY;
	}
	
	/**
	 * The following create methods are added in order to make an instance of the object itself. It will be
	 * used for composition. The subclasses also have such methods and their functionality will be composed
	 * at runtime with the property generators.   
	 */
	public AlloyProcessingParam createIt(final PropertyToAlloyCode alloyCoder, int priority) {
		return new AlloyProcessingParam(alloyCoder,  priority);
	}

	public AlloyProcessingParam createIt(AlloyProcessingParam param) {
		return createIt(param.alloyCoder,  param.priority);
	}
	
	public AlloyProcessingParam createItself() {
		return createIt(this);
	}
	
	
	public String content(){
		return this.alloyCoder.generateAlloyCode();
	}
	
	public File srcPath(){
		return this.alloyCoder.srcPath();
	}

	public File destPath(){
		return this.alloyCoder.destPath();
	}

	public int priority(){
		return priority;
	}
	
	public List<Pair<File, String>> dependencies(){
		return Collections.unmodifiableList(this.alloyCoder.dependencies);
	}
	
	public void dumpContent(){
		dumpFile(srcPath(), content());
	}

	public void dumpDependecies(){
		for(Pair<File, String> pair:dependencies()){
			dumpFile(pair.a, pair.b);
		}
	}

	public AlloyProcessingParam dumpAll(){

		dumpContent();
		dumpDependecies();

		return this;
	}

	public AlloyProcessingParam removeContent(){
		try {
			if(srcPath().exists())
				Files.delete(srcPath().toPath());
		} catch (IOException e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Unable to remove the file.", e);
			e.printStackTrace();
		}
		return this;
	}


	public AlloyProcessingParam removeDependecies(){
		for(Pair<File, String> pair:dependencies()){
			try {
				if(pair.a.exists())
					Utils.deleteRecursivly(pair.a);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Unable to remove the file.", e);
				e.printStackTrace();
			}
		}
		return this;
	}
	
	public AlloyProcessingParam removeAll(){
		removeContent();
		removeDependecies();
		 return this;
	}

	/**
	 * compress and decompress the content for sending over socket. 
	 * The subClass have to override the compressions.   
	 * @return
	 * @throws Exception 
	 */
	/*	public AlloyProcessingParam compress() throws Exception{
		return this;
	}

	public AlloyProcessingParam decompress() throws Exception{
		return this;
	}*/

	protected File dumpFile(final File path, final String content){
		if(!reWrite && path.exists()) {
			logger.info("["+Thread.currentThread().getName()+"] " + "The file is already exists and the configuration does not let to be replaced: "+path.getAbsolutePath());
			return path;
		}
		try {
			Util.writeAll(path.getAbsolutePath(), content);
		} catch (Err e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Unable to dump the file.", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return path;
	}

	//The default behavior is to eagerly store the file on the disk
	public AlloyProcessingParam prepareToUse() throws Exception{

		//final File srcParent = new File(srcPath.getParent());
		return this;//prepareToUse(srcParent);
	}

	//The default behavior is to eagerly store the file on the disk
	public AlloyProcessingParam prepareToSend() throws Exception{
		dumpAll();
		//final File srcParent = new File(srcPath.getParent());
		return this;//prepareToUse(srcParent);
	}


	/*	public AlloyProcessingParam prepareToUse(final File destFolder) throws Exception{
		return this;		
	}*/

	@Override
	public int compareTo(AlloyProcessingParam o) {
		if( o == null) return -1;
		if( o.priority == this.priority ) return 0;
		if( o.priority < this.priority  ) return 1;
		return -1;
	}

	public String getSourceFileName(){
		return alloyCoder.srcName().replace(".als", "");
	}

	@Override
	public String toString() {
		return "AlloyProcessingParam [srcPath=" + srcPath() + ", destPath="
				+ destPath() + /*", content=" + content() + ", dependencies="
				+ dependencies() +*/ ", priority=" + priority + "]";
	}

	protected boolean isEqual(AlloyProcessingParam other){
		if (alloyCoder == null) {
			if (other.alloyCoder != null)
				return false;
		} else if (!alloyCoder.equals(other.alloyCoder))
			return false;
		if (priority != other.priority)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((alloyCoder == null) ? 0 : alloyCoder.hashCode());
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
