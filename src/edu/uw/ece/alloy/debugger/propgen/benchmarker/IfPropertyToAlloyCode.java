package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.util.List;

import edu.mit.csail.sdg.alloy4.Pair;

public class IfPropertyToAlloyCode extends PropertyToAlloyCode {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4702673131807408629L;
	final public static IfPropertyToAlloyCode EMPTY_CONVERTOR = new IfPropertyToAlloyCode();	

	protected IfPropertyToAlloyCode(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA,
			String predNameB, List<Pair<File, String>> dependencies,
			AlloyProcessingParam paramCreator, String header, String scope,
			File tmpDirectory) {
		super(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
				dependencies, paramCreator, header, scope, tmpDirectory);
	}
	
	protected IfPropertyToAlloyCode(String predBodyA, String predBodyB,
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
			String predNameB, List<Pair<File, String>> dependencies,
			AlloyProcessingParam paramCreator, String header, String scope,
			File tmpDirectory) {
			return new IfPropertyToAlloyCode( predBodyA,  predBodyB,
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
		
		return new IfPropertyToAlloyCode(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
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
