package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.io.Serializable;

public class AlloyProcessingParam implements Comparable<AlloyProcessingParam>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2055342494359782938L;
	
	public final File srcPath;
	public final File destPath;
	
	public final String content;
	
	public final int priority;

	public AlloyProcessingParam(File srcPath, File destPath, int priority) {
		this(srcPath, destPath, priority, "");
	}
	
	public AlloyProcessingParam(File srcPath, File destPath, int priority, String content) {
		super();
		this.srcPath = new File(srcPath.getAbsolutePath());
		this.destPath = new File(destPath.getAbsolutePath());
		this.priority = priority;
		this.content = new String(content);
	}

	
	public AlloyProcessingParam(AlloyProcessingParam params) {

		this( new File( params.srcPath.getAbsolutePath()), 
				new File (params.destPath.getAbsolutePath() ), 
					params.priority, 
						new String(params.content));
		
	}

	
	public File saveOnDisk(final File destFolder){
		//TODO save the content on a file in destFolder and return its path.
		return null;
	}
	
	@Override
	public int compareTo(AlloyProcessingParam o) {
		if( o == null) return -1;
		if( o.priority == this.priority ) return 0;
		if( o.priority < this.priority  ) return 1;
		return -1;
	}
	
	public String getSourceFileName(){
		return srcPath.getName().replace(".als", "");
	}

	@Override
	public String toString() {
		return "AlloyProcessingParam [srcPath=" + srcPath.getName() + ", destPath="
				+ destPath.getName() + ", content=" + content + ", priority=" + priority
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((srcPath == null) ? 0 : srcPath.hashCode());
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
		if (srcPath == null) {
			if (other.srcPath != null)
				return false;
		} else if (!srcPath.equals(other.srcPath))
			return false;
		return true;
	}

	
	
}
