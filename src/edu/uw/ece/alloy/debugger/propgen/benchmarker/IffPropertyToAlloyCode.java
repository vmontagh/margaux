package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.util.List;

import edu.mit.csail.sdg.alloy4.Pair;
import edu.uw.ece.alloy.Compressor;

public class IffPropertyToAlloyCode extends PropertyToAlloyCode {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3114204929896862280L;
	final public static IffPropertyToAlloyCode EMPTY_CONVERTOR = new IffPropertyToAlloyCode();

	protected IffPropertyToAlloyCode(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA,
			String predNameB, List<Dependency> dependencies,
			AlloyProcessingParam paramCreator, String header, String scope
			) {
		super(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
				dependencies, paramCreator, header, scope
				);
	}

	protected IffPropertyToAlloyCode(String predBodyA, String predBodyB,
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
		super(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
				dependencies, paramCreator, header, scope,
				predBodyACompressed, predBodyBCompressed,
				predCallACompressed, predCallBCompressed,
				predNameACompressed, predNameBCompressed,
				headerComporessed, scopeCompressed,
				codeDependencies,
				compressedStatus
				);
	}
	
	protected IffPropertyToAlloyCode(){
		super();
	}

	@Override
	String commandKeyword() {
		return "check";
	}

	@Override
	public String commandOperator() {
		return "<=>";
	}

	@Override
	public String srcNameOperator() {
		return "_IFF_";
	}

	@Override
	public boolean isSymmetric() {
		return true;
	}

	@Override
	public PropertyToAlloyCode createIt(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA,
			String predNameB, List<Dependency> dependencies,
			AlloyProcessingParam paramCreator, String header, String scope
			) {
		return new IffPropertyToAlloyCode( predBodyA,  predBodyB,
				predCallA,  predCallB,  predNameA,
				predNameB,  dependencies,
				paramCreator,  header,  scope
				);
	}

	@Override
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
			) {
		return new IffPropertyToAlloyCode(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
				dependencies, paramCreator, header, scope,
				predBodyACompressed, predBodyBCompressed,
				predCallACompressed, predCallBCompressed,
				predNameACompressed, predNameBCompressed,
				headerComporessed, scopeCompressed,
				compressedDependencies,
				compressedStatus
				);
	}

}
