package edu.mit.csail.sdg.gen;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Attr.AttrType;
import edu.mit.csail.sdg.alloy4compiler.ast.Bounds;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.CommandScope;
import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Tuple;
import edu.mit.csail.sdg.alloy4compiler.translator.A4TupleSet;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;
import edu.mit.csail.sdg.alloy4whole.ExampleUsingTheCompiler;

public class NewSyntaxExecuterJob extends ExecuterJob  {


	public NewSyntaxExecuterJob(String reportFile) {
		super(reportFile);
		// TODO Auto-generated constructor stub
	}


	/**
	 * 
	 */
	private static final long serialVersionUID = 131231234134343L;


	protected void callExecuter(String fileName) throws Err {


		/*copyFromJAR();

		final String binary = alloyHome() + fs + "binary";
		System.out.println(binary);

		// Add the new JNI location to the java.library.path
		try {

			System.setProperty("java.library.path", binary);

			// The above line is actually useless on Sun JDK/JRE (see Sun's bug ID 4280189)
			// The following 4 lines should work for Sun's JDK/JRE (though they probably won't work for others)
			String[] newarray = new String[]{binary};

			java.lang.reflect.Field old = ClassLoader.class.getDeclaredField("usr_paths");
			old.setAccessible(true);
			old.set(null,newarray);
		} catch (Throwable ex) { 

		}

		System.out.println("=========== Parsing+Typechecking "+fileName+" =============");
		try {

			CompModule world = CompUtil.parseEverything_fromFile(rep, null, fileName);

			// Choose some default options for how you want to execute the commands
			A4Options options = new A4Options();

			options.solver = A4Options.SatSolver.MiniSatJNI;
			for (Command command: world.getAllCommands()) {
				// Execute the command
				System.out.println("============ Command "+command+": ============");
				System.out.println("Starting to evlauate the code....");
				long time = System.currentTimeMillis();

				for(Sig s:world.getAllSigs()){
					//System.out.println(s.label+"/"+s.isUnique);
					if(s.isUnique != null )
					{
						Bounds oBound = command.bound;
						CommandScope scope = null;
						CommandScope sigScope = null; 
						List<CommandScope> nList = new ArrayList<CommandScope>();

						Map<String,ExprVar> sigAtoms = new HashMap<String,ExprVar>();

						for(Decl fDecl:s.getFieldDecls()){
							Expr query = world.getUniqueFieldFact(fDecl.get().toString());

							Object legal = TranslateAlloyToKodkod.evaluate_command(
									rep, world.getAllReachableSigs(), command, options,query);
							List<List<Expr>> pFields = new ArrayList<List<Expr>>();

							//Make a list of tuples for the field and a set of atoms for the left-most sig
							List<Expr> field;
							for(A4Tuple tuple: (A4TupleSet)legal){
								field =  new ArrayList<Expr>();
								for(int i=0; i<tuple.arity(); i++){
									if(i==0)
										sigAtoms.put(tuple.atom(i), ExprVar.make(command.bound.pos, tuple.atom(i))) ;
									field.add(ExprVar.make(command.bound.pos, tuple.atom(i)) );
								}
								pFields.add(field);

							}
							//Merge the CommandScope
							//First find the related commandscope if it exists.
							List<CommandScope> oList = oBound.scope;
							nList.clear();
							scope = null;
							for(CommandScope sc: oList){
								if(sc.sig.label.equals(fDecl.get().label)){
									scope = sc;
								}else if(sc.sig.label.equals(s.decl.get().label)){
									sigScope = sc;
								}else{
									nList.add(sc);
								}
							}

							if(scope==null){
								//Make a new commandscope for each relation declration
								scope = new CommandScope(oBound.pos, 
										new PrimSig(fDecl.get().label, AttrType.WHERE.make(oBound.pos)),
										true, pFields.size(), pFields.size(), 1, new ArrayList<ExprVar>(),
										pFields.size(), pFields,
										true, false, false,false);
							}else{
								//Alter the current commandscope
								pFields.addAll(scope.pFields);
								scope = new CommandScope(scope.pos, 
										scope.sig,
										scope.isExact, pFields.size(), pFields.size(), scope.increment, 
										scope.pAtoms,
										pFields.size()+scope.pAtomsLowerLastIndex, pFields,
										scope.isPartial, scope.hasLower, scope.hasUpper,
										scope.isSparse);
							}


							//Now change the scope in the bound object

							nList.add(scope);         
							Bounds nBound = new Bounds(oBound.pos, oBound.label, nList);


							oBound = world.replaceBound(oBound, nBound);

						}//End of For
						if(sigAtoms.size() > 0){
							List<ExprVar> pAtoms = new ArrayList<ExprVar>(sigAtoms.values());
							//Replace the signature bound
							if(sigScope==null){
								//Make a new commandscope for each relation declration
								sigScope = new CommandScope(oBound.pos, 
										new PrimSig(s.label, AttrType.WHERE.make(oBound.pos)),
										true, pAtoms.size(), pAtoms.size(), 1, pAtoms,
										pAtoms.size(), new ArrayList<List<Expr>>(),
										true, false, false,false);
							}else{
								//Alter the current commandscope
								pAtoms.addAll(sigScope.pAtoms);
								sigScope = new CommandScope(sigScope.pos, 
										sigScope.sig,
										sigScope.isExact, pAtoms.size(), pAtoms.size(), sigScope.increment, 
										pAtoms,
										pAtoms.size()+scope.pAtomsLowerLastIndex, sigScope.pFields,
										sigScope.isPartial, sigScope.hasLower, sigScope.hasUpper,
										sigScope.isSparse);
							}

							//Now change the scope in the bound object
							nList.add(sigScope);         
							Bounds nBound = new Bounds(oBound.pos, oBound.label, nList);
							nBound = world.replaceBound(oBound, nBound);
							//Detachbound causes the appended fact of the inst block does not considered.
							//world.detachBound(command.bound.label);
							world.removeAllUniquFacts();
							command = new Command(command.pos, command.label, command.check, command.overall, 
									command.bitwidth, command.maxseq, command.expects, nBound.scope, 
									command.additionalExactScopes, command.formula, command.parent, command.isSparse, nBound);
						}//End of if

					}//end of if(sig is unique)
				}//end of sig:Sig loop
				long evaluationTime = System.currentTimeMillis()- time;
				System.out.println("The evaluation has been done in: "+(System.currentTimeMillis()- time)+" mSec");
				time = System.currentTimeMillis();
				System.out.println("Starting to execute the commmand....");
/*				A4Solution ans = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command, options);


				System.out.println("The execution has been done in: "+(System.currentTimeMillis()- time)+" mSec");

				updateResult(System.currentTimeMillis(), fileName, evaluationTime,
						rep.solveTime, rep.trasnalationTime, rep.totalVaraibles, rep.clauses,ans.satisfiable());

				if (ans.satisfiable()) {ans.writeXML(fileName+".xml");}
*/
/*	
			}

		} catch (Err e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/
		
		
		ExampleUsingTheCompiler.run(new String[]{fileName},rep);
		
		updateResult(System.currentTimeMillis(), fileName, rep.evalTime,
				rep.solveTime, rep.trasnalationTime, rep.totalVaraibles, rep.clauses,/*ans.satisfiable()*/rep.sat==1);


	}


	/**
	 * @param args
	 * @throws Err 
	 */
	public static void main(String[] args) throws Err {
		// TODO Auto-generated method stub
		NewSyntaxExecuterJob nsej = new NewSyntaxExecuterJob("report.txt");
		nsej.callExecuter(args[0]);
		nsej = new NewSyntaxExecuterJob("report.txt");
		nsej.callExecuter(args[1]);
	}

}
