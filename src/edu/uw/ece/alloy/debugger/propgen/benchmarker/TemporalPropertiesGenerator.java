package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.TripleBuilder;
import edu.uw.ece.alloy.util.Utils;


public class TemporalPropertiesGenerator {

	/*
	 * Only the file names in the path are processed. 
	 */
	final static String filterPath = Configuration.getProp("processing_filter");

	final static boolean isResumable = Boolean.valueOf(Configuration.getProp("is_resume_processing"));

	final static int PropertiesMin = Integer.valueOf(Configuration.getProp("generated_properties_min_start"));
	final static int PropertiesMax = Integer.valueOf(Configuration.getProp("generated_properties_max_end"));
	static{
		if(PropertiesMin >= PropertiesMax)
			throw new RuntimeException("PropertiesMin: "+PropertiesMin+" has to be less than PropertiesMax: "+ PropertiesMax);
	}

	final public static boolean doCompress = Boolean.valueOf(Configuration.getProp("doCompressAlloyParams"));

	final static Logger logger = Logger.getLogger(TemporalPropertiesGenerator.class.getName()+"--"+Thread.currentThread().getName());

	final File resourcesDir = new File( Configuration.getProp("models_directory") );

	final static File logOutputDir = new File(Configuration.getProp("log_out_directory"));
	//final File logOutputFile = new File(logOutputDir, "log"+new SimpleDateFormat("_yyyy-MM-dd--HH-mm-ss-SSS").format(new Date())+".log");

	final File workingDir = new File( Configuration.getProp("working_directory") );
	//final File tmpDirectory = new File(workingDir, Configuration.getProp("temporary_directory") );
	final static File tmpDirectory = new File( Configuration.getProp("temporary_directory") );


	//final File relationalPropModuleOriginal = new File( resourcesDir, "relational_properties.als");
	final public static File relationalPropModuleOriginal = new File( Configuration.getProp("relational_properties") );


	final Boolean doVAC = Boolean.valueOf( Configuration.getProp("doVAC") );
	final Boolean doIFF = Boolean.valueOf( Configuration.getProp("doIFF") );
	final Boolean doIMPLY = Boolean.valueOf( Configuration.getProp("doIMPLY") );
	final Boolean doAND = Boolean.valueOf( Configuration.getProp("doAND") );

	final static String SigDecl = "sig M,E{}\nsig S{r:M->E}";
	final static String ModuleS = "open util/ordering [S] as so";
	final static String ModuleM = "open util/ordering [M] as mo";
	final static String ModuleE = "open util/ordering [E] as eo";
	final static String RelationProps = "open relational_properties";
	final static String Header =  ModuleS + '\n' + ModuleM + '\n' + ModuleE + '\n' + RelationProps +'\n'+ SigDecl + '\n';
	final static String Scope = " for 5";

	final AlloyProcessingParam paramCreator;

	final static List<Pair<File, String>> dependencies = new LinkedList<Pair<File,String>>(); 
	static{
		dependencies.add(new Pair<File, String>(new File(tmpDirectory, relationalPropModuleOriginal.getName()), Utils.readFile(relationalPropModuleOriginal.getAbsolutePath())));
	}
	final PropertyToAlloyCodeBuilder propertyBuilder;;

	final TripleBuilder builder;

	public TemporalPropertiesGenerator() {
		builder = new TripleBuilder(
				"r", "s", "s_next", "s_first",
				"m", "m_next", "m_first", 
				"e", "e_next", "e_first",

				"r", "S", "so/next", "so/first", 
				"M", 
				"E", "eo/next", "eo/first", 
				"mo/next", "mo/first");

		if(doCompress){
			paramCreator = AlloyProcessingParamLazyCompressing.EMPTY_PARAM;
		}else{
			paramCreator = AlloyProcessingParam.EMPTY_PARAM;
		}

		propertyBuilder = new PropertyToAlloyCodeBuilder(dependencies, Header, Scope, paramCreator,tmpDirectory);

		if(doVAC) propertyBuilder.registerPropertyToAlloyCode(VacPropertyToAlloyCode.EMPTY_CONVERTOR);
		if(doIFF) propertyBuilder.registerPropertyToAlloyCode(IffPropertyToAlloyCode.EMPTY_CONVERTOR);
		if(doIMPLY) propertyBuilder.registerPropertyToAlloyCode(IfPropertyToAlloyCode.EMPTY_CONVERTOR);
		if(doAND) propertyBuilder.registerPropertyToAlloyCode(AndPropertyToAlloyCode.EMPTY_CONVERTOR);


	}

	private Set<String> filterFileNames(){
		final Set<String> fileNames = new HashSet<>();
		final File file = new File(filterPath);
		if(file.exists()){
			Utils.readFile( file , new Consumer<List<String>>() {
				@Override
				public void accept(List<String> t) {
					assert t.size() == 1;
					String name = t.get(0);

					fileNames.add(name);
				}});
		}

		return Collections.unmodifiableSet(fileNames);
	}


	void generateRelationChekers( final Map<String, Pair<String, String>> tripleProps, GeneratedStorage<AlloyProcessingParam> result) throws Err{

		//final SimpleAsynchFileWriter safWriter = new SimpleAsynchFileWriter(); 

		final Set<String> filterNames = filterFileNames();

		//List<File> result = new LinkedList<File>();

		//done is kind of like result, but presumely smaller size and only compare the name.
		Set<String> doneNames = new HashSet<>();

		int generatedCount = 0;
		boolean breakFromAll = false;
		for(String pred1: tripleProps.keySet()){
			for(String pred2: tripleProps.keySet()){
				if(pred1.equals(pred2)) continue;
				
				for(final PropertyToAlloyCode alloyCodeGenerator: propertyBuilder.createObjects(
						tripleProps.get(pred1).b, tripleProps.get(pred2).b, 
						tripleProps.get(pred1).a, tripleProps.get(pred2).a, 
						pred1, pred2, tmpDirectory) ){
					
					if(doneNames.contains(alloyCodeGenerator.srcName())) continue;
					if(filterNames.contains(alloyCodeGenerator.srcName())) continue;

					if(!isResumable /*&& Some condition has to be put here to skip the current property*/){
						logger.info("["+Thread.currentThread().getName()+"] " +alloyCodeGenerator.srcName() +" is created.");
					}else{
						logger.info("["+Thread.currentThread().getName()+"] " +alloyCodeGenerator.srcName() +" is resumable ans is already solved..");
					}

					generatedCount++;

					if( generatedCount >= PropertiesMin && generatedCount < PropertiesMax ){
						try {
							final AlloyProcessingParam generatedParam = alloyCodeGenerator.generate();
							
							result.addGeneratedProp(generatedParam);
						} catch (Exception e) {
							logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Property code generation is failed:", e);
							e.printStackTrace();
							continue;
						}
					}else{
						breakFromAll = true;
						break;
					}

					if(alloyCodeGenerator.isSymmetric()){
						doneNames.add(propertyBuilder.createReverse(alloyCodeGenerator).srcName());
					}

					doneNames.add(alloyCodeGenerator.srcName());
				}
				if(breakFromAll) break;
			}
			if(breakFromAll) break;
		}

		if(result.getSize() != (PropertiesMax - PropertiesMin) ){
			logger.log(Level.WARNING, "["+Thread.currentThread().getName()+"] " + 
					"The generated and stored properties are: "+ result.getSize() +
					" But it was expecpted to have (PropertiesMax="+PropertiesMax+
					"-PropertiesMin="+PropertiesMin+")="+(PropertiesMax-PropertiesMin));
		}
		
		doneNames.clear();

		logger.info("["+Thread.currentThread().getName()+"] " + result.getSize()+ " properties are generated: "+ generatedCount);

	}


	private void setUpFolders() throws Err, IOException{

		if( !workingDir.exists() ){
			workingDir.mkdir();
			logger.info("["+Thread.currentThread().getName()+"] " + "Working directory is created: "+workingDir.getAbsolutePath());
		}

		if( tmpDirectory.exists() ){
			try {
				logger.info("["+Thread.currentThread().getName()+"] " +" exists and has to be recreated." +tmpDirectory.getCanonicalPath());
				Utils.deleteRecursivly(tmpDirectory);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Unable to delete the previous files.", e);
			}
		}

		//After deleting the temp directory create a new one.
		if (!tmpDirectory.mkdir())
			throw new RuntimeException("Can not create a new directory");

		//Copy the relational module into the tmp directory
		try {
			Files.copy( relationalPropModuleOriginal.toPath(), 
					(new File(tmpDirectory,relationalPropModuleOriginal.getName())).toPath());
		} catch (IOException e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Unable to copy: "+relationalPropModuleOriginal.getAbsolutePath(), e);
			throw e;
		}

	}


	public void generateAlloyProcessingParams(final GeneratedStorage<AlloyProcessingParam> generatedStorage) throws Err, IOException{

		setUpFolders();

		Map<String, Pair<String, String>> tripleProps = builder.getAllProperties();
		logger.info("["+Thread.currentThread().getName()+"] " + tripleProps.size()+ " properties are generated.");

		try {
			generateRelationChekers(tripleProps, generatedStorage);
			logger.info("["+Thread.currentThread().getName()+"] " +generatedStorage.getSize()+" files are generated to be checked." );
		} catch (Err e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Unable to generate alloy files: ", e);
			throw e;		
		}

	}

	public static File getDest(File src){
		String name = src.getName();
		return new File(logOutputDir,name+".out.txt");
	}

}
