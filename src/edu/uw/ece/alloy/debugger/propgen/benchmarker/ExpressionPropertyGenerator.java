package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.knowledgebase.PatternToProperty;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.communication.Publisher;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;
import edu.uw.ece.alloy.util.Utils;

public class ExpressionPropertyGenerator
		implements Runnable, ThreadToBeMonitored {

	final static Logger logger = Logger
			.getLogger(ExpressionPropertyGenerator.class.getName() + "--"
					+ Thread.currentThread().getName());

	final public File relationalPropModuleOriginal;
	final public File temporalPropModuleOriginal;
	final public File toBeAnalyzedCode;
	final Publisher<AlloyProcessingParam> generatedStorage;

	final public UUID sessionID;

	/* The field that is going to be transformed to property */
	final Sig.Field field;
	// final List<Open> opens;

	/* The expression that is going to be analyzed. */
	final ExpressionPredicate expressionPredicate;

	final static List<Dependency> dependencies = new LinkedList<Dependency>();

	/* Converting properties and patterns to param. */
	final PropertyToAlloyCodeBuilder propertyBuilder;
	final public PatternToProperty patternToProperty;

	final protected Thread generator;
	Set<String> toBeCheckProperties;
	// Such checks should be excluded from the generation because for example the
	// are generated before.
	final Set<String> excludedChecks;

	/**
	 * A predicate is made and the information is stored as predicateName,
	 * predicateCall and predicateBody.
	 * 
	 * @author vajih
	 *
	 */
	final public static class ExpressionPredicate {

		// predNamePrefix matters. It is used for finding the implied properties
		final public static String predNamePrefix = "predName___";

		final String predicateName;
		final String predicateCall;
		final String predicateBody;
		final String expression;

		public ExpressionPredicate(String expression) {
			this.expression = expression;
			this.predicateName = predNamePrefix
					+ Math.abs(this.expression.hashCode());
			this.predicateBody = String.format("pred %s[]{\n%s}", predicateName,
					this.expression);
			this.predicateCall = String.format("%s[]", predicateName);
		}

		@Override
		public String toString() {
			return "ExpressionPredicate [predicateName=" + predicateName
					+ ", predicateCall=" + predicateCall + ", predicateBody="
					+ predicateBody + ", expression=" + expression + "]";
		}

	}

	public ExpressionPropertyGenerator(final UUID sessionID,
			final Publisher<AlloyProcessingParam> generatedStorage,
			File toBeAnalyzedCode, File relationalPropModuleOriginal,
			File temporalPropModuleOriginal, String fieldName,
			PropertyToAlloyCode propertyToAlloyCode, String expression, String scope,
			List<File> dependecyFiles) throws Err, IOException {

		this(sessionID, generatedStorage, toBeAnalyzedCode,
				relationalPropModuleOriginal, temporalPropModuleOriginal, fieldName,
				propertyToAlloyCode, expression, scope, Collections.emptySet(),
				Optional.empty(), dependecyFiles);
	}

	/**
	 * Wraps an extracted expression and wrap it in a predicate block
	 * 
	 * @param expression
	 * @return A pair of Predicate body and its call.
	 */
	public ExpressionPropertyGenerator(final UUID sessionID,
			final Publisher<AlloyProcessingParam> generatedStorage,
			File toBeAnalyzedCode, File relationalPropModuleOriginal,
			File temporalPropModuleOriginal, String fieldName,
			PropertyToAlloyCode propertyToAlloyCode, String expression, String scope,
			Set<String> excludedChecks, List<File> dependecyFiles)
					throws Err, IOException {

		this(sessionID, generatedStorage, toBeAnalyzedCode,
				relationalPropModuleOriginal, temporalPropModuleOriginal, fieldName,
				propertyToAlloyCode, expression, scope, excludedChecks,
				Optional.empty(), dependecyFiles);
	}

	public ExpressionPropertyGenerator(final UUID sessionID,
			final Publisher<AlloyProcessingParam> generatedStorage,
			File toBeAnalyzedCode, File relationalPropModuleOriginal,
			File temporalPropModuleOriginal,

			String fieldName, PropertyToAlloyCode propertyToAlloyCode,
			String expression, String scope,

			Set<String> excludedChecks, Set<String> toBeCheckProperties,
			List<File> dependecyFiles) throws Err, IOException {
		this(sessionID, generatedStorage, toBeAnalyzedCode,
				relationalPropModuleOriginal, temporalPropModuleOriginal, fieldName,
				propertyToAlloyCode, expression, scope, excludedChecks,
				Optional.ofNullable(toBeCheckProperties), dependecyFiles);
	}

	/**
	 * 
	 * @param sessionID
	 * @param generatedStorage
	 * @param toBeAnalyzedCode
	 * @param relationalPropModuleOriginal
	 *          It is used to generate Relational properties
	 * @param temporalPropModuleOriginal
	 *          It is used to generate Temporal properties.
	 * @param fieldName
	 * @param propertyToAlloyCode
	 * @param expression
	 * @param scope
	 * @param excludedChecks
	 * @param toBeCheckProperties
	 * @param dependecyFiles
	 *          Dependency files including relationalPropModuleOriginal,
	 *          temporalPropModuleOriginal, and any other related files.
	 * @throws Err
	 * @throws IOException
	 */
	public ExpressionPropertyGenerator(final UUID sessionID,
			final Publisher<AlloyProcessingParam> generatedStorage,
			File toBeAnalyzedCode, File relationalPropModuleOriginal,
			File temporalPropModuleOriginal,

			String fieldName, PropertyToAlloyCode propertyToAlloyCode,
			String expression, String scope,

			Set<String> excludedChecks, Optional<Set<String>> toBeCheckProperties,
			List<File> dependecyFiles) throws Err, IOException {

		this.sessionID = sessionID;

		this.relationalPropModuleOriginal = relationalPropModuleOriginal;
		this.temporalPropModuleOriginal = temporalPropModuleOriginal;

		this.toBeAnalyzedCode = toBeAnalyzedCode;

		expressionPredicate = new ExpressionPredicate(expression);
		String header = makeNewHeader(expressionPredicate.predicateBody);

		/*final AlloyProcessingParam paramCreator = AlloyProcessingParam.EMPTY_PARAM;*/

		for (File file : dependecyFiles) {
			dependencies.add(new Dependency(new File(file.getName()),
					Utils.readFile(file.getAbsolutePath())));
		}

		propertyBuilder = new PropertyToAlloyCodeBuilder(dependencies, header,
				scope/*, paramCreator*/);

		propertyBuilder.registerPropertyToAlloyCode(propertyToAlloyCode);

		// Some sort of hacking. The content of the dependency is the path to the
		// original file. So it just need to to copy it instead of carry the content
		// per every request param.

		this.generatedStorage = generatedStorage;

		this.generator = new Thread(this);

		this.excludedChecks = Collections.unmodifiableSet(excludedChecks);

		// This is initialized here
		patternToProperty = new PatternToProperty(relationalPropModuleOriginal,
				temporalPropModuleOriginal, toBeAnalyzedCode, fieldName);
		Optional<Field> field = patternToProperty.getField(fieldName);
		if (!field.isPresent()) {
			throw new RuntimeException("Field name is not found: " + fieldName);
		} else {
			this.field = field.get();
		}

		if (toBeCheckProperties.isPresent()) {
			this.toBeCheckProperties = Collections
					.unmodifiableSet(toBeCheckProperties.get());
		} else {
			this.toBeCheckProperties = Collections
					.unmodifiableSet(new HashSet<>(generateInitialProperties()));
		}

	}

	protected String makeNewOpens() {
		String header = "open "
				+ relationalPropModuleOriginal.getName().replace(".als", " \n");
		header = "open "
				+ temporalPropModuleOriginal.getName().replace(".als", " \n") + header;
		return header;
	}

	protected String sanitizeTheCurrentCode() {
		String result = Utils.readFile(toBeAnalyzedCode.getAbsolutePath());
		return result;
	}

	/**
	 * Header consists of the new opens + the existing code is already + the new
	 * pred name that includes the expression to be analyzed.
	 * 
	 * @return
	 */
	protected String makeNewHeader(String newPred) {
		return makeNewOpens() + "\n" + sanitizeTheCurrentCode() + "\n" + newPred;
	}
	
	/**
	 * generate AlloyProcessingParam for checking properties of an expression
	 * 
	 * @param propertyCalls:
	 *          The property calls are ready to use. (pattern,field)->property.
	 *          E.g. <acyclic,next>->acyclic[next]. It includes both temporal and
	 *          relational patterns
	 * @param patternNames:
	 *          The pattern names that should be included
	 * @param result:
	 *          The result that should the params be stored there.
	 */
	int generatePatternCheckers(Map<Pair<String, String>, String> propertyCalls,
			Set<String> patternNames, Publisher<AlloyProcessingParam> result) {
		int numberOfGeneratedParams = 0;
		for (Pair<String, String> key : propertyCalls.keySet()) {
			final String pattern = key.a;
			final String property = propertyCalls.get(key);
			if (!patternNames.contains(pattern)) {
				if (Configuration.IsInDeubbungMode)
					logger.warning(Utils.threadName() + " The pattern:" + pattern
							+ " is not in the pattern list: " + patternNames);
				continue;
			}

			for (final PropertyToAlloyCode alloyCodeGenerator : propertyBuilder
					.createObjects("", "", expressionPredicate.predicateCall, property,
							expressionPredicate.predicateName, pattern, field.label)) {
				
				if (excludedChecks.contains(alloyCodeGenerator.getPredName())) {
					logger.log(Level.INFO,
							Utils.threadName()
									+ "The property was generated and tested before: "
									+ alloyCodeGenerator);
					continue;
				}
				try {
					final AlloyProcessingParam generatedParam = new AlloyProcessingParam(sessionID, 0, alloyCodeGenerator);
					result.put(generatedParam);
					++numberOfGeneratedParams;
				} catch (Exception e) {
					logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
							+ "Property code generation is failed:", e);
					e.printStackTrace();
					continue;
				}
			}
		}
		return numberOfGeneratedParams;
	}

	/**
	 * Generating the properties and feed into the feeder's queue. It is a
	 * synchronized version. and internal thread won't be triggered to run.
	 * 
	 * @return
	 */
	public int generatePatternCheckers() {
		return generatePatternCheckers(this.toBeCheckProperties,
				this.generatedStorage);
	}

	/**
	 * generate AlloyProcessingParam for relational patterns
	 * 
	 * @param patternNames
	 *          The pattern names that should be included
	 * @param result
	 *          The result that should the params be stored there.
	 */
	int generatePatternCheckers(Set<String> patternNames,
			Publisher<AlloyProcessingParam> result) {
		// The property calls are ready to use. (pattern,field)->property. E.g.
		// <acyclic,next>->acyclic[next]
		return generatePatternCheckers(patternToProperty.propertyCalls,
				patternNames, result);
	}

	public void startThread() {
		generator.start();
	}

	public void cancelThread() {
		generator.interrupt();
	}

	public void changePriority(final int newPriority) {
		generator.setPriority(newPriority);
	}

	@Override
	public void actionOnNotStuck() {

	}

	@Override
	public int triesOnStuck() {

		return 0;
	}

	@Override
	public void actionOnStuck() {

	}

	@Override
	public String amIStuck() {

		return "";
	}

	@Override
	public long isDelayed() {

		return 0;
	}

	@Override
	public void run() {
		// Initiating the properties to be processed.
		generatePatternCheckers();
	}

	Set<String> generateInitialProperties() throws Err {
		Set<String> result = new HashSet<>();

		for (PropertyToAlloyCode ptac : propertyBuilder
				.getAllPropertyGenerators()) {
			result.addAll(ptac.getInitialProperties());
		}

		return Collections.unmodifiableSet(result);
	}

	/**
	 * The Builder class caches some parameters for creating and
	 * ExpressionProperty object.
	 * 
	 * @author vajih
	 *
	 */
	public static class Builder {
		// cached section
		File toBeAnalyzedCode;
		File relationalPropModuleOriginal;
		File temporalPropModuleOriginal;
		String fieldName;
		PropertyToAlloyCode propertyToAlloyCode;
		String expression;
		String scope;
		UUID sessionID;
		List<File> dependecyFiles;

		/**
		 * The method provides singleton, but it is not the only way to create and
		 * object. There might be a case that cache becomes invalid and a new object
		 * is needed.
		 */

		public Builder(final UUID sessionID,

				File toBeAnalyzedCode, File relationalPropModuleOriginal,
				File temporalPropModuleOriginal,

				String fieldName, PropertyToAlloyCode propertyToAlloyCode,
				String expression, String scope, List<File> dependecyFiles) {

			this.relationalPropModuleOriginal = relationalPropModuleOriginal;
			this.temporalPropModuleOriginal = temporalPropModuleOriginal;
			this.toBeAnalyzedCode = toBeAnalyzedCode;
			this.fieldName = fieldName;
			this.propertyToAlloyCode = propertyToAlloyCode;
			this.expression = expression;
			this.scope = scope;
			this.sessionID = sessionID;
			this.dependecyFiles = dependecyFiles;

		};

		public ExpressionPropertyGenerator create(
				final Publisher<AlloyProcessingParam> generatedStorage)
						throws Err, IOException {
			return new ExpressionPropertyGenerator(sessionID, generatedStorage,
					toBeAnalyzedCode, relationalPropModuleOriginal,
					temporalPropModuleOriginal, fieldName,
					propertyToAlloyCode.createItself()/*
																						 * some fields are transient, so the
																						 * object should be recreated.
																						 */, expression, scope,
					dependecyFiles);
		}

		public ExpressionPropertyGenerator createWithHistory(
				final Publisher<AlloyProcessingParam> generatedStorage,
				final Set<String> toBeCheckProperties, Set<String> excludedChecks)
						throws Err, IOException {

			return new ExpressionPropertyGenerator(sessionID, generatedStorage,
					toBeAnalyzedCode, relationalPropModuleOriginal,
					temporalPropModuleOriginal, fieldName,
					propertyToAlloyCode.createItself(), expression, scope, excludedChecks,
					toBeCheckProperties, dependecyFiles);
		}
	}
}
