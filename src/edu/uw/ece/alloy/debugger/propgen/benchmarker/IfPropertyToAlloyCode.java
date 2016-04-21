package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.uw.ece.alloy.Compressor;
import edu.uw.ece.alloy.debugger.knowledgebase.ImplicationLattic;

public class IfPropertyToAlloyCode extends PropertyToAlloyCode {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4702673131807408629L;
	final public static IfPropertyToAlloyCode EMPTY_CONVERTOR = new IfPropertyToAlloyCode();
	final static Logger logger = Logger
			.getLogger(IfPropertyToAlloyCode.class.getName() + "--"
					+ Thread.currentThread().getName());

	protected IfPropertyToAlloyCode(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA, String predNameB,
			List<Dependency> dependencies, AlloyProcessingParam paramCreator,
			String header, String scope, String field) {
		super(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
				dependencies, paramCreator, header, scope, field);
	}

	protected IfPropertyToAlloyCode(String predBodyA, String predBodyB,
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

	protected IfPropertyToAlloyCode() {
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
	String commandKeyWordBody() {
		return "assert";
	}

	@Override
	public boolean isSymmetric() {
		return false;
	}

	@Override
	public PropertyToAlloyCode createIt(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA, String predNameB,
			List<Dependency> dependencies, AlloyProcessingParam paramCreator,
			String header, String scope, String field) {
		return new IfPropertyToAlloyCode(predBodyA, predBodyB, predCallA, predCallB,
				predNameA, predNameB, dependencies, paramCreator, header, scope, field);
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

		return new IfPropertyToAlloyCode(predBodyA, predBodyB, predCallA, predCallB,
				predNameA, predNameB, dependencies, paramCreator, header, scope, field,
				predBodyACompressed, predBodyBCompressed, predCallACompressed,
				predCallBCompressed, predNameACompressed, predNameBCompressed,
				headerComporessed, scopeCompressed, fieldCompressed,
				compressedDependencies, compressedStatus);
	}

	/**
	 * After checking a=>b, if a=>b is true, means the check is unSAT (No
	 * Counter-example): if a=E and b=Prop then allImpliedProperties Of b also has
	 * to be returned if a=prop and b=E then allRevImpliedProperties of a has to
	 * returned. The return type is false. Means stop any furtherAnaylsis and take
	 * the result as the inferred propertied else, there is a counterexample if
	 * a=E and b=Prop then next properties implied from Prop has to be evaluated
	 * if a=Prop and b=E then next properties that implying Prop has to be
	 * evaluated
	 */
	public List<String> getInferedProperties(int sat) {
		List<String> result = new ArrayList<>();
		if (isDesiredSAT(sat)) {
			for (ImplicationLattic il : implications) {
				try {
					// predNameA is supposed to be the name exists in the library.
					// otherwise it throws an exception
					result.addAll(il.getAllRevImpliedProperties(predNameA));
				} catch (Err e) {
					// logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "
					// + "Failed to getInferedSAT for "+predNameA, e);
				}
				try {
					// predNameB is supposed to be the name exists in the library.
					// otherwise it throws an exception
					result.addAll(il.getAllImpliedProperties(predNameB));
				} catch (Err e) {
					// logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "
					// + "Failed to getInferedSAT for "+predNameB, e);
				}
			}
		}
		return Collections.unmodifiableList(result);
	}

	public List<PropertyToAlloyCode> getInferedPropertiesCoder(int sat) {
		List<PropertyToAlloyCode> result = new ArrayList<>();
		if (isDesiredSAT(sat)) {
			for (ImplicationLattic il : implications) {
				try {
					// predNameA is supposed to be the name exists in the library.
					// otherwise it throws an exception
					for (String propName : il.getAllRevImpliedProperties(predNameA)) {
						result.add(createIt("", this.predBodyB, "", this.predCallB,
								propName, this.predNameB, new ArrayList<>(dependencies),
								this.paramCreator, this.header, this.scope, this.field));
					}
				} catch (Err e) {
					logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
							+ "Failed to getInferedSAT for " + predNameA, e);
				}
				try {
					// predNameB is supposed to be the name exists in the library.
					// otherwise it throws an exception
					for (String propName : il.getAllImpliedProperties(predNameB)) {
						result.add(createIt(this.predBodyA, "", this.predCallA, "",
								this.predNameA, propName, new ArrayList<>(dependencies),
								this.paramCreator, this.header, this.scope, this.field));
					}
				} catch (Err e) {
					logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
							+ "Failed to getInferedSAT for " + predNameB, e);
				}
			}
		}

		System.out.println("The inference result of " + this.predNameA + "=>"
				+ this.predNameB + "?" + sat + " is:" + result);

		return Collections.unmodifiableList(result);
	}

	public List<String> getToBeCheckedProperties(int sat) {

		List<String> result = new ArrayList<>();
		if (!isDesiredSAT(sat)) {
			for (ImplicationLattic il : implications) {
				try {
					// predNameA is supposed to be the name exists in the library.
					// otherwise it throws an exception
					result.addAll(il.getNextRevImpliedProperties(predNameA));
				} catch (Err e) {
					logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
							+ "Failed to getInferedSAT for " + predNameA, e);
				}
				try {
					// predNameB is supposed to be the name exists in the library.
					// otherwise it throws an exception
					result.addAll(il.getNextImpliedProperties(predNameB));
				} catch (Err e) {
					logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
							+ "Failed to getInferedSAT for " + predNameB, e);
				}
			}
		}
		return Collections.unmodifiableList(result);
	}

	public List<String> getInitialProperties() {
		List<String> result = new ArrayList<>();
		for (ImplicationLattic il : implications) {
			try {
				result.addAll(il.getAllSources());
			} catch (Err e) {
				logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
						+ "Cannot find the initial properties ", e);
				e.printStackTrace();
			}
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * no counter-example should be found.
	 */
	public boolean isDesiredSAT(int sat) {
		return sat == -1;
	}

}
