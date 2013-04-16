/* Alloy Analyzer 4 -- Copyright (c) 2006-2009, Felix Chang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF
 * OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package edu.mit.csail.sdg.alloy4whole;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import kodkod.ast.Relation;
import kodkod.instance.Instance;
import kodkod.instance.TupleSet;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorFatal;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.OurDialog;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Pos;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Attr.AttrType;
import edu.mit.csail.sdg.alloy4compiler.ast.Bounds;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.CommandScope;
import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprConstant;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprHasName;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprQt;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4SolutionWriter;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Tuple;
import edu.mit.csail.sdg.alloy4compiler.translator.A4TupleSet;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;
import edu.mit.csail.sdg.alloy4viz.VizGUI;

/** This class demonstrates how to access Alloy4 via the compiler methods. */

public final class ExampleUsingTheCompiler {


	//Exploited as a structure
	private static class UniqSigMessage{
		public Map<String,ExprVar> sigAtoms =null;
		public CommandScope sigScope = null; 
		public Bounds oBound = null;
		public CommandScope scope = null;
		public List<CommandScope> nList = null;
		public UniqSigMessage(Map<String, ExprVar> sigAtoms,
				CommandScope sigScope, Bounds oBound, CommandScope scope,
				List<CommandScope> nList) {
			super();
			this.sigAtoms = sigAtoms;
			this.sigScope = sigScope;
			this.oBound = oBound;
			this.scope = scope;
			this.nList = nList;
		}

	}

	
	
	
	public static String run(String[] args) throws Err{
		
		String retString = "";
		copyFromJAR();
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
		} catch (Throwable ex) { }

		// The visualizer (We will initialize it to nonnull when we visualize an Alloy solution)
		VizGUI viz = null;

		// Alloy4 sends diagnostic messages and progress reports to the A4Reporter.
		// By default, the A4Reporter ignores all these events (but you can extend the A4Reporter to display the event for the user)
		A4Reporter rep = new A4Reporter() {
			private long lastTime=0;

			// For example, here we choose to display each "warning" by printing it to System.out
			@Override public void warning(ErrorWarning msg) {
				System.out.println("Relevance Warning:\n"+(msg.toString().trim())+"\n\n");
				System.out.flush();
			}
			@Override public void solve(final int primaryVars, final int totalVars, final int clauses) {
				System.out.println("solve->"+totalVars+" vars. "+primaryVars+" primary vars. "+clauses+" clauses. "+(System.currentTimeMillis()-lastTime)+"ms.\n");
				lastTime = System.currentTimeMillis();
				System.out.flush();

			}
			@Override public void translate(String solver, int bitwidth, int maxseq, int skolemDepth, int symmetry) {
				lastTime = System.currentTimeMillis();
				System.out.println("translate->Solver="+solver+" Bitwidth="+bitwidth+" MaxSeq="+maxseq
						+ (skolemDepth==0?"":" SkolemDepth="+skolemDepth)
						+ " Symmetry="+(symmetry>0 ? (""+symmetry) : "OFF")+'\n');
				System.out.flush();

			}

		};

		for(String filename:args) {

			// Parse+typecheck the model
			System.out.println("=========== Parsing+Typechecking "+filename+" =============");
			CompModule world = CompUtil.parseEverything_fromFile(rep, null, filename);

			// Choose some default options for how you want to execute the commands
			A4Options options = new A4Options();

			options.solver = A4Options.SatSolver.MiniSatJNI;

			boolean touched = false;


			for (Command command: world.getAllCommands()) {
				// Execute the command
				System.out.println("============ Command "+command+": ============");
				System.out.println("Starting to evlauate the code....");
				long time = System.currentTimeMillis();
				//System.out.println(command.scope.get(0).sig);
				PrintWriter out=null;
				int i=0;
				try {
					out=new PrintWriter("../tmp/out.xml","UTF-8");
					UniqSigMessage usm = new UniqSigMessage(
							new HashMap<String,ExprVar>(),
							null, command.bound, null,
							new ArrayList<CommandScope>());

					for(Sig s:world.getAllSigs()){
						//System.out.println(s.label+"/"+s.isUnique);
						i++;
						if(s.isUnique != null )
						{	

							//if (i==0)
							//System.exit(-10);
							usm = makeWorld(command,world,s,options,rep,usm);

							if(usm.sigAtoms.size() > 0){
								Set<ExprVar> pAtoms = new HashSet<ExprVar>(usm.sigAtoms.values());
								//Replace the signature bound
								if(usm.sigScope==null){
									//Make a new commandscope for each relation declration
									usm.sigScope = new CommandScope(usm.oBound.pos, 
											new PrimSig(s.label, AttrType.WHERE.make(usm.oBound.pos)),
											true, pAtoms.size(), pAtoms.size(), 1, new ArrayList<ExprVar>(pAtoms),
											pAtoms.size(), new ArrayList<List<Expr>>(),
											true, false, false,false);
								}else{
									//Alter the current commandscope
									System.out.println("pAtoms before:"+pAtoms);
									System.out.println("usm.sigScope.pAtoms:"+usm.sigScope.pAtoms);
									pAtoms.addAll(usm.sigScope.pAtoms);
									
									System.out.println("pAtoms after:"+pAtoms);
									usm.sigScope = new CommandScope(usm.sigScope.pos, 
											usm.sigScope.sig,
											usm.sigScope.isExact, pAtoms.size(), pAtoms.size(), usm.sigScope.increment, 
											new ArrayList<>( pAtoms),
											pAtoms.size()+
											usm.scope.pAtomsLowerLastIndex, 
											usm.sigScope.pFields,
											usm.sigScope.isPartial, usm.sigScope.hasLower, usm.sigScope.hasUpper,
											usm.sigScope.isSparse);
								}

								//Now change the scope in the bound object
								usm.nList.add(usm.sigScope);      
								System.out.println("usm.nList->"+usm.nList);
								Bounds nBound = new Bounds(usm.oBound.pos, usm.oBound.label, usm.nList);
								usm.oBound = world.replaceBound(usm.oBound, nBound);
								//Detachbound causes the appended fact of the inst block does not considered.
								//world.detachBound(command.bound.label);
								world.removeUniquFacts(s);
								command = command.change( ConstList.make(usm.oBound.scope));
								command = command.change(usm.oBound); 

							}//End of if

						}//end of if(sig is unique)
					}//end of sig:Sig loop



					System.out.println("The evaluation has been done in: "+(System.currentTimeMillis()- time)+" mSec");
					time = System.currentTimeMillis();
					System.out.println("Starting to execute the commmand....");
					A4Solution ans = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command, options);
					System.out.println("The execution has been done in: "+(System.currentTimeMillis()- time)+" mSec");

					// Print the outcome
					System.out.println(ans.satisfiable());
					
					String output = filename.replace(".als", ".out.xml");
					
					ans.writeXML(output);
					retString = ans.toString();
					/*					System.exit(-10);
					Object legal = TranslateAlloyToKodkod.evaluate_command(
							rep, world.getAllReachableSigs(), command, options,world.getEvalQuery() );
					//Instance inst = new Instance(half_sol. .universe());
					System.out.println(legal);
					System.out.println();
					//A4SolutionWriter.writeInstance(  );
					if (!Util.close(out)) throw new ErrorFatal("Error writing the solution XML file.");*/
				} catch(IOException ex) {
					Util.close(out);
					throw new ErrorFatal("Error writing the solution XML file.", ex);
				}
				//				System.exit(1);

				/*				A4Solution ans = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command, options);
				// Print the outcome
				System.out.println("------------------ans->---------\n"+ans);
				// If satisfiable...
				if (ans.satisfiable()) {
					// You can query "ans" to find out the values of each set or type.
					// This can be useful for debugging.
					//
					// You can also write the outcome to an XML file
					ans.writeXML("alloy_example_output.xml");
					//
					// You can then visualize the XML file by calling this:
					if (viz==null) {
						viz = new VizGUI(false, "alloy_example_output.xml", null);
					} else {
						viz.loadXML("alloy_example_output.xml", true);
					}
				}*/
			}
		}
		
		return retString;

	}
	
	
	/*
	 * Execute every command in every file.
	 *
	 * This method parses every file, then execute every command.
	 *
	 * If there are syntax or type errors, it may throw
	 * a ErrorSyntax or ErrorType or ErrorAPI or ErrorFatal exception.
	 * You should catch them and display them,
	 * and they may contain filename/line/column information.
	 */
	public static void main(String[] args) throws Err {
		System.out.println(run(args));
	}


	private static UniqSigMessage makeWorld(final Command command, final CompModule world, final Sig s,
			final  A4Options options, final A4Reporter rep,
			final UniqSigMessage usm) throws Err{

		
		
		Bounds oBound = new Bounds(usm.oBound);
		List<CommandScope> nList = new ArrayList<CommandScope>(usm.nList);

		CommandScope sigScope = usm.sigScope==null? null : (CommandScope)usm.sigScope.clone();
		CommandScope scope =  usm.scope == null? null :(CommandScope)usm.scope.clone();		

		Map<String,ExprVar> sigAtoms =  usm.sigAtoms==null? new HashMap<String,ExprVar>():  new HashMap<String,ExprVar>(usm.sigAtoms);

		System.out.println("sigAtoms before->"+sigAtoms);
		
		Expr expr = world.getUniqueFieldFact(s.label.replace("this/", ""));

		Pair<A4Solution,List<Instance>> legalPair = (Pair<A4Solution,List<Instance>>)TranslateAlloyToKodkod.evaluate_command_Itreational(
				rep, world.getAllReachableSigs(), command, options,s,expr);

		//legalPair.a.getfieldSolutions(legalPair.b, s.label);

		Set<String> fldNames = new HashSet<String>();
		for(Decl fDecl:s.getFieldDecls()){											
			A4TupleSet legal =  legalPair.a.getfieldSolutions(legalPair.b, s.label+"."+fDecl.get().label);
			fldNames.add(s.label+"."+fDecl.get().label);

			List<List<Expr>> pFields = new ArrayList<List<Expr>>();

			System.out.println("sigAtoms2->"+sigAtoms);
			
			//Make a list of tuples for the field and a set of atoms for the left-most sig
			List<Expr> field;
			for(A4Tuple tuple: (A4TupleSet)legal){
				field =  new ArrayList<Expr>();
				for(int i=0; i<tuple.arity(); i++){
					if(i==0 && s.isOne==null)
						sigAtoms.put(tuple.atom(i), ExprVar.make(command.bound.pos, tuple.atom(i))) ;
					field.add(ExprVar.make(command.bound.pos, tuple.atom(i)) );
				}
				pFields.add(field);

			}
			System.out.println("sigAtoms3->"+sigAtoms);
			//Merge the CommandScope
			//First find the related commandscope if it exists.
			List<CommandScope> oScopes =  oBound.scope;
			nList.clear();
			scope = null;
			for(CommandScope sc: oScopes){
				if(sc.sig.label.equals(fDecl.get().label)){
					scope = sc;
				}else if(sc.sig.label.equals(s.decl.get().label)){
					sigScope = sc;
				}else{
					nList.add(sc);
				}
			}

			System.out.println("scope.pAtoms->"+(scope==null?null:scope.pAtoms));

			if( scope==null){
				//Make a new commandscope for each relation declration
				scope = new CommandScope( oBound.pos, 
						new PrimSig(fDecl.get().label, AttrType.WHERE.make( oBound.pos)),
						true, pFields.size(), pFields.size(), 1, new ArrayList<ExprVar>(),
						pFields.size(), pFields,
						true, false, false,false);
			}else{
				//Alter the current commandscope
				pFields.addAll( scope.pFields);
				scope = new CommandScope( scope.pos, 
						scope.sig,
						scope.isExact, pFields.size(), pFields.size(),  scope.increment, 
						scope.pAtoms,
						pFields.size()+ scope.pAtomsLowerLastIndex, pFields,
						scope.isPartial,  scope.hasLower,  scope.hasUpper,
						scope.isSparse);
			}


			//Now change the scope in the bound object
			System.out.println("scope->"+scope);
			System.out.println("nList->"+nList);

			nList.add( scope);
			
			System.out.println("nList after->"+nList);
			
			Bounds nBound = new Bounds( oBound.pos,  oBound.label,  nList);
			oBound = world.replaceBound( oBound, nBound);
			

		}//End of For

		//Add the an empty instance to the solution if it is valid.
		Object hasEmpty = legalPair.a.getEmpty(legalPair.b, fldNames, s);

		System.out.println("sigAtoms4->"+sigAtoms);
		if(hasEmpty!=null ){
			sigAtoms.put(hasEmpty.toString(), ExprVar.make(command.bound.pos, hasEmpty.toString()));
		}

		System.out.println("sigAtoms->"+sigAtoms);
		return new UniqSigMessage(sigAtoms, sigScope, oBound, scope, nList);




	}


	private static Command makeCommand(Sig s, Map<String,ExprVar> sigAtoms,CommandScope sigScope,
			CommandScope scope,Bounds oBound,List<CommandScope> nList, Command command, CompModule world) throws Err{
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
						pAtoms.size()+
						scope.pAtomsLowerLastIndex, 
						sigScope.pFields,
						sigScope.isPartial, sigScope.hasLower, sigScope.hasUpper,
						sigScope.isSparse);
			}

			//Now change the scope in the bound object
			nList.add(sigScope);         
			Bounds nBound = new Bounds(oBound.pos, oBound.label, nList);
			oBound = world.replaceBound(oBound, nBound);
			//Detachbound causes the appended fact of the inst block does not considered.
			//world.detachBound(command.bound.label);
			world.removeUniquFacts(s);
			return new Command(command.pos, command.label, command.check, command.overall, 
					command.bitwidth, command.maxseq, command.expects, oBound.scope, 
					command.additionalExactScopes, command.formula, command.parent, command.isSparse, oBound);

		}else
			//no changes happened
			return command;
	}


	private static Expr  getEmptyCheckingExpr(Sig sig){
		//Check to see whether an empty atom is acceptable or not
		Expr epmtyChkrExpr = null;
		ExprHasName uSigName = ExprVar.make(null, sig.label+Math.abs(sig.label.hashCode()),sig.type()) ; 

		for(Decl fDecl:sig.getFieldDecls()){

			Expr right = ExprBinary.Op.JOIN.make(null, null, uSigName, fDecl.get());
			Sig.Field fld = (Sig.Field)fDecl.get();
			Expr empty = ExprConstant.EMPTYNESS;
			for(int i=2; i< fld.type().arity(); i++){
				empty = ExprBinary.Op.ARROW.make(null, null, empty, ExprConstant.EMPTYNESS);
			}
			Expr emptyExpr = ExprBinary.Op.EQUALS.make(null, null, right,empty);
			if(epmtyChkrExpr == null){
				epmtyChkrExpr = emptyExpr;
			}else{
				epmtyChkrExpr = ExprBinary.Op.AND.make(null, null, epmtyChkrExpr, emptyExpr);
			}
		}
		if(epmtyChkrExpr!=null){
			List<ExprHasName> names = new ArrayList<ExprHasName>();
			names.add(uSigName);
			Decl decl = new Decl(null, null, null, names, sig);
			List<Decl> decls = new ArrayList<Decl>();
			decls.add(decl);
			epmtyChkrExpr = ExprQt.Op.ONE.make(null, null, decls, epmtyChkrExpr);

		}
		return epmtyChkrExpr;

	}


	private static boolean loadLibrary(String library) {
		try { System.loadLibrary(library);      return true; } catch(UnsatisfiedLinkError ex) { }
		try { System.loadLibrary(library+"x1"); return true; } catch(UnsatisfiedLinkError ex) { }
		try { System.loadLibrary(library+"x2"); return true; } catch(UnsatisfiedLinkError ex) { }
		try { System.loadLibrary(library+"x3"); return true; } catch(UnsatisfiedLinkError ex) { }
		try { System.loadLibrary(library+"x4"); return true; } catch(UnsatisfiedLinkError ex) { }
		try { System.loadLibrary(library+"x5"); return true; } catch(UnsatisfiedLinkError ex) { return false; }
	}

	/** Copy the required files from the JAR into a temporary directory. */
	private static void copyFromJAR() {
		// Compute the appropriate platform
		String os = System.getProperty("os.name").toLowerCase(Locale.US).replace(' ','-');
		if (os.startsWith("mac-")) os="mac"; else if (os.startsWith("windows-")) os="windows";
		String arch = System.getProperty("os.arch").toLowerCase(Locale.US).replace(' ','-');
		if (arch.equals("powerpc")) arch="ppc-"+os; else arch=arch.replaceAll("\\Ai[3456]86\\z","x86")+"-"+os;
		if (os.equals("mac")) arch="x86-mac"; // our pre-compiled binaries are all universal binaries
		// Find out the appropriate Alloy directory
		final String platformBinary = alloyHome() + fs + "binary";
		// Write a few test files
		try {
			(new File(platformBinary)).mkdirs();
			Util.writeAll(platformBinary + fs + "tmp.cnf", "p cnf 3 1\n1 0\n");
		} catch(Err er) {
			// The error will be caught later by the "berkmin" or "spear" test
		}
		// Copy the platform-dependent binaries
		Util.copy(true, false, platformBinary,
				arch+"/libminisat.so", arch+"/libminisatx1.so", arch+"/libminisat.jnilib",
				arch+"/libminisatprover.so", arch+"/libminisatproverx1.so", arch+"/libminisatprover.jnilib",
				arch+"/libzchaff.so", arch+"/libzchaffx1.so", arch+"/libzchaff.jnilib",
				arch+"/berkmin", arch+"/spear");
		Util.copy(false, false, platformBinary,
				arch+"/minisat.dll", arch+"/minisatprover.dll", arch+"/zchaff.dll",
				arch+"/berkmin.exe", arch+"/spear.exe");
		// Copy the model files
		Util.copy(false, true, alloyHome(),
				"models/book/appendixA/addressBook1.als", "models/book/appendixA/addressBook2.als", "models/book/appendixA/barbers.als",
				"models/book/appendixA/closure.als", "models/book/appendixA/distribution.als", "models/book/appendixA/phones.als",
				"models/book/appendixA/prison.als", "models/book/appendixA/properties.als", "models/book/appendixA/ring.als",
				"models/book/appendixA/spanning.als", "models/book/appendixA/tree.als", "models/book/appendixA/tube.als", "models/book/appendixA/undirected.als",
				"models/book/appendixE/hotel.thm", "models/book/appendixE/p300-hotel.als", "models/book/appendixE/p303-hotel.als", "models/book/appendixE/p306-hotel.als",
				"models/book/chapter2/addressBook1a.als", "models/book/chapter2/addressBook1b.als", "models/book/chapter2/addressBook1c.als",
				"models/book/chapter2/addressBook1d.als", "models/book/chapter2/addressBook1e.als", "models/book/chapter2/addressBook1f.als",
				"models/book/chapter2/addressBook1g.als", "models/book/chapter2/addressBook1h.als", "models/book/chapter2/addressBook2a.als",
				"models/book/chapter2/addressBook2b.als", "models/book/chapter2/addressBook2c.als", "models/book/chapter2/addressBook2d.als",
				"models/book/chapter2/addressBook2e.als", "models/book/chapter2/addressBook3a.als", "models/book/chapter2/addressBook3b.als",
				"models/book/chapter2/addressBook3c.als", "models/book/chapter2/addressBook3d.als", "models/book/chapter2/theme.thm",
				"models/book/chapter4/filesystem.als", "models/book/chapter4/grandpa1.als",
				"models/book/chapter4/grandpa2.als", "models/book/chapter4/grandpa3.als", "models/book/chapter4/lights.als",
				"models/book/chapter5/addressBook.als", "models/book/chapter5/lists.als", "models/book/chapter5/sets1.als", "models/book/chapter5/sets2.als",
				"models/book/chapter6/hotel.thm", "models/book/chapter6/hotel1.als", "models/book/chapter6/hotel2.als",
				"models/book/chapter6/hotel3.als", "models/book/chapter6/hotel4.als", "models/book/chapter6/mediaAssets.als",
				"models/book/chapter6/memory/abstractMemory.als", "models/book/chapter6/memory/cacheMemory.als",
				"models/book/chapter6/memory/checkCache.als", "models/book/chapter6/memory/checkFixedSize.als",
				"models/book/chapter6/memory/fixedSizeMemory.als", "models/book/chapter6/memory/fixedSizeMemory_H.als",
				"models/book/chapter6/ringElection.thm", "models/book/chapter6/ringElection1.als", "models/book/chapter6/ringElection2.als",
				"models/examples/algorithms/dijkstra.als", "models/examples/algorithms/dijkstra.thm",
				"models/examples/algorithms/messaging.als", "models/examples/algorithms/messaging.thm",
				"models/examples/algorithms/opt_spantree.als", "models/examples/algorithms/opt_spantree.thm",
				"models/examples/algorithms/peterson.als",
				"models/examples/algorithms/ringlead.als", "models/examples/algorithms/ringlead.thm",
				"models/examples/algorithms/s_ringlead.als",
				"models/examples/algorithms/stable_mutex_ring.als", "models/examples/algorithms/stable_mutex_ring.thm",
				"models/examples/algorithms/stable_orient_ring.als", "models/examples/algorithms/stable_orient_ring.thm",
				"models/examples/algorithms/stable_ringlead.als", "models/examples/algorithms/stable_ringlead.thm",
				"models/examples/case_studies/INSLabel.als", "models/examples/case_studies/chord.als",
				"models/examples/case_studies/chord2.als", "models/examples/case_studies/chordbugmodel.als",
				"models/examples/case_studies/com.als", "models/examples/case_studies/firewire.als", "models/examples/case_studies/firewire.thm",
				"models/examples/case_studies/ins.als", "models/examples/case_studies/iolus.als",
				"models/examples/case_studies/sync.als", "models/examples/case_studies/syncimpl.als",
				"models/examples/puzzles/farmer.als", "models/examples/puzzles/farmer.thm",
				"models/examples/puzzles/handshake.als", "models/examples/puzzles/handshake.thm",
				"models/examples/puzzles/hanoi.als", "models/examples/puzzles/hanoi.thm",
				"models/examples/systems/file_system.als", "models/examples/systems/file_system.thm",
				"models/examples/systems/javatypes_soundness.als",
				"models/examples/systems/lists.als", "models/examples/systems/lists.thm",
				"models/examples/systems/marksweepgc.als", "models/examples/systems/views.als",
				"models/examples/toys/birthday.als", "models/examples/toys/birthday.thm",
				"models/examples/toys/ceilingsAndFloors.als", "models/examples/toys/ceilingsAndFloors.thm",
				"models/examples/toys/genealogy.als", "models/examples/toys/genealogy.thm",
				"models/examples/toys/grandpa.als", "models/examples/toys/grandpa.thm",
				"models/examples/toys/javatypes.als", "models/examples/toys/life.als", "models/examples/toys/life.thm",
				"models/examples/toys/numbering.als", "models/examples/toys/railway.als", "models/examples/toys/railway.thm",
				"models/examples/toys/trivial.als",
				"models/examples/tutorial/farmer.als",
				"models/util/boolean.als", "models/util/graph.als", "models/util/integer.als", "models/util/natural.als",
				"models/util/ordering.als", "models/util/relation.als", "models/util/seqrel.als", "models/util/sequence.als",
				"models/util/sequniv.als", "models/util/ternary.als", "models/util/time.als"
				);
		// Record the locations
		System.setProperty("alloy.theme0", alloyHome() + fs + "models");
		System.setProperty("alloy.home", alloyHome());
	}

	private static synchronized String alloyHome() {
		if (alloyHome!=null) return alloyHome;
		String temp=System.getProperty("java.io.tmpdir");
		if (temp==null || temp.length()==0)
			OurDialog.fatal("Error. JVM need to specify a temporary directory using java.io.tmpdir property.");
		String username=System.getProperty("user.name");
		File tempfile=new File(temp+File.separatorChar+"alloy4tmp40-"+(username==null?"":username));
		tempfile.mkdirs();
		String ans=Util.canon(tempfile.getPath());
		if (!tempfile.isDirectory()) {
			OurDialog.fatal("Error. Cannot create the temporary directory "+ans);
		}
		if (!Util.onWindows()) {
			String[] args={"chmod", "700", ans};
			try {Runtime.getRuntime().exec(args).waitFor();}
			catch (Throwable ex) {} // We only intend to make a best effort.
		}
		return alloyHome=ans;
	}

	private static String alloyHome = null;
	private static final String fs = System.getProperty("file.separator");




}
