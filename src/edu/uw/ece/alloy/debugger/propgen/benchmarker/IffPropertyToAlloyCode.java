package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.util.List;

import edu.uw.ece.alloy.Compressor;

public class IffPropertyToAlloyCode extends PropertyToAlloyCode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3114204929896862280L;
	final public static IffPropertyToAlloyCode EMPTY_CONVERTOR = new IffPropertyToAlloyCode();

	protected IffPropertyToAlloyCode(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA, String predNameB,
			List<Dependency> dependencies, AlloyProcessingParam paramCreator,
			String header, String scope, String field) {
		super(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
				dependencies, paramCreator, header, scope, field);
	}

	protected IffPropertyToAlloyCode(String predBodyA, String predBodyB,
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

	protected IffPropertyToAlloyCode() {
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
			String predCallA, String predCallB, String predNameA, String predNameB,
			List<Dependency> dependencies, AlloyProcessingParam paramCreator,
			String header, String scope, String field) {
		return new IffPropertyToAlloyCode(predBodyA, predBodyB, predCallA,
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
		return new IffPropertyToAlloyCode(predBodyA, predBodyB, predCallA,
				predCallB, predNameA, predNameB, dependencies, paramCreator, header,
				scope, field, predBodyACompressed, predBodyBCompressed,
				predCallACompressed, predCallBCompressed, predNameACompressed,
				predNameBCompressed, headerComporessed, scopeCompressed,
				fieldCompressed, compressedDependencies, compressedStatus);
	}

	/**
	 * no counter-example should be found.
	 */
	public boolean isDesiredSAT(int sat) {
		return sat == -1;
	}

	@Override
	public String toString() {
		return "IffPropertyToAlloyCode [predBodyA=" + predBodyA + ", predBodyB="
				+ predBodyB + ", predCallA=" + predCallA + ", predCallB=" + predCallB
				+ ", predNameA=" + predNameA + ", predNameB=" + predNameB + ", header="
				+ header + ", scope=" + scope + ", field=" + field + ", dependencies="
				+ dependencies + ", getPredName()=" + getPredName() + "]";
	}

}
