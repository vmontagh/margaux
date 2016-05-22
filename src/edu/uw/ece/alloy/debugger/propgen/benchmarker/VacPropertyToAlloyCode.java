package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.util.List;

public class VacPropertyToAlloyCode extends PropertyToAlloyCode {

	private static final long serialVersionUID = 4111622846959553154L;

	final public static VacPropertyToAlloyCode EMPTY_CONVERTOR = new VacPropertyToAlloyCode();

	protected VacPropertyToAlloyCode(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA, String predNameB,
			List<Dependency> dependencies, /*AlloyProcessingParam paramCreator,*/
			String header, String scope, String field) {
		super(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
				dependencies, /*paramCreator,*/ header, scope, field);
	}

/*	protected VacPropertyToAlloyCode(String predBodyA, String predBodyB,
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
	}*/

	protected VacPropertyToAlloyCode() {
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

	protected String generateAlloyCode() {
		String source = "";

		source += generatePrepend();
		source += '\n' + generatePredicateBody(predBodyA);
		source += '\n' + commandStatement(predCallA, predCallB);
		source += scope;

		return source;
	}

	@Override
	String commandStatement(final String predCallA, final String predCallB) {
		return commandKeyword() + "{ some r " + commandOperator() + " " + predCallA
				+ "}";
	}

	@Override
	public String srcNameOperator() {
		return "_VAC_";
	}

	@Override
	public boolean isSymmetric() {
		return false;
	}

	public String srcName() {
		return predNameA + srcNameOperator() + predNameA + ".als";
	}

	@Override
	public PropertyToAlloyCode createIt(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA, String predNameB,
			List<Dependency> dependencies, /*AlloyProcessingParam paramCreator,*/
			String header, String scope, String field) {
		return new VacPropertyToAlloyCode(predBodyA, predBodyB, predCallA,
				predCallB, predNameA, predNameB, dependencies, /*paramCreator,*/ header,
				scope// [tmpDirectory], tmpDirectory
				, field);
	}
/*
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
		return new VacPropertyToAlloyCode(predBodyA, predBodyB, predCallA,
				predCallB, predNameA, predNameB, dependencies, paramCreator, header,
				scope, field, predBodyACompressed, predBodyBCompressed,
				predCallACompressed, predCallBCompressed, predNameACompressed,
				predNameBCompressed, headerComporessed, scopeCompressed,
				fieldCompressed, compressedDependencies, compressedStatus);
	}*/

}
