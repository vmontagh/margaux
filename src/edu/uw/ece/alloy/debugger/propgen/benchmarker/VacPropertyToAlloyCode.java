package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.util.List;

import edu.mit.csail.sdg.alloy4.Pair;

public class VacPropertyToAlloyCode extends PropertyToAlloyCode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4111622846959553154L;
	
	final public static VacPropertyToAlloyCode EMPTY_CONVERTOR = new VacPropertyToAlloyCode();
	
	protected VacPropertyToAlloyCode(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA,
			String predNameB, List<Pair<File, String>> dependencies,
			AlloyProcessingParam paramCreator, String header, String scope,
			File tmpDirectory) {
		super(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
				dependencies, paramCreator, header, scope, tmpDirectory);
	}

	protected VacPropertyToAlloyCode(String predBodyA, String predBodyB,
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
		super(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
				dependencies, paramCreator, header, scope,
				predBodyACompressed, predBodyBCompressed,
				predCallACompressed, predCallBCompressed,
				predNameACompressed, predNameBCompressed,
				headerComporessed, scopeCompressed,
				tmpDirectoryCompressed,
				dependenciesFileNameCompressed, dependenciesContentCompressed,
				isCompressed, tmpDirectory);
	}
	

	
	protected VacPropertyToAlloyCode(){
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
	
	String generateAlloyCode(){
		String source = "";
		
		source += generatePrepend();
		source += '\n'+generatePredicateBody(predBodyA);		
		source += '\n'+commandStatement(predCallA, predCallB);
		source += scope;
		
		return source;
	}
	
	@Override
	String commandStatement(final String predCallA, final String predCallB){
		return commandKeyword() + "{ some r "  + commandOperator() +" "+ predCallA + "}";
	}
	
	@Override
	public String srcNameOperator() {
		return "_VAC_";
	}

	@Override
	public boolean isSymmetric() {
		return false;
	}

	public String srcName(){
		return predNameA + srcNameOperator() + predNameA + ".als";
	}
	
	
	@Override
	public PropertyToAlloyCode createIt(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA,
			String predNameB, List<Pair<File, String>> dependencies,
			AlloyProcessingParam paramCreator, String header, String scope, 
			File tmpDirectory) {
			return new VacPropertyToAlloyCode( predBodyA,  predBodyB,
					 predCallA,  predCallB,  predNameA,
					 predNameB,  dependencies,
					 paramCreator,  header,  scope, tmpDirectory);
	}

	@Override
	protected PropertyToAlloyCode createIt(String predBodyA,
			String predBodyB, String predCallA, String predCallB,
			String predNameA, String predNameB,
			List<Pair<File, String>> dependencies,
			AlloyProcessingParam paramCreator, String header, String scope,
			byte[] predBodyACompressed, byte[] predBodyBCompressed,
			byte[] predCallACompressed, byte[] predCallBCompressed,
			byte[] predNameACompressed, byte[] predNameBCompressed,
			byte[] headerComporessed, byte[] scopeCompressed,
			byte[] tmpDirectoryCompressed,
			byte[][] dependenciesFileNameCompressed,
			byte[][] dependenciesContentCompressed, boolean isCompressed,
			File tmpDirectory) {
		return new VacPropertyToAlloyCode(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
				dependencies, paramCreator, header, scope,
				predBodyACompressed, predBodyBCompressed,
				predCallACompressed, predCallBCompressed,
				predNameACompressed, predNameBCompressed,
				headerComporessed, scopeCompressed,
				tmpDirectoryCompressed,
				dependenciesFileNameCompressed, dependenciesContentCompressed,
				isCompressed, tmpDirectory);
	}
	
}
