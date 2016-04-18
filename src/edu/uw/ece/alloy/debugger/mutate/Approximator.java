/**
 * 
 */
package edu.uw.ece.alloy.debugger.mutate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.IfPropertyToAlloyCode;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyProcessedResult;

/**
 * @author vajih
 * 
 *         Given the implied properties and consistent properties, an
 *         approximation of a given expression.
 *
 */
public class Approximator {

	final static Logger logger = Logger.getLogger(
			Approximator.class.getName() + "--" + Thread.currentThread().getName());
	// Map from an expression to analysis results.
	final private Map<String, Set<String>> impliedProperties = new ConcurrentHashMap<String, Set<String>>();

	final static Approximator self = new Approximator();

	public static Approximator getInstance() {
		return self;
	}

	/**
	 * Only direct implications are added.
	 * 
	 * @param expression
	 * @param result
	 */
	public void addDirectImplication(AlloyProcessedResult result) {

		System.out.println(
				"result.params.alloyCoder->" + result.params.alloyCoder.getClass());

		System.out.println("The condition is1:" + (result.getClass().getName()
				.equals(AlloyProcessedResult.class.getName())));
		System.out.println("The condition is2:"
				+ (result.params.alloyCoder instanceof IfPropertyToAlloyCode));
		System.out.println("The condition is3:" + result.getClass().getName());
		System.out.println("The condition is4:" + result.params.alloyCoder);

		if ((result.getClass().getName()
				.equals(AlloyProcessedResult.class
						.getName()) /*
												 * || result.getClass().getName().equals(
												 * AlloyProcessedResult.InferredResult.class.getName())
												 */ )
				&& result.params.alloyCoder instanceof IfPropertyToAlloyCode)
			addImplication(result.params.alloyCoder.predCallA,
					(IfPropertyToAlloyCode) result.params.alloyCoder);
		System.out.println("addImplication2->" + result);
		System.out.println("addImplication3->" + impliedProperties);
		System.out.println("addImplication3->" + impliedProperties.keySet());
	}

	public void addImplication(String expression,
			IfPropertyToAlloyCode property) {
		if (!impliedProperties.containsKey(expression))
			impliedProperties.put(expression,
					Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>()));
		System.out.println("addImplication1->" + expression + "<  " + property);
		impliedProperties.get(expression).add(property.predCallB);
		System.out.println("implication->" + impliedProperties);
	}

	public List<String> getDirectImpliedPropertyCalls(String expression) {
		List<String> result = new ArrayList<>();

		for (String imply : impliedProperties.get(expression)) {
			result.add(imply);
		}

		return Collections.unmodifiableList(result);
	}

	public String getDirectImpliedApproximation(String expression) {
		System.out.println("getDirectImpliedPropertyCalls(expression)->"
				+ getDirectImpliedPropertyCalls(expression));
		return String.join(" and ", getDirectImpliedPropertyCalls(expression));
	}

	// TODO remove it and delta it
	public String getDirectImpliedApproximation() {
		System.out
				.println("impliedProperties.keySet()>" + impliedProperties.keySet());
		if (impliedProperties.keySet().isEmpty())
			return "";
		else {
			String firstKey = impliedProperties.keySet().iterator().next();
			System.out.println("firstKey>" + firstKey);
			return getDirectImpliedApproximation(firstKey);
		}
	}

}
