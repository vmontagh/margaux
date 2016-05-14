package edu.uw.ece.alloy.debugger.exec;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.mit.csail.sdg.alloy4.Err;
import edu.uw.ece.alloy.BenchmarkRunner;
import edu.uw.ece.alloy.LoggerUtil;
import edu.uw.ece.alloy.debugger.RelationalPropertiesExecuterJob;
import edu.uw.ece.alloy.util.Utils;

public class RelationalPropertiesRelationChecker {

	public RelationalPropertiesRelationChecker() {
	}

	static void run(final String _s, final String sameType)
			throws IOException, Err, InterruptedException {
		final File resourcesDir = new File("models/debugger/models2015");

		final File logOutput = new File("expr_output");
		final File archivedLogOutput = new File(logOutput, "aggregated");

		final File workingDir = new File("relational_props");
		final File tmpDirectory = new File(workingDir, "tmp");

		final File relationalPropIniOriginal = new File(resourcesDir, "props.ini");
		final File relationalPropModuleOriginal = new File(resourcesDir,
				"relational_properties.als");

		if (!workingDir.exists())
			workingDir.mkdir();

		// The ini file will be copied into the working directory.
		Files.copy(relationalPropIniOriginal.toPath(),
				(new File(workingDir, relationalPropIniOriginal.getName())).toPath(),
				REPLACE_EXISTING);

		if (tmpDirectory.exists()) {

			LoggerUtil.debug(RelationalPropertiesAnalyzer.class,
					"%s exists and has to be recreated.",
					tmpDirectory.getCanonicalPath());

			Utils.deleteRecursivly(tmpDirectory);

		}

		// After deleting the temp directory create a new one.
		if (!tmpDirectory.mkdir())
			throw new RuntimeException("Can not create a new directory");

		// Copy the relational module into the tmp directory
		Files.copy(relationalPropModuleOriginal.toPath(),
				(new File(tmpDirectory, relationalPropModuleOriginal.getName()))
						.toPath());

		// make a dummy alloy file
		final File dummyFile = new File(tmpDirectory,
				"dummy_" + _s + "_" + sameType + ".als");
		final String dummyAlloy = sameType.toUpperCase().equals("SAMETYPE")
				? "sig A{r:A->A}\nrun {}" : "sig A,B{}\nsig C{r:A->B}\nrun {}";
		edu.mit.csail.sdg.alloy4.Util.writeAll(dummyFile.getAbsolutePath(),
				dummyAlloy);

		RelationalPropertiesChecker propertiesChecker = (new RelationalPropertiesChecker(
				(new File(workingDir, relationalPropIniOriginal.getName())), dummyFile,
				(new File(tmpDirectory, relationalPropModuleOriginal.getName()))))
						.replacingCheckAndAsserts();
		List<File> propCheckingFiles = null;

		try {
			if (_s.equals("and")) {
				propCheckingFiles = propertiesChecker
						.findInconsistentProperties(tmpDirectory);
			}
			if (_s.equals("imply")) {
				propCheckingFiles = propertiesChecker
						.findImplicationsProperties(tmpDirectory);
			}
			assert propCheckingFiles != null;
			System.out.printf("%d files are enerated to be checked.%n",
					propCheckingFiles.size());
			BenchmarkRunner.getInstance().doTest(
					new RelationalPropertiesExecuterJob(""), 1, 1, propCheckingFiles,
					logOutput);

		} catch (Err | IOException | InterruptedException e) {
			System.err.printf("%s Failed to be checked.%n", dummyFile);
			e.printStackTrace();
		}

		// System.exit(0);

		// aggregated the output logs
		final String specName = propertiesChecker.alloySepcFileName.getName()
				.replace(".als", "");

		final Map<String, String> replaceMapping = new HashMap<>();

		replaceMapping.put(tmpDirectory.getCanonicalPath() + File.separator, "");
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
		replaceMapping.put("_" + specName,
				"," + specName.substring(0, specName.length() - 1));

		final long timeStart = System.currentTimeMillis();

		Utils.replaceTextFiles(logOutput, "(repo).*", specName, replaceMapping);

		System.out.printf("%d files are aggregated in %d ms",
				propCheckingFiles.size(), (System.currentTimeMillis() - timeStart));

		try {

			Utils.moveFiles(Utils.files(logOutput.getAbsolutePath(), "^repo.*"),
					archivedLogOutput);
		} catch (Exception e) {
			System.out.println("moving failed " + e.getMessage());
		}
	}

	public static void main(String[] args)
			throws IOException, Err, InterruptedException {

		run("imply", "");
		run("imply", "SAMETYPE");
		run("and", "");
		run("and", "");
	}

}
