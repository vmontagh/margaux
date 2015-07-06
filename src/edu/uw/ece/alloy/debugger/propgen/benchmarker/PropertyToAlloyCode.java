package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.util.Utils;

public class PropertyToAlloyCode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7891570520910464309L;
	final static String ENCODING = "UTF-8";
	final protected static byte[] EMPTY_1D = {};
	final protected static byte[][] EMPTY_2D = {{}};
	
	final public File tmpDirectory ;
	
	final byte[][] dependenciesFileNameCompressed, dependenciesContentCompressed;
	
	final public static String EMPTY_STRING = "N";
	final public static File EMPTY_FILE = new File(EMPTY_STRING);
	
	final public static PropertyToAlloyCode EMPTY_CONVERTOR = new PropertyToAlloyCode();

	final boolean isCompressed;
	final byte[] predBodyACompressed, predBodyBCompressed, 
	predCallACompressed, predCallBCompressed,
		predNameACompressed, predNameBCompressed,
			headerComporessed, scopeCompressed, tmpDirectoryCompressed;
	
	//Sources are generated here.
	
	//template names
	final static String Scope = "for 5";
	
	final public String predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB, header, scope;
	
	//pair.a is the file and pair.b is the content
	final List<Pair<File, String>> dependencies;
	
	final AlloyProcessingParam paramCreator;
	
	
	/*protected PropertyToAlloyCode(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA,
			String predNameB, List<Pair<File, String>> dependencies,
			AlloyProcessingParam paramCreator, String header, String scope,
			byte[] predBodyACompressed, byte[] predBodyBCompressed,
			byte[] predCallACompressed, byte[] predCallBCompressed,
			byte[] predNameACompressed, byte[] predNameBCompressed,
			byte[] headerComporessed, byte[] scopeCompressed,
			byte[] tmpDirectoryCompressed,
			byte[][] dependenciesFileNameCompressed,
			byte[][] dependenciesContentCompressed, boolean isCompressed
			) {
		this(predBodyA,  predBodyB,
				 predCallA,  predCallB,  predNameA,
				 predNameB,  dependencies,
				 paramCreator,  header,  scope,
				 predBodyACompressed,  predBodyBCompressed,
				 predCallACompressed,  predCallBCompressed,
				 predNameACompressed,  predNameBCompressed,
				 headerComporessed,  scopeCompressed,
				 tmpDirectoryCompressed,
				 dependenciesFileNameCompressed,
				 dependenciesContentCompressed,  isCompressed, new File( Configuration.getProp("temporary_directory") ));
	}*/
	
	
	protected PropertyToAlloyCode(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA,
			String predNameB, List<Pair<File, String>> dependencies,
			AlloyProcessingParam paramCreator, String header, String scope,
			byte[] predBodyACompressed, byte[] predBodyBCompressed,
			byte[] predCallACompressed, byte[] predCallBCompressed,
			byte[] predNameACompressed, byte[] predNameBCompressed,
			byte[] headerComporessed, byte[] scopeCompressed,
			byte[] tmpDirectoryCompressed,
			byte[][] dependenciesFileNameCompressed,
			byte[][] dependenciesContentCompressed, boolean isCompressed
			,File tmpDirectory) {
		super();
		this.predBodyA = predBodyA;
		this.predBodyB = predBodyB;
		this.predCallA = predCallA;
		this.predCallB = predCallB;
		this.predNameA = predNameA;
		this.predNameB = predNameB;
		this.paramCreator = paramCreator;
		
		this.dependencies = new LinkedList<Pair<File,String>>();
		dependencies.forEach(p->this.dependencies.add(new Pair<File, String>(p.a, p.b)));
		
		this.header = header;
		this.scope = scope;
		
		this.predBodyACompressed = Arrays.copyOf(predBodyACompressed, predBodyACompressed.length) ;
		this.predBodyBCompressed = Arrays.copyOf(predBodyBCompressed, predBodyBCompressed.length) ;
		this.predCallACompressed = Arrays.copyOf(predCallACompressed, predCallACompressed.length) ;
		this.predCallBCompressed = Arrays.copyOf(predCallBCompressed, predCallBCompressed.length) ;
		this.predNameACompressed = Arrays.copyOf(predNameACompressed, predNameACompressed.length) ;
		this.predNameBCompressed = Arrays.copyOf(predNameBCompressed, predNameBCompressed.length) ;
		this.headerComporessed = Arrays.copyOf(headerComporessed, headerComporessed.length) ;
		this.scopeCompressed = Arrays.copyOf(scopeCompressed, scopeCompressed.length) ;
		this.tmpDirectoryCompressed = Arrays.copyOf(tmpDirectoryCompressed, tmpDirectoryCompressed.length) ;
				
		this.dependenciesFileNameCompressed = new byte[dependenciesFileNameCompressed.length][];
		for(int i = 0; i < dependenciesFileNameCompressed.length; ++i){
			this.dependenciesFileNameCompressed[i] = Arrays.copyOf(dependenciesFileNameCompressed[i], dependenciesFileNameCompressed[i].length);
		}
		this.dependenciesContentCompressed = new byte[dependenciesContentCompressed.length][];
		for(int i = 0; i < dependenciesContentCompressed.length; ++i){
			this.dependenciesContentCompressed[i] = Arrays.copyOf(dependenciesContentCompressed[i], dependenciesContentCompressed[i].length);
		}
		
		this.isCompressed = isCompressed;
		
		this.tmpDirectory = tmpDirectory;
	}
	
	/*protected PropertyToAlloyCode(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA,
			String predNameB, List<Pair<File, String>> dependencies,
			AlloyProcessingParam paramCreator, String header, String scope) {
		this(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
				dependencies, paramCreator, header, scope, 
				EMPTY_1D,EMPTY_1D,EMPTY_1D,EMPTY_1D,
				EMPTY_1D,EMPTY_1D,EMPTY_1D,EMPTY_1D,
				EMPTY_1D,
				EMPTY_2D,EMPTY_2D, false);
	}*/
	
	protected PropertyToAlloyCode(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA,
			String predNameB, List<Pair<File, String>> dependencies,
			AlloyProcessingParam paramCreator, String header, String scope,
			File tmpDirectory) {
		this(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
				dependencies, paramCreator, header, scope, 
				EMPTY_1D,EMPTY_1D,EMPTY_1D,EMPTY_1D,
				EMPTY_1D,EMPTY_1D,EMPTY_1D,EMPTY_1D,
				EMPTY_1D,
				EMPTY_2D,EMPTY_2D, false,tmpDirectory);
	}
	
	
	public PropertyToAlloyCode() {
		this(EMPTY_STRING, EMPTY_STRING,EMPTY_STRING,EMPTY_STRING,EMPTY_STRING,EMPTY_STRING, Collections.emptyList() ,new AlloyProcessingParam(),EMPTY_STRING,EMPTY_STRING, EMPTY_FILE);
	}
	
	public PropertyToAlloyCode compress() throws Exception{
		
		if( isCompressed ) throw new RuntimeException("The object is already compressed.");
		
		if(predBodyA == null) throw new RuntimeException("The predBodyA is null and cannot be compressed.");
		if(predBodyB == null) throw new RuntimeException("The predBodyB is null and cannot be compressed.");
		if(predCallA == null) throw new RuntimeException("The predCallA is null and cannot be compressed.");
		if(predCallB == null) throw new RuntimeException("The predCallB is null and cannot be compressed.");
		if(predNameA == null) throw new RuntimeException("The predNameA is null and cannot be compressed.");
		if(predNameB == null) throw new RuntimeException("The predNameB is null and cannot be compressed.");
		if(header == null) throw new RuntimeException("The header is null and cannot be compressed.");
		if(scope == null) throw new RuntimeException("The scope is null and cannot be compressed.");

		if(dependencies == null) throw new RuntimeException("The dependencies is empty and cannot be compressed.");

		
		final byte[] predBodyACompressed  = Utils.compress(predBodyA, ENCODING);
		final byte[] predBodyBCompressed  = Utils.compress(predBodyB, ENCODING);
				
		final byte[] predCallACompressed  = Utils.compress(predCallA, ENCODING);
		final byte[] predCallBCompressed  = Utils.compress(predCallB, ENCODING);
		
		final byte[] predNameACompressed  = Utils.compress(predNameA, ENCODING);
		final byte[] predNameBCompressed  = Utils.compress(predNameB, ENCODING);
		
		final byte[] headerComporessed  = Utils.compress(header, ENCODING);
		final byte[] scopeCompressed  = Utils.compress(scope, ENCODING);
		final byte[] tmpDirectoryCompressed  = Utils.compress(tmpDirectory.getAbsolutePath(), ENCODING);
				
		final byte[][] dependenciesFileNameCompressed = new byte[dependencies.size()][];
		final byte[][] dependenciesContentCompressed  = new byte[dependencies.size()][];
		
		for(int i = 0; i < dependencies.size(); ++i){
			Pair<File, String> pair = dependencies.get(i);
			dependenciesFileNameCompressed[i] = Utils.compress(pair.a.getAbsolutePath(), ENCODING);
			dependenciesContentCompressed[i] = Utils.compress(pair.b, ENCODING);
		}
				
		return createIt(EMPTY_STRING, EMPTY_STRING,
						EMPTY_STRING, EMPTY_STRING,
						EMPTY_STRING, EMPTY_STRING,
						Collections.emptyList(), this.paramCreator, 
						EMPTY_STRING, EMPTY_STRING,
						predBodyACompressed, predBodyBCompressed,
						predCallACompressed, predCallBCompressed,
						predNameACompressed, predNameBCompressed,
						headerComporessed, scopeCompressed, tmpDirectoryCompressed,
						dependenciesFileNameCompressed, dependenciesContentCompressed, true, EMPTY_FILE);
	}
	
	public PropertyToAlloyCode decompress() throws Exception{
		
		if( !isCompressed ) throw new RuntimeException("The object is already uncompressed.");

		if(predBodyACompressed == null) throw new RuntimeException("The predBodyACompressed is null and cannot be compressed.");
		if(predBodyBCompressed == null) throw new RuntimeException("The predBodyBCompressed is null and cannot be compressed.");
		if(predCallACompressed == null) throw new RuntimeException("The predCallACompressed is null and cannot be compressed.");
		if(predCallBCompressed == null) throw new RuntimeException("The predCallBCompressed is null and cannot be compressed.");
		if(predNameACompressed == null) throw new RuntimeException("The predNameACompressed is null and cannot be compressed.");
		if(predNameBCompressed == null) throw new RuntimeException("The predNameBCompressed is null and cannot be compressed.");
		if(headerComporessed == null) throw new RuntimeException("The headerComporessed is null and cannot be compressed.");
		if(scopeCompressed == null) throw new RuntimeException("The scopeCompressed is null and cannot be compressed.");

		if(dependenciesFileNameCompressed == null) throw new RuntimeException("The dependenciesFileNameCompressed is empty and cannot be decompressed.");
		if(dependenciesContentCompressed == null) throw new RuntimeException("The dependenciesContentCompressed is empty and cannot be decompressed.");
		if(dependenciesContentCompressed.length != dependenciesFileNameCompressed.length) throw new RuntimeException("The dependenciesFileNameCompressed and dependenciesContentCompressed have different lengths.");
		
		final String predBodyA  = Utils.decompress(predBodyACompressed, ENCODING);
		final String predBodyB = Utils.decompress(predBodyBCompressed, ENCODING);
		final String predCallA  = Utils.decompress(predCallACompressed, ENCODING);
		final String predCallB = Utils.decompress(predCallBCompressed, ENCODING);
		final String predNameA  = Utils.decompress(predNameACompressed, ENCODING);
		final String predNameB = Utils.decompress(predNameBCompressed, ENCODING);
		final String header  = Utils.decompress(headerComporessed, ENCODING);
		final String scope = Utils.decompress(scopeCompressed, ENCODING);
		final File tmpDirectory = new File( Utils.decompress(tmpDirectoryCompressed, ENCODING));
		
		final List<Pair<File, String>> dependencies = new LinkedList<Pair<File,String>>();
		
		for(int i = 0; i < dependenciesFileNameCompressed.length; ++i){
			final File depFile = new File(Utils.decompress(dependenciesFileNameCompressed[i], ENCODING));
			final String depContent = Utils.decompress(dependenciesContentCompressed[i], ENCODING);
			final Pair<File, String> pair = new Pair<File, String>(depFile, depContent);
			dependencies.add(pair);
		}
		
		return this.createIt(predBodyA, predBodyB, 
						predCallA,predCallB,
						predNameA, predNameB,
						dependencies,this.paramCreator, 
						header, scope, 
						EMPTY_1D,EMPTY_1D,EMPTY_1D,EMPTY_1D,
						EMPTY_1D,EMPTY_1D,EMPTY_1D,EMPTY_1D,
						EMPTY_1D,
						EMPTY_2D,EMPTY_2D, false, tmpDirectory
						);
	}
	
	
	public  PropertyToAlloyCode createIt(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA,
			String predNameB, List<Pair<File, String>> dependencies,
			AlloyProcessingParam paramCreator, String header, String scope, 
			File tmpDirectory){
		throw new RuntimeException("Invalid call!");
	}
	
	protected PropertyToAlloyCode createIt(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA,
			String predNameB, List<Pair<File, String>> dependencies,
			AlloyProcessingParam paramCreator, String header, String scope,
			byte[] predBodyACompressed, byte[] predBodyBCompressed,
			byte[] predCallACompressed, byte[] predCallBCompressed,
			byte[] predNameACompressed, byte[] predNameBCompressed,
			byte[] headerComporessed, byte[] scopeCompressed,
			byte[] tmpDirectoryCompressed,
			byte[][] dependenciesFileNameCompressed,
			byte[][] dependenciesContentCompressed, boolean isCompressed,
			File tmpDirectory){
		throw new RuntimeException("Invalid call!");
	}
	
	
	public PropertyToAlloyCode createItself(){
		return createIt(this.predBodyA, this. predBodyB,
				this. predCallA, this. predCallB, this. predNameA,
				this. predNameB, this.dependencies,
				this. paramCreator, this. header, this. scope,
				this. predBodyACompressed, this. predBodyBCompressed,
				this. predCallACompressed, this. predCallBCompressed,
				this. predNameACompressed, this. predNameBCompressed,
				this. headerComporessed, this. scopeCompressed,
				this.tmpDirectoryCompressed,
				this.dependenciesFileNameCompressed, this. dependenciesContentCompressed,
				this. isCompressed, this.tmpDirectory );
	}
	
	String generateAlloyCode(){
		String source = "";
		
		source += generatePrepend();
		source += '\n'+generatePredicateBody(predBodyA);
		source += '\n'+generatePredicateBody(predBodyB);
		
		source += '\n'+commandStatement(predCallA, predCallB);
		
		source += scope;
		
		return source;
	}
	
	
	String generatePrepend(){

		return header;
	}
	
	String generatePredicateBody(final String preProcessedBody){
		//No process Now.
		return preProcessedBody;
	}
	
	String commandKeyword(){
		throw new RuntimeException("Invalid call!");
	}
	/*String commandKeyword(){
		return "check";
	}*/
	
	public String commandOperator(){
		throw new RuntimeException("Invalid call!");
	}
	/*String commandOperator(){
		return "implies";
	}*/
	
	String commandStatement(final String predCallA, final String predCallB){
		return commandKeyword() + "{" + predCallA + " "+ commandOperator() + " "+ predCallB + "}";
	}
	
	
	public File srcPath(){
		return  new File(tmpDirectory, srcName());
	}
	
	public File destPath(){
		return  new File(tmpDirectory, destName());
	}
	
	public AlloyProcessingParam generate() {
		
		int priority = 0;
		return paramCreator.createIt(this, priority); 
	}
	
	
	public String srcName(){
		return predNameA + srcNameOperator() + predNameB + ".als";
	}
	
	
	public String srcNameOperator(){
		throw new RuntimeException("Invalid call!");
	}
	
	public String destName(){
		return srcName()+".out.txt";
	}
	
	public boolean isSymmetric(){
		throw new RuntimeException("Invalid call!");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		
		if(dependencies != null){
			for(Pair<File, String> pair: dependencies){
				result = prime * result + (pair.a == null ? 0 :  pair.a.getAbsolutePath().hashCode());
				result = prime * result + (pair.b == null ? 0 :  pair.b.hashCode());				
			}
		}
		
		result = prime * result + ((header == null) ? 0 : header.hashCode());
		result = prime * result
				+ ((paramCreator == null) ? 0 : paramCreator.hashCode());
		result = prime * result
				+ ((predBodyA == null) ? 0 : predBodyA.hashCode());
		result = prime * result
				+ ((predBodyB == null) ? 0 : predBodyB.hashCode());
		result = prime * result
				+ ((predCallA == null) ? 0 : predCallA.hashCode());
		result = prime * result
				+ ((predCallB == null) ? 0 : predCallB.hashCode());
		result = prime * result
				+ ((predNameA == null) ? 0 : predNameA.hashCode());
		result = prime * result
				+ ((predNameB == null) ? 0 : predNameB.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		result = prime * result
				+ ((tmpDirectory == null) ? 0 : tmpDirectory.hashCode());
		result = prime * result
				+ Arrays.deepHashCode(dependenciesContentCompressed);
		result = prime * result
				+ Arrays.deepHashCode(dependenciesFileNameCompressed);
		result = prime * result + Arrays.hashCode(headerComporessed);
		result = prime * result + (isCompressed ? 1231 : 1237);
		result = prime * result + Arrays.hashCode(predBodyACompressed);
		result = prime * result + Arrays.hashCode(predBodyBCompressed);
		result = prime * result + Arrays.hashCode(predCallACompressed);
		result = prime * result + Arrays.hashCode(predCallBCompressed);
		result = prime * result + Arrays.hashCode(predNameACompressed);
		result = prime * result + Arrays.hashCode(predNameBCompressed);
		result = prime * result + Arrays.hashCode(scopeCompressed);
		result = prime * result + Arrays.hashCode(tmpDirectoryCompressed);
		return result;
	}

	protected boolean isEqual(PropertyToAlloyCode other){
		if (dependencies == null) {
			if (other.dependencies != null)
				return false;
		} else{
			if(dependencies.size() != other.dependencies.size())
				return false;
			for(int i = 0; i < dependencies.size(); ++i){
				final Pair<File, String> pair = dependencies.get(i);
				final Pair<File, String> otherPair = other.dependencies.get(i);
				if(!pair.a.getAbsolutePath().equals(otherPair.a.getAbsolutePath())) 
					return false;
				if(!pair.b.equals(otherPair.b)) 
					return false;
				
			}
		}
		if (header == null) {
			if (other.header != null)
				return false;
		} else if (!header.equals(other.header))
			return false;
		if (paramCreator == null) {
			if (other.paramCreator != null)
				return false;
		} else if (!paramCreator.equals(other.paramCreator))
			return false;
		if (predBodyA == null) {
			if (other.predBodyA != null)
				return false;
		} else if (!predBodyA.equals(other.predBodyA))
			return false;
		if (predBodyB == null) {
			if (other.predBodyB != null)
				return false;
		} else if (!predBodyB.equals(other.predBodyB))
			return false;
		if (predCallA == null) {
			if (other.predCallA != null)
				return false;
		} else if (!predCallA.equals(other.predCallA))
			return false;
		if (predCallB == null) {
			if (other.predCallB != null)
				return false;
		} else if (!predCallB.equals(other.predCallB))
			return false;
		if (predNameA == null) {
			if (other.predNameA != null)
				return false;
		} else if (!predNameA.equals(other.predNameA))
			return false;
		if (predNameB == null) {
			if (other.predNameB != null)
				return false;
		} else if (!predNameB.equals(other.predNameB))
			return false;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		if (tmpDirectory == null) {
			if (other.tmpDirectory != null)
				return false;
		} else if (!tmpDirectory.equals(other.tmpDirectory))
			return false;
		if (!Arrays.deepEquals(dependenciesContentCompressed,
				other.dependenciesContentCompressed))
			return false;
		if (!Arrays.deepEquals(dependenciesFileNameCompressed,
				other.dependenciesFileNameCompressed))
			return false;
		if (!Arrays.equals(headerComporessed, other.headerComporessed))
			return false;
		if (isCompressed != other.isCompressed)
			return false;
		if (!Arrays.equals(predBodyACompressed, other.predBodyACompressed))
			return false;
		if (!Arrays.equals(predBodyBCompressed, other.predBodyBCompressed))
			return false;
		if (!Arrays.equals(predCallACompressed, other.predCallACompressed))
			return false;
		if (!Arrays.equals(predCallBCompressed, other.predCallBCompressed))
			return false;
		if (!Arrays.equals(predNameACompressed, other.predNameACompressed))
			return false;
		if (!Arrays.equals(predNameBCompressed, other.predNameBCompressed))
			return false;
		if (!Arrays.equals(scopeCompressed, other.scopeCompressed))
			return false;
		if (!Arrays.equals(tmpDirectoryCompressed, other.tmpDirectoryCompressed))
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
		PropertyToAlloyCode other = (PropertyToAlloyCode) obj;
		return isEqual(other);
	}
	
	
	
	
}
