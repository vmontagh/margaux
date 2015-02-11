package edu.uw.ece.alloy.debugger.exec;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import kodkod.ast.Relation;
import kodkod.instance.Instance;
import kodkod.instance.Tuple;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorFatal;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.OurDialog;
import edu.mit.csail.sdg.alloy4.Util;
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
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Attr.AttrType;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;
import edu.mit.csail.sdg.alloy4viz.VizGUI;
import edu.mit.csail.sdg.alloy4whole.ExampleUsingTheCompiler;
import edu.mit.csail.sdg.gen.LoggerUtil;
import edu.mit.csail.sdg.gen.visitor.MakeDOTCallGraph;

public class Alloy2DotGenerator {


	/*public static int PACE = 1;
	final public static boolean oneFound = false;
	final public static boolean usingKodkod = true;
	final public static boolean usingKKItr = false;
	final public static boolean symmetryOff = false;
	final public static boolean usingSymmetry = true;
	public static String reportFileName = "";*/

	
	public static class QuickReporter extends A4Reporter{
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
	}

	public static class EvalReporter extends A4Reporter{
		public long evalTime;
		public long insts;
		public void evalute(long elauationTime, long instances) {
			evalTime = elauationTime;
			insts = instances;
		}
	};
	

	public static String run(String[] args,A4Reporter rep) throws Err{

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

		for(String filename:args) {
			// Parse+typecheck the model
			System.out.println("=========== Parsing+Typechecking "+filename+" =============");
			CompModule world = CompUtil.parseEverything_fromFile(rep, null, filename);

			// Choose some default options for how you want to execute the commands
			A4Options options = new A4Options();

			options.solver = A4Options.SatSolver.MiniSatJNI;
			options.symmetry = 0;
			
			for (Command command: world.getAllCommands()) {

				
				System.out.printf("The command is: %s %n and the formunla is: %s %n%n%n", command.label, command.formula);
				
				String dotContent = MakeDOTCallGraph.makeString(command.label, command.formula);
				//Where the output file goes? The output file is a dot file and is determined from he input. By default
				//it will be located beside the input file.
				String dotoutput = filename.replace(".als", "."+command.label+".dot");

				
				Util.writeAll(dotoutput, String.format("digraph G {%n %s %n}", dotContent));
				
				System.out.println(dotContent);
				
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
		run(args);
	}


	public static String run(String[] args) throws Err{
		return run(args, new QuickReporter());
	}



	private static Command excludeCommandScope(final Command command, final Set<String> excludedSigs){
		List<CommandScope> scope = new ArrayList<CommandScope>( command.scope);
		List<CommandScope> newScope = new ArrayList<CommandScope>();
		for(CommandScope cs: scope){
			if(!excludedSigs.contains(cs.sig.label))
				newScope.add(cs);
		}
		return command.change(ConstList.make(newScope));
	}

/*
	private static UniqSigMessage makeWorld(final Command command, final CompModule world, final Sig s,
			final  A4Options options, final A4Reporter rep,
			final UniqSigMessage usm) throws Err{



		Bounds oBound = new Bounds(usm.oBound);
		List<CommandScope> nList = new ArrayList<CommandScope>(usm.nList);

		CommandScope sigScope = usm.sigScope==null? null : (CommandScope)usm.sigScope.clone();
		CommandScope scope =  usm.scope == null? null :(CommandScope)usm.scope.clone();		

		Map<String,ExprVar> sigAtoms = 
				//usm.sigAtoms==null? 
				new HashMap<String,ExprVar>()
				//:  new HashMap<String,ExprVar>(usm.sigAtoms)
				;


		Expr expr = world.getUniqueFieldFact(s.label.replace("this/", ""));

		LoggerUtil.debug(ExampleUsingTheCompiler.class,"expr->%s",expr);

		Pair<A4Solution,List<Instance>> legalPair = (Pair<A4Solution,List<Instance>>)TranslateAlloyToKodkod.evaluate_command_Itreational(
				rep, world.getAllReachableSigs(), command, options,s,expr);


		for(Instance i: legalPair.b){
			LoggerUtil.Detaileddebug("The instance is: %s", i);
		}

		System.exit(-10);


		//legalPair.a.getfieldSolutions(legalPair.b, s.label);


		Set<String> fldNames = new HashSet<String>();
		for(Decl fDecl:s.getFieldDecls()){											
			A4TupleSet legal =  legalPair.a.getfieldSolutions(legalPair.b, s.label+"."+fDecl.get().label);
			fldNames.add(s.label+"."+fDecl.get().label);

			List<List<Expr>> pFields = new ArrayList<List<Expr>>();


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

			nList.add( scope);


			Bounds nBound = new Bounds( oBound.pos,  oBound.label,  nList);
			oBound = world.replaceBound( oBound, nBound);


		}//End of For



		//Add the an empty instance to the solution if it is valid.
		Object hasEmpty = legalPair.a.getEmpty(legalPair.b, fldNames, s);




		if(hasEmpty!=null ){
			sigAtoms.put(hasEmpty.toString(), ExprVar.make(command.bound.pos, hasEmpty.toString(),s.type()));
		}

		LoggerUtil.debug(ExampleUsingTheCompiler.class,"sigScope in makeworld->%s%n scope->%s%n oBound->%s%n nList->%s%n sigAtoms->%s%n",sigScope,scope,oBound,nList,sigAtoms);


		return new UniqSigMessage(sigAtoms, sigScope, oBound, scope, nList);

	}

*/


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
