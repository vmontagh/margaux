package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.util.List;

import edu.uw.ece.alloy.Compressor;

public class AndPropertyToAlloyCode extends PropertyToAlloyCode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 152443632901622400L;
	final public static AndPropertyToAlloyCode EMPTY_CONVERTOR = new AndPropertyToAlloyCode();
	
	protected AndPropertyToAlloyCode(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA,
			String predNameB, List<Dependency> dependencies,
			AlloyProcessingParam paramCreator, String header, String scope
			) {
		super(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
				dependencies, paramCreator, header, scope
				);
	}

	protected AndPropertyToAlloyCode(String predBodyA, String predBodyB,
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

	protected AndPropertyToAlloyCode(){
		super();
	}

	@Override
	String commandKeyword() {
		return "run";
	}

	@Override
	public String commandOperator() {
		return "and";
	}

	@Override
	public String srcNameOperator() {
		return "_AND_";
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
		return new AndPropertyToAlloyCode( predBodyA,  predBodyB,
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

		return new AndPropertyToAlloyCode(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
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
