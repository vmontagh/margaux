package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.gen.BenchmarkRunner;
import edu.mit.csail.sdg.gen.LoggerUtil;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.RelationalPropertiesExecuterJob;
import edu.uw.ece.alloy.debugger.exec.RelationalPropertiesAnalyzer;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ProcessRemoteMonitor;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.TripleBuilder;
import edu.uw.ece.alloy.util.Utils;


public class TemporalPropertiesAnalyzer {

	/*
	 * Only the file names in the path are processed. 
	 */
	final static String filterPath = Configuration.getProp("processing_filter");

	final static boolean isResumable = Boolean.valueOf(Configuration.getProp("is_resume_processing"));

	final static int PropertiesMax = Integer.valueOf(Configuration.getProp("generated_properties"));

	final static Logger logger = Logger.getLogger(TemporalPropertiesAnalyzer.class.getName()+"--"+Thread.currentThread().getName());

	final File resourcesDir = new File( Configuration.getProp("models_directory") );

	final File logOutputDir = new File(Configuration.getProp("log_out_directory"));
	//final File logOutputFile = new File(logOutputDir, "log"+new SimpleDateFormat("_yyyy-MM-dd--HH-mm-ss-SSS").format(new Date())+".log");

	final File workingDir = new File( Configuration.getProp("working_directory") );
	//final File tmpDirectory = new File(workingDir, Configuration.getProp("temporary_directory") );
	final File tmpDirectory = new File( Configuration.getProp("temporary_directory") );


	//final File relationalPropModuleOriginal = new File( resourcesDir, "relational_properties.als");
	final File relationalPropModuleOriginal = new File( Configuration.getProp("relational_properties") );


	final Boolean doVAC = Boolean.valueOf( Configuration.getProp("doVAC") );
	final Boolean doIFF = Boolean.valueOf( Configuration.getProp("doIFF") );
	final Boolean doIMPLY = Boolean.valueOf( Configuration.getProp("doIMPLY") );
	final Boolean doAND = Boolean.valueOf( Configuration.getProp("doAND") );

	public TemporalPropertiesAnalyzer() {
	}
/*
	public static class SimpleAsynchFileWriter {
		final ConcurrentLinkedQueue<Pair<File,String>> logQueue = new ConcurrentLinkedQueue<>();

		final private File outputFile;
		private boolean stop;

		final void log(File destFile, String content){
			logQueue.add(new Pair(destFile, content));
		}

		final void log( String content){
			if(outputFile == null) throw new RuntimeException("The output loog is not set.");
			logQueue.add(new Pair(outputFile, content));
		}

		public SimpleAsynchFileWriter(){
			this(null, Thread.MIN_PRIORITY);
		}

		public SimpleAsynchFileWriter(final int priority){
			this(null, priority);
		}

		private void stop(){
			stop = true;
		}

		public SimpleAsynchFileWriter(final File destFile, final int priority){
			outputFile = destFile;
			stop = false;
			Thread t = new Thread(new Runnable() {
				public void run() {
					while(true){
						if(!logQueue.isEmpty()){
							final Pair<File, String> p = logQueue.poll();
							try {
								Util.writeAll(p.a.getAbsolutePath(), p.b);
							} catch (Err e) {
								e.printStackTrace();
							}
						}else{
							//no more String is in the queue
							if(stop) break;
						}
					}
				}
			});


			t.setPriority(priority);
			t.start();		}
	}
*/

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


	private  List<File> generateRelationChekers(final File destFolder) throws Err{

		//final SimpleAsynchFileWriter safWriter = new SimpleAsynchFileWriter(); 

		final Set<String> filterNames = filterFileNames();

		List<File> result = new LinkedList<File>();

		final String SigDecl = "sig M,E{}\nsig S{r:M->E}";
		final String ModuleS = "open util/ordering [S] as so";
		final String ModuleM = "open util/ordering [M] as mo";
		final String ModuleE = "open util/ordering [E] as eo";
		final String RelationProps = "open relational_properties";
		final String Scope = "for 5";

		final TripleBuilder builder = new TripleBuilder(
				"r", "s", "s_next", "s_first",
				"m", "m_next", "m_first", 
				"e", "e_next", "e_first",

				"r", "S", "so/next", "so/first", 
				"M", 
				"E", "eo/next", "eo/first", 
				"mo/next", "mo/first");

		Map<String, Pair<String, String>> tripleProps = builder.getAllProperties();

		logger.info("["+Thread.currentThread().getName()+"] " + tripleProps.size()+ " properties are generated.");
		
		tripleProps.keySet().stream().forEach(p->System.out.println(p));
		
		System.exit(-10);
		//done is kind of like result, but presumly smaller size and only comare the name.
		Set<String> doneNames = new HashSet<>();

		for(String pred1: tripleProps.keySet()){
			for(String pred2: tripleProps.keySet()){
				if(pred1.equals(pred2)) continue;
				//Making the source
				String source = RelationProps;
				source += "\n" +  ModuleS;
				source += "\n" +  ModuleM;
				source += "\n" +  ModuleE;
				source += "\n" +  SigDecl;

				source += "\n" + tripleProps.get(pred1).b;
				source += "\n" + tripleProps.get(pred2).b;

				final String implyCheck = source + "\n check {"+tripleProps.get(pred1).a+" implies "+tripleProps.get(pred2).a+" } "+Scope;
				final String iffCheck = source + "\n check {"+tripleProps.get(pred1).a+" <=> "+tripleProps.get(pred2).a+" } "+Scope;
				final String andCheck   = source + "\n run   {"+tripleProps.get(pred1).a+" and "+tripleProps.get(pred2).a+" } "+Scope;
				final String vacucityCheck   = source + "\n run   {some r and "+tripleProps.get(pred1).a+" } "+Scope;

				final String implyFileName = pred1+"_IMPLY_"+pred2+".als";
				final String iffFileName = pred1+"_IFF_"+pred2+".als";
				final String reviffFileName = pred2+"_IFF_"+pred1+".als";
				final String andFileName = pred1+"_AND_"+pred2+".als";
				final String revAndFileName = pred2+"_AND_"+pred1+".als";
				final String vacFileName = pred1+"_VAC_"+pred1+".als";

				if(doIMPLY){
					//implication predicate generator
					if( filterNames.isEmpty() || filterNames.contains(implyFileName) ){
						final File implyFilePath = new File(destFolder, implyFileName);
						if(!isResumable || !getDest(implyFilePath).exists()){
							Util.writeAll(implyFilePath.getAbsolutePath(), implyCheck);
							result.add(implyFilePath);
							logger.info("["+Thread.currentThread().getName()+"] " +implyFilePath.getName() +" is created.");
						}else{
							logger.info("["+Thread.currentThread().getName()+"] " +implyFilePath.getName() +" is resumable ans is already solved..");
						}
						if(PropertiesMax > 0 &&  result.size() > PropertiesMax) break;
					}
				}

				if(doIFF){
					//Equisatisfiability predicate generator
					if(/*(!(doneNames.contains(iffFileName) || doneNames.contains(reviffFileName))) &&*/  (filterNames.isEmpty() || filterNames.contains(iffFileName) ) ){
						final File iffFilePath = new File(destFolder, iffFileName);
						if(!isResumable || !getDest(iffFilePath).exists()){
							//doneNames.add(iffFileName);
							Util.writeAll(iffFilePath.getAbsolutePath(), iffCheck);
							result.add(iffFilePath);
							logger.info("["+Thread.currentThread().getName()+"] " +iffFilePath.getAbsolutePath() +" is created.");
						}else{
							logger.info("["+Thread.currentThread().getName()+"] " +iffFilePath.getName() +" is resumable ans is already solved..");
						}
						if(PropertiesMax > 0 &&  result.size() > PropertiesMax) break;
					}
				}

				if(doVAC){
					//vacucity predicate generator
					if(!doneNames.contains(vacFileName) &&  (filterNames.isEmpty() || filterNames.contains(vacFileName)) ){
						final File vacFilePath = new File(destFolder, vacFileName);
						if(!isResumable || !getDest(vacFilePath).exists() ){
							doneNames.add(vacFileName);
							Util.writeAll(vacFilePath.getAbsolutePath(), vacucityCheck);
							result.add(vacFilePath);
							logger.info("["+Thread.currentThread().getName()+"] " +vacFilePath.getName() +" is created.");
						}else{
							logger.info("["+Thread.currentThread().getName()+"] " +vacFilePath.getName() +" is resumable ans is already solved..");
						}
						if(PropertiesMax > 0 &&  result.size() > PropertiesMax) break;
					}
				}



				if(doAND){
					//Unsatisfiability predicate generator
					if( (!(doneNames.contains(andFileName) || doneNames.contains(revAndFileName))) &&  (filterNames.isEmpty() || filterNames.contains(andFileName)) ){
						final File andFilePath = new File(destFolder, andFileName);
						if(!isResumable || !getDest(andFilePath).exists()){
							doneNames.add(andFileName);
							Util.writeAll(andFilePath.getAbsolutePath(), andCheck);
							result.add(andFilePath);
							logger.info("["+Thread.currentThread().getName()+"] " +andFilePath.getAbsolutePath() +" is created.");
						}else{
							logger.info("["+Thread.currentThread().getName()+"] " +andFilePath.getName() +" is resumable ans is already solved..");
						}
						if(PropertiesMax > 0 &&  result.size() > PropertiesMax) break;
					}
				}
			}

		}

		System.out.println(result.size());

		return Collections.unmodifiableList(result);

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


	public List<File> generateFiles() throws Err, IOException{

		setUpFolders();

		List<File> propCheckingFiles;
		try {
			propCheckingFiles = generateRelationChekers(tmpDirectory);
			logger.info("["+Thread.currentThread().getName()+"] " +propCheckingFiles.size()+" files are generated to be checked." );
		} catch (Err e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "UUnable to generate alloy files: ", e);
			throw e;		
		}
		return propCheckingFiles;
	}

	public File getDest(File src){
		String name = src.getName();
		return new File(logOutputDir,name+".out.txt");
	}

	public List<AlloyProcessingParam> generateRemoteFiles() throws Err, IOException{
		List<AlloyProcessingParam> result = new LinkedList<AlloyProcessingParam>();
		for(File src: generateFiles() ){
			result.add(new AlloyProcessingParam(src, getDest(src), 1));
		}
		return Collections.unmodifiableList(result);
	}


	public static void main(String ... args) throws Exception{


		System.out.print( (new TemporalPropertiesAnalyzer()). generateFiles());


	}

}
