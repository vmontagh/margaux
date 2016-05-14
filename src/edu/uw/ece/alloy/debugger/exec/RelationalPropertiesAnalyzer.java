package edu.uw.ece.alloy.debugger.exec;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import edu.mit.csail.sdg.alloy4.Err;
import edu.uw.ece.alloy.BenchmarkRunner;
import edu.uw.ece.alloy.LoggerUtil;
import edu.uw.ece.alloy.debugger.RelationalPropertiesExecuterJob;
import edu.uw.ece.alloy.util.Utils;

/**
 * RelationalPropertiesAnalyzer uses RelationalPropertiesChecker to generate the
 * checking alloy specs then uses BenchmarkRunner to run them
 * 
 * @author vajih
 *
 */
public final class RelationalPropertiesAnalyzer {

	protected final static Logger logger = Logger
			.getLogger(RelationalPropertiesAnalyzer.class.getName() + "--"
					+ Thread.currentThread().getName());

	@Option(
			name = "-log-output-dir-path",
			usage = "The output log directory path. The default is 'expr_output'")
	private String logOutputDirPath = "expr_output";
	final File logOutput;

	@Option(
			name = "-archived-log-output-path",
			usage = "The output logs for archive. The default is 'expr_output/aggregated'")
	private String archivedLogOutputPath = "expr_output/aggregated";
	final File archivedLogOutput;

	@Option(
			name = "-working-dir-path",
			usage = "The working directory path. The default is 'relational_props'")
	private String workingDirPath = "relational_props";
	public final File workingDir;

	@Option(
			name = "-tmp-dir-path",
			usage = "The temporary directory. The default is 'relational_props/tmp'")
	private String tmpDirPath = "relational_props/tmp";
	public final File tmpDirectory;

	@Option(
			name = "-props_path",
			usage = "The propeties metadata path. The default is 'models/debugger/models2015/props.ini'")
	private String propsPath = "models/debugger/models2015/props.ini";
	public final File relationalPropIniOriginal;

	@Option(
			name = "-relational-lib-path",
			usage = "The relational properties path. The default is 'models/debugger/models2015/relational_properties.als'")
	private String relationalPropModuleOriginalPath = "models/debugger/models2015/relational_properties.als";
	public final File relationalPropModuleOriginal;

	// receives other command line parameters than options
	@Argument
	private List<String> arguments = new ArrayList<String>();
	public final List<String> files;

	RelationalPropertiesAnalyzer(String... args) {
		CmdLineParser parser = new CmdLineParser(this);

		try {
			// parse the arguments.
			parser.parseArgument(args);

			// after parsing arguments, you should check
			// if enough arguments are given.
			if (arguments.isEmpty()) {
				logger.warning("No argument is given as the file list.");
			}
		} catch (CmdLineException e) {
			// if there's a problem in the command line,
			// you'll get this exception. this will report
			logger.severe("Commands are invalid due to:\n" + e.getMessage());
			System.exit(1);
		}

		relationalPropIniOriginal = new File(propsPath);
		relationalPropModuleOriginal = new File(relationalPropModuleOriginalPath);
		workingDir = new File(workingDirPath);
		tmpDirectory = new File(tmpDirPath);
		logOutput = new File(logOutputDirPath);
		archivedLogOutput = new File(archivedLogOutputPath);
		files = Collections.unmodifiableList(arguments);
	}

	/**
	 * Before executing this main `relational_props' has to be in path and
	 * includes props.ini and relational_properties.als. These files in
	 * `models/debugger/models2015'
	 * 
	 * @param args
	 * @throws IOException
	 * @throws Err
	 * @throws InterruptedException
	 */
	public static void main(String[] args)
			throws IOException, Err, InterruptedException {

		RelationalPropertiesAnalyzer rpa = new RelationalPropertiesAnalyzer(args);

		if (!rpa.workingDir.exists())
			rpa.workingDir.mkdir();

		// The ini file will be copied into the working directory.
		Files.copy(rpa.relationalPropIniOriginal.toPath(),
				(new File(rpa.workingDir, rpa.relationalPropIniOriginal.getName()))
						.toPath(),
				REPLACE_EXISTING);

		if (rpa.tmpDirectory.exists()) {
			LoggerUtil.debug(RelationalPropertiesAnalyzer.class,
					"%s exists and has to be recreated.",
					rpa.tmpDirectory.getCanonicalPath());
			Utils.deleteRecursivly(rpa.tmpDirectory);
		}

		// After deleting the temp directory create a new one.
		if (!rpa.tmpDirectory.mkdir())
			throw new RuntimeException("Can not create a new directory");

		// Copy the relational module into the tmp directory
		Files.copy(rpa.relationalPropModuleOriginal.toPath(),
				(new File(rpa.tmpDirectory, rpa.relationalPropModuleOriginal.getName()))
						.toPath());

		for (String file : rpa.files) {
			RelationalPropertiesChecker propertiesChecker = (new RelationalPropertiesChecker(
					(new File(rpa.workingDir, rpa.relationalPropIniOriginal.getName())),
					new File(file),
					(new File(rpa.tmpDirectory,
							rpa.relationalPropModuleOriginal.getName()))))
									.replacingCheckAndAsserts();

			try {
				List<File> propCheckingFiles = propertiesChecker
						.transformForChecking(rpa.tmpDirectory);

				System.out.printf("%d files are enerated to be checked.",
						propCheckingFiles.size());

				BenchmarkRunner.getInstance().doTest(
						new RelationalPropertiesExecuterJob(""), 1, 0.6, propCheckingFiles,
						rpa.logOutput);
			} catch (Err | IOException | InterruptedException e) {
				System.err.printf("%s Failed to be checked.%n", file);
				e.printStackTrace();
			}

			// aggregated the output logs
			final String specName = propertiesChecker.alloySepcFileName.getName()
					.replace(".als", "");

			final Map<String, String> replaceMapping = new HashMap();

			replaceMapping.put(rpa.tmpDirectory.getCanonicalPath() + File.separator,
					"");
			// replaceMapping.put("relational_properties_S_c_P_", "");
			replaceMapping.put("_S_p_R_", ",");
			replaceMapping.put("_I__f_", "=>");
			replaceMapping.put("_F__i_", "<=");
			replaceMapping.put("_F__F_", "<=>");
			replaceMapping.put("_S_c_P_", ".");
			replaceMapping.put("_D_m_N_", "<:");
			replaceMapping.put("_D_o_T_", ".");
			replaceMapping.put("_tc.als", "");
			replaceMapping.put("_F_l_d_", ",");
			replaceMapping.put("_A_n_D_", "&&");
			replaceMapping.put("_U_n_N_", "+");
			replaceMapping.put("_D_i_F_", "-");
			replaceMapping.put("_I_t_S_", "&");
			replaceMapping.put("_" + specName,
					"," + specName.substring(0, specName.length() - 1));

			final long timeStart = System.currentTimeMillis();

			Utils.replaceTextFiles(rpa.logOutput, "(repo).*", specName,
					replaceMapping);

			System.out.println(System.currentTimeMillis() - timeStart);

			(Runtime.getRuntime().exec(
					"bash " + (new File(rpa.logOutput, "move.sh")).getAbsolutePath()))
							.waitFor();
			try {

				// Utils.moveFiles(Utils.files(logOutput.getAbsolutePath(), "^repo.*"),
				// archivedLogOutput);
			} catch (Exception e) {
				System.out.println("moving failed " + e.getMessage());
			}
		}

	}
}
