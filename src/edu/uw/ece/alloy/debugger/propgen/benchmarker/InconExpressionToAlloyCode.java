package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.uw.ece.alloy.debugger.knowledgebase.ImplicationLattic;

/**
 * The class is for determining whether an expression is inconsistent by itself.
 * It does not return which are in uncsatCore.
 * The class does not deal with the properties so that is why 
 * the class name does not contains Property in it.
 * @author vajih
 *
 */
public class InconExpressionToAlloyCode extends PropertyToAlloyCode {

	private static final long serialVersionUID = 1L;

	final public static InconExpressionToAlloyCode EMPTY_CONVERTOR = new InconExpressionToAlloyCode();
	final static Logger logger = Logger
			.getLogger(InconExpressionToAlloyCode.class.getName() + "--"
					+ Thread.currentThread().getName());

	
	public InconExpressionToAlloyCode(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA, String predNameB,
			List<Dependency> dependencies, String header, String scope,
			String field) {
		super(predBodyA, predBodyB, predCallA, predCallB, predNameA, predNameB,
				dependencies, header, scope, field);
		// TODO Auto-generated constructor stub
	}

	public InconExpressionToAlloyCode() {
		super();
	}

	@Override
	String commandKeyword() {
		return "run";
	}

	@Override
	public String commandOperator() {
		return "";
	}

	@Override
	public String srcNameOperator() {
		return "_INCON_EXPR_";
	}

	@Override
	String commandKeyWordBody() {
		return "pred";
	}
	
	
	protected String generateAlloyCode() {
		String source = "";

		source += generatePrepend();
		source += '\n' + generatePredicateBody(predBodyA);
		source += '\n' + generatePredicateBody(predBodyB);
		source += '\n' + commandStatement(predCallA, "");
		source += scope;

		return source;
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
		return new InconExpressionToAlloyCode(predBodyA, predBodyB, predCallA,
				predCallB, predNameA, predNameB, dependencies,
				/* paramCreator, */ header, scope, field);
	}

	/**
	 * There is no inference supposed to be returned. Either an expression
	 * is inconsistent by itself, either not. 
	 */
	public List<String> getInferedProperties(int sat) {
		return Collections.emptyList();
	}

	/**
	 * Same as {@link #getInferedProperties(int) getInferedProperties}
	 */
	public List<PropertyToAlloyCode> getInferedPropertiesCoder(int sat) {
		return Collections.emptyList();
	}

	/**
	 * Same as {@link #getInferedProperties(int) getInferedProperties}   
	 */
	public List<String> getToBeCheckedProperties(int sat) {
		return Collections.emptyList();
	}

	/**
	 * Whatever is returned should be neutral 
	 */
	public List<String> getInitialProperties() {
		List<String> result = new ArrayList<>();
		for (ImplicationLattic il : getImplicationLattices()
				.orElseThrow(() -> new RuntimeException(
						"Implication List is null.Since it is a trinsient property, recreating the object might be effective"))) {
			try {
				result.add(il.getAllSources().get(0));
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
