package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule.Open;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.PropertyCallBuilder;
import edu.uw.ece.alloy.debugger.exec.A4CommandExecuter;
import edu.uw.ece.alloy.debugger.filters.BlocksExtractorByComments.ExtractExpression;
import edu.uw.ece.alloy.debugger.filters.BlocksExtractorByComments.ExtractScope;
import edu.uw.ece.alloy.debugger.knowledgebase.PatternToProperty;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadDelayToBeMonitored;
import edu.uw.ece.alloy.util.Utils;

public class ExpressionPropertyGenerator
		implements Runnable, ThreadDelayToBeMonitored {

	final static Logger logger = Logger
			.getLogger(ExpressionPropertyGenerator.class.getName() + "--"
					+ Thread.currentThread().getName());
	final public static boolean doCompress = Boolean
			.valueOf(Configuration.getProp("doCompressAlloyParams"));
	// final File resourcesDir = new File(
	// Configuration.getProp("models_directory") );
	// final static File logOutputDir = new
	// File(Configuration.getProp("log_out_directory"));
	// final File workingDir = new File(
	// Configuration.getProp("working_directory") );
	final public File relationalPropModuleOriginal;/*
																								  * = new File(
																								  * Configuration.getProp(
																								  * "relational_properties_tagged"
																								  * ));
																								  */
	final public File temporalPropModuleOriginal;/*
																							  * = new File(
																							  * Configuration.getProp(
																							  * "temporal_properties_tagged"))
																							  * ;
																							  */
	final GeneratedStorage<AlloyProcessingParam> generatedStorage;

	final List<Sig.Field> fields;
	final List<Open> opens;
	final File toBeAnalyzedCode;

	final String predFromExpression;

	final Boolean doVAC;// = Boolean.valueOf(Configuration.getProp("doVAC"));
	final Boolean doIFF;// = Boolean.valueOf(Configuration.getProp("doIFF"));
	final Boolean doIMPLY;// = Boolean.valueOf(Configuration.getProp("doIMPLY"));
	final Boolean doAND;// = Boolean.valueOf(Configuration.getProp("doAND"));

	final AlloyProcessingParam paramCreator;

	final static List<Dependency> dependencies = new LinkedList<Dependency>();

	final PropertyToAlloyCodeBuilder propertyBuilder;
	final public PatternToProperty patternToProperty;

	final protected Thread generator;

	Set<String> toBeCheckProperties;
	// Such checks should be excluded from the generation because for example the
	// are generated before.
	Set<String> excludedChecks;
	final public static String predNamePrefix = "predName___";

	/**
	 * Wraps an extracted expression and wrap it in a predicate block
	 * 
	 * @param expression
	 * @return A pair of Predicate body and its call.
	 */
	private Pair<String, String> expressionToPred(String expression) {
		String predName = predNamePrefix + Math.abs(expression.hashCode()); // +
																																				// System.currentTimeMillis();

		return new Pair<>(predName,
				String.format("pred %s[]{\n%s}", predName, expression));
	}

	/*
	 * public ExpressionPropertyChecker( final
	 * GeneratedStorage<AlloyProcessingParam> generatedStorage, File
	 * toBeAnalyzedCode) throws Err, IOException { this(generatedStorage,
	 * toBeAnalyzedCode, new
	 * File(Configuration.getProp("relational_properties_tagged")), new
	 * File(Configuration.getProp("temporal_properties_tagged")),
	 * Boolean.valueOf(Configuration.getProp("doVAC")),
	 * Boolean.valueOf(Configuration.getProp("doIFF")),
	 * Boolean.valueOf(Configuration.getProp("doIMP")),
	 * Boolean.valueOf(Configuration.getProp("doAND"))); }
	 */
	public ExpressionPropertyGenerator(
			final GeneratedStorage<AlloyProcessingParam> generatedStorage,
			File toBeAnalyzedCode, File relationalPropModuleOriginal,
			File temporalPropModuleOriginal, boolean doVAC, boolean doIFF,
			boolean doIMPLY, boolean doAND) throws Err, IOException {
		this(generatedStorage, toBeAnalyzedCode, Collections.emptySet(),
				Collections.emptySet(), relationalPropModuleOriginal,
				temporalPropModuleOriginal, doVAC, doIFF, doIMPLY, doAND);
		// IT is changed because `propertyBuilder' is used to for getting
		// the initial properties.
		toBeCheckProperties = new HashSet<>(generateInitialProperties());
	}

	public ExpressionPropertyGenerator(
			final GeneratedStorage<AlloyProcessingParam> generatedStorage,
			File toBeAnalyzedCode, final Set<String> toBeCheckProperties,
			Set<String> excludedChecks, File relationalPropModuleOriginal,
			File temporalPropModuleOriginal, boolean doVAC, boolean doIFF,
			boolean doIMPLY, boolean doAND) throws Err, IOException {

		this.doVAC = doVAC;
		this.doIFF = doIFF;
		this.doIMPLY = doIMPLY;
		this.doAND = doAND;

		this.relationalPropModuleOriginal = relationalPropModuleOriginal;
		this.temporalPropModuleOriginal = temporalPropModuleOriginal;

		this.toBeAnalyzedCode = toBeAnalyzedCode;

		ExtractExpression expressionExtractor = new ExtractExpression(
				toBeAnalyzedCode.getAbsolutePath());
		ExtractScope scopeExtractor = new ExtractScope(
				toBeAnalyzedCode.getAbsolutePath());

		Map<String, List<Sig.Field>> extractedPairs;
		try {
			extractedPairs = expressionExtractor.getAllExpressionsAndFields();
		} catch (Err e) {
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
					+ "Unable to open alloy files: ", e);
			throw e;
		}
		List<String> scopes = scopeExtractor.getAllBlocks();

		// Just for the sake of simplicity
		assert (extractedPairs.size() == 1);
		assert (scopes.size() == 1);

		System.out.println("extractedPairs->" + extractedPairs);
		System.out.println("scopes->" + scopes);

		String expression = extractedPairs.keySet().iterator().next();

		System.out.println("expression->" + expression);

		fields = extractedPairs.get(expression);
		Pair<String, String> toBeAnalyzedPred = expressionToPred(expression);
		predFromExpression = toBeAnalyzedPred.a;

		opens = ((CompModule) A4CommandExecuter.getInstance()
				.parse(toBeAnalyzedCode.getAbsolutePath(), A4Reporter.NOP)).getOpens();

		String header = Utils.readFile(toBeAnalyzedCode.getAbsolutePath());
		// prepend the module for the relational properties
		// TODO(vajih) check the module name"

		header = "open relational_properties_tagged\n" + header;
		header = "open "
				+ temporalPropModuleOriginal.getName().replace(".als", " \n") + header;
		header = header + "\n" + toBeAnalyzedPred.b;

		String scope = scopes.get(0);

		if (doCompress) {
			paramCreator = AlloyProcessingParamLazyCompressing.EMPTY_PARAM;
		} else {
			paramCreator = AlloyProcessingParam.EMPTY_PARAM;
		}

		dependencies
				.add(new Dependency(new File(relationalPropModuleOriginal.getName()),
						Utils.readFile(relationalPropModuleOriginal.getAbsolutePath())));
		dependencies
				.add(new Dependency(new File(temporalPropModuleOriginal.getName()),
						Utils.readFile(temporalPropModuleOriginal.getAbsolutePath())));

		propertyBuilder = new PropertyToAlloyCodeBuilder(dependencies, header,
				scope, paramCreator);

		if (doVAC)
			propertyBuilder
					.registerPropertyToAlloyCode(VacPropertyToAlloyCode.EMPTY_CONVERTOR);
		if (doIFF)
			propertyBuilder
					.registerPropertyToAlloyCode(IffPropertyToAlloyCode.EMPTY_CONVERTOR);
		if (doIMPLY)
			propertyBuilder
					.registerPropertyToAlloyCode(IfPropertyToAlloyCode.EMPTY_CONVERTOR);
		if (doAND)
			propertyBuilder
					.registerPropertyToAlloyCode(AndPropertyToAlloyCode.EMPTY_CONVERTOR);

		// Some sort of hacking. The content of the dependency is the path to the
		// original file. So it just need to to copy it instead of carry the content
		// per every request param.

		this.generatedStorage = generatedStorage;

		this.generator = new Thread(this);
		this.toBeCheckProperties = Collections.unmodifiableSet(toBeCheckProperties);
		this.excludedChecks = Collections.unmodifiableSet(excludedChecks);

		// This is initialized here
		patternToProperty = new PatternToProperty(relationalPropModuleOriginal,
				temporalPropModuleOriginal, toBeAnalyzedCode);

	}

	void generateTemporalChekers(Set<String> propertyNames,
			GeneratedStorage<AlloyProcessingParam> result) throws Err {

		CompModule world = (CompModule) A4CommandExecuter.getInstance()
				.parse(temporalPropModuleOriginal.getAbsolutePath(), A4Reporter.NOP);
		// add the temporal file header
		for (Func func : world.getAllFunc()) {
			String funcName = func.label.replace("this/", "");
			if (!propertyNames.contains(funcName))
				continue;
			final PropertyCallBuilder pcb = new PropertyCallBuilder();
			try {
				pcb.addPropertyDeclration(func);
			} catch (IllegalArgumentException ia) {
				logger.log(Level.WARNING, "[" + Thread.currentThread().getName() + "] "
						+ "Failling to add a property declaration:", ia);
			}

			for (Field field : fields.stream().filter(f -> f.type().arity() == 3)
					.collect(Collectors.toList())) {
				for (String PropertyCall : pcb.makeAllTernaryProperties(field, opens)) {
					for (final PropertyToAlloyCode alloyCodeGenerator : propertyBuilder
							.createObjects("", "", predFromExpression, PropertyCall,
									predFromExpression, funcName, field.label)) {
						if (excludedChecks.contains(alloyCodeGenerator.getPredName())) {
							logger.log(Level.INFO,
									"[" + Thread.currentThread().getName() + "] "
											+ "The property was generated and tested before: "
											+ alloyCodeGenerator);
							continue;
						}

						try {
							final AlloyProcessingParam generatedParam = alloyCodeGenerator
									.generate();

							System.out.println("generateTemporalChekers->" + generatedParam);

							result.addGeneratedProp(generatedParam);
						} catch (Exception e) {
							logger.log(Level.SEVERE, "[" + Thread.currentThread().getName()
									+ "] " + "Property code generation is failed:", e);
							e.printStackTrace();
							continue;
						}
					}
				}
			}
		}
	}

	void generateRelationalPropertyCalls(
			Map<Pair<String, String>, String> propertyCalls) throws Err {
		CompModule world = (CompModule) A4CommandExecuter.getInstance()
				.parse(relationalPropModuleOriginal.getAbsolutePath(), A4Reporter.NOP);
		for (Func func : world.getAllFunc()) {
			String funcName = func.label.replace("this/", "");
			final PropertyCallBuilder pcb = new PropertyCallBuilder();
			try {
				pcb.addPropertyDeclration(func);
			} catch (IllegalArgumentException ia) {
				logger.log(Level.WARNING, "[" + Thread.currentThread().getName() + "] "
						+ "Failling to add a property declaration:", ia);
			}
			for (Field field : fields.stream().filter(f -> f.type().arity() == 2)
					.collect(Collectors.toList())) {
				for (String PropertyCall : pcb.makeAllBinaryProperties(field)) {
					propertyCalls.put(new Pair<>(funcName, field.label), PropertyCall);
				}
			}
		}
	}

	void generateRelationalChekers(Set<String> propertyNames,
			GeneratedStorage<AlloyProcessingParam> result) throws Err {
		// Read the tagged relational properties library.
		CompModule world = (CompModule) A4CommandExecuter.getInstance()
				.parse(relationalPropModuleOriginal.getAbsolutePath(), A4Reporter.NOP);
		for (Func func : world.getAllFunc()) {
			String funcName = func.label.replace("this/", "");
			if (!propertyNames.contains(funcName))
				continue;
			final PropertyCallBuilder pcb = new PropertyCallBuilder();
			try {
				pcb.addPropertyDeclration(func);
			} catch (IllegalArgumentException ia) {
				logger.log(Level.WARNING, "[" + Thread.currentThread().getName() + "] "
						+ "Failling to add a property declaration:", ia);
			}

			System.out.println("predFromExpression->" + predFromExpression);

			for (Field field : fields.stream().filter(f -> f.type().arity() == 2)
					.collect(Collectors.toList())) {
				for (String PropertyCall : pcb.makeAllBinaryProperties(field)) {
					for (final PropertyToAlloyCode alloyCodeGenerator : propertyBuilder
							.createObjects("", "", predFromExpression, PropertyCall,
									predFromExpression, funcName, field.label)) {

						if (excludedChecks.contains(alloyCodeGenerator.getPredName())) {
							logger.log(Level.INFO,
									"[" + Thread.currentThread().getName() + "] "
											+ "The property was generated and tested before: "
											+ alloyCodeGenerator);
							continue;
						}

						try {
							final AlloyProcessingParam generatedParam = alloyCodeGenerator
									.generate();

							System.out.println("result.addGeneratedProp->" + generatedParam
									+ "\n\t" + generatedParam.alloyCoder.predBodyA);
							result.addGeneratedProp(generatedParam);
						} catch (Exception e) {
							logger.log(Level.SEVERE, "[" + Thread.currentThread().getName()
									+ "] " + "Property code generation is failed:", e);
							e.printStackTrace();
							continue;
						}
					}
				}
			}
		}
		// Convert the code snippet into a predicate
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
		// TODO Auto-generated method stub

	}

	@Override
	public int triesOnStuck() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void actionOnStuck() {
		// TODO Auto-generated method stub

	}

	@Override
	public String amIStuck() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public long isDelayed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void run() {
		try {
			// Initiating the properties to be processed.
			generateRelationalChekers(this.toBeCheckProperties,
					this.generatedStorage);
			System.out.println("----this.generatedStorage1------:"
					+ this.generatedStorage.getGeneratedProps());

			generateTemporalChekers(this.toBeCheckProperties, this.generatedStorage);

			System.out.println("----this.generatedStorage2------:"
					+ this.generatedStorage.getGeneratedProps());

		} catch (Err e) {
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
					+ "Property code generation is failed:", e);
			e.printStackTrace();
		}
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
		Boolean doVAC;
		Boolean doIFF;
		Boolean doIMPLY;
		Boolean doAND;
		File relationalPropModuleOriginal;
		File temporalPropModuleOriginal;
		File toBeAnalyzedCode;
		/**
		 * The method provides singleton, but it is not the only way to create and
		 * object. There might be a case that cache becomes invalid and a new object
		 * is needed.
		 */
		final private static Builder self = new Builder();
		private ExpressionPropertyGenerator lastCreated = null;

		public static Builder getInstance() {
			return self;
		}

		public Builder() {
		};

		public ExpressionPropertyGenerator initiateAndCreate(
				final GeneratedStorage<AlloyProcessingParam> generatedStorage,
				File toBeAnalyzedCode, File relationalPropModuleOriginal,
				File temporalPropModuleOriginal, boolean doVAC, boolean doIFF,
				boolean doIMPLY, boolean doAND) throws Err, IOException {

			this.doVAC = doVAC;
			this.doIFF = doIFF;
			this.doIMPLY = doIMPLY;
			this.doAND = doAND;
			this.relationalPropModuleOriginal = relationalPropModuleOriginal;
			this.temporalPropModuleOriginal = temporalPropModuleOriginal;
			this.toBeAnalyzedCode = toBeAnalyzedCode;

			if (lastCreated == null) {
				this.lastCreated = new ExpressionPropertyGenerator(generatedStorage,
						toBeAnalyzedCode, relationalPropModuleOriginal,
						temporalPropModuleOriginal, doVAC, doIFF, doIMPLY, doAND);
			} else {
				synchronized (lastCreated) {
					this.lastCreated = new ExpressionPropertyGenerator(generatedStorage,
							toBeAnalyzedCode, relationalPropModuleOriginal,
							temporalPropModuleOriginal, doVAC, doIFF, doIMPLY, doAND);
				}
			}

			return this.lastCreated;
		}

		public ExpressionPropertyGenerator create(
				final GeneratedStorage<AlloyProcessingParam> generatedStorage,
				final Set<String> toBeCheckProperties, Set<String> excludedChecks)
						throws Err, IOException {
			if (null == this.lastCreated)
				throw new RuntimeException("Objects are not initilized yet.");

			synchronized (lastCreated) {
				this.lastCreated = new ExpressionPropertyGenerator(generatedStorage,
						this.toBeAnalyzedCode, toBeCheckProperties, excludedChecks,
						this.relationalPropModuleOriginal, this.temporalPropModuleOriginal,
						this.doVAC, this.doIFF, this.doIMPLY, this.doAND);
			}

			return this.lastCreated;
		}

		public ExpressionPropertyGenerator lastCreated() {
			if (null == this.lastCreated)
				throw new RuntimeException("Objects are not initilized yet.");

			return lastCreated;
		}
	}

}
