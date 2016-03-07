package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.uw.ece.alloy.Compressor;
import edu.uw.ece.alloy.debugger.knowledgebase.BinaryImplicationLattic;
import edu.uw.ece.alloy.debugger.knowledgebase.ImplicationLattic;

public class PropertyToAlloyCode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7891570520910464309L;
	
	final public static PropertyToAlloyCode EMPTY_CONVERTOR = new PropertyToAlloyCode();
	final public static String COMMAND_BLOCK_NAME = "check_or_run_assert_or_preicate_name"; 

	final public Compressor.STATE compressedStatus;
	
	final byte[] predBodyACompressed, predBodyBCompressed, 
	predCallACompressed, predCallBCompressed,
		predNameACompressed, predNameBCompressed,
			headerComporessed, scopeCompressed
			;
	
	//Sources are generated here.
	
	//template names
	final static String Scope = "for 5";
	
	final public String predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB, header, scope;
	
	//pair.a is the file and pair.b is the content
	final List<Dependency> dependencies, compressedDependencies;
	
	final AlloyProcessingParam paramCreator;
	
	final transient List<ImplicationLattic> implications;

	protected PropertyToAlloyCode(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA,
			String predNameB, List<Dependency> dependencies,
			AlloyProcessingParam paramCreator, String header, String scope,
			byte[] predBodyACompressed, byte[] predBodyBCompressed,
			byte[] predCallACompressed, byte[] predCallBCompressed,
			byte[] predNameACompressed, byte[] predNameBCompressed,
			byte[] headerComporessed, byte[] scopeCompressed,
			List<Dependency> codeDependencies,
			Compressor.STATE compressedStatus
			) {
		super();
		this.predBodyA = predBodyA;
		this.predBodyB = predBodyB;
		this.predCallA = predCallA;
		this.predCallB = predCallB;
		this.predNameA = predNameA;
		this.predNameB = predNameB;
		this.paramCreator = paramCreator;
		
		this.dependencies = new LinkedList<Dependency>();
		dependencies.forEach(p->this.dependencies.add(p.createItsef()));
		
		this.compressedDependencies = new LinkedList<Dependency>();
		codeDependencies.forEach(p->this.compressedDependencies.add(p.createItsef()));
		
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
		
		this.compressedStatus = compressedStatus;
		
		implications = new LinkedList<>();
		implications.add(new BinaryImplicationLattic());
		
	}
	
	protected PropertyToAlloyCode(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA,
			String predNameB, List<Dependency> dependencies,
			AlloyProcessingParam paramCreator, String header, String scope
			) {
		this(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
				dependencies, paramCreator, header, scope, 
				Compressor.EMPTY_1D,Compressor.EMPTY_1D,Compressor.EMPTY_1D,Compressor.EMPTY_1D,
				Compressor.EMPTY_1D,Compressor.EMPTY_1D,Compressor.EMPTY_1D,Compressor.EMPTY_1D,
				Compressor.EMPTY_LIST,
				Compressor.STATE.DEOMPRESSED
				);
	}
	
	
	public PropertyToAlloyCode() {
		this(Compressor.EMPTY_STRING, Compressor.EMPTY_STRING,
				Compressor.EMPTY_STRING,Compressor.EMPTY_STRING,
				Compressor.EMPTY_STRING,Compressor.EMPTY_STRING, 
				Compressor.EMPTY_LIST ,AlloyProcessingParam.EMPTY_PARAM,
				Compressor.EMPTY_STRING,Compressor.EMPTY_STRING
				);
	}
	
	public PropertyToAlloyCode compress() throws Exception{
		
		if( compressedStatus.equals(Compressor.STATE.COMPRESSED) ) throw new RuntimeException("The object is already compressed.");
		
		if(predBodyA == null) throw new RuntimeException("The predBodyA is null and cannot be compressed.");
		if(predBodyB == null) throw new RuntimeException("The predBodyB is null and cannot be compressed.");
		if(predCallA == null) throw new RuntimeException("The predCallA is null and cannot be compressed.");
		if(predCallB == null) throw new RuntimeException("The predCallB is null and cannot be compressed.");
		if(predNameA == null) throw new RuntimeException("The predNameA is null and cannot be compressed.");
		if(predNameB == null) throw new RuntimeException("The predNameB is null and cannot be compressed.");
		if(header == null) throw new RuntimeException("The header is null and cannot be compressed.");
		if(scope == null) throw new RuntimeException("The scope is null and cannot be compressed.");

		if(dependencies == null) throw new RuntimeException("The dependencies is empty and cannot be compressed.");

		
		final byte[] predBodyACompressed  = Compressor.compress(predBodyA);
		final byte[] predBodyBCompressed  = Compressor.compress(predBodyB);
				
		final byte[] predCallACompressed  = Compressor.compress(predCallA);
		final byte[] predCallBCompressed  = Compressor.compress(predCallB);
		
		final byte[] predNameACompressed  = Compressor.compress(predNameA);
		final byte[] predNameBCompressed  = Compressor.compress(predNameB);
		
		final byte[] headerComporessed  = Compressor.compress(header);
		final byte[] scopeCompressed  = Compressor.compress(scope);
		
		final List<Dependency> compressedDependencies = new LinkedList<Dependency>();
		for(Dependency dependency: this.dependencies){
			compressedDependencies.add(dependency.compress());
		}
		
		return createIt(Compressor.EMPTY_STRING, Compressor.EMPTY_STRING,
				Compressor.EMPTY_STRING, Compressor.EMPTY_STRING,
				Compressor.EMPTY_STRING, Compressor.EMPTY_STRING,
				Compressor.EMPTY_LIST, this.paramCreator, 
						Compressor.EMPTY_STRING, Compressor.EMPTY_STRING,
						predBodyACompressed, predBodyBCompressed,
						predCallACompressed, predCallBCompressed,
						predNameACompressed, predNameBCompressed,
						headerComporessed, scopeCompressed, 
						compressedDependencies,
						Compressor.STATE.COMPRESSED
						);
	}
	
	public PropertyToAlloyCode deCompress() throws Exception{
		
		if( compressedStatus.equals(Compressor.STATE.DEOMPRESSED) ) throw new RuntimeException("The object is not compressed.");

		if(predBodyACompressed == null) throw new RuntimeException("The predBodyACompressed is null and cannot be compressed.");
		if(predBodyBCompressed == null) throw new RuntimeException("The predBodyBCompressed is null and cannot be compressed.");
		if(predCallACompressed == null) throw new RuntimeException("The predCallACompressed is null and cannot be compressed.");
		if(predCallBCompressed == null) throw new RuntimeException("The predCallBCompressed is null and cannot be compressed.");
		if(predNameACompressed == null) throw new RuntimeException("The predNameACompressed is null and cannot be compressed.");
		if(predNameBCompressed == null) throw new RuntimeException("The predNameBCompressed is null and cannot be compressed.");
		if(headerComporessed == null) throw new RuntimeException("The headerComporessed is null and cannot be compressed.");
		if(scopeCompressed == null) throw new RuntimeException("The scopeCompressed is null and cannot be compressed.");
		if(compressedDependencies == null) throw new RuntimeException("The compressedDependencies is null and cannot be compressed.");

		final String predBodyA  = Compressor.decompress(predBodyACompressed);
		final String predBodyB = Compressor.decompress(predBodyBCompressed);
		final String predCallA  = Compressor.decompress(predCallACompressed);
		final String predCallB = Compressor.decompress(predCallBCompressed);
		final String predNameA  = Compressor.decompress(predNameACompressed);
		final String predNameB = Compressor.decompress(predNameBCompressed);
		final String header  = Compressor.decompress(headerComporessed);
		final String scope = Compressor.decompress(scopeCompressed);
		
		final List<Dependency> dependencies = new LinkedList<Dependency>();
		for(Dependency dependency: this.compressedDependencies){
			dependencies.add(dependency.deCompress());
		}
		
		return this.createIt(predBodyA, predBodyB, 
						predCallA,predCallB,
						predNameA, predNameB,
						dependencies,this.paramCreator, 
						header, scope, 
						Compressor.EMPTY_1D,Compressor.EMPTY_1D,Compressor.EMPTY_1D,Compressor.EMPTY_1D,
						Compressor.EMPTY_1D,Compressor.EMPTY_1D,Compressor.EMPTY_1D,Compressor.EMPTY_1D,
						Compressor.EMPTY_LIST,
						Compressor.STATE.DEOMPRESSED
						);
	}
	
	
	public  PropertyToAlloyCode createIt(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA,
			String predNameB, List<Dependency> dependencies,
			AlloyProcessingParam paramCreator, String header, String scope 
			){
		throw new RuntimeException("Invalid call!");
	}
	
	protected PropertyToAlloyCode createIt(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA,
			String predNameB, List<Dependency> dependencies,
			AlloyProcessingParam paramCreator, String header, String scope,
			byte[] predBodyACompressed, byte[] predBodyBCompressed,
			byte[] predCallACompressed, byte[] predCallBCompressed,
			byte[] predNameACompressed, byte[] predNameBCompressed,
			byte[] headerComporessed, byte[] scopeCompressed
			,List<Dependency> compressedDependencies
			, Compressor.STATE compressedStatus
			){
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
				this.compressedDependencies,
				compressedStatus 
				);
	}
	
	protected String generateAlloyCode(){
		String source = "";
		
		source += generatePrepend();
		source += '\n'+generatePredicateBody(predBodyA);
		source += '\n'+generatePredicateBody(predBodyB);
		source += '\n'+commandStatement(predCallA, predCallB);
		source += scope;
		
		return source;
	}
	
	
	protected String generatePrepend(){

		return header;
	}
	
	protected String generatePredicateBody(final String preProcessedBody){
		//No process Now.
		return preProcessedBody;
	}
	
	String commandKeyword(){
		throw new RuntimeException("Invalid call!");
	}
	
	String commandKeyWordBody(){
		// It could be 'assert' or 'pred'
		throw new RuntimeException("Invalid call!");
	}
	
	public String commandOperator(){
		throw new RuntimeException("Invalid call!");
	}
	
	String commandStatement(final String predCallA, final String predCallB){
		
		final String block = commandKeyWordBody() + " " + 
												 COMMAND_BLOCK_NAME + " {\n" + 
												 predCallA + " "+ commandOperator() + 
												 " "+ predCallB + "\n}\n";
		
		return block + commandKeyword() + " " + COMMAND_BLOCK_NAME;
	}
	
	
	public String srcPath(){
		return   
				srcName();
	}
	
	public String destPath(){
		return  
				destName();
	}
	
	public AlloyProcessingParam generate() {
		
		int priority = 0;
		return paramCreator.createIt(this, priority, paramCreator.tmpDirectory); 
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
			for(Dependency dependency: dependencies){
				result = prime * result +((dependency == null) ? 0 : dependency.hashCode());
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
		
		if(compressedDependencies != null){
			for(Dependency dependency: compressedDependencies){
				result = prime * result +((dependency == null) ? 0 : dependency.hashCode());
			}
		}
		
		result = prime * result + Arrays.hashCode(headerComporessed);
		result = prime * result + compressedStatus.hashCode();
		result = prime * result + Arrays.hashCode(predBodyACompressed);
		result = prime * result + Arrays.hashCode(predBodyBCompressed);
		result = prime * result + Arrays.hashCode(predCallACompressed);
		result = prime * result + Arrays.hashCode(predCallBCompressed);
		result = prime * result + Arrays.hashCode(predNameACompressed);
		result = prime * result + Arrays.hashCode(predNameBCompressed);
		result = prime * result + Arrays.hashCode(scopeCompressed);
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
				if(!dependencies.get(i).equals(other.dependencies.get(i)))
					return false;
			}
		}
		
		if (compressedDependencies == null) {
			if (other.compressedDependencies != null)
				return false;
		} else{
			if(compressedDependencies.size() != other.compressedDependencies.size())
				return false;
			for(int i = 0; i < compressedDependencies.size(); ++i){
				if(!compressedDependencies.get(i).equals(other.compressedDependencies.get(i)))
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

		if (!Arrays.equals(headerComporessed, other.headerComporessed))
			return false;
		if (!compressedStatus.equals(other.compressedStatus) )
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
	
	
	/**
	 * After checking a=>b,
	 * if a=>b is true, means the check is unSAT (No Counter-example):
	 * 	if a=E and b=Prop then allImpliedProperties Of b also has to be returned
	 *  if a=prop and b=E then allRevImpliedProperties of a has to returned.
	 *  The return type is false. Means stop any furtherAnaylsis and take the result as
	 *  the inferred propertied
	 */
	public List<String> getInferedProperties(boolean sat){
		throw new RuntimeException("Invalid call!");
	}
	
	public List<PropertyToAlloyCode> getInferedPropertiesCoder(boolean sat){
		throw new RuntimeException("Invalid call!");
	}
	
	/**
	 *  if sat, there is a counterexample
	 * 		if a=E and b=Prop then next properties implied from Prop has to be evaluated
	 *  	if a=Prop and b=E then next properties that implying Prop has to be evaluated
	 * @param sat
	 * @return
	 */
	public List<String> getToBeCheckedProperties(boolean sat){
		throw new RuntimeException("Invalid call!");
	}
	
	public List<String> getInitialProperties(){
		throw new RuntimeException("Invalid call!");
	}
	
	/**
	 * return a predicated name: predA operator predB
	 * This function is used to see whether a check is done
	 * regardless it inferred or ran.
	 * @return
	 */
	public String getPredName(){
		return predCallA.substring(0, predCallA.indexOf("___")) + commandOperator() + predCallB.substring(0, predCallA.indexOf("___"));
	}
}
