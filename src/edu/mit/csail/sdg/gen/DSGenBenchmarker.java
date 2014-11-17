package edu.mit.csail.sdg.gen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.uw.ece.alloy.util.TestInputs;

public class DSGenBenchmarker {
	
	
	protected static final DSGenBenchmarker myself = new DSGenBenchmarker();
	
	protected DSGenBenchmarker(){};
	
	public static DSGenBenchmarker getInstance(){ return myself; };

	private String makeNewDSfile(final String templatePath, final String dest, final int number, DSPartialInstanceGen instBlock) throws FileNotFoundException, IOException, Err{
		int lastInx = dest.lastIndexOf(File.separator);
		String fileName = lastInx > 0 ? templatePath.substring(lastInx) : templatePath;
		fileName = dest+File.separator+fileName.replace(".","_"+number+".");
		Util.writeAll(fileName, Util.readAll(templatePath).replace("$INST_I",instBlock.generate(number)));
		return fileName;
	}
	
	
	private void runDSTest(int min, int max, int experiments, long timeOutMin, String method, String template, String dest, DSPartialInstanceGen instBlock) throws FileNotFoundException, IOException, Err, InterruptedException{
		
		Collection<Object[]> tcs = new ArrayList();
		
		for(int i = min; i <= max; i++){
			Object[] f = new Object[1];
			f[0] =  makeNewDSfile(template,dest,i, instBlock);
			tcs.add( f);
		}
		//Just generate the output files
		BenchmarkRunner.getInstance().doTest(method,experiments, timeOutMin, tcs);
	}
	
	
	
	public synchronized void runLLSTest(int min, int max, int experiments, long timeOutMin, String method) throws FileNotFoundException, IOException, Err, InterruptedException{
		runOneLinkTest( min,  max,  experiments,  timeOutMin,  method, "models/partial/gen/abz14/LLS_template.als",  "models/partial/gen/abz14/tmp");
	}
	
	public synchronized void runLLSTestToCompareAlloyStar(int min, int max, int experiments, long timeOutMin, String method) throws FileNotFoundException, IOException, Err, InterruptedException{
		//runOneLinkTest( min,  max,  experiments,  timeOutMin,  method, "models/partial/gen/abz14/LLS_template_validinsert_validremove_assert.als",  "models/partial/gen/abz14/tmp");
		runOneLinkTest( min,  max,  experiments,  timeOutMin,  method, "models/partial/gen/abz14/LLS_template_validinsert_validremove_no.als",  "models/partial/gen/abz14/tmp");

		//runOneLinkTest( min,  max,  experiments,  timeOutMin,  method, "models/partial/gen/abz14/LLS_template_invalidinsert_validremove_assert.als",  "models/partial/gen/abz14/tmp");
		runOneLinkTest( min,  max,  experiments,  timeOutMin,  method, "models/partial/gen/abz14/LLS_template_invalidinsert_validremove_no.als",  "models/partial/gen/abz14/tmp");
		
		//runOneLinkTest( min,  max,  experiments,  timeOutMin,  method, "models/partial/gen/abz14/LLS_template_validinsert_invalidremove_assert.als",  "models/partial/gen/abz14/tmp");
		runOneLinkTest( min,  max,  experiments,  timeOutMin,  method, "models/partial/gen/abz14/LLS_template_validinsert_invalidremove_no.als",  "models/partial/gen/abz14/tmp");
		
		//runOneLinkTest( min,  max,  experiments,  timeOutMin,  method, "models/partial/gen/abz14/LLS_template_invalidinsert_invalidremove_assert.als",  "models/partial/gen/abz14/tmp");
		runOneLinkTest( min,  max,  experiments,  timeOutMin,  method, "models/partial/gen/abz14/LLS_template_invalidinsert_invalidremove_no.als",  "models/partial/gen/abz14/tmp");


	}

	public synchronized void runBSTTest(int min, int max, int experiments, long timeOutMin, String method) throws FileNotFoundException, IOException, Err, InterruptedException{
		runOneLinkTest( min,  max,  experiments,  timeOutMin,  method, "models/partial/gen/abz14/BST_template.als",  "models/partial/gen/abz14/tmp");
	}

	public synchronized void runBSTTestToCompareAlloyStar(int min, int max, int experiments, long timeOutMin, String method) throws FileNotFoundException, IOException, Err, InterruptedException{
		runOneLinkTest( min,  max,  experiments,  timeOutMin,  method, "models/partial/gen/abz14/BST_template_validinsert_validremove_assert.als",  "models/partial/gen/abz14/tmp");
		//runOneLinkTest( min,  max,  experiments,  timeOutMin,  method, "models/partial/gen/abz14/BST_template_validinsert_validremove_no.als",  "models/partial/gen/abz14/tmp");

		runOneLinkTest( min,  max,  experiments,  timeOutMin,  method, "models/partial/gen/abz14/BST_template_invalidinsert_validremove_assert.als",  "models/partial/gen/abz14/tmp");
		//runOneLinkTest( min,  max,  experiments,  timeOutMin,  method, "models/partial/gen/abz14/BST_template_invalidinsert_validremove_no.als",  "models/partial/gen/abz14/tmp");
		
		runOneLinkTest( min,  max,  experiments,  timeOutMin,  method, "models/partial/gen/abz14/BST_template_validinsert_invalidremove_assert.als",  "models/partial/gen/abz14/tmp");
		//runOneLinkTest( min,  max,  experiments,  timeOutMin,  method, "models/partial/gen/abz14/BST_template_validinsert_invalidremove_no.als",  "models/partial/gen/abz14/tmp");
		
		runOneLinkTest( min,  max,  experiments,  timeOutMin,  method, "models/partial/gen/abz14/BST_template_invalidinsert_invalidremove_assert.als",  "models/partial/gen/abz14/tmp");
		//runOneLinkTest( min,  max,  experiments,  timeOutMin,  method, "models/partial/gen/abz14/BST_template_invalidinsert_invalidremove_no.als",  "models/partial/gen/abz14/tmp");


	}

	
	
	private void runOneLinkTest(int min, int max, int experiments, long timeOutMin, String method, String template, String dest) throws FileNotFoundException, IOException, Err, InterruptedException{
		
		runDSTest(min, max, experiments,timeOutMin,method,template,dest,
				(new DSPartialInstanceGen() {
					
					@Override
					public String generate(int max) {
						int bitwidth = (int)Math.ceil((Math.log(max) / Math.log(2)));
						StringBuilder result = new StringBuilder();
						result.append("inst i {\n").append("\t0,\n").append('\t').append(bitwidth+1).append(" Int,\n");
						
						int maxInt = (1<<bitwidth-1) - 1;
						int minInt = maxInt - (1<<(bitwidth)) + 1;
						
						LoggerUtil.debug(CardGenBenchmarker.class, "bitwidth = %d \t maxInt = %d \t minInt = %d ",bitwidth,maxInt, minInt);
						
						StringBuilder nodes = new StringBuilder();
						StringBuilder values = new StringBuilder();
						
						
						//generating the nodes and vals tuples
						for(int i = 0; i < max; i++){
							nodes.append(" n").append(i).append(" +");
							values.append(" n").append(i).append("->").append(minInt+i).append(" +");
						}
						
						nodes.setCharAt(nodes.length()-1, ',');
						values.setCharAt(values.length()-1, ' ');

						//making the nodes
						result.append('\t').append("Node=").append(nodes).append("\n");
						//making the realations
						result.append('\t').append("val=").append(values).append("\n");

						result.append('}');
						return result.toString();
					}
				})
				);
	
	}
	
	public synchronized void runRBTTest(int min, int max, int experiments, long timeOutMin, String method) throws FileNotFoundException, IOException, Err, InterruptedException{
		
		runDSTest(min, max, experiments,timeOutMin,method,"models/partial/gen/abz14/RBT_template.als","models/partial/gen/abz14/tmp",
				(new DSPartialInstanceGen() {
					
					@Override
					public String generate(int max) {
						int bitwidth = (int)Math.ceil((Math.log(max) / Math.log(2)));
						StringBuilder result = new StringBuilder();
						result.append("inst i {\n").append("\t0,\n").append('\t').append(bitwidth+1).append(" Int,\n");
						
						int maxInt = (1<<bitwidth-1) - 1;
						int minInt = maxInt - (1<<(bitwidth)) + 1;
						
						LoggerUtil.debug(CardGenBenchmarker.class, "bitwidth = %d \t maxInt = %d \t minInt = %d ",bitwidth,maxInt, minInt);
						
						StringBuilder nodes = new StringBuilder();
						StringBuilder values = new StringBuilder();
						StringBuilder colors = new StringBuilder();
						
						char[] RB = {'R','B'};
						
						//generating the nodes and vals tuples
						for(int i = 0; i < max*2; i++){
							nodes.append(" n").append(i).append(" +");
							values.append(" n").append(i).append("->").append(minInt+i/2).append(" +");
							colors.append(" n").append(i).append("->").append(RB[i%2]).append(" +");
						}
						
						nodes.setCharAt(nodes.length()-1, ',');
						values.setCharAt(values.length()-1, ',');
						colors.setCharAt(colors.length()-1, ' ');

						//making the nodes
						result.append('\t').append("Red=").append('R').append(",\n");
						result.append('\t').append("Black=").append('B').append(",\n");
						result.append('\t').append("Node=").append(nodes).append("\n");
						//making the relations
						result.append('\t').append("val=").append(values).append("\n");
						result.append('\t').append("col=").append(colors).append("\n");

						result.append('}');
						return result.toString();
					}
				})
				);
	
	}
	
	
	
	
	
	public static void main(String[] args) throws Exception {
		long timeOutMin = 1000; 
		int experiments = 1;
		final List<String> fileGroups = new ArrayList<String>();
		int numbers = 5;
		
		//runBSTTestToCompareAlloyStar(8,8,experiments, timeOutMin,"ee");
		DSGenBenchmarker.getInstance().runLLSTestToCompareAlloyStar(8,8,experiments, timeOutMin,"ee");

		//runLLSTestToCompareAlloyStar(2,8,experiments, timeOutMin,"ee");
		//runBSTTest(2,7,experiments, timeOutMin,"walker");
		//runRBTTest(2,7,experiments, timeOutMin,"walker");
		
		System.exit(-10);
		/*Object[] m = {"models/partial/gen/david_expr/model1_new_gpce2013.als"};
		List<Object[]> list = new ArrayList<Object[]>();
		list.add(m);
		doTest("walker",experiments, timeOutMin,list);*/
		
		BenchmarkRunner.getInstance().doTest("walker",experiments, timeOutMin, TestInputs.generatorFORMLABZ14());
		
		BenchmarkRunner.getInstance().doTest("walker",experiments, timeOutMin, TestInputs.generatorPhoneBookABZ14());

		
		DSGenBenchmarker.getInstance().runLLSTest(2,7,experiments, timeOutMin,"ee");
		DSGenBenchmarker.getInstance().runBSTTest(2,7,experiments, timeOutMin,"ee");
		DSGenBenchmarker.getInstance().runRBTTest(2,7,experiments, timeOutMin,"ee");
		
		/*Object[] m = {"models/partial/gen/david_expr/model1_new_gpce2013.als"};
		List<Object[]> list = new ArrayList<Object[]>();
		list.add(m);
		doTest("walker",experiments, timeOutMin,list);*/
		
		BenchmarkRunner.getInstance().doTest("ee",experiments, timeOutMin, TestInputs.generatorFORMLABZ14());
		
		BenchmarkRunner.getInstance().doTest("ee",experiments, timeOutMin, TestInputs.generatorPhoneBookABZ14());
		
		System.exit(-10);

		
		
/*		doTest("NewWithConstraint1",experiments, timeOutMin, TestInputs.generatorBenchmarkNewWithConstraint1());
		doTest("NewWithConstraint2",experiments, timeOutMin, TestInputs.generatorBenchmarkNewWithConstraint2());
		doTest("NewWithout",experiments, timeOutMin, TestInputs.generatorBenchmarkNewWithoutConstraint());

		
		doTest("NewWithConstraint1_Itr",experiments, timeOutMin, TestInputs.generatorBenchmarkNewWithConstraint1());
		doTest("NewWithConstraint2_Itr",experiments, timeOutMin, TestInputs.generatorBenchmarkNewWithConstraint2());
		doTest("NewWithout_Itr",experiments, timeOutMin, TestInputs.generatorBenchmarkNewWithoutConstraint());

		
/*		doTest("OldWithConstraint1_Itr",experiments, timeOutMin, TestInputs.generatorBenchmarkNormalWithConstraint1());
		doTest("OldWithConstraint2_Itr",experiments, timeOutMin, TestInputs.generatorBenchmarkNormalWithConstraint1());
		doTest("OldWithout_Itr",experiments, timeOutMin, TestInputs.generatorBenchmarkNormal());
	*/	
		System.exit(-10);
		
	}
	
}
