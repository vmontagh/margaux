package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.util.List;

import edu.mit.csail.sdg.alloy4.Pair;

public class AndPropertyToAlloyCode extends PropertyToAlloyCode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 152443632901622400L;
	final public static AndPropertyToAlloyCode EMPTY_CONVERTOR = new AndPropertyToAlloyCode();
	
	protected AndPropertyToAlloyCode(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA,
			String predNameB, List<Pair<File, String>> dependencies,
			AlloyProcessingParam paramCreator, String header, String scope,
			File tmpDirectory) {
		super(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
				dependencies, paramCreator, header, scope, tmpDirectory);
	}

	protected AndPropertyToAlloyCode(String predBodyA, String predBodyB,
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
			File tmpDirectory) {
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
			String predNameB, List<Pair<File, String>> dependencies,
			AlloyProcessingParam paramCreator, String header, String scope,
			File tmpDirectory) {
		return new AndPropertyToAlloyCode( predBodyA,  predBodyB,
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

		return new AndPropertyToAlloyCode(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
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
