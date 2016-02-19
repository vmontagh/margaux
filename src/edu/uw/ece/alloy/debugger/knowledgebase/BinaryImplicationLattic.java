package edu.uw.ece.alloy.debugger.knowledgebase;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;

public class BinaryImplicationLattic extends ImplicationLattic {

	public BinaryImplicationLattic(String tempPath, String[] moduleName) {
		super(tempPath, moduleName);
		// TODO Auto-generated constructor stub
	}
	
	public List<String> getAllSources() throws Err{
		final String alloyCode = 
				"open binary_implication\n" +
				"pred run_getSourcesImplication[]{\n" +
				"  some p: prop | getSourcesImplication[p]}\n" +
				"run run_getSourcesImplication for 0";
		final File file = new File(this.TEMPORARY_FOLDER, 
				"sources_"+System.currentTimeMillis() +"_" +
				rand.nextInt() + ".als");

		List<String> result = new ArrayList<>();
		for (A4Solution sol: writeAndFind(alloyCode, file)){
				result.add(extractSkolemedValue(sol));
		}
		return Collections.unmodifiableList(result); 
	}
	
	public List<String> getAllImpliedProperties(String property) throws Err{
		final String alloyCode = 
				String.format(
				"open binary_implication\n" +
				"pred run_getNextImplications[p: prop]{\n" +
				"	some p': prop | getNextImplications[p, p']}\n" +
				"run {run_getNextImplications[%s]} for 0", property);
		final File file = new File(this.TEMPORARY_FOLDER, 
				"implied_"+System.currentTimeMillis() + "_" +
		    rand.nextInt() + ".als");
		List<String> result = new ArrayList<>();
		for (A4Solution sol: writeAndFind(alloyCode, file)){
				result.add(extractSkolemedValue(sol));
		}
		return Collections.unmodifiableList(result);
	}
	
	public List<String> getAllReverseImpliedProperties(String property) throws Err{
		final String alloyCode = 
				String.format(
				"open binary_implication\n" +
				"pred run_getPreviousImplications[p: prop]{\n" +
				"	some p': prop | getNextImplications[p', p]}\n" +
				"run {run_getPreviousImplications[%s]} for 0", property);
		final File file = new File(this.TEMPORARY_FOLDER, 
				"revimplied_"+System.currentTimeMillis() + "_" +
		    rand.nextInt() + ".als");
		List<String> result = new ArrayList<>();
		for (A4Solution sol: writeAndFind(alloyCode, file)){
				result.add(extractSkolemedValue(sol));
		}
		return Collections.unmodifiableList(result);
	}
	
	public List<String> getAllSinks() throws Err{
		final String alloyCode = 
				"open binary_implication\n" +
				"pred run_getFinalSinksImplication[]{\n" +
				"  some p: prop | getFinalSinksImplication[p]}\n" +
				"run run_getFinalSinksImplication for 0";
		final File file = new File(this.TEMPORARY_FOLDER, 
				"sinks_"+System.currentTimeMillis() +"_" +
				rand.nextInt() + ".als");
		List<String> result = new ArrayList<>();
		for (A4Solution sol: writeAndFind(alloyCode, file)){
				result.add(extractSkolemedValue(sol));
		}
		return Collections.unmodifiableList(result);
	}
	
}
