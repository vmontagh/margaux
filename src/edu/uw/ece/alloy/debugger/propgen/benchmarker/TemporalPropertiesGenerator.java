package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.communication.Queue;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.TripleBuilder;
import edu.uw.ece.alloy.util.Utils;

public class TemporalPropertiesGenerator
		implements Runnable, ThreadToBeMonitored {

	/*
	 * Only the file names in the path are processed.
	 */
	final static String filterPath = Configuration.getProp("processing_filter");

	final static boolean isResumable = Boolean
			.valueOf(Configuration.getProp("is_resume_processing"));

	final static int PropertiesMin = Integer
			.valueOf(Configuration.getProp("generated_properties_min_start"));
	final static int PropertiesMax = Integer
			.valueOf(Configuration.getProp("generated_properties_max_end"));

	static {
		if (PropertiesMin >= PropertiesMax)
			throw new RuntimeException("PropertiesMin: " + PropertiesMin
					+ " has to be less than PropertiesMax: " + PropertiesMax);
	}

	final public static boolean doCompress = Boolean
			.valueOf(Configuration.getProp("doCompressAlloyParams"));

	final static Logger logger = Logger
			.getLogger(TemporalPropertiesGenerator.class.getName() + "--"
					+ Thread.currentThread().getName());

	final File resourcesDir = new File(Configuration.getProp("models_directory"));

	final static File logOutputDir = new File(
			Configuration.getProp("log_out_directory"));
	// final File logOutputFile = new File(logOutputDir, "log"+new
	// SimpleDateFormat("_yyyy-MM-dd--HH-mm-ss-SSS").format(new Date())+".log");

	final File workingDir = new File(Configuration.getProp("working_directory"));
	// final File tmpDirectory = new File(workingDir,
	// Configuration.getProp("temporary_directory") );

	// final File relationalPropModuleOriginal = new File( resourcesDir,
	// "relational_properties.als");
	final public static File relationalPropModuleOriginal = new File(
			Configuration.getProp("relational_properties"));

	final Queue<AlloyProcessingParam> generatedStorage;

	final Boolean doVAC = Boolean.valueOf(Configuration.getProp("doVAC"));
	final Boolean doIFF = Boolean.valueOf(Configuration.getProp("doIFF"));
	final Boolean doIMPLY = Boolean.valueOf(Configuration.getProp("doIMPLY"));
	final Boolean doAND = Boolean.valueOf(Configuration.getProp("doAND"));

	final static String SigDecl = "sig M,E{}\nsig S{r:M->E}";
	final static String ModuleS = "open util/ordering [S] as so";
	final static String ModuleM = "open util/ordering [M] as mo";
	final static String ModuleE = "open util/ordering [E] as eo";
	final static String RelationProps = "open relational_properties";
	final public static String Header = ModuleS + '\n' + ModuleM + '\n' + ModuleE
			+ '\n' + RelationProps + '\n' + SigDecl + '\n';
	final static String Scope = " for 5";

	final AlloyProcessingParam paramCreator;

	final static List<Dependency> dependencies = new LinkedList<Dependency>();

	final PropertyToAlloyCodeBuilder propertyBuilder;;
	final TripleBuilder builder;

	final Thread generator = new Thread(this);

	public TemporalPropertiesGenerator() {
		// A synchronized list is returned and the alloyfeeder is not called
		// directly.
		this(new Queue<AlloyProcessingParam>());
	}

	public TemporalPropertiesGenerator(
			final Queue<AlloyProcessingParam> generatedStorage) {
		builder = new TripleBuilder("r", "s", "s_next", "s_first", "m", "m_next",
				"m_first", "e", "e_next", "e_first",

				// Concretes are not required.
				"r", "S", "so/next", "so/first", "M", "E", "eo/next", "eo/first",
				"mo/next", "mo/first");

		if (doCompress) {
			paramCreator = AlloyProcessingParamLazyCompressing.EMPTY_PARAM;
		} else {
			paramCreator = AlloyProcessingParam.EMPTY_PARAM;
		}

		propertyBuilder = new PropertyToAlloyCodeBuilder(dependencies, Header,
				Scope, paramCreator// [tmpDirectory],tmpDirectory
		);

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

		dependencies
				.add(new Dependency(new File(relationalPropModuleOriginal.getName()),
						Utils.readFile(relationalPropModuleOriginal.getAbsolutePath())));
		// Some sort of hacking. The content of the dependency is the path to the
		// original file. So it just need to to copy it instead of carry the content
		// per every request param.
		// dependencies.add(new Dependency(new File(
		// relationalPropModuleOriginal.getName()),
		// relationalPropModuleOriginal.getAbsolutePath()));

		this.generatedStorage = generatedStorage;

	}

	private /* Set<String> */Set<Integer> filterFileNames() {
		final Set<Integer> fileNames = new TreeSet<>();
		final File file = new File(filterPath);
		if (file.exists()) {
			Utils.readFile(file, new Consumer<List<String>>() {
				@Override
				public void accept(List<String> t) {
					assert t.size() == 1;
					String name = t.get(0);

					fileNames.add(name.hashCode());
				}
			});
		}

		return Collections.unmodifiableSet(fileNames);
	}

	void generateRelationChekers(
			final Map<String, Pair<String, String>> tripleProps,
			Queue<AlloyProcessingParam> result) throws Err {

		// final SimpleAsynchFileWriter safWriter = new SimpleAsynchFileWriter();

		final /* Set<String> */ Set<Integer> filterNames = filterFileNames();

		// List<File> result = new LinkedList<File>();

		// done is kind of like result, but presumely smaller size and only compare
		// the name.
		// Set<String> doneNames = new TreeSet<>();
		Set<Integer> doneHashedNames = new TreeSet<>();

		int generatedCount = 0;
		boolean breakFromAll = false;
		for (String pred1 : tripleProps.keySet()) {
			for (String pred2 : tripleProps.keySet()) {
				if (pred1.equals(pred2))
					continue;

				for (final PropertyToAlloyCode alloyCodeGenerator : propertyBuilder
						.createObjects(tripleProps.get(pred1).b, tripleProps.get(pred2).b,
								tripleProps.get(pred1).a, tripleProps.get(pred2).a, pred1,
								pred2, "r"/* Field name */// [tmpDirectory], tmpDirectory
				)) {

					// if(doneNames.contains(alloyCodeGenerator.srcName())) continue;
					if (doneHashedNames.contains(alloyCodeGenerator.srcName().hashCode()))
						continue;
					// if(filterNames.contains(alloyCodeGenerator.srcName())) continue;
					if (filterNames.contains(alloyCodeGenerator.srcName().hashCode()))
						continue;

					if (!isResumable /*
													  * && Some condition has to be put here to skip the
													  * current property
													  */) {
						if (Configuration.IsInDeubbungMode)
							logger.info("[" + Thread.currentThread().getName() + "] "
									+ alloyCodeGenerator.srcName() + " is created.");
					} else {
						if (Configuration.IsInDeubbungMode)
							logger.info("[" + Thread.currentThread().getName() + "] "
									+ alloyCodeGenerator.srcName()
									+ " is resumable ans is already solved..");
					}

					generatedCount++;

					if (generatedCount >= PropertiesMin
							&& generatedCount < PropertiesMax) {
						try {
							final AlloyProcessingParam generatedParam = alloyCodeGenerator
									.generate(UUID.randomUUID());

							result.put(generatedParam);
						} catch (Exception e) {
							logger.log(Level.SEVERE, "[" + Thread.currentThread().getName()
									+ "] " + "Property code generation is failed:", e);
							e.printStackTrace();
							continue;
						}
					} else {
						breakFromAll = true;
						break;
					}

					if (alloyCodeGenerator.isSymmetric()) {
						// doneNames.add(propertyBuilder.createReverse(alloyCodeGenerator).srcName());
						doneHashedNames.add(propertyBuilder
								.createReverse(alloyCodeGenerator).srcName().hashCode());
					}

					// doneNames.add(alloyCodeGenerator.srcName());
					doneHashedNames.add(alloyCodeGenerator.srcName().hashCode());
				}
				if (breakFromAll)
					break;
			}
			if (breakFromAll)
				break;
		}

		if (result.size() != (PropertiesMax - PropertiesMin)) {
			logger.log(Level.WARNING,
					"[" + Thread.currentThread().getName() + "] "
							+ "The generated and stored properties are: " + result.size()
							+ " But it was expecpted to have (PropertiesMax=" + PropertiesMax
							+ "-PropertiesMin=" + PropertiesMin + ")="
							+ (PropertiesMax - PropertiesMin));
		}

		// doneNames.clear();
		doneHashedNames.clear();

		if (Configuration.IsInDeubbungMode)
			logger.info("[" + Thread.currentThread().getName() + "] " + result.size()
					+ " properties are generated: " + generatedCount);

	}

	private void setUpFolders() throws Err, IOException {

		if (!workingDir.exists()) {
			workingDir.mkdir();
			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "] "
						+ "Working directory is created: " + workingDir.getAbsolutePath());
		}

	}

	public void generateAlloyProcessingParams() throws Err, IOException {
		this.generateAlloyProcessingParams(this.generatedStorage);
	}

	public void generateAlloyProcessingParams(
			final Queue<AlloyProcessingParam> generatedStorage)
					throws Err, IOException {

		setUpFolders();

		Map<String, Pair<String, String>> tripleProps = builder.getAllProperties();
		if (Configuration.IsInDeubbungMode)
			logger.info("[" + Thread.currentThread().getName() + "] "
					+ tripleProps.size() + " properties are generated.");

		try {
			generateRelationChekers(tripleProps, generatedStorage);
			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "] "
						+ generatedStorage.size() + " files are generated to be checked.");
		} catch (Err e) {
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
					+ "Unable to generate alloy files: ", e);
			throw e;
		}

	}

	public static File getDest(File src) {
		String name = src.getName();
		return new File(logOutputDir, name + ".out.txt");
	}

	@Override
	public void run() {
		// A finite thread that fill the storage with the generated properties, then
		// finish.
		try {
			generateAlloyProcessingParams();
		} catch (Err | IOException e1) {
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
					+ "Unable to generate alloy files: ", e1);
			throw new RuntimeException(e1);
		}
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
		return null;
	}

	@Override
	public long isDelayed() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getStatus() {
		return "Not sure what is happening in TemporalPropertiesGenerator!";
	}
}
