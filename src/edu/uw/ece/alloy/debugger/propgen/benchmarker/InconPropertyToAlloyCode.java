package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.uw.ece.alloy.debugger.knowledgebase.ImplicationLattic;

/**
 * The Alloy coder for generating Alloy code to check if two predicates are
 * inconsistent.
 * 
 * @author vajih
 *
 */
public class InconPropertyToAlloyCode extends PropertyToAlloyCode {

	private static final long serialVersionUID = -1823724347450192446L;

	final public static InconPropertyToAlloyCode EMPTY_CONVERTOR = new InconPropertyToAlloyCode();
	final static Logger logger = Logger
			.getLogger(InconPropertyToAlloyCode.class.getName() + "--"
					+ Thread.currentThread().getName());

	protected InconPropertyToAlloyCode(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA, String predNameB,
			List<Dependency> dependencies, /* AlloyProcessingParam paramCreator, */
			String header, String scope, String field) {
		super(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
				dependencies, /* paramCreator, */ header, scope, field);
	}

	protected InconPropertyToAlloyCode() {
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
		return "_INCON_";
	}

	@Override
	String commandKeyWordBody() {
		return "pred";
	}

	@Override
	public boolean isSymmetric() {
		return true;
	}

	@Override
	public PropertyToAlloyCode createIt(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA, String predNameB,
			List<Dependency> dependencies, /* AlloyProcessingParam paramCreator, */
			String header, String scope, String field) {
		return new InconPropertyToAlloyCode(predBodyA, predBodyB, predCallA,
				predCallB, predNameA, predNameB, dependencies,
				/* paramCreator, */ header, scope, field);
	}

	/**
	 * After checking a ^ b, if a ^ b is false, means the check is UnSAT (not
	 * Found an example): if a=E and b=Prop then allRevImpliedProperties Of b also
	 * has to be returned. The return type is false. Means stop any
	 * furtherAnaylsis and take the result as the inferred propertied else, there
	 * is a counterexample if a=E and b=Prop then next properties implied from
	 * Prop has to be evaluated if a=Prop and b=E then next properties that
	 * implying Prop has to be evaluated
	 */
	public List<String> getInferedProperties(int sat) {
		List<String> result = new ArrayList<>();
		if (isDesiredSAT(sat)) {
			for (ImplicationLattic il : getImplicationLattices()
					.orElseThrow(() -> new RuntimeException(
							"Implication List is null.Since it is a trinsient property, recreating the object might be effective"))) {
				try {
					// predNameA is supposed to be the name exists in the library.
					// otherwise it throws an exception
					result.addAll(il.getAllRevImpliedProperties(predNameA));
				} catch (Err e) {
				}
				try {
					// predNameB is supposed to be the name exists in the library.
					// otherwise it throws an exception
					result.addAll(il.getAllRevImpliedProperties(predNameB));
				} catch (Err e) {
				}
			}
		}
		return Collections.unmodifiableList(result);
	}

	public List<PropertyToAlloyCode> getInferedPropertiesCoder(int sat) {
		List<PropertyToAlloyCode> result = new ArrayList<>();
		if (isDesiredSAT(sat)) {
			for (ImplicationLattic il : getImplicationLattices()
					.orElseThrow(() -> new RuntimeException(
							"Implication List is null.Since it is a trinsient property, recreating the object might be effective"))) {
				try {
					// predNameA is supposed to be the name exists in the library.
					// otherwise it throws an exception
					for (String propName : il.getAllRevImpliedProperties(predNameA)) {
						result.add(createIt("", this.predBodyB, "", this.predCallB,
								propName, this.predNameB, new ArrayList<>(dependencies),
								/* this.paramCreator, */ this.header, this.scope, this.field));
					}
				} catch (Err e) {
					logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
							+ "Failed to getInferedSAT for " + predNameA, e);
				}
				try {
					// predNameB is supposed to be the name exists in the library.
					// otherwise it throws an exception
					for (String propName : il.getAllRevImpliedProperties(predNameB)) {
						result.add(createIt(this.predBodyA, "", this.predCallA, "",
								this.predNameA, propName, new ArrayList<>(dependencies),
								/* this.paramCreator, */this.header, this.scope, this.field));
					}
				} catch (Err e) {
					logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
							+ "Failed to getInferedSAT for " + predNameB, e);
				}
			}
		}

		// System.out.println("The inference result of " + this.predNameA + "=>"
		// + this.predNameB + "?" + sat + " is:" + result);

		return Collections.unmodifiableList(result);
	}

	public List<String> getToBeCheckedProperties(int sat) {
		List<String> result = new ArrayList<>();
		if (!isDesiredSAT(sat)) {
			for (ImplicationLattic il : getImplicationLattices()
					.orElseThrow(() -> new RuntimeException(
							"Implication List is null.Since it is a trinsient property, recreating the object might be effective"))) {
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
					result.addAll(il.getNextRevImpliedProperties(predNameB));
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
		for (ImplicationLattic il : getImplicationLattices()
				.orElseThrow(() -> new RuntimeException(
						"Implication List is null.Since it is a trinsient property, recreating the object might be effective"))) {
			try {
				result.addAll(il.getAllSinks());
			} catch (Err e) {
				logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
						+ "Cannot find the initial properties ", e);
				e.printStackTrace();
			}
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * an example should be found.
	 */
	public boolean isDesiredSAT(int sat) {
		return sat == -1;
	}

}
