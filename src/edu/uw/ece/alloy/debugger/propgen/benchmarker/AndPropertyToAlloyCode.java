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
			String predCallA, String predCallB, String predNameA, String predNameB,
			List<Dependency> dependencies, AlloyProcessingParam paramCreator,
			String header, String scope, String field) {
		super(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
				dependencies, paramCreator, header, scope, field);
	}

	protected AndPropertyToAlloyCode(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA, String predNameB,
			List<Dependency> dependencies, AlloyProcessingParam paramCreator,
			String header, String scope, String field, byte[] predBodyACompressed,
			byte[] predBodyBCompressed, byte[] predCallACompressed,
			byte[] predCallBCompressed, byte[] predNameACompressed,
			byte[] predNameBCompressed, byte[] headerComporessed,
			byte[] scopeCompressed, byte[] fieldCompressed,
			List<Dependency> codeDependencies, Compressor.STATE compressedStatus) {
		super(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
				dependencies, paramCreator, header, scope, field, predBodyACompressed,
				predBodyBCompressed, predCallACompressed, predCallBCompressed,
				predNameACompressed, predNameBCompressed, headerComporessed,
				scopeCompressed, fieldCompressed, codeDependencies, compressedStatus);
	}

	protected AndPropertyToAlloyCode() {
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
			String predCallA, String predCallB, String predNameA, String predNameB,
			List<Dependency> dependencies, AlloyProcessingParam paramCreator,
			String header, String scope, String field) {
		return new AndPropertyToAlloyCode(predBodyA, predBodyB, predCallA,
				predCallB, predNameA, predNameB, dependencies, paramCreator, header,
				scope, field);
	}

	@Override
	protected PropertyToAlloyCode createIt(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA, String predNameB,
			List<Dependency> dependencies, AlloyProcessingParam paramCreator,
			String header, String scope, String field, byte[] predBodyACompressed,
			byte[] predBodyBCompressed, byte[] predCallACompressed,
			byte[] predCallBCompressed, byte[] predNameACompressed,
			byte[] predNameBCompressed, byte[] headerComporessed,
			byte[] scopeCompressed, byte[] fieldCompressed,
			List<Dependency> compressedDependencies,
			Compressor.STATE compressedStatus) {

		return new AndPropertyToAlloyCode(predBodyA, predBodyB, predCallA,
				predCallB, predNameA, predNameB, dependencies, paramCreator, header,
				scope, field, predBodyACompressed, predBodyBCompressed,
				predCallACompressed, predCallBCompressed, predNameACompressed,
				predNameBCompressed, headerComporessed, scopeCompressed,
				fieldCompressed, compressedDependencies, compressedStatus);
	}
	
	/**
	 * an example should be found.
	 */
	public boolean isDesiredSAT(int sat){
		return sat == 1;
	}

}