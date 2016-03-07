package edu.uw.ece.alloy.debugger.knowledgebase;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.uw.ece.alloy.debugger.exec.A4CommandExecuter;

/**
 * The class returns all necessary info properties from 
 * implication lattice.
 * @author vajih
 *
 */

public abstract class ImplicationLattic {

	// a folder for temporary alloy codes.
	// it should contains the related alloy modules
	final String TEMPORARY_FOLDER;
	final String[] MODLUE_NAMES;
	final static Random rand = new Random();
	
	protected final static Logger logger = Logger.getLogger(ImplicationLattic.class.getName()+"--"+Thread.currentThread().getName());
	
	public ImplicationLattic(String tempPath, String[] moduleName) {
		this.TEMPORARY_FOLDER = tempPath;
		this.MODLUE_NAMES = Arrays.copyOf(moduleName, moduleName.length);
		
		// TEMPORARY_FOLDER has to exist
		if( !(new File(this.TEMPORARY_FOLDER)).exists() )
			throw new RuntimeException(this.TEMPORARY_FOLDER + "does not exists");
		
		// Modules have to exists in the temp folder
		for (String name: this.MODLUE_NAMES){
			File pathToModule = new File(this.TEMPORARY_FOLDER, name);
			if( !pathToModule.exists() )
				throw new RuntimeException(pathToModule.getAbsolutePath() + "does not exists");
		}
		
	}

	protected List<A4Solution> getAllproperties(String path) throws Err{
		
		Map<Command, A4Solution> executionResult = 
				A4CommandExecuter.getInstance().runThenGetAnswers(new String[]{path}, A4Reporter.NOP);
		
		if (executionResult.size() != 1)
			throw new RuntimeException("There hsa to be one command be executed");
		
		A4Solution solution = executionResult.values().iterator().next();
		List<A4Solution> result = new LinkedList<>();
		try {
			while(solution.satisfiable()){
				result.add(solution);
				solution = solution.next();
			}
		}catch (Err e){}
		
		return Collections.unmodifiableList(result);
		
	}
	
	protected List<A4Solution> writeAndFind(String content, File file) throws Err{
		final List<A4Solution> result = new ArrayList<>();
		try {
			Util.writeAll(file.getAbsolutePath(), content);
			result.addAll(getAllproperties(file.getAbsolutePath()));
			file.delete();
		} catch (Err e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while getting all sources in implication lattice: ", e);
			throw e;
		}
		
		return Collections.unmodifiableList(result);
	}
	
	protected String extractSkolemedValue(A4Solution sol) throws Err{
		
		if (!sol.getAllSkolems().iterator().hasNext())
			throw new RuntimeException("The skolemized atom is empty");
		String value = sol.eval(sol.getAllSkolems().iterator().next()).toString();
		int lastIndexOfSlash = value.lastIndexOf('/');
		int lastIndexOfDollar = value.lastIndexOf('$');
		if (lastIndexOfDollar > lastIndexOfSlash)				
			return value.substring(lastIndexOfSlash+1, lastIndexOfDollar);
		else
			return value;
	}
	
	public abstract List<String> getAllSources() throws Err;
	public abstract List<String> getNextImpliedProperties(String property) throws Err;
	public abstract List<String> getNextRevImpliedProperties(String property) throws Err;
	public abstract List<String> getAllSinks() throws Err;
	public abstract List<String> getAllImpliedProperties(String property) throws Err;
	public abstract List<String> getAllRevImpliedProperties(String property) throws Err;
	
}
