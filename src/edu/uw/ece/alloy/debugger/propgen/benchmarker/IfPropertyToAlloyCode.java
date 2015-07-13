package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.util.List;

import edu.mit.csail.sdg.alloy4.Pair;
import edu.uw.ece.alloy.Compressor;

public class IfPropertyToAlloyCode extends PropertyToAlloyCode {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4702673131807408629L;
	final public static IfPropertyToAlloyCode EMPTY_CONVERTOR = new IfPropertyToAlloyCode();	

	protected IfPropertyToAlloyCode(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA,
			String predNameB, List<Dependency> dependencies,
			AlloyProcessingParam paramCreator, String header, String scope
			) {
		super(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
				dependencies, paramCreator, header, scope
				);
	}
	
	protected IfPropertyToAlloyCode(String predBodyA, String predBodyB,
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

	
	protected IfPropertyToAlloyCode(){
		super();
	}

	
	@Override
	String commandKeyword() {
		return "check";
	}

	@Override
	public String commandOperator() {
		return "implies";
	}

	@Override
	public String srcNameOperator() {
		return "_IMPLY_";
	}

	@Override
	public boolean isSymmetric() {
		return false;
	}

	@Override
	public PropertyToAlloyCode createIt(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA,
			String predNameB, List<Dependency> dependencies,
			AlloyProcessingParam paramCreator, String header, String scope
			) {
			return new IfPropertyToAlloyCode( predBodyA,  predBodyB,
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
		
		return new IfPropertyToAlloyCode(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
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
