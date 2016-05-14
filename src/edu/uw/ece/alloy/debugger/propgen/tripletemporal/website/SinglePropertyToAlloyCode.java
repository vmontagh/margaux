package edu.uw.ece.alloy.debugger.propgen.tripletemporal.website;

import java.util.List;

import edu.uw.ece.alloy.Compressor;
import edu.uw.ece.alloy.Compressor.STATE;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.Dependency;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.PropertyToAlloyCode;

public class SinglePropertyToAlloyCode extends PropertyToAlloyCode {

	private static final long serialVersionUID = 1L;
	final public static SinglePropertyToAlloyCode EMPTY_CONVERTOR = new SinglePropertyToAlloyCode();

	/*public SinglePropertyToAlloyCode(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA, String predNameB,
			List<Dependency> dependencies, AlloyProcessingParam paramCreator,
			String header, String scope, String field, byte[] predBodyACompressed,
			byte[] predBodyBCompressed, byte[] predCallACompressed,
			byte[] predCallBCompressed, byte[] predNameACompressed,
			byte[] predNameBCompressed, byte[] headerComporessed,
			byte[] scopeCompressed, byte[] fieldCompressed,
			List<Dependency> codeDependencies, STATE compressedStatus) {
		super(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
				dependencies, paramCreator, header, scope, field, predBodyACompressed,
				predBodyBCompressed, predCallACompressed, predCallBCompressed,
				predNameACompressed, predNameBCompressed, headerComporessed,
				scopeCompressed, fieldCompressed, codeDependencies, compressedStatus);
		// TODO Auto-generated constructor stub
	}*/

	public SinglePropertyToAlloyCode(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA, String predNameB,
			List<Dependency> dependencies, /*AlloyProcessingParam paramCreator,*/
			String header, String scope, String field) {
		super(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
				dependencies, /*paramCreator,*/ header, scope, field);
		// TODO Auto-generated constructor stub
	}

	public SinglePropertyToAlloyCode() {
		super();
	}

	protected String generateAlloyCode() {
		String source = "";

		source += generatePrepend();
		source += '\n' + generatePredicateBody(predBodyA);

		return source;
	}

	public String srcName() {
		return predNameA + ".als";
	}

	@Override
	public PropertyToAlloyCode createIt(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA, String predNameB,
			List<Dependency> dependencies, /*AlloyProcessingParam paramCreator,*/
			String header, String scope, String field) {
		return new SinglePropertyToAlloyCode(predBodyA, predBodyB, predCallA,
				predCallB, predNameA, predNameB, dependencies, /*paramCreator,*/ header,
				scope, field// [tmpDirectory], tmpDirectory
		);
	}

/*	@Override
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
		return new SinglePropertyToAlloyCode(predBodyA, predBodyB, predCallA,
				predCallB, predNameA, predNameB, dependencies, paramCreator, header,
				scope, field, predBodyACompressed, predBodyBCompressed,
				predCallACompressed, predCallBCompressed, predNameACompressed,
				predNameBCompressed, headerComporessed, scopeCompressed,
				fieldCompressed, compressedDependencies, compressedStatus);
	}*/

}
