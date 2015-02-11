/**
 * 
 */
package edu.uw.ece.alloy.debugger.exec;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardCopyOption.*;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.gen.BenchmarkRunner;
import edu.mit.csail.sdg.gen.LoggerUtil;
import edu.uw.ece.alloy.debugger.RelationalPropertiesExecuterJob;
import edu.uw.ece.alloy.util.Utils;


/**
 * RelationalPropertiesAnalyzer uses RelationalPropertiesChecker to generate the checking alloy specs
 * then uses BenchmarkRunner to run them
 * @author vajih
 *
 */
public final class RelationalPropertiesAnalyzer {

	/**
	 * Before executing this main `relational_props' has to be in path and includes
	 *  props.ini and relational_properties.als. These files in `models/debugger/models2015'
	 * @param args
	 * @throws IOException 
	 * @throws Err 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, Err, InterruptedException {
		
		final File resourcesDir = new File( "models/debugger/models2015");

		final File logOutput = new File("expr_output");
		
		final File workingDir = new File( "relational_props");
		final File tmpDirectory = new File(workingDir, "tmp");
		
		final File relationalPropIniOriginal = new File( resourcesDir, "props.ini");
		final File relationalPropModuleOriginal = new File( resourcesDir, "relational_properties.als");
		
		if( !workingDir.exists() )
			workingDir.mkdir();
		
		//The ini file will be copied into the working directory.
		Files.copy( relationalPropIniOriginal.toPath() ,
						(new File(workingDir, relationalPropIniOriginal.getName())).toPath() ,
							REPLACE_EXISTING);
		
		
		if(tmpDirectory.exists()){
			
			LoggerUtil.debug(RelationalPropertiesAnalyzer.class,"%s exists and has to be recreated.", tmpDirectory.getCanonicalPath());
			
			Utils.deleteRecursivly(tmpDirectory);
			
		}
		
		//After deleting the temp directory create a new one.
		if (!tmpDirectory.mkdir())
			throw new RuntimeException("Can not create a new directory");
		
		//Copy the relational module into the tmp directory
		Files.copy( relationalPropModuleOriginal.toPath(), 
						(new File(tmpDirectory,relationalPropModuleOriginal.getName())).toPath());

		
		for(String file: args){
			RelationalPropertiesChecker propertiesChecker = (new RelationalPropertiesChecker(
																	(new File(workingDir, relationalPropIniOriginal.getName()) ),
																			new File(file),
																					(new File(tmpDirectory, relationalPropModuleOriginal.getName()) )))
																					.replacingCheckAndAsserts();
			
			try {
				List<File> propCheckingFiles = propertiesChecker.transformForChecking(tmpDirectory);
				
				System.out.printf("%d fiels are enerated to be checked.",propCheckingFiles.size());
				
				BenchmarkRunner.getInstance().doTest(new RelationalPropertiesExecuterJob(""), 1, 0.6, propCheckingFiles, logOutput);
			} catch (Err | IOException | InterruptedException e) {
				System.err.printf("%s Failed to be checked.%n", file);
				e.printStackTrace();
			}
			
			
			
			
			
			//aggregated the output logs
			final String specName =  propertiesChecker.alloySepcFileName.getName().replace(".als", "") ;
			
			final Map<String, String> replaceMapping = new HashMap();

			replaceMapping.put("/Users/vajih/Documents/workspace-git/alloy/relational_props/tmp/", "");
			//replaceMapping.put("relational_properties_S_c_P_", "");
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
			replaceMapping.put("_"+specName, ","+specName.substring(0, specName.length()-1));
			
			final long timeStart  = System.currentTimeMillis();
			
			Utils.replaceTextFiles(logOutput, "(repo).*", specName, replaceMapping) ;
			
			System.out.println(System.currentTimeMillis() - timeStart);
			
			
			(Runtime.getRuntime().exec("bash " + (new File(logOutput, "move.sh")).getAbsolutePath())).waitFor();
		    
			
			
		}
		
		
		
		
		
	}

}
