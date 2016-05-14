package edu.uw.ece.alloy.debugger.propgen.tripletemporal.website;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import edu.mit.csail.sdg.alloy4.Pair;
import edu.uw.ece.alloy.Compressor;
import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.Dependency;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.PropertyToAlloyCode;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.PropertyToAlloyCodeBuilder;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.TripleBuilder;
import edu.uw.ece.alloy.util.Utils;

public class GeneratorAllPropsToFiles {

	public final static File outputDir = new File("web");
	final TripleBuilder builder;
	final PropertyToAlloyCodeBuilder propertyBuilder;;

	public GeneratorAllPropsToFiles() {
		if (outputDir.exists())
			outputDir.delete();
		outputDir.mkdir();

		builder = new TripleBuilder("r", "s", "s_next", "s_first", "m", "m_next",
				"m_first", "e", "e_next", "e_first",

				"r", "S", "so/next", "so/first", "M", "E", "eo/next", "eo/first",
				"mo/next", "mo/first");

		final List<Dependency> dependencies = new LinkedList<Dependency>();
		final File relationalPropModuleOriginal = new File(
				Configuration.getProp("relational_properties"));

		dependencies.add(Dependency.EMPTY_DEPENDENCY.createIt(
				new File(relationalPropModuleOriginal.getName()),
				Utils.readFile(relationalPropModuleOriginal.getAbsolutePath())));

		propertyBuilder = new PropertyToAlloyCodeBuilder(dependencies,
				/* TemporalPropertiesGenerator.Header */ "open relational_properties\n",
				""/*, AlloyProcessingParam.EMPTY_PARAM.changeTmpLocalDirectory(outputDir)*/// [tmpDirectory],tmpDirectory
		);

		propertyBuilder
				.registerPropertyToAlloyCode(SinglePropertyToAlloyCode.EMPTY_CONVERTOR);
	}

	public void genPropsAsHTMLControls() {

		final String select = "<label for='__N_A_M_E__'>__N_A_M_E__:</label>\n"
				+ "<select name='__N_A_M_E__' id='__N_A_M_E__'>\n" + "__O_P_T_I_O_N_S__"
				+ "</select>";
		final String option = "\t<option>__O_P_T_I_O_N__</option>\n";

		String result = "";

		Map<String, Set<String>> features = builder.getAllFeatureNames();

		for (final String feature : features.keySet()) {
			String options = "";

			// System.out.println(feature);

			for (final String featureName : features.get(feature)) {
				options += option.replaceAll("__O_P_T_I_O_N__", featureName);
			}
			result += select.replaceAll("__N_A_M_E__", feature)
					.replaceAll("__O_P_T_I_O_N_S__", options);
		}

		System.out.println(result);

	}

	public void genPropsBodyAasHTML() {

		Map<String, Pair<String, String>> props = builder.getAllProperties();

		for (String pred : props.keySet()) {
			for (final PropertyToAlloyCode alloyCodeGenerator : propertyBuilder
					.createObjects(props.get(pred).b, Compressor.EMPTY_STRING,
							props.get(pred).a, Compressor.EMPTY_STRING, pred,
							Compressor.EMPTY_STRING// [tmpDirectory], tmpDirectory
							, Compressor.EMPTY_STRING)) {

				try {
					final AlloyProcessingParam generatedParam =
							new AlloyProcessingParam(UUID.randomUUID(), 0, alloyCodeGenerator);
					generatedParam.dumpContent();
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}

			}
		}

		Map<String, Set<String>> features = builder.getAllFeatureNames();
		System.out.println(features);

	}

	public static void main(String[] args) {

		GeneratorAllPropsToFiles allPropsToFiles = new GeneratorAllPropsToFiles();
		allPropsToFiles.genPropsBodyAasHTML();

		// allPropsToFiles.genPropsAsHTMLControls();

	}

}
