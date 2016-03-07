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

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.PropertyCallBuilder;
import edu.uw.ece.alloy.debugger.BlocksExtractorByComments.ExtractExpression;
import edu.uw.ece.alloy.debugger.BlocksExtractorByComments.ExtractScope;
import edu.uw.ece.alloy.debugger.exec.A4CommandExecuter;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadDelayToBeMonitored;
import edu.uw.ece.alloy.util.Utils;

public class ExpressionPropertyChecker implements Runnable, ThreadDelayToBeMonitored {

	final static Logger logger = Logger.getLogger(ExpressionPropertyChecker.class.getName()+"--"+Thread.currentThread().getName());
	final public static boolean doCompress = Boolean.valueOf(Configuration.getProp("doCompressAlloyParams"));
	final File resourcesDir = new File( Configuration.getProp("models_directory") );
	final static File logOutputDir = new File(Configuration.getProp("log_out_directory"));
	final File workingDir = new File( Configuration.getProp("working_directory") );
	final public static File relationalPropModuleOriginal = new File( Configuration.getProp("relational_properties_tagged") );
	final GeneratedStorage<AlloyProcessingParam> generatedStorage;

	final List<Sig.Field> fields;
	final File toBeAnalyzedCode;

	final String predFromExpression;

	final Boolean doVAC = Boolean.valueOf( Configuration.getProp("doVAC") );
	final Boolean doIFF = Boolean.valueOf( Configuration.getProp("doIFF") );
	final Boolean doIMPLY = Boolean.valueOf( Configuration.getProp("doIMPLY") );
	final Boolean doAND = Boolean.valueOf( Configuration.getProp("doAND") );

	//final List<Fiel>

	final AlloyProcessingParam paramCreator;

	final static List<Dependency> dependencies = new LinkedList<Dependency>(); 

	final PropertyToAlloyCodeBuilder propertyBuilder;

	final protected Thread generator;

	Set<String> toBeCheckProperties;
	// Such checks should be excluded from the generation because for example the are generated before.
	Set<String> excludedChecks;

	/**
	 * Wraps an extracted expression and wrap it in a predicate block 
	 * @param expression
	 * @return A pair of Predicate body and its call.
	 */
	private Pair<String, String> expressionToPred(String expression){
		String predName = "predName___" + System.currentTimeMillis();

		return new Pair<>(predName, String.format("pred %s[]{\n%s}", predName, expression));
	}

	public ExpressionPropertyChecker(final GeneratedStorage<AlloyProcessingParam> generatedStorage, File toBeAnalyzedCode) throws Err, IOException {
		this(generatedStorage, toBeAnalyzedCode, Collections.emptySet(), Collections.emptySet());
		toBeCheckProperties = new HashSet(generateInitialRelationalProperties());
	}

	public ExpressionPropertyChecker(final GeneratedStorage<AlloyProcessingParam> generatedStorage, 
																	 File toBeAnalyzedCode, final Set<String> toBeCheckProperties,
																	 Set<String> excludedChecks) throws Err, IOException {

		this.toBeAnalyzedCode = toBeAnalyzedCode;

		ExtractExpression expressionExtractor = new ExtractExpression(toBeAnalyzedCode.getAbsolutePath());
		ExtractScope scopeExtractor = new ExtractScope(toBeAnalyzedCode.getAbsolutePath());

		Map<String, List<Sig.Field>> extractedPairs;
		try {
			extractedPairs = expressionExtractor.getAllExpressionsAndFields();
		} catch (Err e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Unable to open alloy files: ", e);
			throw e;
		}
		List<String> scopes = scopeExtractor.getAllBlocks();

		// Just for the sake of simplicity
		assert(extractedPairs.size() == 1);
		assert(scopes.size() == 1);

		String expression = extractedPairs.keySet().iterator().next();
		fields = extractedPairs.get(expression);
		Pair<String, String> toBeAnalyzedPred = expressionToPred(expression);
		predFromExpression = toBeAnalyzedPred.a;

		String header = Utils.readFile(toBeAnalyzedCode.getAbsolutePath());
		// prepend the module for the relational properties
		//TODO(vajih) check the module name"
		header = "open relational_properties_tagged\n" + header;
		header = header + "\n" + toBeAnalyzedPred.b;

		String scope = scopes.get(0);

		if(doCompress){
			paramCreator = AlloyProcessingParamLazyCompressing.EMPTY_PARAM;
		}else{
			paramCreator = AlloyProcessingParam.EMPTY_PARAM;
		}

		dependencies.add(new Dependency(new File( relationalPropModuleOriginal.getName()), Utils.readFile(relationalPropModuleOriginal.getAbsolutePath())));

		propertyBuilder = new PropertyToAlloyCodeBuilder(dependencies, header, scope, paramCreator);

		if(doVAC) propertyBuilder.registerPropertyToAlloyCode(VacPropertyToAlloyCode.EMPTY_CONVERTOR);
		if(doIFF) propertyBuilder.registerPropertyToAlloyCode(IffPropertyToAlloyCode.EMPTY_CONVERTOR);
		if(doIMPLY) propertyBuilder.registerPropertyToAlloyCode(IfPropertyToAlloyCode.EMPTY_CONVERTOR);
		if(doAND) propertyBuilder.registerPropertyToAlloyCode(AndPropertyToAlloyCode.EMPTY_CONVERTOR);

		//Some sort of hacking. The content of the dependency is the path to the original file. So it just need to to copy it instead of carry the content per every request param.

		this.generatedStorage = generatedStorage;

		this.generator = new Thread(this);
		this.toBeCheckProperties = Collections.unmodifiableSet(toBeCheckProperties);
		this.excludedChecks = Collections.unmodifiableSet(excludedChecks);
	}

	void generateRelationalChekers(Set<String> propertyNames, GeneratedStorage<AlloyProcessingParam> result) throws Err{
		// Read the tagged relational properties library.
		CompModule world = (CompModule) A4CommandExecuter.getInstance().parse(relationalPropModuleOriginal.getAbsolutePath() , A4Reporter.NOP );
		for (Func func : world.getAllFunc()) {
			String funcName = func.label.replace("this/", "");
			if (!propertyNames.contains(funcName))
				continue;
			final PropertyCallBuilder pcb = new PropertyCallBuilder();
			try {
				pcb.addPropertyDeclration(func);
			} catch (IllegalArgumentException ia) {
				logger.log(Level.WARNING, "["+Thread.currentThread().getName()+"] " + "Failling to add a property declaration:", ia);
			}


			for (Field field: fields){
				for (String PropertyCall: pcb.makeAllBinaryProperties(field)){
					for(final PropertyToAlloyCode alloyCodeGenerator: propertyBuilder.createObjects(
							"", "", 
							predFromExpression, PropertyCall,  
							predFromExpression, funcName
							) ){

						
						System.out.println("[------------]"+alloyCodeGenerator.getPredName()+"->"+excludedChecks);
						if (excludedChecks.contains(alloyCodeGenerator.getPredName())){
							logger.log(Level.INFO, "["+Thread.currentThread().getName()+"] " + "The property was generated and tested before: "+alloyCodeGenerator);
							continue;
						}
							
						try {
							final AlloyProcessingParam generatedParam = alloyCodeGenerator.generate();
							result.addGeneratedProp(generatedParam);
						} catch (Exception e) {
							logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Property code generation is failed:", e);
							e.printStackTrace();
							continue;
						}
					}
				}
			}
		}	
		// Convert the code snippet into a predicate
	}

	public void startThread(){
		generator.start();
	}

	public void cancelThread(){
		generator.interrupt();
	}

	public void changePriority(final int newPriority){
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
		return null;
	}

	@Override
	public long isDelayed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void run() {
		try {
			generateRelationalChekers(this.toBeCheckProperties, this.generatedStorage);
		} catch (Err e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Property code generation is failed:", e);
			e.printStackTrace();
		}
	}

	Set<String> generateInitialRelationalProperties() throws Err{
		Set<String> result = new HashSet<>(); 
		for (PropertyToAlloyCode ptac: propertyBuilder.getAllPropertyGenerators()){
			result.addAll(ptac.getInitialProperties());
		}
		return Collections.unmodifiableSet(result);
	}

}
