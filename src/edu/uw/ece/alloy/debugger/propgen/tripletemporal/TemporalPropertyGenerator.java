/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

import java.io.File;
import java.util.Map;
import java.util.Set;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Util;
import edu.uw.ece.alloy.debugger.PropertyDeclaration;

/**
 * GEnerating Properties and store them in a single file.
 * 
 * @author vajih
 *
 */
public class TemporalPropertyGenerator {

	final public File relationalPropModuleOriginal;
	final public File temporalPropModuleOriginal;

	public TemporalPropertyGenerator(File relationalPropModuleOriginal,
			File temporalPropModuleOriginal) {
		this.relationalPropModuleOriginal = relationalPropModuleOriginal;
		this.temporalPropModuleOriginal = temporalPropModuleOriginal;
	}

	public static void generateAndStoreAllTemporalProps(
			File relationalPropModuleOriginal, File temporalPropModuleOriginal)
					throws Err {
		Map<String, Pair<String, String>> tripleProps = makeTemporalBuilder().getAllPropertiesNamesAndContent();
		StringBuilder sbPropertyDeclarations = new StringBuilder();
		final String relationalOpenStatement = "open "
				+ relationalPropModuleOriginal.getName().replace(".als",
						" as relational_properties");
		sbPropertyDeclarations.append(relationalOpenStatement).append("\n");
		for (String key : tripleProps.keySet()) {
			sbPropertyDeclarations.append(tripleProps.get(key).b).append("\n");
		}

		Util.writeAll(temporalPropModuleOriginal.getAbsolutePath(),
				sbPropertyDeclarations.toString());
	}
	
	protected static TripleBuilder makeTemporalBuilder()
			throws Err {
		// Generate all the temporal properties and store them in a file.
		// The file is read then, and functions are extracted from.
		final TripleBuilder builder = new TripleBuilder("r",
				PropertyDeclaration.LEFT_LABEL, PropertyDeclaration.LEFT_NEXT_LABEL,
				PropertyDeclaration.LEFT_FIRST_LABEL, PropertyDeclaration.MIDDLE_LABEL,
				PropertyDeclaration.MIDDLE_NEXT_LABEL,
				PropertyDeclaration.MIDDLE_FIRST_LABEL, PropertyDeclaration.RIGHT_LABEL,
				PropertyDeclaration.RIGHT_NEXT_LABEL,
				PropertyDeclaration.RIGHT_FIRST_LABEL,

				// Concretes are not required.
				"NONE", "NODE", "NONE", "NONE", "NONE", "NONE", "NONE", "NONE", "NONE",
				"NONE");

		return builder;
	}
	
	public static Map<String, Integer> generateAllPropertiesPiority() throws Err{
		return makeTemporalBuilder().getAllPropertiesPriorities();
	}


	public void generateAndStoreAllTemporalProps() throws Err {
		generateAndStoreAllTemporalProps(this.relationalPropModuleOriginal,
				this.temporalPropModuleOriginal);
	}
	
	public static void main(String...args) throws Err{
		Map<String, Integer> map = generateAllPropertiesPiority();
		map.keySet().stream()
        .sorted((e1,e2)->map.get(e1) - map.get(e2)) 
        .forEach(e->System.out.println(e+" " + map.get(e)));
	}

}
