package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

import edu.uw.ece.alloy.Compressor;

/**
 * It is very much like LazyFile but compresses the content. 
 * @author vajih
 *
 */
public class Dependency implements Serializable {

	private static final long serialVersionUID = 6911183169029150968L;
	final public File path;
	final public String content;

	final protected byte[] pathCompressed;
	final protected byte[] contentCompressed;

	final public Compressor.STATE compressedStatus;

	final public static Dependency EMPTY_DEPENDENCY = new Dependency();

	protected Dependency(
			final File path, final String content,
			final byte[] pathCompressed, final byte[] contentCompressed, 
			final Compressor.STATE compressedStatus) {
		this.path = new File(path.getPath());
		this.content = content;
		this.pathCompressed = Arrays.copyOf(pathCompressed, pathCompressed.length) ;
		this.contentCompressed = Arrays.copyOf(contentCompressed, contentCompressed.length) ;
		this.compressedStatus = compressedStatus;
	}

	protected Dependency(
			final File path, final String content) {
		this(path, content, Compressor.EMPTY_1D, Compressor.EMPTY_1D, Compressor.STATE.DEOMPRESSED);
	}

	protected Dependency() {
		this(Compressor.EMPTY_FILE,Compressor.EMPTY_STRING,  Compressor.EMPTY_1D,Compressor.EMPTY_1D, Compressor.STATE.DEOMPRESSED);
	}

	protected Dependency createIt(final File path, final String content,
			final byte[] pathCompressed, final byte[] contentCompressed, 
			final Compressor.STATE compressedStatus) {
		return new Dependency(path, content, pathCompressed, contentCompressed, compressedStatus);
	}


	public Dependency createIt(final File path, final String content) {
		return new Dependency(path, content);
	}

	public Dependency createItsef() {
		return this.createIt(this.path, this.content, this.pathCompressed, this.contentCompressed, this.compressedStatus );
	}
	
	public Dependency compress() throws Exception{
		if( compressedStatus.equals(Compressor.STATE.COMPRESSED) ) throw new RuntimeException("The object is already compressed.");
		
		if(path == null) throw new RuntimeException("The path is null and cannot be compressed.");
		if(content == null) throw new RuntimeException("The content is null and cannot be compressed.");
		
		final byte[] pathCompressed = Compressor.compress(path.getPath());
		final byte[] contentCompressed = Compressor.compress(content);
		
		return createIt(Compressor.EMPTY_FILE, Compressor.EMPTY_STRING, pathCompressed, contentCompressed, Compressor.STATE.COMPRESSED);
	}

	public Dependency deCompress() throws Exception{
		
		if( compressedStatus.equals(Compressor.STATE.DEOMPRESSED) ) throw new RuntimeException("The object is not compressed.");
		
		if(pathCompressed == null) throw new RuntimeException("The pathCompressed is null and cannot be decompressed.");
		if(contentCompressed == null) throw new RuntimeException("The contentCompressed is null and cannot be decompressed.");
		
		final File path  = new File(Compressor.decompress(pathCompressed) );
		final String content = Compressor.decompress(contentCompressed);
		
		
		return createIt(path, content, Compressor.EMPTY_1D, Compressor.EMPTY_1D, Compressor.STATE.DEOMPRESSED);
	}
	
	

	@Override
	public String toString() {
		return "Dependency[path=" + path +"compressed?"
				+ compressedStatus + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + Arrays.hashCode(contentCompressed);
		result = prime * result + compressedStatus.hashCode();
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + Arrays.hashCode(pathCompressed);
		return result;
	}
	
	protected boolean isEqual(Dependency other){
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (!Arrays.equals(contentCompressed, other.contentCompressed))
			return false;
		if (!compressedStatus.equals(other.compressedStatus ))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (!Arrays.equals(pathCompressed, other.pathCompressed))
			return false;
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dependency other = (Dependency) obj;
		return isEqual(other);
	}
	
	

}
