package edu.uw.ece.alloy.debugger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;

/**
 * This class only generates output files to be checked.
 * @author vajih
 *
 */
public class PropertiesConsistencyChecking extends PropertyCheckingSource {

	static Map<String, String> props = new HashMap<String, String>();
	final String name;
	final String notName;

	public PropertiesConsistencyChecking(File sourceFile_, String property_,
			String fieldName_, Set<String> binaryProperties_,
			Set<String> ternaryProperties_, String sigs_, String openModule_,
			String openStatements_, String functions_, String commandHeader_,
			String formula_, String commandScope_) {
		super(sourceFile_, property_, fieldName_, binaryProperties_,
				ternaryProperties_, sigs_, openModule_, openStatements_,
				functions_, commandHeader_, formula_, commandScope_);

		name = String.format("%s%s%s", sanitizedPropertyName, SEPARATOR , sanitizedFieldName);
		notName = String.format("_N_o_T_%s", name);

		props.put(name, property);
		props.put(notName, String.format("(not %s)", property) );

	}



	@Override
	protected String makeNewFileName() {
		throw new RuntimeException("Invalid call");
	}

	@Override
	protected String getNewStatement() {
		throw new RuntimeException("Invalid call");
	}

	public String toAlloy(){
		throw new RuntimeException("Invalid call");
	}

	public List<File> toAlloyFile(final File destFolder) {
		List<File> files = new ArrayList<File>();

		for(String current: Arrays.asList(name,notName)){
			for(String key: props.keySet() ){
				if( !key.equals(current)   ){	
					
					String empty = "";
					if ( !(current.contains("empty") || key.contains("empty")) )
						empty = " and " + emptyProperty ;
					
					final String pred = String.format("pred %1$s{\n %2$s  and %3$s %4$s}\nrun %1$s %5$s", key, props.get(current), props.get(key), empty ,commandScope);

					final StringBuilder newAlloySpec = new StringBuilder();
					newAlloySpec.append(openStatements);
					newAlloySpec.append("\n").append(openModule);
					newAlloySpec.append("\n").append(sigs);
					newAlloySpec.append("\n").append(functions);
					newAlloySpec.append("\n").append(pred);

					final String fileName = String.format("%s_%s.als", current,key);
					
					final File file = new File( destFolder, fileName ); 

					try {
						Util.writeAll(  file.getAbsolutePath(), newAlloySpec.toString());
						files.add(file);
					} catch (Err e) {
						e.printStackTrace();
						throw new RuntimeException( String.format( "Output file could be created: %s\n", e.getMessage() ));
					}

				}

			}
		}
		
		return Collections.unmodifiableList(files);
	}

}
