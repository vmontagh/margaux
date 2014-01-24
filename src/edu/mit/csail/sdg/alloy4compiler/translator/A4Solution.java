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

package edu.mit.csail.sdg.alloy4compiler.translator;

import static edu.mit.csail.sdg.alloy4compiler.ast.Sig.UNIV;
import static edu.mit.csail.sdg.alloy4compiler.ast.Sig.SIGINT;
import static edu.mit.csail.sdg.alloy4compiler.ast.Sig.SEQIDX;
import static edu.mit.csail.sdg.alloy4compiler.ast.Sig.STRING;
import static edu.mit.csail.sdg.alloy4compiler.ast.Sig.NONE;
import static kodkod.engine.Solution.Outcome.UNSATISFIABLE;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;

import kodkod.ast.BinaryExpression;
import kodkod.ast.BinaryFormula;
import kodkod.ast.Decl;
import kodkod.ast.Decls;
import kodkod.ast.Expression;
import kodkod.ast.Formula;
import kodkod.ast.IntExpression;
import kodkod.ast.Node;
import kodkod.ast.Relation;
import kodkod.ast.Variable;
import kodkod.ast.operator.ExprOperator;
import kodkod.ast.operator.FormulaOperator;
import kodkod.engine.CapacityExceededException;
import kodkod.engine.Evaluator;
import kodkod.engine.Proof;
import kodkod.engine.Solution;
import kodkod.engine.Solver;
import kodkod.engine.config.AbstractReporter;
import kodkod.engine.config.Options;
import kodkod.engine.config.Reporter;
import kodkod.engine.fol2sat.TranslationRecord;
import kodkod.engine.fol2sat.Translator;
import kodkod.engine.satlab.SATFactory;
import kodkod.engine.ucore.HybridStrategy;
import kodkod.engine.ucore.RCEStrategy;
import kodkod.instance.Bounds;
import kodkod.instance.Instance;
import kodkod.instance.Tuple;
import kodkod.instance.TupleFactory;
import kodkod.instance.TupleSet;
import kodkod.instance.Universe;
import kodkod.util.ints.IndexedEntry;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.ConstMap;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorAPI;
import edu.mit.csail.sdg.alloy4.ErrorFatal;
import edu.mit.csail.sdg.alloy4.ErrorSyntax;
import edu.mit.csail.sdg.alloy4.ErrorType;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Pos;
import edu.mit.csail.sdg.alloy4.SafeList;
import edu.mit.csail.sdg.alloy4.UniqueNameGenerator;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Attr.AttrType;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.CommandScope;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprConstant;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprHasName;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprList;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprQt;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary.Op;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Type;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options.SatSolver;
import edu.mit.csail.sdg.alloy4whole.ExampleUsingTheCompiler;
import edu.mit.csail.sdg.gen.LoggerUtil;
import edu.mit.csail.sdg.gen.MyReporter;
import edu.mit.csail.sdg.gen.flw.AdjMatrixEdgeWeightedDigraph;
import edu.mit.csail.sdg.gen.flw.DirectedEdge;
import edu.mit.csail.sdg.gen.flw.FloydWarshall;

/** This class stores a SATISFIABLE or UNSATISFIABLE solution.
 * It is also used as a staging area for the solver before generating the solution.
 * Once solve() has been called, then this object becomes immutable after that.
 */

public final class A4Solution {

	//====== static immutable fields ====================================================================//

	/** The constant unary relation representing the smallest Int atom. */
	static final Relation KK_MIN = Relation.unary("Int/min");

	/** The constant unary relation representing the Int atom "0". */
	static final Relation KK_ZERO = Relation.unary("Int/zero");

	/** The constant unary relation representing the largest Int atom. */
	static final Relation KK_MAX = Relation.unary("Int/max");

	/** The constant binary relation representing the "next" relation from each Int atom to its successor. */
	static final Relation KK_NEXT = Relation.binary("Int/next");

	/** The constant unary relation representing the set of all seq/Int atoms. */
	static final Relation KK_SEQIDX = Relation.unary("seq/Int");

	/** The constant unary relation representing the set of all String atoms. */
	static final Relation KK_STRING = Relation.unary("String");

	//====== immutable fields ===========================================================================//

	/** The original Alloy options that generated this solution. */
	private final A4Options originalOptions;

	/** The original Alloy command that generated this solution; can be "" if unknown. */
	private final String originalCommand;

	/** The bitwidth; always between 1 and 30. */
	private final int bitwidth;

	/** The maximum allowed sequence length; always between 0 and 2^(bitwidth-1)-1. */
	private final int maxseq;

	/** The maximum allowed number of loop unrolling and recursion level. */
	private final int unrolls;

	/** The list of all atoms. */
	private final ConstList<String> kAtoms;

	/** The Kodkod TupleFactory object. */
	private final TupleFactory factory;

	/** The set of all Int atoms; immutable. */
	private final TupleSet sigintBounds;

	/** The set of all seq/Int atoms; immutable. */
	private final TupleSet seqidxBounds;

	/** The set of all String atoms; immutable. */
	private final TupleSet stringBounds;

	/** The Kodkod Solver object. */
	private final Solver solver;

	/** The default bitwidth to set the kodkod bidwidth in case of having out-of-bound numbers*/
	final int exceedBitWidth = 31;


	//====== mutable fields (immutable after solve() has been called) ===================================//

	/** True iff the problem is solved. */
	private boolean solved = false;

	//[VM] changed to public for debugging
	/** The Kodkod Bounds object. */
	public Bounds bounds;

	/** The list of Kodkod formulas; can be empty if unknown; once a solution is solved we must not modify this anymore */
	ArrayList<Formula> formulas = new ArrayList<Formula>();

	/** The list of known Alloy4 sigs. */
	private SafeList<Sig> sigs;

	/** If solved==true and is satisfiable, then this is the list of known skolems. */
	private SafeList<ExprVar> skolems = new SafeList<ExprVar>();

	/** If solved==true and is satisfiable, then this is the list of actually used atoms. */
	private SafeList<ExprVar> atoms = new SafeList<ExprVar>();

	/** If solved==true and is satisfiable, then this maps each Kodkod atom to a short name. */
	private Map<Object,String> atom2name = new LinkedHashMap<Object,String>();

	/** If solved==true and is satisfiable, then this maps each Kodkod atom to its most specific sig. */
	private Map<Object,PrimSig> atom2sig = new LinkedHashMap<Object,PrimSig>();

	/** If solved==true and is satisfiable, then this is the Kodkod evaluator. */
	private Evaluator eval = null;

	/** If not null, you can ask it to get another solution. */
	private Iterator<Solution> kEnumerator = null;

	/** The map from each Sig/Field/Skolem/Atom to its corresponding Kodkod expression. */
	private Map<Expr,Expression> a2k;

	/** The map from each String literal to its corresponding Kodkod expression. */
	private final ConstMap<String,Expression> s2k;

	/** The map from each kodkod Formula to Alloy Expr or Alloy Pos (can be empty if unknown) */
	private Map<Formula,Object> k2pos;

	/** The map from each Kodkod Relation to Alloy Type (can be empty or incomplete if unknown) */
	private Map<Relation,Type> rel2type;

	/** The map from each Kodkod Variable to an Alloy Type and Alloy Pos. */
	private Map<Variable,Pair<Type,Pos>> decl2type;

	/** The flag would be on if the integers in the inst block exceed the bitwidth range*/
	boolean exceededInt = false;

	List<Integer> exceededInts = new ArrayList<Integer>();

	/** The upperbound for abstract sigs is stored in regards to evaluation without execution*/
	private Map<Sig,TupleSet> abstractSUB = new LinkedHashMap<Sig,TupleSet>(); 

	//===================================================================================================//

	/** Construct a blank A4Solution containing just UNIV, SIGINT, SEQIDX, STRING, and NONE as its only known sigs.
	 * @param originalCommand  - the original Alloy command that generated this solution; can be "" if unknown
	 * @param bitwidth - the bitwidth; must be between 1 and 30
	 * @param maxseq - the maximum allowed sequence length; must be between 0 and (2^(bitwidth-1))-1
	 * @param atoms - the set of atoms
	 * @param rep - the reporter that will receive diagnostic and progress messages
	 * @param opt - the Alloy options that will affect the solution and the solver
	 * @param expected - whether the user expected an instance or not (1 means yes, 0 means no, -1 means the user did not express an expectation)
	 */
	A4Solution(String originalCommand, int bitwidth, int maxseq, Set<String> stringAtoms, Collection<String> atoms, final A4Reporter rep, A4Options opt, int expected) throws Err {
		opt = opt.dup();
		this.unrolls = opt.unrolls;
		this.sigs = new SafeList<Sig>(Arrays.asList(UNIV, SIGINT, SEQIDX, STRING, NONE));
		this.a2k = Util.asMap(new Expr[]{UNIV, SIGINT, SEQIDX, STRING, NONE}, Relation.INTS.union(KK_STRING), Relation.INTS, KK_SEQIDX, KK_STRING, Relation.NONE);
		this.k2pos = new LinkedHashMap<Formula,Object>();
		this.rel2type = new LinkedHashMap<Relation,Type>();
		this.decl2type = new LinkedHashMap<Variable,Pair<Type,Pos>>();
		this.originalOptions = opt;
		this.originalCommand = (originalCommand==null ? "" : originalCommand);
		this.bitwidth = bitwidth;
		this.maxseq = maxseq;
		if (bitwidth < 0)   throw new ErrorSyntax("Cannot specify a bitwidth less than 0");
		if (bitwidth > 30)  throw new ErrorSyntax("Cannot specify a bitwidth greater than 30");
		if (maxseq < 0)     throw new ErrorSyntax("The maximum sequence length cannot be negative.");
		if (maxseq > 0 && maxseq > max()) throw new ErrorSyntax("With integer bitwidth of "+bitwidth+", you cannot have sequence length longer than "+max());
		if (atoms.isEmpty()) {
			atoms = new ArrayList<String>(1);
			atoms.add("<empty>");
		}
		kAtoms = ConstList.make(atoms);
		bounds = new Bounds(new Universe(kAtoms));
		factory = bounds.universe().factory();

		TupleSet sigintBounds = factory.noneOf(1);
		TupleSet seqidxBounds = factory.noneOf(1);
		TupleSet stringBounds = factory.noneOf(1);
		final TupleSet next = factory.noneOf(2);

		int min=min(), max=max();

		if (max >= min){
			ConcurrentSkipListSet<Integer> ints = new ConcurrentSkipListSet<Integer>();
			for(String a: atoms){
				try{
					ints.add(Integer.valueOf(a));
				}catch(NumberFormatException e){}
			}
			if(!ints.isEmpty() && (ints.last() > max() || ints.first() < min)){
				exceededInt = true;
				if(ints.first() < min ) exceededInts.addAll(ints.subSet(ints.first(), min));
				if(ints.last() > max) exceededInts.addAll(ints.subSet(max+1, ints.last()+1 ));
			}

			for(Integer i: ints) { // Safe since we know 1 <= bitwidth <= 30
				Tuple ii = factory.tuple(""+i);
				TupleSet is = factory.range(ii, ii);
				bounds.boundExactly(i, is);
				sigintBounds.add(ii);
				if (i>=0 && i<maxseq) seqidxBounds.add(ii);
				if (i < ints.last()) next.add(factory.tuple(""+i, ""+(ints.ceiling(i+1))));
				if (i==ints.first()) bounds.boundExactly(KK_MIN,  is);
				if (i==ints.last()) bounds.boundExactly(KK_MAX,  is);
				if (i==0)   bounds.boundExactly(KK_ZERO, is);
			}
		}
		this.sigintBounds = sigintBounds.unmodifiableView();
		this.seqidxBounds = seqidxBounds.unmodifiableView();
		bounds.boundExactly(KK_NEXT, next);
		bounds.boundExactly(KK_SEQIDX, this.seqidxBounds);
		Map<String,Expression> s2k = new HashMap<String,Expression>();
		for(String e: stringAtoms) {
			Relation r = Relation.unary("");
			Tuple t = factory.tuple(e);
			s2k.put(e, r);
			bounds.boundExactly(r, factory.range(t, t));
			stringBounds.add(t);
		}
		this.s2k = ConstMap.make(s2k);
		this.stringBounds = stringBounds.unmodifiableView();
		bounds.boundExactly(KK_STRING, this.stringBounds);
		int sym = (expected==1 ? 0 : opt.symmetry);
		solver = new Solver();
		solver.options().setNoOverflow(opt.noOverflow);
		solver.options().setFlatten(false); // added for now, since multiplication and division circuit takes forever to flatten
		if (opt.solver.external()!=null) {
			String ext = opt.solver.external();
			if (opt.solverDirectory.length()>0 && ext.indexOf(File.separatorChar)<0) ext=opt.solverDirectory+File.separatorChar+ext;
			try {
				File tmp = File.createTempFile("tmp", ".cnf", new File(opt.tempDirectory));
				tmp.deleteOnExit(); 
				solver.options().setSolver(SATFactory.externalFactory(ext, tmp.getAbsolutePath(), "", opt.solver.options()));
				//solver.options().setSolver(SATFactory.externalFactory(ext, tmp.getAbsolutePath(), opt.solver.options()));
			} catch(IOException ex) { throw new ErrorFatal("Cannot create temporary directory.", ex); }
		} else if (opt.solver.equals(A4Options.SatSolver.ZChaffJNI)) {
			solver.options().setSolver(SATFactory.ZChaff);
		} else if (opt.solver.equals(A4Options.SatSolver.MiniSatJNI)) {
			solver.options().setSolver(SATFactory.MiniSat);
		} else if (opt.solver.equals(A4Options.SatSolver.MiniSatProverJNI)) {
			sym=20;
			solver.options().setSolver(SATFactory.MiniSatProver);
			solver.options().setLogTranslation(2);
			solver.options().setCoreGranularity(opt.coreGranularity);
		} else {
			solver.options().setSolver(SATFactory.DefaultSAT4J); // Even for "KK" and "CNF", we choose SAT4J here; later, just before solving, we'll change it to a Write2CNF solver
		}
		solver.options().setSymmetryBreaking(sym);
		solver.options().setSkolemDepth(opt.skolemDepth);
		//[VM] 
		if(exceededInt){
			solver.options().setBitwidth(exceedBitWidth);

		}else{
			solver.options().setBitwidth(bitwidth > 0 ? bitwidth : (int) Math.ceil(Math.log(atoms.size())) + 1);
		}
		solver.options().setIntEncoding(Options.IntEncoding.TWOSCOMPLEMENT);
	}

	/** Construct a new A4Solution that is the continuation of the old one, but with the "next" instance. */
	private A4Solution(A4Solution old) throws Err {
		if (!old.solved) throw new ErrorAPI("This solution is not yet solved, so next() is not allowed.");
		if (old.kEnumerator==null) throw new ErrorAPI("This solution was not generated by an incremental SAT solver.\n" + "Solution enumeration is currently only implemented for MiniSat and SAT4J.");
		if (old.eval==null) throw new ErrorAPI("This solution is already unsatisfiable, so you cannot call next() to get the next solution.");
		Instance inst = old.kEnumerator.next().instance();
		unrolls = old.unrolls;
		originalOptions = old.originalOptions;
		originalCommand = old.originalCommand;
		bitwidth = old.bitwidth;
		maxseq = old.maxseq;
		kAtoms = old.kAtoms;
		factory = old.factory;
		sigintBounds = old.sigintBounds;
		seqidxBounds = old.seqidxBounds;
		stringBounds = old.stringBounds;
		exceededInts = old.exceededInts;
		exceededInt = old.exceededInt;
		solver = old.solver;
		bounds = old.bounds;
		formulas = old.formulas;
		sigs = old.sigs;
		kEnumerator = old.kEnumerator;
		k2pos = old.k2pos;
		rel2type = old.rel2type;
		decl2type = old.decl2type;
		if (inst!=null) {
			eval = new Evaluator(inst, old.solver.options());
			a2k = new LinkedHashMap<Expr,Expression>();
			for(Map.Entry<Expr,Expression> e: old.a2k.entrySet())
				if (e.getKey() instanceof Sig || e.getKey() instanceof Field)
					a2k.put(e.getKey(), e.getValue());
			UniqueNameGenerator un = new UniqueNameGenerator();
			rename(this, null, null, un);
			a2k = ConstMap.make(a2k);
		} else {
			skolems = old.skolems;
			eval = null;
			a2k = old.a2k;
		}
		s2k = old.s2k;
		atoms = atoms.dup();
		atom2name = ConstMap.make(atom2name);
		atom2sig = ConstMap.make(atom2sig);
		solved = true;
	}

	/** Turn the solved flag to be true, and make all remaining fields immutable. */
	private void solved() {
		if (solved) return; // already solved
		bounds = bounds.clone().unmodifiableView();
		sigs = sigs.dup();
		skolems = skolems.dup();
		atoms = atoms.dup();
		atom2name = ConstMap.make(atom2name);
		atom2sig = ConstMap.make(atom2sig);
		a2k = ConstMap.make(a2k);
		k2pos = ConstMap.make(k2pos);
		rel2type = ConstMap.make(rel2type);
		decl2type = ConstMap.make(decl2type);
		solved = true;
	}

	//===================================================================================================//

	/** Returns the bitwidth; always between 1 and 30. */
	public int getBitwidth() { return bitwidth; }

	/** Returns the maximum allowed sequence length; always between 0 and 2^(bitwidth-1)-1. */
	public int getMaxSeq() { return maxseq; }

	/** Returns the largest allowed integer, or -1 if no integers are allowed. */
	public int max() { return Util.max(bitwidth); }

	/** Returns the smallest allowed integer, or 0 if no integers are allowed */
	public int min() { return Util.min(bitwidth); }

	/** Returns the maximum number of allowed loop unrolling or recursion level. */
	public int unrolls() { return unrolls; }

	//===================================================================================================//

	/** Returns the original Alloy file name that generated this solution; can be "" if unknown. */
	public String getOriginalFilename() { return originalOptions.originalFilename; }

	/** Returns the original command that generated this solution; can be "" if unknown. */
	public String getOriginalCommand() { return originalCommand; }

	//===================================================================================================//

	/** Returns the Kodkod input used to generate this solution; returns "" if unknown. */
	public String debugExtractKInput() {
		if (solved)
			return TranslateKodkodToJava.convert(Formula.and(formulas), bitwidth, kAtoms, bounds, atom2name);
		else
			return TranslateKodkodToJava.convert(Formula.and(formulas), bitwidth, kAtoms, bounds.unmodifiableView(), null);
	}

	public List<Integer> getExceededInts(){
		return this.exceededInts;
	}

	public boolean isExceededInt(){
		return this.exceededInt;
	}
	//===================================================================================================//

	/** Returns the Kodkod TupleFactory object. */
	TupleFactory getFactory() { return factory; }

	/** Returns a modifiable copy of the Kodkod Bounds object. */
	Bounds getBounds() { return bounds.clone(); }

	/** Add a new relation with the given label and the given lower and upper bound.
	 * @param label - the label for the new relation; need not be unique
	 * @param lower - the lowerbound; can be null if you want it to be the empty set
	 * @param upper - the upperbound; cannot be null; must contain everything in lowerbound
	 */
	Relation addRel(String label, TupleSet lower, TupleSet upper) throws ErrorFatal {

		if (solved) throw new ErrorFatal("Cannot add a Kodkod relation since solve() has completed.");
		Relation rel = Relation.nary(label, upper.arity());
		if (lower == upper) {
			bounds.boundExactly(rel, upper);
		} else if (lower == null || lower.size() == 0) {
			bounds.bound(rel, upper);

		} else {
			if (lower.arity() != upper.arity()) throw new ErrorFatal("Relation "+label+" must have same arity for lowerbound and upperbound.");
			bounds.bound(rel, lower, upper);

		}
		return rel;
	}



	/** Add a new sig to this solution and associate it with the given expression (and if s.isTopLevel then add this expression into Sig.UNIV).
	 * <br> The expression must contain only constant Relations or Relations that are already bound in this solution.
	 * <br> (If the sig was already added by a previous call to addSig(), then this call will return immediately without altering what it is associated with)
	 */
	void addSig(Sig s, Expression expr) throws ErrorFatal {
		if (solved) throw new ErrorFatal("Cannot add an additional sig since solve() has completed.");
		if (expr.arity()!=1) throw new ErrorFatal("Sig "+s+" must be associated with a unary relational value.");
		if (a2k.containsKey(s)) return;
		a2k.put(s, expr);
		sigs.add(s);
		if (s.isTopLevel()) a2k.put(UNIV, a2k.get(UNIV).union(expr));
	}

	/** Add a new field to this solution and associate it with the given expression.
	 * <br> The expression must contain only constant Relations or Relations that are already bound in this solution.
	 * <br> (If the field was already added by a previous call to addField(), then this call will return immediately without altering what it is associated with)
	 */
	void addField(Field f, Expression expr) throws ErrorFatal {
		if (solved) throw new ErrorFatal("Cannot add an additional field since solve() has completed.");
		if (expr.arity()!=f.type().arity()) throw new ErrorFatal("Field "+f+" must be associated with an "+f.type().arity()+"-ary relational value.");
		if (a2k.containsKey(f)) return;
		a2k.put(f, expr);
	}

	/** Add a new skolem to this solution and associate it with the given expression.
	 * <br> The expression must contain only constant Relations or Relations that are already bound in this solution.
	 */
	private ExprVar addSkolem(String label, Type type, Expression expr) throws Err {
		if (solved) throw new ErrorFatal("Cannot add an additional skolem since solve() has completed.");
		int a = type.arity();
		if (a<1) throw new ErrorFatal("Skolem "+label+" must be associated with a relational value.");
		if (a!=expr.arity()) throw new ErrorFatal("Skolem "+label+" must be associated with an "+a+"-ary relational value.");
		ExprVar v = ExprVar.make(Pos.UNKNOWN, label, type);
		a2k.put(v, expr);
		skolems.add(v);
		return v;
	}

	/**
	 * The bounds object does not contain the abstract sigs, so the evaluator operators do not work properly.
	 * After computing the bounds, the abstract sigs are stored here to be added to the temporary instance
	 * to get evaluated.
	 * @param sig
	 * @param ub
	 * @throws ErrorFatal
	 */
	void addAbstractUpperBound(Sig sig, TupleSet ub) throws ErrorFatal{
		if (solved) throw new ErrorFatal("Cannot add an upperbound of an abstract sig since solve() has completed.");
		abstractSUB.put(sig, ub);
	}

	/** Returns an unmodifiable copy of the map from each Sig/Field/Skolem/Atom to its corresponding Kodkod expression. */
	ConstMap<Expr,Expression> a2k()  { return ConstMap.make(a2k); }

	/** Returns an unmodifiable copy of the map from each String literal to its corresponding Kodkod expression. */
	ConstMap<String,Expression> s2k()  { return s2k; }

	/** Returns the corresponding Kodkod expression for the given Sig, or null if it is not associated with anything. */
	Expression a2k(Sig sig)  { return a2k.get(sig); }

	/** Returns the corresponding Kodkod expression for the given Field, or null if it is not associated with anything. */
	Expression a2k(Field field)  { return a2k.get(field); }

	/** Returns the corresponding Kodkod expression for the given Atom/Skolem, or null if it is not associated with anything. */
	Expression a2k(ExprVar var)  { return a2k.get(var); }

	/** Returns the corresponding Kodkod expression for the given String constant, or null if it is not associated with anything. */
	Expression a2k(String stringConstant)  { return s2k.get(stringConstant); }

	/** Returns the corresponding Kodkod expression for the given expression, or null if it is not associated with anything. */
	Expression a2k(Expr expr) throws ErrorFatal {
		while(expr instanceof ExprUnary) {
			if (((ExprUnary)expr).op==ExprUnary.Op.NOOP) { expr = ((ExprUnary)expr).sub; continue; }
			if (((ExprUnary)expr).op==ExprUnary.Op.EXACTLYOF) { expr = ((ExprUnary)expr).sub; continue; }
			break;
		}
		if (expr instanceof ExprConstant && ((ExprConstant)expr).op==ExprConstant.Op.EMPTYNESS) return Expression.NONE;
		if (expr instanceof ExprConstant && ((ExprConstant)expr).op==ExprConstant.Op.STRING) return s2k.get(((ExprConstant)expr).string);
		if (expr instanceof Sig || expr instanceof Field || expr instanceof ExprVar) return a2k.get(expr);
		if (expr instanceof ExprBinary) {
			Expr a=((ExprBinary)expr).left, b=((ExprBinary)expr).right;
			switch(((ExprBinary)expr).op) {
			case ARROW: return a2k(a).product(a2k(b));
			case PLUS: return a2k(a).union(a2k(b));
			case MINUS: return a2k(a).difference(a2k(b));
			//TODO: IPLUS, IMINUS???
			}
		}
		return null; // Current only UNION, PRODUCT, and DIFFERENCE of Sigs and Fields and ExprConstant.EMPTYNESS are allowed in a defined field's definition.
	}

	/** Return a modifiable TupleSet representing a sound overapproximation of the given expression. */
	TupleSet approximate(Expression expression) {
		return factory.setOf(expression.arity(), Translator.approximate(expression, bounds, solver.options()).denseIndices());
	}

	/** Query the Bounds object to find the lower/upper bound; throws ErrorFatal if expr is not Relation, nor a union of Relations. */
	TupleSet query(boolean findUpper, Expression expr, boolean makeMutable) throws ErrorFatal {
		if (expr==Relation.NONE) return factory.noneOf(1);
		if (expr==Relation.INTS) return makeMutable ? sigintBounds.clone() : sigintBounds;
		if (expr==KK_SEQIDX) return makeMutable ? seqidxBounds.clone() : seqidxBounds;
		if (expr==KK_STRING) return makeMutable ? stringBounds.clone() : stringBounds;
		if (expr instanceof Relation) {
			TupleSet ans = findUpper ? bounds.upperBound((Relation)expr) : bounds.lowerBound((Relation)expr);
			if (ans!=null) return makeMutable ? ans.clone() : ans;
		}
		else if (expr instanceof BinaryExpression) {
			BinaryExpression b = (BinaryExpression)expr;
			if (b.op() == ExprOperator.UNION) {
				TupleSet left = query(findUpper, b.left(), true);
				TupleSet right = query(findUpper, b.right(), false);
				left.addAll(right);
				return left;
			}
		}
		throw new ErrorFatal("Unknown expression encountered during bounds computation: "+expr);
	}

	/** Shrink the bounds for the given relation; throws an exception if the new bounds is not sameAs/subsetOf the old bounds. */
	void shrink(Relation relation, TupleSet lowerBound, TupleSet upperBound) throws Err {
		if (solved) throw new ErrorFatal("Cannot shrink a Kodkod relation since solve() has completed.");
		TupleSet oldL = bounds.lowerBound(relation);
		TupleSet oldU = bounds.upperBound(relation);
		if (oldU.containsAll(upperBound) && upperBound.containsAll(lowerBound) && lowerBound.containsAll(oldL)) {
			bounds.bound(relation, lowerBound, upperBound);
		} else {
			throw new ErrorAPI("Inconsistent bounds shrinking on relation: "+relation);
		}
	}

	//===================================================================================================//

	/** Returns true iff the problem has been solved and the result is satisfiable. */
	public boolean satisfiable() { return eval!=null; }

	/** Returns an unmodifiable copy of the list of all sigs in this solution's model; always contains UNIV+SIGINT+SEQIDX+STRING+NONE and has no duplicates. */
	public SafeList<Sig> getAllReachableSigs() { return sigs.dup(); }

	/** Returns an unmodifiable copy of the list of all skolems if the problem is solved and is satisfiable; else returns an empty list. */
	public Iterable<ExprVar> getAllSkolems() { return skolems.dup(); }

	/** Returns an unmodifiable copy of the list of all atoms if the problem is solved and is satisfiable; else returns an empty list. */
	public Iterable<ExprVar> getAllAtoms() { return atoms.dup(); }

	/** Returns the short unique name corresponding to the given atom if the problem is solved and is satisfiable; else returns atom.toString(). */
	String atom2name(Object atom) { 
		String ans=atom2name.get(atom); return ans==null ? atom.toString() : ans; 
	}

	/** Returns the most specific sig corresponding to the given atom if the problem is solved and is satisfiable; else returns UNIV. */
	PrimSig atom2sig(Object atom) { PrimSig sig=atom2sig.get(atom); return sig==null ? UNIV : sig; }

	/** Caches eval(Sig) and eval(Field) results. */
	private Map<Expr,A4TupleSet> evalCache = new LinkedHashMap<Expr,A4TupleSet>();

	/** Return the A4TupleSet for the given sig (if solution not yet solved, or unsatisfiable, or sig not found, then return an empty tupleset) */
	public A4TupleSet eval(Sig sig) {
		try {
			if (!solved || eval==null) return new A4TupleSet(factory.noneOf(1), this);
			A4TupleSet ans = evalCache.get(sig);
			if (ans!=null) return ans;
			TupleSet ts = eval.evaluate((Expression) TranslateAlloyToKodkod.alloy2kodkod(this, sig));
			ans = new A4TupleSet(ts, this);
			evalCache.put(sig, ans);
			return ans;
		} catch(Err er) {
			return new A4TupleSet(factory.noneOf(1), this);
		}
	}

	/** Return the A4TupleSet for the given field (if solution not yet solved, or unsatisfiable, or field not found, then return an empty tupleset) */
	public A4TupleSet eval(Field field) {
		try {
			if (!solved || eval==null) return new A4TupleSet(factory.noneOf(field.type().arity()), this);
			A4TupleSet ans = evalCache.get(field);
			if (ans!=null) return ans;
			TupleSet ts = eval.evaluate((Expression) TranslateAlloyToKodkod.alloy2kodkod(this, field));
			ans = new A4TupleSet(ts, this);
			evalCache.put(field, ans);
			return ans;
		} catch(Err er) {
			return new A4TupleSet(factory.noneOf(field.type().arity()), this);
		}
	}

	/** If this solution is solved and satisfiable, evaluates the given expression and returns an A4TupleSet, a java Integer, or a java Boolean. */
	public Object eval(Expr expr) throws Err {
		try {
			if (expr instanceof Sig) return eval((Sig)expr);
			if (expr instanceof Field) return eval((Field)expr);
			if (!solved) throw new ErrorAPI("This solution is not yet solved, so eval() is not allowed.");
			if (eval==null) throw new ErrorAPI("This solution is unsatisfiable, so eval() is not allowed.");
			if (expr.ambiguous && !expr.errors.isEmpty()) expr = expr.resolve(expr.type(), null);
			if (!expr.errors.isEmpty()) throw expr.errors.pick();
			Object result = TranslateAlloyToKodkod.alloy2kodkod(this, expr);
			if (result instanceof IntExpression) return eval.evaluate((IntExpression)result) + (eval.wasOverflow() ? " (OF)" : "");
			if (result instanceof Formula) return eval.evaluate((Formula)result);
			if (result instanceof Expression) return new A4TupleSet(eval.evaluate((Expression)result), this);
			throw new ErrorFatal("Unknown internal error encountered in the evaluator.");
		} catch(CapacityExceededException ex) {
			throw TranslateAlloyToKodkod.rethrow(ex);
		}
	}



	//private List<Integer> indices =  new ArrayList<Integer>();

	private AlloyTuplesList getAllSubsets(TupleSet tuples){


		TupleSet emptyTupleSet = tuples.clone();
		emptyTupleSet.clear();

		AlloyTuplesList cur_subsets = new AlloyTuplesList();
		AlloyTuplesList pre_subsets = new AlloyTuplesList();

		TupleSet fstSet = emptyTupleSet.clone();

		//Convert the TupleSet to a list.
		List<Tuple> tuples_list = TupleSet2List(tuples);

		if(tuples_list.size()>0){
			fstSet.add(tuples_list.get(0));
			pre_subsets.add(fstSet);
			//cur_subsets.add(new HashSet());
			cur_subsets.add(fstSet);
		}

		for(int i=1; i < tuples_list.size(); i++){
			cur_subsets = new AlloyTuplesList();
			TupleSet iSet = emptyTupleSet.clone();
			iSet.add(tuples_list.get(i));
			cur_subsets.add(iSet);
			//merging
			for(TupleSet set: pre_subsets){
				cur_subsets.add(set.clone());
				set.add(tuples_list.get(i));
				cur_subsets.add(set);
			}
			pre_subsets = cur_subsets;
		}

		return cur_subsets;
	}

	private AlloyTuplesList getAllSubsetsWithEmptySubSet(final TupleSet tuples){

		TupleSet emptyTupleSet = tuples.clone();
		emptyTupleSet.clear();

		AlloyTuplesList ret = new AlloyTuplesList();

		ret.add(emptyTupleSet);
		ret.addAll(getAllSubsets(tuples));

		return ret;
	}


	private List<Tuple> TupleSet2List(final TupleSet tuples){
		List<Tuple> tuples_list = new ArrayList<Tuple>();
		Iterator<Tuple> itrTuples = tuples.iterator();
		while(itrTuples.hasNext()){
			tuples_list.add(itrTuples.next());
		}
		return tuples_list;
	}

	private AlloyTuplesList getAllElements(final TupleSet tuples){
		AlloyTuplesList ret = new AlloyTuplesList();

		//Convert the TupleSet to a list.
		List<Tuple> tuples_list = TupleSet2List(tuples);

		//Make an empty TupleSet
		TupleSet emptyTupleSet = tuples.clone();
		emptyTupleSet.clear();

		for(int i=0; i< tuples_list.size(); i++){
			TupleSet newTupleSet = emptyTupleSet.clone();
			newTupleSet.add(tuples_list.get(i));
			ret.add(newTupleSet);
		}
		if(ret.size() == 0)
			ret.add(emptyTupleSet);
		return ret;
	}

	protected abstract class Counter implements Iterator<List<Integer>>{

		protected final ArrayList<Integer> current ;
		protected final ArrayList<Integer> max;
		protected int maxCap = 0;

		public Counter(final ArrayList<Integer> current,final  ArrayList<Integer> max, final int maxCap){
			this.current = new ArrayList<Integer>(current);
			this.max = new ArrayList<Integer>(max);
			this.maxCap = maxCap;
		}


		public Counter(List<Integer> max){
			this();
			for(int i=0; i < max.size(); i++){
				addMax(max.get(i));
			}
		}

		public Counter(){
			this.current = new ArrayList<Integer>();
			this.max = new ArrayList<Integer>();
			this.maxCap = 0;

		}

		public abstract void addMax(int max);

		public abstract void resetCounter();

		public abstract Counter clone();

		public void remove() {}

	}

	protected class IncreasingCounter extends Counter{

		public IncreasingCounter(final ArrayList<Integer> current,final  ArrayList<Integer> max, final int maxCap){
			super(current,max,maxCap);
		}

		public IncreasingCounter(List<Integer> max){
			super(max);
		}

		public IncreasingCounter(){
			super();
		}

		public boolean hasNext() {
			return maxCap>0;
		}

		public void addMax(int max){
			if(maxCap==0) maxCap=1;
			maxCap = maxCap* max;
			this.max.add(max);
			this.current.add(0);
			resetCounter();    		
		}

		public  void resetCounter(){
			for(int i=0; i< current.size(); i++){
				current.set(i, 0);
			}
			if(current.size() > 0){
				current.set(current.size()-1, -1);    			
			}
		}


		public List<Integer> next() {
			int index = current.size()-1;
			do{
				if (current.get(index) > max.get(index)-2){
					current.set(index, 0); 
					index--;
					if(index  < 0)
						return null;
				}else{
					current.set(index,current.get(index)+1);
					maxCap--;
					return current;
				}
			}while(true);
		}

		public Counter clone(){
			return new IncreasingCounter(current, max, maxCap);
		}

	}


	protected class DecreasingCounter extends Counter{

		public DecreasingCounter(final ArrayList<Integer> current,final  ArrayList<Integer> max, final int maxCap){
			super(current,max,maxCap);
		}

		public DecreasingCounter(List<Integer> max){
			super(max);
		}

		public DecreasingCounter(){
			super();
		}

		public void addMax(int max){
			if(maxCap==0) maxCap=1;
			maxCap = maxCap* max;
			this.max.add(max);
			current.add(max-1/*or max -1*/);
			resetCounter();    		
		}

		public  void resetCounter(){
			for(int i=0; i< current.size(); i++){
				current.set(i, max.get(i)-1);
			}
			if(current.size() > 0){
				current.set(current.size()-1, max.get(current.size()-1));    			
			}
		}


		public boolean hasNext() {
			return maxCap>0;
		}


		public List<Integer> next() {
			int index = current.size()-1;
			do{
				if (current.get(index) == 0){
					current.set(index, max.get(index)-1);
					index--;
					if(index  < 0)
						return null;
				}else{
					current.set(index,current.get(index)-1);
					maxCap--;
					return current;
				}
			}while(true);
		}

		public Counter clone(){
			return new DecreasingCounter(current, max, maxCap);
		}

	}








	private class FieldOrder{
		/*
		 * (Sig->Field)
		 * (Sig,Int)->Field
		 */
		Map<String ,Sig.Field> sigFieldOrder = new HashMap<String, Sig.Field>();
		Map<String, Set<Sig.Field>> sigFields = new HashMap<String, Set<Field>>();
		Map<String, Integer> fieldIntOrder = new HashMap<String, Integer>();

		public void putFields(Sig sig, Sig.Field field){
			Set<Sig.Field> set;
			if(!this.sigFields.containsKey(sig.label)){
				set = new HashSet<Sig.Field>();
			}else{
				set = sigFields.remove(sig.label);
			}
			set.add(field);
			sigFields.put(sig.label, set);
		}

		public void putFieldOrder(Sig sig, Integer i, Sig.Field field){
			this.sigFieldOrder.put(sig.label+i, field);
			this.fieldIntOrder.put(field.label, i);
		}

		public Set<Sig.Field>  getFields(Sig sig){
			return this.sigFields.get(sig.label);
		}

		public Sig.Field getFieldOrder(Sig sig, Integer i){
			return this.sigFieldOrder.get(sig.label+i);
		}

		public Integer getFieldOrder(Sig.Field field){
			return fieldIntOrder.get(field.label);
		}
	}

	private Map<Field,TupleSet> getTupleSet(Object atom1, Sig sig, List<Integer> indices, Universe universe, 
			List<List<Set<List>>> allInstances, FieldOrder fieldsOrder){

		Map<Field,TupleSet> tupleSets = new HashMap<Sig.Field, TupleSet>();
		//List of list of tuple sets
		Map<Field,List<Tuple>> tuplesLists = new HashMap<Sig.Field, List<Tuple>>();
		for(Field field: fieldsOrder.getFields(sig)){
			tuplesLists.put(field, new ArrayList<Tuple>());
		}
		//Set a counter to generate all possible tuples
		Counter tupleCntr = new IncreasingCounter();
		List<Set<List>> tuples = new ArrayList<Set<List>>();
		for(int i=0; i < indices.size(); i++){
			tuples.add(allInstances.get(i).get(indices.get(i)));
			tupleCntr.addMax(tuples.get(tuples.size()-1).size());
		}
		final TupleFactory factory = universe.factory();
		//List<Tuple> tupleList = new ArrayList<Tuple>();
		while(tupleCntr.hasNext()){
			List<Integer> next = tupleCntr.next();
			//List of tuples
			Map<Field,List> tuplesList = new HashMap<Sig.Field, List>();
			for(Field field: fieldsOrder.getFields(sig)){
				tuplesList.put(field, new ArrayList());
				tuplesList.get(field).add(atom1);
			}

			for(int i=0; i<next.size(); i++){
				Set ts = tuples.get(i);
				List iTh = (List)ts.toArray()[next.get(i)];

				tuplesList.get(fieldsOrder.getFieldOrder(sig, i)).add(((Tuple)iTh.get(0)).atom(0));
			}

			//Add all the extracted tuples with respect to their fields
			for(Field field: fieldsOrder.getFields(sig)){
				tuplesLists.get(field).add(factory.tuple(tuplesList.get(field)));
			}

		}

		for(Field field: fieldsOrder.getFields(sig)){
			tupleSets.put(field, factory.setOf(tuplesLists.get(field)));
		}

		return tupleSets;

	}

	private List<Set> instancesGenerator(int indx, int next ){



		/*	
    	List<Set> ret;
    	if(indx == allInstances.size()-1){
    		ret = new ArrayList<Set>();
    		Set set = new HashSet();
    		set.add(allInstances.get(indx).get(next));
    		ret.add(set);
    	}else{

    		ret = instancesGenerator(indx+1,);

    	}*/
		return null;
	}

	private LinkedList<ExprUnary.Op> findMultiplicity(Expr expr){
		LinkedList<ExprUnary.Op> result = null;
		if(expr instanceof ExprUnary){
			result = new LinkedList<ExprUnary.Op>();
			result.add(((ExprUnary)expr).mult());
		}else if(expr instanceof ExprBinary){

			result = findMultiplicity(((ExprBinary)expr).right);
			switch(((ExprBinary)expr).op){
			case LONE_ARROW_LONE:
				result.set(0, ExprUnary.Op.LONEOF);
				result.addFirst(ExprUnary.Op.LONEOF);
				break;
			case LONE_ARROW_ONE:
				result.set(0, ExprUnary.Op.ONEOF);
				result.addFirst(ExprUnary.Op.LONEOF);
				break;
			case ONE_ARROW_ONE:
				result.set(0, ExprUnary.Op.ONEOF);
				result.addFirst(ExprUnary.Op.ONEOF);
				break;
			case ANY_ARROW_ONE:
				result.set(0, ExprUnary.Op.ONEOF);
				result.addFirst(ExprUnary.Op.NOOP);
				break;
			case ONE_ARROW_ANY:
				result.set(0, ExprUnary.Op.NOOP);
				result.addFirst(ExprUnary.Op.ONEOF);
				break;
			case ARROW:
				result.set(0, ExprUnary.Op.NOOP);
				result.addFirst(ExprUnary.Op.NOOP);
				break;

			}
		}    	
		return result;
	}

	private int computeUBField(List<ExprUnary.Op> ops, List<Integer> cardinals){

		int i = 0;
		int result = 0;
		switch(ops.get(i)){
		case ONEOF:
			result = cardinals.get(i);
			break;
		case SETOF:
			result = (int)Math.pow(2, cardinals.get(i));
			break;
		case SOMEOF:
			result = (int)Math.pow(2, cardinals.get(i)) -1;
			break;
		case LONEOF:
			result = cardinals.get(i) +1;
			break;

		}


		return result;
	}

	public Map<Sig, Integer> getMaxPossibleTuples(){
		Map<Sig, Integer> maxNums = new HashMap<Sig, Integer>();
		for(Sig s: sigs){
			//Find the sigs supposed to be unique
			if(s.isUnique != null){
				//List<List<Set>> allInstances = new ArrayList<List<Set>>();
				//Counter instCounter = new Counter();
				//Find the related sigs
				//int iFldPos = 0;
				int upperB = 1;
				for(Field d: s.getFields()){
					List<Integer> cardinals = new ArrayList<Integer>();
					List<List<PrimSig>> f = s.join(d).type().fold();
					for(Sig sig: f.get(0)){
						cardinals.add(abstractSUB.get(sig).size());
						//System.out.println(d.decl().expr.mul(sig));
						//allInstances.add(getAllElements(new ArrayList( abstractSUB.get(sig))));
						//instCounter.addMax(allInstances.get(allInstances.size()-1).size());
						//fieldsOrder.put(new Pair<Sig, Integer>(s, iFldPos++), d);
					}
					//Decide how many unique atoms should be existed regarding the relations
					upperB *= computeUBField(findMultiplicity(d.decl().expr),cardinals);
				}
				maxNums.put(s, upperB);
				//uniqueSigs.put(s, allInstances);
				//uniqueInstsCntr.put(s,instCounter);
			}
		}
		return maxNums;
	}

	private final TupleSet changeTupleSetUniverse(final Universe universe, final TupleSet oldTupleSet){
		if (oldTupleSet.size() == 0)
			return universe.factory().noneOf(oldTupleSet.arity());
		else{
			List<Tuple> newTuples = new ArrayList<Tuple>();
			for(Tuple oldTuple: oldTupleSet){
				List<Object> newTuple = new ArrayList<Object>();
				for(int i=0; i < oldTuple.arity(); i++)
					newTuple.add(oldTuple.atom(i));
				try{
					newTuples.add(universe.factory().tuple(newTuple));
				}catch( java.lang.IllegalArgumentException e){}
			}
			if(newTuples.size()>0){
				return universe.factory().setOf(newTuples);
			}else{
				return universe.factory().noneOf(oldTupleSet.arity());
			}

		}
	}

	private Map<Field,TupleSet> mergeFieldsTupleSets(Map<Field,TupleSet> oldMap, Map<Field,TupleSet> newMap){
		for(Sig.Field newField: newMap.keySet()){
			if(!oldMap.containsKey(newField)){
				oldMap.putAll(newMap);
			}else{
				TupleSet newTupleSet = newMap.get(newField);
				TupleSet oldTupleSet = oldMap.get(newField);
				oldTupleSet.addAll(newTupleSet);
				oldMap.put(newField, oldTupleSet);
			}
		}
		return oldMap;
	}

	private TupleSet TupleSetsProduct(final TupleSet ts1, final TupleSet ts2){
		Counter counter = new IncreasingCounter();
		TupleFactory factory = ts1.universe().factory();
		counter.addMax(ts1.size());
		counter.addMax(ts1.size());
		List<Tuple> tupleList = new ArrayList<Tuple>();

		List<Tuple> ts1lst = TupleSet2List(ts1);
		List<Tuple> ts2lst = TupleSet2List(ts2);

		while(counter.hasNext()){
			List<Integer> current = counter.next();
			tupleList.add(factory.tuple(ts1lst.get(current.get(0)), ts2lst.get(current.get(1))));
		}
		return factory.setOf(tupleList);
	}

	private class MultipleAlloyTuples extends HashSet<AlloyTuples>{

		public MultipleAlloyTuples(){
			super();
		}

		public MultipleAlloyTuples(Collection<AlloyTuples> items ){
			super();
			for(AlloyTuples item : items){
				this.add(item);
			}
		}

		public String toString(){
			Object[] items = super.toArray();
			StringBuilder ret = new StringBuilder();
			ret.append("[");
			for(int i=0; i < items.length; i++){
				ret.append( items[i]).append("+");
			}
			ret.delete(ret.length()-1, ret.length());
			ret.append("]");
			return ret.toString();	
		}	

	}

	private class AlloyDependentTuplesList extends AlloyTuplesList/* implements Iterable<TupleSet> */{

		//Map the dependent tupleset to the range
		private final HashMap<String, AlloyTuplesList> lists = new HashMap<String, AlloyTuplesList>();
		private final static String UNKNOWN = "unknown";

		public AlloyDependentTuplesList(){}


		public AlloyDependentTuplesList(AlloyDependentTuplesList that){
			super();
			super.addAll(that);
			this.lists.putAll(that.lists);
		}

		public AlloyDependentTuplesList(final TupleSet tupleSet, final Collection<TupleSet> alloyTuplesList){
			super(alloyTuplesList);
			lists.put(tupleSet.toString(), new AlloyTuplesList(alloyTuplesList));
		}

		private boolean addLocal(final String key, final TupleSet e){
			AlloyTuplesList currentList = lists.get(key);
			if(currentList == null){
				currentList = new AlloyTuplesList();
			}
			return (currentList.add(e)) && (lists.put(key, currentList)!=null);
		}

		private boolean add(final String key, final TupleSet e){
			boolean ret = false;
			ret = super.add(e);
			ret = ret & addLocal(key,e);
			return ret;
		}

		public boolean add(final TupleSet tupleSet, final TupleSet e){
			return add(tupleSet.toString(),e);
		}


		public boolean add(TupleSet e){
			return add(UNKNOWN,e);
		}

		private boolean addAllLocal(final String key, final AlloyTuplesList e){
			AlloyTuplesList currentList = lists.get(key);
			if(currentList == null){
				currentList = new AlloyTuplesList();
			}
			return (currentList.addAll(e)) && (lists.put(key, currentList)!=null);
		}

		private boolean addAll(final String key, final AlloyTuplesList e){
			boolean ret = true;
			for(TupleSet ts:e){
				ret = ret & super.add(ts);
			}
			return ret & this.addAllLocal(key,e) ;
		}

		public boolean addAll(final TupleSet tupleSet, final AlloyTuplesList group){
			return addAll(tupleSet.toString(),group);
		}

		private boolean addAll(final String key, final Collection<TupleSet> c){
			AlloyTuplesList alloyTuplesList = new AlloyTuplesList();
			alloyTuplesList.addAll(c);
			return addAll(key,alloyTuplesList);
		}

		public boolean addAll(final TupleSet key, final Collection<TupleSet> c){
			return this.addAll(key.toString(),c);
		}



		public void add(int index,TupleSet element){
			addLocal(UNKNOWN,element);
			super.add(index, element); 
		}

		public final AlloyTuplesList getAllAlloyTuplesLists(){
			AlloyTuplesList ret = new AlloyTuplesList();
			for(AlloyTuplesList alloyTuplesList: lists.values()){
				ret.addAll(alloyTuplesList);
			}
			return ret;
		}

		public AlloyDependentTuplesList clone(){
			return new AlloyDependentTuplesList(this);
		}

		public String toString(){
			StringBuilder ret = new StringBuilder();
			ret.append("#").append(this.size());
			for(String key:lists.keySet()){
				ret.append("{#").append(key).append("=").append(lists.get(key).size()).append("->").append(lists.get(key)).append("}").append("\n");
			}
			if(ret.length() > 0) ret.delete(ret.length()-1, ret.length());

			ret.insert(0,"<").append(">");
			return ret.toString();
		}
		/*
		@Override
		public Iterator<TupleSet> iterator() {
			// TODO Auto-generated method stub
			return new AlloyDependentTuplesListIterator();
		}

		private class AlloyDependentTuplesListIterator implements Iterator<TupleSet>{

			private int currentRow;
			private int currentCol;
			//private final  HashMap<String, AlloyTuplesList> lists;
			private final String[] keys;

			public AlloyDependentTuplesListIterator(/*final HashMap<String, AlloyTuplesList> lists*//*){
				//this.lists = lists;
				keys = (String[]) lists.keySet().toArray();
				currentRow = 0;
				currentCol = -1;
			}

			@Override
			public boolean hasNext() {
				if((currentRow == keys.length-1) && 
						(currentCol == lists.get(keys[currentRow]).size()) )
					return true;
				return false;
			}

			@Override
			public TupleSet next() {

				if(currentCol < lists.get(keys[currentRow]).size()){
					currentCol++;
				}else if(currentRow < keys.length){
					currentRow++;
					currentCol=0;
				}else{
					return null;
				}
				return lists.get(keys[currentRow]).get(currentCol);
			}

			@Override
			public void remove() {
				altered = true;
				lists.get(keys[keys.length-1]).removeLast();
			}

		}
			 */

	}

	private class AlloyTuplesList extends ArrayList<TupleSet> {


		private static final long serialVersionUID = 11478473247238472L;

		public AlloyTuplesList() {
			super();
		}

		public AlloyTuplesList(Collection<TupleSet> alloyTuplesList) {
			super(alloyTuplesList);
		}



		public final TupleSet list2TupleSet(){
			if(!isEmpty()){
				TupleSet empty_tupleSet = get(0).clone();
				empty_tupleSet.clear();
				TupleSet ret = empty_tupleSet.clone();
				for(int i=0;i<size();i++){
					ret.addAll(get(i));
				}
				return ret;
			}else{
				return null;
			}
		}


		public final TupleSet getMaxTupleSet(){
			TupleSet max = null;
			if(this.size()>0)
				max = this.get(0);
			for(TupleSet current:this){
				if(max.size() < current.size())
					max=current;
			}
			return max;
		}

		public final AlloyTuples getFieldTuple(final TupleFactory tupleFactory) throws Exception{
			return this.getFieldTuple(tupleFactory,"");
		}


		public final AlloyTuples getFieldTuple(final TupleFactory tupleFactory, final String uniqAtom) throws Exception{
			AlloyTuples ret = new AlloyTuples();
			if(this.size() > 1)
				throw new Exception("Each instances is supposed to have one TupleSet");

			//Get first
			TupleSet firstTuple = this.get(0);

			for(Tuple rightTuple:firstTuple){
				List<Object> notPrccsed = new ArrayList<Object>();
				//To support the one signature
				if(!uniqAtom.equals(""))
					notPrccsed.add(uniqAtom);
				for(int i=0;i<rightTuple.arity();i++){
					notPrccsed.add(rightTuple.atom(i));
				}
				Tuple urightTuple = tupleFactory.tuple(notPrccsed);
				ret.add(urightTuple);
			}
			return ret;
		}

		public String toString(){
			Object[] items = super.toArray();
			StringBuilder ret = new StringBuilder();

			for(int i=0; i < items.length; i++){
				ret.append( items[i].toString()).append( ",");
			}
			if(ret.length() > 0) ret.delete(ret.length()-1, ret.length());
			ret.insert(0, "(").append(")");
			return ret.toString();	
		}	

		public AlloyTuplesList clone(){
			return new AlloyTuplesList(this);
		}
	}

	private class FieldDependecyGroup{

		private final HashMap<String, Integer> fldLevel = new HashMap<String, Integer>();  
		private final HashMap<String, Set<String>> depTable = new HashMap<String, Set<String>>();
		private final HashMap<String, Sig.Field> name2Field = new HashMap<String, Sig.Field>();

		private void putDepField_(final String key,final String value) {
			Set<String> crntFldSet = this.depTable.get(key);
			if(crntFldSet == null){
				crntFldSet = new HashSet<String>();
			}
			if (value!=null)
				crntFldSet.add(value);
			this.depTable.put(key.toString(), crntFldSet);
			for(String fld: this.depTable.keySet()){
				this.fldLevel.put(fld,getLevel_(fld));
			}
		}

		public void putDepField(final Sig.Field key,final Sig.Field value) {
			if(key!=null)
				this.name2Field.put(key.toString(), key);
			if(value!=null)
				this.name2Field.put(value.toString(), value);
			putDepField_(key.toString(),value != null ?value.toString():null);
		}

		public void putDepExprs(final Sig.Field key,final Collection<Expr> c) {
			boolean added = false;
			for(Expr expr:c){
				if (expr instanceof Sig.Field) {
					putDepField(key,(Sig.Field)expr);
					added = true;
				}
			}	
			if(!added)
				putInpendent(key);
		}

		public void putInpendent(final Sig.Field key){

			putDepField(key, null);
		}

		private int getLevel_(final String key){
			String crnt = key;
			Set<String> crntFldSet = this.depTable.get(crnt);
			int level = 0;
			if(crntFldSet!=null && !crntFldSet.isEmpty()){
				level++;
				for(String prntfld: crntFldSet){
					level = Math.max(level, getLevel_(prntfld)+1);
				}
			}
			return level;
		}

		public int getLevel(final Sig.Field key){
			return fldLevel.get(key);
		}

		public Set<Sig.Field> getLeveli(int i){
			Set<Sig.Field> ret = new HashSet<Sig.Field>();
			for(String fld:fldLevel.keySet()){
				if(fldLevel.get(fld) == i ){
					ret.add(name2Field.get(fld));
				}
			}
			return ret;
		}

		public Set<Sig.Field> getDepTo(final Sig.Field key){
			Set<String> depTo = this.depTable.get(key.toString());
			Set<Sig.Field> ret = new HashSet<Sig.Field>();
			if(depTo != null)
				for(String fldStr :depTo){
					ret.add(name2Field.get(fldStr));
				}
			return ret;
		}

		public int maxLevel(){
			int ret = 0;
			for(Integer i: fldLevel.values()){
				ret = Math.max(ret, i);
			}
			return ret;
		}

		public String toString(){

			for(String fld: this.depTable.keySet()){
				this.fldLevel.put(fld,getLevel_(fld));
			}

			StringBuilder ret =  new StringBuilder();
			ret.append("{");
			for(String fld:depTable.keySet())
				ret.append("[Name=").append(fld).append(",Level=").append(getLevel_(fld)).append(",Deps=").append(depTable.get(fld)).append("]\n");
			if(ret.length() > 0) ret.delete(ret.length()-1, ret.length());
			ret.append("}"); 
			return ret.toString();
		}
	}

	private class AlloyTuplesTable extends TreeMap<String, AlloyTuplesList>{

		//private Counter counter;

		public AlloyTuplesTable(AlloyTuplesTable that){
			super(that);
			//this.counter = that.counter.clone();
		}

		public AlloyTuplesTable clone(){

			return new AlloyTuplesTable(this);
		}

		public AlloyTuplesTable(){
			super();
			//this.counter = new IncreasingCounter();
		}

		public String toString(){
			StringBuilder ret = new StringBuilder();
			ret.append("\t{");
			for(String key: keySet()){
				ret.append(key).append("=").append(get(key).toString()).append(",");
			}
			ret.delete(ret.length()-1, ret.length());
			ret.append("}");
			return ret.toString();	
		}			


		public void merge(AlloyTuplesTable that){
			for(String thatKey:that.keySet()){
				if(this.containsKey(thatKey)){
					AlloyTuplesList thatList = that.get(thatKey);
					AlloyTuplesList thisList = this.get(thatKey);
					thisList.addAll(thatList);
					this.put(thatKey, thisList);
				}else{
					this.put(thatKey, that.get(thatKey));
				}
			}
		}

		private class InstancesIterator implements Iterable<AlloyTuplesTable>,Iterator<AlloyTuplesTable>{

			final AlloyTuplesTable table;
			final Counter counter;

			public InstancesIterator(AlloyTuplesTable outerClass,Counter counter){
				table = outerClass;
				this.counter = counter;
				for(String key:descendingKeySet()){
					counter.addMax(get(key).size());
				}
			}

			@Override
			public boolean hasNext() {
				return counter.hasNext();
			}

			@Override
			public AlloyTuplesTable next() {
				AlloyTuplesTable ret = new AlloyTuplesTable();
				if (!counter.hasNext())
					return null;
				else{
					List<Integer> inst = counter.next();
					int i=0;
					for(String key:table.descendingKeySet()){
						//A unary tupleList
						AlloyTuplesList tuplesList = new AlloyTuplesList();
						tuplesList.add(table.get(key).get(inst.get(i)));
						ret.put(key, tuplesList);
						i++;
					}
				}
				return ret;
			}

			@Override
			public void remove() {}

			@Override
			public Iterator<AlloyTuplesTable> iterator() {
				// TODO Auto-generated method stub
				return this;
			}
		}

		public Iterable<AlloyTuplesTable> getInstanceIteratorIncreasing(){
			return new InstancesIterator(this,new IncreasingCounter());
		}

		public Iterable<AlloyTuplesTable> getInstanceIteratorDecreasing(){
			return new InstancesIterator(this,new DecreasingCounter());
		}

	}


	private class AlloyTuples extends ArrayList<Tuple> {

		public String toString(){
			StringBuilder ret = new StringBuilder();
			ret.append("<");
			for(int i=0; i < this.size(); i++){
				ret.append( get(i).getClass()).append( "->");
			}
			ret.delete(ret.length()-2, ret.length());
			ret.append(">");
			return ret.toString();

		}

	}










	private Instance getEvalInstace() throws Exception{
		//if (evaInst == null){

		Map<Sig,List<AlloyTuplesTable>> unqSig2InstsTbl = new HashMap<Sig,List<AlloyTuplesTable>>();
		Map<String,Sig> uSigNames = new HashMap<String, Sig>();
		for(Sig s: sigs){

			//Find the sigs supposed to be unique
			if(s.isUnique != null){
				uSigNames.put(s.label, s);
				//Find the dependecies
				FieldDependecyGroup fDeps = new FieldDependecyGroup();
				for(Field d: s.getFields()){
					CompModule.FieldDecomposer fdcmpsr = new CompModule.FieldDecomposer();
					fDeps.putDepExprs(d, fdcmpsr.extractFieldsItems(d));
				}

				List<AlloyTuplesTable> newTables = new LinkedList<A4Solution.AlloyTuplesTable>();

				AlloyTuplesTable lvl0Instances = new AlloyTuplesTable();
				for(Field d:fDeps.getLeveli(0)){
					List<List<PrimSig>> f = s.join(d).type().fold();
					List<Integer> cardinals = new ArrayList<Integer>();
					CompModule.FieldDecomposer fdcmpsr = new CompModule.FieldDecomposer();
					List<ExprUnary.Op> mults = fdcmpsr.getMultiplicities(d);
					Sig sig = f.get(0).get(0);
					cardinals.add(abstractSUB.get(sig).size());
					AlloyTuplesList alloyTuplesList = null;
					if(mults.get(0)==Op.ONEOF){
						alloyTuplesList = getAllElements(abstractSUB.get(sig));
					}else if(mults.get(0)==Op.SETOF){
						alloyTuplesList = getAllSubsetsWithEmptySubSet(abstractSUB.get(sig));
					}
					lvl0Instances.put(d.label,  alloyTuplesList);
				}


				for(AlloyTuplesTable att : lvl0Instances.getInstanceIteratorIncreasing()){
					boolean invalidInst = false;
					for(Field d:fDeps.getLeveli(1)){
						CompModule.FieldDecomposer fdcmpsr = new CompModule.FieldDecomposer();
						List<Expr> fieldItems = fdcmpsr.extractFieldsItems(d);
						List<ExprUnary.Op> fieldMults = fdcmpsr.getMultiplicities(d);

						for(int i=fieldItems.size()-2; i>=0; i-- ){
							Expr fieldItem = fieldItems.get(i);
							Expr nxtfieldItem = fieldItems.get(i+1);
							String nxtLabel = "lastProduct";							
							ExprUnary.Op crntOP = fieldMults.get(i);
							ExprUnary.Op nxtOP = fieldMults.get(i+1);

							AlloyTuplesList fldList = new AlloyTuplesList( 
									fieldItem instanceof Sig ? 
											getAllElements(abstractSUB.get((Sig)fieldItem)):
												att.get(((Sig.Field)fieldItem).label));
							AlloyTuplesList nxtList = new AlloyTuplesList( 
									nxtfieldItem instanceof Sig ? 
											getAllElements(abstractSUB.get((Sig)nxtfieldItem)):
												att.get(((Sig.Field)nxtfieldItem).label));

							TupleSet nxtTuple = nxtList.list2TupleSet();
							TupleSet fldTuple = fldList.list2TupleSet();

							TupleSet producted = fldTuple.product(nxtTuple);

							List<TupleSet> resultTupleSet;

							if(crntOP.equals(ExprUnary.Op.NOOP) && nxtOP.equals(ExprUnary.Op.NOOP)){
								resultTupleSet = getAllSubsetsWithEmptySubSet(producted);
							}else if(crntOP.equals(ExprUnary.Op.NOOP) && nxtOP.equals(ExprUnary.Op.ONEOF)){
								AlloyTuplesList tmpResultTupleSet = getAllSubsetsWithEmptySubSet(producted);
								resultTupleSet = new ArrayList<TupleSet>();
								for(TupleSet ts: tmpResultTupleSet){
									if(ts.project(0).containsAll(fldTuple) && ts.size() == fldTuple.size()){
										resultTupleSet.add(ts);
									}
								}
								if(resultTupleSet.isEmpty()){
									invalidInst = true;
									break;
								}
							}else if(crntOP.equals(ExprUnary.Op.NOOP) && nxtOP.equals(ExprUnary.Op.LONEOF)){
								AlloyTuplesList tmpResultTupleSet = getAllSubsetsWithEmptySubSet(producted);
								resultTupleSet = new ArrayList<TupleSet>();
								for(TupleSet ts: tmpResultTupleSet){
									if(ts.project(0).size() == ts.size() ){
										resultTupleSet.add(ts);
									}
								}
								if(resultTupleSet.isEmpty()){
									invalidInst = true;
									break;
								}
							}else if(crntOP.equals(ExprUnary.Op.ONEOF) && nxtOP.equals(ExprUnary.Op.ONEOF)){
								AlloyTuplesList tmpResultTupleSet = getAllSubsetsWithEmptySubSet(producted);
								resultTupleSet = new ArrayList<TupleSet>();
								for(TupleSet ts: tmpResultTupleSet){
									if(
											ts.project(0).containsAll(fldTuple) && ts.size() == fldTuple.size() //For right one
											&&
											ts.project(1).containsAll(nxtTuple) && ts.size() == nxtTuple.size()
											){
										resultTupleSet.add(ts);
									}
								}
								if(resultTupleSet.isEmpty()){
									invalidInst = true;
									break;
								}
							}else if(crntOP.equals(ExprUnary.Op.ONEOF) && nxtOP.equals(ExprUnary.Op.LONEOF)){
								AlloyTuplesList tmpResultTupleSet = getAllSubsetsWithEmptySubSet(producted);
								resultTupleSet = new ArrayList<TupleSet>();
								for(TupleSet ts: tmpResultTupleSet){
									if(
											ts.project(0).size() == ts.size() //For right lone
											&&
											ts.project(1).containsAll(nxtTuple) && ts.size() == nxtTuple.size()
											){
										resultTupleSet.add(ts);
									}
								}
								if(resultTupleSet.isEmpty()){
									invalidInst = true;
									break;
								}
							}else{
								resultTupleSet = null;
								throw new Exception("Other multiplicitities have not been implemented yet. Please contanct to Vajih");
							}
							if(invalidInst)
								break;
							att.put(d.label, new AlloyTuplesList(resultTupleSet));
						}//end of field items iterator
					}//end of level iterator
					if(!invalidInst)
						newTables.add(att);
				}//end of instance iterator

				unqSig2InstsTbl.put(s, newTables);

			}//End of if for filtering unique sig
		}//End of all sigs



		Universe kkUniv = bounds.universe();
		Instance inst2Run = null;
		for(Sig s:unqSig2InstsTbl.keySet()){
			String sigLabel = s.label.replace("this/", "");
			List<AlloyTuplesTable> newTables = unqSig2InstsTbl.get(s);
			//Finding the number of instances.
			int numSum = 0;
			for(AlloyTuplesTable att:newTables){
				for(AlloyTuplesTable inst: att.getInstanceIteratorIncreasing()){
					numSum++;
				}
			}

			List<String> newKKUnivList = new LinkedList<String>();
			//Adding the new atoms for the uniqueSig to the universe
			for(Object atom:kkUniv){
				if(!atom.toString().startsWith(sigLabel))
					newKKUnivList.add(atom.toString());
			}
			for(int i=0; i < numSum ; i++){
				newKKUnivList.add(sigLabel+"$"+i);
			}

			kkUniv = new Universe(newKKUnivList);
			//Put the tuples for each realtion in the map
			numSum = 0;
			Map<String, List<Tuple>> fldsTpls = new HashMap<String, List<Tuple>>();
			for(AlloyTuplesTable att:newTables){
				for(AlloyTuplesTable inst: att.getInstanceIteratorIncreasing()){
					//Tuple uniqSigTuple = kkUniv.factory().tuple(sigLabel+"$"+numSum);
					for(Field fld: s.getFields()){
						String fldLabel = sigLabel+"."+fld.label;
						List<Tuple> tplsList = fldsTpls.get(fldLabel);
						if(tplsList == null) tplsList = new ArrayList<Tuple>();
						tplsList.addAll(inst.get(fld.label).getFieldTuple(kkUniv.factory(),sigLabel+"$"+numSum));
						fldsTpls.put(fldLabel, tplsList);
					}
					numSum++;
				}
			}

			inst2Run = new Instance(kkUniv);
			for(Relation r: bounds.relations()){
				String name = r.name().replace("this/", "");
				if (uSigNames.containsKey(r.name())){
					List<Tuple> tuples = new ArrayList<Tuple>();
					for(int i=0; i<numSum; i++){
						tuples.add(kkUniv.factory().tuple(name+"$"+i));
					}
					inst2Run.add(r, kkUniv.factory().setOf(tuples));

				}else if(fldsTpls != null && fldsTpls.containsKey(name)  ){
					inst2Run.add(r, kkUniv.factory().setOf(fldsTpls.get(name)));
				}else
					inst2Run.add(r, changeTupleSetUniverse(kkUniv,bounds.upperBound(r))); 
			}

		}
		//}
		return inst2Run.unmodifiableView();
	}


	/** This method intended to solve the expression wit respect to the bound 
	 * @throws Exception */
	public Object eval_woSolve(Expr expr) throws Exception {
		//		try {



		if (expr instanceof Sig) return eval((Sig)expr);
		if (expr instanceof Field) return eval((Field)expr);



		try{
			Instance inst2Run = getEvalInstace();

			if(inst2Run==null)
				throw new Exception("Unable to find the instance object");

			solver.options().setBitwidth(31);
			Evaluator eval = new Evaluator(inst2Run,solver.options());
			Object result = TranslateAlloyToKodkod.alloy2kodkod(this, expr);

			//System.exit(-10);
			//evaInst = null;

			if (result instanceof IntExpression) 
				return eval.evaluate((IntExpression)result) + (eval.wasOverflow() ? " (OF)" : "");
			if (result instanceof Formula) 
				return eval.evaluate((Formula)result);
			if (result instanceof Expression) 
				return new A4TupleSet(eval.evaluate((Expression)result), this);
			throw new ErrorFatal("Unknown internal error encountered in the evaluator.");
		} catch(CapacityExceededException ex) {
			ex.printStackTrace();
			throw TranslateAlloyToKodkod.rethrow(ex);
		}
	}



	/**
	 * 
	 * @param field
	 * @param att pass by reference. This object will be effected in the method for the memory saving.
	 * @return
	 * @throws Exception
	 */
	private AlloyTuplesTable extendInstances(final Sig uSig, final Sig.Field  field, AlloyTuplesTable att2) throws Exception{

		AlloyTuplesTable att = att2.clone();
		if(!field.sig.label.equals(uSig.label)){
			//if the domain of the field is not uniqSig, the extra tuples should be extracted from the bound. It must been calculated before.
			//One exmple is from other unique.
			TupleSet upper=null,lower=null;
			for(Relation r:bounds.lowerBounds().keySet()){
				if(r.name().equals(field.sig.label+"."+field.label)){
					lower= bounds.lowerBound(r);
				}
			}
			for(Relation r:bounds.upperBounds().keySet()){
				if(r.name().equals(field.sig.label+"."+field.label)){
					upper=bounds.upperBound(r);
				}
			}
			//This could be supposed as just lower bound. If lower bound is not empty and not a subset of upperbound, then  some counting should be done.
			if(!lower.isEmpty() && upper.equals(lower)){
				AlloyTuplesList ts = new AlloyTuplesList();
				ts.add(lower);
				att.put(field.label, ts);
			}
		}else{

			boolean ret = true;
			CompModule.FieldDecomposer fdcmpsr = new CompModule.FieldDecomposer();
			List<Expr> fieldItems = fdcmpsr.extractFieldsItems(field);
			List<ExprUnary.Op> fieldMults = fdcmpsr.getMultiplicities(field);



			if(fieldMults.size() == 1){
				fdcmpsr = new CompModule.FieldDecomposer();
				Expr fieldItem = fieldItems.get(0);
				ExprUnary.Op crntOP = fieldMults.get(0);
				AlloyTuplesList alloyTuplesList = null;
				if(crntOP.equals(Op.ONEOF)){
					alloyTuplesList = fieldItem instanceof Sig ? 
							getAllElements(abstractSUB.get((Sig)fieldItem)):
								att.get(((Sig.Field)fieldItem).label);
				}else if(crntOP.equals(Op.SETOF)){
					alloyTuplesList = fieldItem instanceof Sig ? 
							getAllSubsetsWithEmptySubSet(abstractSUB.get((Sig)fieldItem)):
								att.get(((Sig.Field)fieldItem).label);
				}
				att.put(field.label, new AlloyTuplesList(alloyTuplesList));
			}else{
				for(int i=fieldItems.size()-2; i>=0; i-- ){
					Expr fieldItem = fieldItems.get(i);
					Expr nxtfieldItem = fieldItems.get(i+1);
					ExprUnary.Op crntOP = fieldMults.get(i);
					ExprUnary.Op nxtOP = fieldMults.get(i+1);


					AlloyTuplesList fldList = new AlloyTuplesList( 
							fieldItem instanceof Sig ? 
									getAllElements(abstractSUB.get((Sig)fieldItem)):
										att.get(((Sig.Field)fieldItem).label));
					AlloyTuplesList nxtList = new AlloyTuplesList( 
							nxtfieldItem instanceof Sig ? 
									getAllElements(abstractSUB.get((Sig)nxtfieldItem)):
										att.get(((Sig.Field)nxtfieldItem).label));


					TupleSet nxtTuple = nxtList.list2TupleSet();
					TupleSet fldTuple = fldList.list2TupleSet();

					TupleSet producted = fldTuple.product(nxtTuple);

					List<TupleSet> resultTupleSet;

					if(crntOP.equals(ExprUnary.Op.NOOP) && nxtOP.equals(ExprUnary.Op.NOOP)){
						resultTupleSet = getAllSubsetsWithEmptySubSet(producted);
					}else if(crntOP.equals(ExprUnary.Op.NOOP) && nxtOP.equals(ExprUnary.Op.ONEOF)){
						AlloyTuplesList tmpResultTupleSet = getAllSubsetsWithEmptySubSet(producted);

						resultTupleSet = new ArrayList<TupleSet>();
						for(TupleSet ts: tmpResultTupleSet){
							if(ts.project(0).containsAll(fldTuple) && ts.size() == fldTuple.size()){
								resultTupleSet.add(ts);
							}
						}
						if(resultTupleSet.isEmpty()){
							ret = false;
							//att.clear();
							break;
						}
					}else if(crntOP.equals(ExprUnary.Op.NOOP) && nxtOP.equals(ExprUnary.Op.LONEOF)){
						AlloyTuplesList tmpResultTupleSet = getAllSubsetsWithEmptySubSet(producted);
						resultTupleSet = new ArrayList<TupleSet>();
						for(TupleSet ts: tmpResultTupleSet){
							if(ts.project(0).size() == ts.size() ){
								resultTupleSet.add(ts);
							}
						}
						if(resultTupleSet.isEmpty()){
							ret = false;
							break;
						}
					}else if(crntOP.equals(ExprUnary.Op.ONEOF) && nxtOP.equals(ExprUnary.Op.ONEOF)){
						AlloyTuplesList tmpResultTupleSet = getAllSubsetsWithEmptySubSet(producted);
						resultTupleSet = new ArrayList<TupleSet>();
						for(TupleSet ts: tmpResultTupleSet){
							if(
									ts.project(0).containsAll(fldTuple) && ts.size() == fldTuple.size() //For right one
									&&
									ts.project(1).containsAll(nxtTuple) && ts.size() == nxtTuple.size()
									){
								resultTupleSet.add(ts);
							}
						}
						if(resultTupleSet.isEmpty()){
							ret = false;
							break;
						}
					}else if(crntOP.equals(ExprUnary.Op.ONEOF) && nxtOP.equals(ExprUnary.Op.LONEOF)){
						AlloyTuplesList tmpResultTupleSet = getAllSubsetsWithEmptySubSet(producted);
						resultTupleSet = new ArrayList<TupleSet>();
						for(TupleSet ts: tmpResultTupleSet){
							if(
									ts.project(0).size() == ts.size() //For right lone
									&&
									ts.project(1).containsAll(nxtTuple) && ts.size() == nxtTuple.size()
									){
								resultTupleSet.add(ts);
							}
						}
						if(resultTupleSet.isEmpty()){
							ret = false;
							break;
						}
					}else if(crntOP.equals(ExprUnary.Op.NOOP) && nxtOP.equals(ExprUnary.Op.SOMEOF)){
						AlloyTuplesList tmpResultTupleSet = getAllSubsetsWithEmptySubSet(producted);
						resultTupleSet = new ArrayList<TupleSet>();
						int j=0;
						for(TupleSet ts: tmpResultTupleSet){
							if(ts.project(0).containsAll(fldTuple) ){
								resultTupleSet.add(ts);
							}
						}
						if(resultTupleSet.isEmpty()){
							ret = false;
							break;
						}
					}
					else if(crntOP.equals(ExprUnary.Op.LONEOF) && nxtOP.equals(ExprUnary.Op.LONEOF)){
						AlloyTuplesList tmpResultTupleSet = getAllSubsetsWithEmptySubSet(producted);
						resultTupleSet = new ArrayList<TupleSet>();
						int j=0;
						for(TupleSet ts: tmpResultTupleSet){
							if(ts.project(0).size() == ts.size() //For left lone
									&& ts.project(1).size() == ts.size() //For right lone
									){
								resultTupleSet.add(ts);
							}
						}
						if(resultTupleSet.isEmpty()){
							ret = false;
							break;
						}
					}
					else{
						resultTupleSet = null;
						throw new Exception("Other multiplicitities have not been implemented yet. Please contanct to Vajih");
					}
					if(ret){
						att.put(field.label, new AlloyTuplesList(resultTupleSet));
					}
				}//end of field items iterator

			}
			if(!ret)
				att.clear();
		}
		return att;
	}


	private List<String> convertOldUnivToList(Sig s){
		String sigLabel = s.label.replace("this/", "");
		Universe kkUniv = bounds.universe();
		List<String> oldKKUnivList = new LinkedList<String>();
		//Adding the new atoms for the uniqueSig to the universe
		for(Object atom:kkUniv){
			if(!atom.toString().startsWith(sigLabel))
				oldKKUnivList.add(atom.toString());
		}	
		return oldKKUnivList;
	}

	private Instance convertAlloyTupleListToInstance(Sig s, AlloyTuplesTable inst, long uIndex,List<String> oldKKUnivList, Set<Sig.Field> flds) throws Exception{


		Instance inst2Run = null;
		String sigLabel = s.label.replace("this/", "");

		Map<String, List<Tuple>> fldsTpls = new HashMap<String, List<Tuple>>();
		List<String> newKKUnivList =  new ArrayList<String>( oldKKUnivList) ;
		newKKUnivList.add(sigLabel+"$"+uIndex);
		Universe kkUniv = new Universe(newKKUnivList);

		//System.out.println(inst);
		//System.out.println(kkUniv);

		for(Field fld: flds){
			String fldLabel = sigLabel+"."+fld.label;
			List<Tuple> tplsList = new ArrayList<Tuple>();
			/*System.out.println("inst="+inst+", fld.label="+fld.label+", inst.get(fld.label)="+inst.get(fld.label)+", uniqSigTuple="+
								uniqSigTuple+
								", inst.get(fld.label).getFieldTuple(uniqSigTuple)="//+
								//inst.get(fld.label).getFieldTuple(uniqSigTuple)
								);*/
			if(s.isOne == null){
				tplsList.addAll(inst.get(fld.label).getFieldTuple(kkUniv.factory(),sigLabel+"$"+uIndex));
			}
			else{
				tplsList.addAll(inst.get(fld.label).getFieldTuple(kkUniv.factory()));
			}
			fldsTpls.put(fldLabel, tplsList);

		}

		inst2Run = new Instance(kkUniv);


		for(Relation r: bounds.relations()){
			String name = r.name().replace("this/", "");
			if (s.label.equals(r.name())){
				List<Tuple> tuples = new ArrayList<Tuple>();
				tuples.add(kkUniv.factory().tuple(name+"$"+uIndex));
				inst2Run.add(r, kkUniv.factory().setOf(tuples));
			}else if(fldsTpls != null && fldsTpls.containsKey(name)  ){

				if(fldsTpls.get(name).size() > 0)
					inst2Run.add(r,kkUniv.factory().setOf(fldsTpls.get(name)) );
				else
					inst2Run.add(r,kkUniv.factory().noneOf(r.arity()) );
			}else{
				TupleSet ts = changeTupleSetUniverse(kkUniv,bounds.upperBound(r));
				if(!ts.isEmpty())
					inst2Run.add(r,ts ); 
			}
			TupleSet upper = (TupleSet)bounds.upperBounds().get(r);
			TupleSet lower = (TupleSet)bounds.lowerBounds().get(r);
			TupleSet added = (TupleSet)inst2Run.relationTuples().get(r);
			if ((upper.equals(lower)) && upper.isEmpty() && 
					added==null && ((Relation)r).arity() == 1) {
				//The relations is not in the inst2Run
				//The relation should be empty
				//The relation should be signature
				for(Sig sig:sigs){
					if(sig.label.equals(r.name()) && !sig.builtin){
						TupleSet ts = changeTupleSetUniverse(kkUniv,upper);
						inst2Run.add(r, ts);						
					}
				}
			}
		}

		return inst2Run;

	}


	private Map<TupleSet,TupleSet> computeClosure(AlloyTuplesList input){
		Map<TupleSet,TupleSet> ret = new IdentityHashMap<TupleSet,TupleSet>();
		for(TupleSet tupleSet: input){
			assert tupleSet.arity() == 2;
			//for each tuple set, first the range and dmain are extracted to put in a list
			Map<Object,Integer> code = new IdentityHashMap<Object,Integer>();
			Map<Integer,Tuple> decode = new IdentityHashMap<Integer,Tuple>();
			//Store the domain
			for(Tuple tuple: tupleSet.project(0)){
				if(!code.containsKey(tuple.atom(0))){
					code.put(tuple.atom(0), code.size());
					decode.put(code.get(tuple.atom(0)), tuple);
				}
			}
			//Store the range
			for(Tuple tuple: tupleSet.project(1)){
				if(!code.containsKey(tuple.atom(0))){
					code.put(tuple.atom(0), code.size());
					decode.put(code.get(tuple.atom(0)), tuple);
				}
			}
			AdjMatrixEdgeWeightedDigraph G = new AdjMatrixEdgeWeightedDigraph(code.size());
			for(Tuple tuple: tupleSet){
				G.addEdge(new DirectedEdge(code.get(tuple.atom(0)), code.get(tuple.atom(1)), 0));
			}
			FloydWarshall spt = new FloydWarshall(G);
			TupleSet closr = tupleSet.clone();
			closr.clear();
			for (int v = 0; v < G.V(); v++) {
				for (int w = 0; w < G.V(); w++) {
					if (spt.hasPath(v, w)){
						closr.add(decode.get(v).product(decode.get(w)));
					}
				}
			}
			ret.put(tupleSet, closr);
		}
		return ret;
	}


	//Copied from the CompParser
	private Expr mult(Expr x) throws Err {
		if (x instanceof ExprUnary) {
			ExprUnary y=(ExprUnary)x;
			if (y.op==ExprUnary.Op.SOME) return ExprUnary.Op.SOMEOF.make(y.pos, y.sub);
			if (y.op==ExprUnary.Op.LONE) return ExprUnary.Op.LONEOF.make(y.pos, y.sub);
			if (y.op==ExprUnary.Op.ONE)  return ExprUnary.Op.ONEOF.make(y.pos, y.sub);
		}
		return x;
	}



	/**The urrent command is needed if the a part of model is required to be solved.*/
	private List<Instance> getEvalInstaces(final Expr expr, Sig uniqSig,Command command) throws Exception{

		boolean oneFound = ExampleUsingTheCompiler.oneFound;
		boolean usingKodkod = ExampleUsingTheCompiler.usingKodkod;
		boolean usingKKItr = ExampleUsingTheCompiler.usingKKItr;
		int PACE = ExampleUsingTheCompiler.PACE;

		//First detect any closure declration in the appended fact
		CompModule.ClosureDetector clsrDtctr = new CompModule.ClosureDetector(expr);
		Map<TupleSet,TupleSet> clsred2clsr = null;


		CompModule.FieldDecomposer fdcmpsr = new CompModule.FieldDecomposer();
		List<AlloyTuplesTable> newTables = new LinkedList<A4Solution.AlloyTuplesTable>();
		List<Instance> insts = new ArrayList<Instance>();
		List<String> oldKKUnivList = convertOldUnivToList(uniqSig);

		//Find the dependecies
		FieldDependecyGroup fDeps = new FieldDependecyGroup();
		Set<Sig.Field> uSigFlds = new HashSet<Sig.Field>();
		for(Field d: uniqSig.getFields()){
			if(clsrDtctr.getClosureField()==null || !clsrDtctr.getClosureField().equals(d)){
				uSigFlds.add(d);
				fDeps.putDepExprs(d, fdcmpsr.extractFieldsItems(d));
			}
		}

		Expr refinedExpr = clsrDtctr.getRemovedStatement();

		Set<Sig.Field> refFields = fdcmpsr.extractFieldsInExpr(refinedExpr);
		Set<Sig.Field> fldsLevel0 = fDeps.getLeveli(0);
		Set<Sig.Field> fldsLevel1 = fDeps.getLeveli(1);
		Set<Sig.Field> fldsLevel01 = new HashSet<Sig.Field>();
		fldsLevel01.addAll(fldsLevel0);
		fldsLevel01.addAll(fldsLevel1);
		Set<Sig.Field> fldsRefDep = new HashSet<Sig.Field>(refFields);

		//filterout the unrelated fields
		//Set<Sig.Field> sigFields = new HashSet<Sig.Field>(uniqSig.getFields().makeCopy());
		//1fldsRefDep.retainAll(sigFields);


		//Find a set that the fields in refFields are dependent on them and it is not dependent on any other thing.
		Set<Sig.Field> fixSet = new HashSet<Sig.Field>(refFields);
		do{
			//fldsRefDep.clear();
			for(Field d: fixSet){
				fldsRefDep.addAll(fDeps.getDepTo(d));
			}
			if(fixSet.equals(fldsRefDep)){
				break;
			}else{
				fixSet.clear();
				fixSet.addAll(fldsRefDep);
			}
		}while(true);

		Set<Sig.Field> fldsRefPlusDep = new HashSet<Sig.Field>(fldsRefDep);
		fldsRefDep.removeAll(refFields);

		Set<Sig.Field> fldsLevelI0 = new HashSet<Sig.Field>(fldsLevel0);
		Set<Sig.Field> fldsLevelE0 = new HashSet<Sig.Field>(fldsLevel0);
		fldsLevelI0.retainAll(fldsRefPlusDep);
		//fldsLevelI0.retainAll(fldsRefPlusDep);
		fldsLevelE0.removeAll(fldsRefPlusDep);

		Set<Sig.Field> fldsLevelI1 = new HashSet<Sig.Field>(fldsLevel1);
		Set<Sig.Field> fldsLevelE1 = new HashSet<Sig.Field>(fldsLevel1);
		fldsLevelI1.addAll(fldsRefPlusDep);
		fldsLevelI1.removeAll(fldsLevelI0);
		fldsLevelE1.removeAll(fldsRefPlusDep);


		Set<Sig.Field> fldsLevelI0I1MinusRefDep = new HashSet<Sig.Field>(fldsLevel0);
		fldsLevelI0I1MinusRefDep.addAll(fldsLevel1);
		fldsLevelI0I1MinusRefDep.removeAll(fldsRefPlusDep);

		LoggerUtil.debug(this,"fldsRefPlusDep%n%s\t",fldsRefPlusDep);
		LoggerUtil.debug(this,"fldsLevelI0%n%s\t", fldsLevelI0);
		LoggerUtil.debug(this,"fldsLevelI1%n%s\t", fldsLevelI1);
		LoggerUtil.debug(this,"fldsLevelE0%n%s\t", fldsLevelE0);
		LoggerUtil.debug(this,"fldsLevelE1%n%s\t", fldsLevelE1);		
		LoggerUtil.debug(this,"fldsLevelI0I1MinusRefDep%n%s\t", fldsLevelI0I1MinusRefDep);

		List<AlloyTuplesTable> newTables3 = new ArrayList<A4Solution.AlloyTuplesTable>();


		if(usingKodkod && uniqSig.isOne!=null && expr!=null){	
			System.out.println("There is one:"+uniqSig);
			Command newCommand = command.change(refinedExpr);
			A4Options options = new A4Options();
			options.solver = A4Options.SatSolver.MiniSatJNI;
			List<CommandScope> scopes = new ArrayList<CommandScope>(newCommand.scope)  ;
			if(clsrDtctr.getClosureField()!=null){
				//The the new sig list contians all the sigs before, but the altered unique sig. The closured field in the
				//unique labeled field is changed,which is possible because the fields is mutable in the Sig definition.
				//It has a huge side effect. So, the solution is to set the upper bound of the clousred field to empty.

				CommandScope emptyClsrd = new CommandScope(clsrDtctr.getClosureField().pos,
						new PrimSig(clsrDtctr.getClosureField().label, AttrType.WHERE.make(clsrDtctr.getClosureField().pos))
				, true, 0, 0, 1, new ArrayList() , 0); 
				scopes.add(emptyClsrd);
				newCommand.change(ConstList.make(scopes) );
			}

			A4Solution ans = TranslateAlloyToKodkod.execute_command(null, this.sigs, newCommand, options);
			if (ans.eval == null)
				throw new ErrorType("Unstatisfiable appended fact of "+uniqSig.label+" at "+expr.pos);			
			Instance sol = ans.eval.instance();
			int k=0;
			Relation sigma = null;
			for(Relation r: sol.relations()){
				if(sigma.name().contains("sigma"))
					sigma  = r;
			}
			System.out.println(sigma);
			int max = -1;
			TupleSet sigmaTuples = null;
			if (sigma!=null){
				max = 5;
				sigmaTuples = sol.tuples(sigma);
			}
			Instance maxSol = sol;
			while(k < max && ans.eval!=null){
				ans=ans.next();
				sol = ans.eval.instance();
				if(sol.tuples(sigma).size() > sigmaTuples.size()){
					sigmaTuples = sol.tuples(sigma);
					maxSol = sol;
				}
				k++;
				System.out.println("The "+k+"'th is:"+convertInstancetoAlloyTuple(sol,uniqSig,uSigFlds));
			}
			System.out.println("The max sol is:"+convertInstancetoAlloyTuple(maxSol,uniqSig,uSigFlds));
			newTables3.add(convertInstancetoAlloyTuple(maxSol,uniqSig,uSigFlds));
			System.out.println("The uniq+one level is finished."+newTables3);
		}
		else if(usingKodkod){
			if(!usingKKItr){

				//I am trying to make each signature one, then iterativly run the solver.

				uniqSig.isOne = Pos.UNKNOWN;


				Command newCommand = command.change(expr!=null?expr:ExprList.make(Pos.UNKNOWN, Pos.UNKNOWN, ExprList.Op.AND, new ArrayList<Expr>()));
				A4Options options = new A4Options();
				options.solver = A4Options.SatSolver.MiniSatJNI;
				options.symmetry = 0;
				List<CommandScope> scopes = new ArrayList<CommandScope>(newCommand.scope)  ;
				if(clsrDtctr.getClosureField()!=null){
					//The the new sig list contians all the sigs before, but the altered unique sig. The closured field in the
					//unique labeled field is changed,which is possible because the fields is mutable in the Sig definition.
					//It has a huge side effect. So, the solution is to set the upper bound of the clousred field to empty.

					CommandScope emptyClsrd = new CommandScope(clsrDtctr.getClosureField().pos,
							new PrimSig(clsrDtctr.getClosureField().label, AttrType.WHERE.make(clsrDtctr.getClosureField().pos))
					, true, 0, 0, 1, new ArrayList() , 0); 
					scopes.add(emptyClsrd);
					newCommand.change(ConstList.make(scopes) );
				}

				A4Solution ans = TranslateAlloyToKodkod.execute_command(null, this.sigs, newCommand, options);
				if (ans.eval == null)
					throw new ErrorType("Unstatisfiable appended fact of "+uniqSig.label+" at "+expr.pos);			
				do{
					Instance sol = ans.eval.instance();
					LoggerUtil.debug(this,"the founded sol is:%s", sol);
					newTables3.add(convertInstancetoAlloyTuple(sol,uniqSig,uSigFlds));
					ans=ans.next();
				}while(ans.eval!=null);
				uniqSig.isOne = null;	

			}else{

				//I am trying to make each signature one, then iterativly run the solver.
				ExprHasName u1 =  ExprVar.make(Pos.UNKNOWN,Util.tail(uniqSig.label).toLowerCase()+"_1",uniqSig.type());
				ExprHasName u2 =  ExprVar.make(Pos.UNKNOWN,Util.tail(uniqSig.label).toLowerCase()+"_2",uniqSig.type());
				List<ExprHasName> names = new ArrayList<ExprHasName>();
				names.add(u1);
				names.add(u2);
				edu.mit.csail.sdg.alloy4compiler.ast.Decl decl = 
						new edu.mit.csail.sdg.alloy4compiler.ast.Decl(null, Pos.UNKNOWN, null, names, mult( uniqSig));
				List<edu.mit.csail.sdg.alloy4compiler.ast.Decl> decls = new ArrayList<edu.mit.csail.sdg.alloy4compiler.ast.Decl>();
				decls.add(decl);
				List<Expr> orOprands = new ArrayList<Expr>();
				for(Sig.Field field:uniqSig.getFields()){
					orOprands.add(
							ExprBinary.Op.NOT_EQUALS.make(Pos.UNKNOWN, Pos.UNKNOWN, 
									ExprBinary.Op.JOIN.make(Pos.UNKNOWN, Pos.UNKNOWN, u1, field), 
									ExprBinary.Op.JOIN.make(Pos.UNKNOWN, Pos.UNKNOWN, u2, field)));
				}

				Expr orExpr = null;
				if(orOprands.size() > 0){
					orExpr = orOprands.get(0);
				}
				for(int i=1;i<orOprands.size(); i++){
					orExpr = ExprBinary.Op.OR.make(Pos.UNKNOWN, Pos.UNKNOWN, orExpr, orOprands.get(i));
				}
				assert orExpr!=null:"The uniq Expr is empty";

				Expr uniqExpr = ExprQt.Op.ALL.make(Pos.UNKNOWN, Pos.UNKNOWN, decls, orExpr);

				//The unique predicate now should be conjuct wotht he appended fact.
				Expr commandExpr = expr!= null? ExprBinary.Op.AND.make(Pos.UNKNOWN, Pos.UNKNOWN, expr, uniqExpr):uniqExpr;

				List<ErrorWarning> warnings = new ArrayList();
				commandExpr = commandExpr.resolve_as_formula(warnings);
				Command newCommand = command.change(commandExpr);

				A4Options options = new A4Options();
				options.solver = A4Options.SatSolver.MiniSatJNI;
				List<CommandScope> scopes = new ArrayList<CommandScope>(newCommand.scope);
				//Im not working on the closure part now.
				/*if(clsrDtctr.getClosureField()!=null){
					//The the new sig list contians all the sigs before, but the altered unique sig. The closured field in the
					//unique labeled field is changed,which is possible because the fields is mutable in the Sig definition.
					//It has a huge side effect. So, the solution is to set the upper bound of the clousred field to empty.

					CommandScope emptyClsrd = new CommandScope(clsrDtctr.getClosureField().pos,
							new PrimSig(clsrDtctr.getClosureField().label, AttrType.WHERE.make(clsrDtctr.getClosureField().pos))
					, true, 0, 0, 1, new ArrayList() , 0); 
					scopes.add(emptyClsrd);
					newCommand.change(ConstList.make(scopes) );
				}*/
				Instance sol = null;
				int ExactUpper = PACE;
				scopes.add(new CommandScope(Pos.UNKNOWN,uniqSig, true, ExactUpper, ExactUpper, 1));
				newCommand = newCommand.change(ConstList.make(scopes));
				int maxUnSAT = Integer.MAX_VALUE;

				SafeList<Sig> refdSig = new SafeList<Sig>(); 
				refdSig.addAll((new CompModule.FieldDecomposer()).extractSigsFromFields(fldsLevelI0I1MinusRefDep));
				refdSig.add(uniqSig);

				for(Sig sig:refdSig){
					if(sig instanceof PrimSig && !((PrimSig)sig).children().isEmpty())
						refdSig.addAll(((PrimSig)sig).children().makeCopy());

				}
				//System.exit(-10);

				do{
					MyReporter rep = new MyReporter();
					long beforeTime = System.currentTimeMillis();
					A4Solution ans = TranslateAlloyToKodkod.execute_command_includeInstance(rep, refdSig, newCommand, options,sol,uniqSig);
					long afterTime = System.currentTimeMillis();
					edu.mit.csail.sdg.gen.LoggerUtil.fileLogger(ExampleUsingTheCompiler.reportFileName,
							String.valueOf(ExactUpper),
							String.valueOf(PACE),
							String.valueOf(afterTime -beforeTime),
							String.valueOf(rep.solveTime),
							String.valueOf(rep.trasnalationTime),
							String.valueOf(rep.clauses),
							String.valueOf(rep.totalVaraibles),
							String.valueOf(rep.sat)
							);

					System.out.println(scopes+"......."+ans.satisfiable());
					if(ans.satisfiable()){
						sol = ans.eval.instance();
						if((maxUnSAT - ExactUpper) == 1)
							break;
						//PACE = PACE/2;
						if((ExactUpper + PACE) >= maxUnSAT){
							ExactUpper = ExactUpper + PACE -1;
						}else
							ExactUpper = ExactUpper + PACE;
						scopes.remove(scopes.size()-1);
						scopes.add(new CommandScope(Pos.UNKNOWN,uniqSig, true, ExactUpper, ExactUpper, 1));
						newCommand = newCommand.change(ConstList.make(scopes));
					}else{
						if(PACE >1){
							maxUnSAT = ExactUpper;
							PACE = PACE/2;
							ExactUpper = ExactUpper - PACE;
							scopes.remove(scopes.size()-1);
							scopes.add(new CommandScope(Pos.UNKNOWN,uniqSig, true, ExactUpper, ExactUpper, 1));
							newCommand = newCommand.change(ConstList.make(scopes));
						}else{
							break;
						}
					}
				}while(true);



				return solutionDecompser( sol,  uniqSig);

			}
		}else{			




			AlloyTuplesTable lvl0Instances = new AlloyTuplesTable();
			for(Field d:fldsLevelI0){
				lvl0Instances = extendInstances(uniqSig,d,lvl0Instances);
			}


			long i =0,all = 0;
			//Already enrolled
			for(AlloyTuplesTable att : 
				uniqSig.isOne!=null?
						lvl0Instances.getInstanceIteratorDecreasing():
							lvl0Instances.getInstanceIteratorIncreasing()){
				AlloyTuplesTable extendedAtt = att;
				for(Field d:fldsLevelI1){
					extendedAtt = extendInstances(uniqSig,d,extendedAtt);
					if (extendedAtt.isEmpty())
						break;
				}//end of level iterator

				if (extendedAtt.isEmpty())
					continue;
				for(AlloyTuplesTable newAtt:
					uniqSig.isOne!=null?
							extendedAtt.getInstanceIteratorDecreasing():
								extendedAtt.getInstanceIteratorIncreasing()){
					all++;
					if(extendedAtt != null && checkInstance(
							convertAlloyTupleListToInstance(uniqSig,newAtt,0,oldKKUnivList,fldsRefPlusDep)
							,refinedExpr)){
						i++;
						newTables.add(newAtt);
						//System.out.println("Found Object("+i +","+all +","+((double)i/(double)all )*100+"):"+newAtt);
					}
				}
				if(oneFound && i > 0 && uniqSig.isOne!=null)
					break;
			}//end of instance iterator

			//System.exit(-10);

			//System.out.println("The second level is finished."+newTables.size());


			List<AlloyTuplesTable> newTables2 = new ArrayList<A4Solution.AlloyTuplesTable>();

			//Probably there was no dependent or lower level instances
			if(newTables.size() == 0){
				AlloyTuplesTable extendedAtt = new AlloyTuplesTable();
				for(Field d:fldsLevelE0){
					extendedAtt = extendInstances(uniqSig,d,extendedAtt);

					System.out.println("The closured field is found?->"+d.equals(clsrDtctr.getClosuredField()));
					if(d.equals(clsrDtctr.getClosuredField())){
						//The closured field is found, and all its closured are computed and stored in map.
						//The returned map, maps a tuple set of the closured field to its closured one.
						//Later, the result is merged to each instance.
						clsred2clsr = computeClosure(extendedAtt.get(d.label));
					}else if(uniqSig.isOne!=null){
						List<TupleSet> tupleSets = new ArrayList<TupleSet>();
						tupleSets.add(extendedAtt.get(d.label).getMaxTupleSet());
						extendedAtt.put(d.label, new AlloyTuplesList(tupleSets));
					}

				}//end of level iterator
				newTables2.add(extendedAtt);
			}else{
				for(AlloyTuplesTable atts: newTables){
					for(AlloyTuplesTable att : atts.getInstanceIteratorIncreasing()){
						AlloyTuplesTable extendedAtt = att;
						for(Field d:fldsLevelE0){
							extendedAtt = extendInstances(uniqSig,d,extendedAtt);
							if(uniqSig.isOne!=null){
								List<TupleSet> tupleSets = new ArrayList<TupleSet>();
								tupleSets.add(extendedAtt.get(d.label).getMaxTupleSet());
								extendedAtt.put(d.label, new AlloyTuplesList(tupleSets));
							}
						}//end of level iterator

						newTables2.add(extendedAtt);
					}//end of instance iterator
				}
			}



			//System.out.println("The third level is finished."+newTables2.size());
			//System.out.println("The third level resit is:"+newTables2);

			for(AlloyTuplesTable atts: newTables2){
				for(AlloyTuplesTable att : atts.getInstanceIteratorIncreasing()){
					AlloyTuplesTable extendedAtt = att;
					for(Field d:fldsLevelE1){
						extendedAtt = extendInstances(uniqSig,d,extendedAtt);
						if(extendedAtt.size() == 0)
							break;
					}//end of level iterator
					newTables3.add(extendedAtt);
				}//end of instance iterator
			}

			//System.out.println("The fourth level is finished."+newTables3.size());
			//System.out.println("The fourth level resultt is:"+newTables3);


			/*System.out.println(insts);
		for(Instance inst:insts){
			System.out.println(inst.universe());			
		}*/
		}//end of if-else
		long num = 0;
		for(AlloyTuplesTable atts: newTables3){
			for(AlloyTuplesTable att : atts.getInstanceIteratorIncreasing()){
				AlloyTuplesTable exAtt = att;
				//If the unique sig is not constrainted to 'one' then for each all valid instances of the 
				if(clsred2clsr!=null){
					AlloyTuplesList atl = new AlloyTuplesList();
					atl.add(clsred2clsr.get(exAtt.get(clsrDtctr.getClosuredField().label).get(0)));
					exAtt.put(clsrDtctr.getClosureField().label, atl);
					uSigFlds.add(clsrDtctr.getClosureField());
				}
				insts.add(
						convertAlloyTupleListToInstance(
								uniqSig,
								exAtt,
								num++,
								oldKKUnivList,
								uSigFlds));
			}//end of instance iterator
		}
		LoggerUtil.debug(this, "Total number is: %d",insts.size());
		LoggerUtil.debug(this,"%s",insts);
		return insts;

	}

	private List<Instance> solutionDecompser(Instance sol, Sig uniqSig){

		//System.out.println("The solution is:"+sol);

		Map<Object,List<Object>> uniqSigTupleSets = new HashMap<Object,List<Object>>();  
		Set<Object> uniqSigUni = new HashSet<Object>(); 
		for(Relation r: sol.relations()){
			if(Util.tail(r.name()).equals(Util.tail(uniqSig.label))){
				assert r.arity() == 1 : "The arity of  UniqSig <"+r+"> relation has to be one but it is:"+r.arity();
				for(Tuple tuple: sol.tuples(r)){
					uniqSigTupleSets.put(tuple.atom(0),new ArrayList<Object>());
					uniqSigUni.add(tuple.atom(0));
				}
				break;
			}
		}


		Set<Object> oldUni = new HashSet<Object>();
		for(Iterator iterator=sol.universe().iterator();iterator.hasNext();){
			oldUni.add(iterator.next());
		}

		for(Object atom: uniqSigTupleSets.keySet()){
			Collection<Object> toBeRemoved = new TreeSet<Object>(uniqSigUni);
			toBeRemoved.remove(atom);
			Collection<Object> newUnivers = new TreeSet<Object>(oldUni);
			newUnivers.removeAll(toBeRemoved);
			uniqSigTupleSets.get(atom).addAll(newUnivers);
		}


		Map<Object, Instance> retInsts = new IdentityHashMap<Object, Instance>();
		for(Object atom: uniqSigTupleSets.keySet()){
			Universe instUni = new Universe(uniqSigTupleSets.get(atom));
			Instance inst = new Instance(instUni);
			for(Relation r: sol.relations()){
				if(uniqSigUni.contains(Util.tail(r.name())) && !Util.tail(r.name()).equals(atom) ){
					continue;
				}else if(Util.tail(r.name()).equals(Util.tail(uniqSig.label))){
					inst.add(r, instUni.factory().setOf(atom));
				}else if(!sol.tuples(r).isEmpty() && sol.tuples(r).arity()>1 && uniqSigUni.containsAll(tupleSet2Atoms(sol.tuples(r).project(0))) ){
					List<Tuple> tuplesList = new ArrayList<Tuple>();
					for(Tuple tuple:sol.tuples(r)){
						if(tuple.atom(0).equals(atom)){
							List<Object> atomsList = new ArrayList<Object>();
							for(int i= 0; i<tuple.arity();i++){
								atomsList.add(tuple.atom(i));
							}
							tuplesList.add( instUni.factory().tuple(atomsList));
						}
					}
					inst.add(r,  tuplesList.isEmpty() ? instUni.factory().noneOf(r.arity()): instUni.factory().setOf(tuplesList));
				}else if(!sol.tuples(r).isEmpty()){
					//Just change the universe
					List<Tuple> tuplesList = new ArrayList<Tuple>();
					for(Tuple tuple:sol.tuples(r)){
						List<Object> atomsList = new ArrayList<Object>();
						for(int i= 0; i<tuple.arity();i++){
							atomsList.add(tuple.atom(i));
						}

						assert atomsList.size()==0:"Is there any tuple with zero arity?";
						tuplesList.add(instUni.factory().tuple(atomsList));
					}
					inst.add(r,  instUni.factory().setOf(tuplesList));
				}else{
					inst.add(r,  instUni.factory().noneOf(r.arity()));
				}
			}
			retInsts.put(atom, inst);
		}

		List<Instance> retIntances = new ArrayList<Instance>(retInsts.values());
		return retIntances;

	}

	private List<Object> tupleSet2Atoms(TupleSet tupleSet){
		assert (tupleSet.arity() == 1) : "Tupleset with arity more than 1 cannot be converted to a List";
		List<Object> atoms = new ArrayList<Object>();
		for(Tuple tuple: tupleSet){
			atoms.add(tuple.atom(0));
		}
		return atoms;
	}

	public void includeIntoLowerbound(Instance inst ,Sig uniqSig){
		/*
		long bgnInx = 0L;
		String delim = "_";
		Universe oldUni = bounds.universe();

		List<Object> allAtoms = new ArrayList<Object>();
		List<Object> newAtoms = new ArrayList<Object>();
		Map<Object,Object> old2NewMap = new IdentityHashMap<Object, Object>();
		Map<String,TupleSet> newExactBound = new HashMap<>();

		Iterator<Object> uniIterator = oldUni.iterator(); 
		while(uniIterator.hasNext()){
			allAtoms.add(uniIterator.next());
		}

		//Extracting the uniqSig atoms.
		//First finding the atoms, then put them into the table for the relabeling
		//Extracting the uniqSig label from the range of any relation then add to universe
		for(Relation r: inst.relations()){
			if(Util.tail(r.name()).equals(Util.tail( uniqSig.label))){
				TupleSet ts = inst.tuples(r);
				assert ts.arity()==1 : "The arity uniqSig tuples has to be 1";
				Iterator<Tuple> tuples = ts.iterator();
				while(tuples.hasNext()){
					Object atom = tuples.next().atom(0);
					allAtoms.add( atom + delim + String.valueOf(bgnInx));
					newAtoms.add(atom + delim + String.valueOf(bgnInx));
					old2NewMap.put(atom, atom + delim + String.valueOf(bgnInx));
					bgnInx++;
				}
			}
		}
		System.out.println("newAtoms->"+newAtoms);
		System.out.println("allAtoms->"+allAtoms);
		System.out.println("old2NewMap->"+old2NewMap);

		//Now 'atoms' contains all the old and new atoms to make the universe.
		Universe newUni = new Universe(allAtoms);
		List<Tuple> newUniqAtoms = new ArrayList<Tuple>();
		bgnInx=0;
		Relation uniqSigRelation = null;

		//Now update the inst tuple set, regarding the new universe
		for(Relation r: inst.relations()){
			//make the new relation instances
			if(r.arity() > 1 ){
				TupleSet tupleSet = inst.tuples(r);
				List<Tuple> oldTuples = new ArrayList<Tuple>();
				Iterator<Tuple> tplIterator = tupleSet.iterator();
				while(tplIterator.hasNext()){
					oldTuples.add(tplIterator.next());
				}
				List<Tuple> newTuples = new ArrayList<Tuple>();
				for(Tuple tuple:oldTuples){
					List<Object> atomList = new ArrayList<Object>();
					for(int i=0; i<tuple.arity();i++){
						atomList.add(old2NewMap.containsKey(tuple.atom(i))?old2NewMap.get(tuple.atom(i)):tuple.atom(i) );
					}
					newTuples.add(newUni.factory().tuple(atomList));
				}

				if (newExactBound.containsKey(r)){
					TupleSet ts = newExactBound.get(r);
					ts.addAll(newTuples);
					newExactBound.put(r.name(), ts);
				}else{
					newExactBound.put(r.name(),newTuples.size()==0? newUni.factory().noneOf(r.arity()) :newUni.factory().setOf(newTuples));
				}
			}else 
				if(uniqSigRelation==null && Util.tail(r.name()).equals(Util.tail(uniqSig.label))){
					TupleSet ts = inst.tuples(r);
					List<Tuple> newTuples = new ArrayList<>();

					for(Tuple tuple:ts){
						System.out.println("ts->"+ts);
						Object atom = tuple.atom(0);
						if(old2NewMap.containsKey(atom)){
							newTuples.add(newUni.factory().tuple( old2NewMap.get(atom) ));
						}else{
							newTuples.add(newUni.factory().tuple( atom ));
						}
					}
					newExactBound.put(r.name(), newUni.factory().setOf(newTuples));
					System.out.println("..."+newExactBound.get(r.name()));
				}
		}
		//find the relation with is equal to the uniqSig

		 */
		Map<String,TupleSet> instRelationNames = new HashMap<String,TupleSet>();
		Set<Object> oldUniqSigAtoms = new HashSet<Object>();
		for(Relation r: inst.relations()){
			if(Util.tail(r.name()).equals(Util.tail( uniqSig.label))){
				oldUniqSigAtoms.addAll(tupleSet2Atoms(inst.tuples(r)));
			}
			if(Util.tail(r.name()).contains(Util.tail( uniqSig.label)+'.'))
				instRelationNames.put(r.name(),inst.tuples(r));
		}


		//System.out.println("uniqSigRelation->"+uniqSigRelation);

		//System.out.println("newExactBound->"+newExactBound);

		//Bounds newBounds = new Bounds(newUni);
		for(Relation r:bounds.relations()){
			if(Util.tail(r.name()).equals(Util.tail( uniqSig.label)))
				continue;
			//System.out.println("r->"+r+"..."+newExactBound.containsKey(r.name()));
			TupleSet upper = changeUniverseOfTupleSet(refineTuples( bounds.upperBound(r),oldUniqSigAtoms),bounds.universe());
			TupleSet lower = changeUniverseOfTupleSet(refineTuples( bounds.lowerBound(r),oldUniqSigAtoms),bounds.universe());
			/*if(newExactBound.containsKey(r.name()) && Util.tail(r.name()).equals(Util.tail( uniqSig.label))){
				TupleSet exact = newExactBound.get(r.name());
				upper.clear();
				lower.clear();
				upper.addAll(exact);
				lower.addAll(exact);
			}
			else if(newExactBound.containsKey(r.name())){*/
			if(instRelationNames.containsKey(r.name())){
				TupleSet exact = changeUniverseOfTupleSet(instRelationNames.get(r.name()),bounds.universe());
				upper.addAll(exact);
				lower.addAll(exact);
			}
			bounds.bound(r, lower, upper);
		}
		/*System.out.println("--------------------------------------------------------------");
		for(Relation r:bounds.relations()){
			System.out.println("r:"+r+", #lower:"+bounds.lowerBound(r).size()+", #upper:"+bounds.upperBound(r).size());
			System.out.println("\tlower:"+bounds.lowerBound(r)+", \n\tnupper:"+bounds.upperBound(r));
		}
		System.out.println("--------------------------------------------------------------");
		 */
		//System.out.println("The bounds is+"+bounds);
		//System.out.println("The bounds is+"+newBounds);
		//System.exit(-10);
	}


	private TupleSet changeUniverseOfTupleSet(TupleSet tupleSet, Universe newUniverse){
		TupleSet newTupleSet = newUniverse.factory().noneOf(tupleSet.arity());
		for(Tuple tuple:tupleSet){
			List<Object> atoms = new ArrayList<Object>();
			for(int i=0;i<tuple.arity();i++)
				atoms.add(tuple.atom(i));
			newTupleSet.add(newUniverse.factory().tuple(atoms));
		}
		return newTupleSet;
	}

	/**
	 * This funcation takes a tupleset, and returns a tuples set not refering to any atoms in the atoms
	 * @return
	 */
	private TupleSet refineTuples(TupleSet tupleSet, Set<Object> atoms){
		TupleSet retTupleSet = tupleSet.clone();
		for(Tuple tuple:tupleSet){
			for(int i=0;i<tuple.arity();i++){
				if(atoms.contains(tuple.atom(i))){
					retTupleSet.remove(tuple);
					continue;
				}
			}
		}
		return retTupleSet;
	}

	/**
	 * Convert the Alloy instance to the AlloyTuplesTable. This is helperclass for making all the evlaution progess integrated and unified.
	 * @param instance
	 * @param uniqSig
	 * @param includedFields
	 * @return
	 */
	private AlloyTuplesTable convertInstancetoAlloyTuple(Instance instance, Sig uniqSig, Set<Sig.Field> includedFields){
		AlloyTuplesTable retAtt = new AlloyTuplesTable();
		Set<String> uSigFldsStr = new HashSet<String>();
		for(Field field:includedFields){
			uSigFldsStr.add(uniqSig.label+"."+field.label);
		}
		uSigFldsStr.add(uniqSig.label);
		for(Relation r:instance.relations()){
			if(uSigFldsStr.contains(r.name())){
				AlloyTuplesList atl = new AlloyTuplesList();
				atl.add(instance.tuples(r));
				retAtt.put(r.name().substring(r.name().indexOf(".") > 0 ? r.name().indexOf(".")+1 : 0), atl);
			}
		}
		return retAtt;
	}

	private boolean checkInstance(Instance inst, Expr expr) throws Err{
		//System.exit(10);
		solver.options().setBitwidth(31);
		Evaluator eval = new Evaluator(inst,solver.options());
		Object result = TranslateAlloyToKodkod.alloy2kodkod(this, expr);
		if (result instanceof Formula) 
			return eval.evaluate((Formula)result);
		else
			return false;
	}

	public  A4TupleSet getfieldSolutions(List<Instance> insts, String fldName){

		TupleSet ret =null;
		List<List<Object>> tupleSets = new ArrayList<List<Object>>();
		Set<Object> univAtoms = new HashSet<Object>(); 
		int arity = 0;
		for(Instance inst:insts){
			for(Relation r: inst.relations()){
				if(r.name().equals(fldName)){
					arity = r.arity();
					for(Tuple tuple: inst.tuples(r)){
						List<Object> tupleList = new ArrayList<Object>();
						for(int i=0; i<tuple.arity();i++){
							tupleList.add(tuple.atom(i));
						}
						tupleSets.add(tupleList);
					}
					for(int i=0;i<inst.universe().size();i++){
						univAtoms.add(inst.universe().atom(i));
					}
				}
			}
		}
		Universe newUniv = new Universe(univAtoms);
		List<Tuple> tuples = new ArrayList<Tuple>(); 
		for(List<Object> atoms: tupleSets){
			tuples.add(newUniv.factory().tuple(atoms));
		}
		if(tuples.size() == 0)
			ret = newUniv.factory().noneOf(arity);
		else
			ret = newUniv.factory().setOf(tuples);
		return new A4TupleSet(ret, this);
	}


	public  Object getEmpty(List<Instance> insts, Set<String> fldName, Sig sig){
		String sigName = sig.label;
		Object ret = null;

		if(sig.isOne==null)
			insts:
			for(Instance inst:insts){
				relations:
				for(Relation r: inst.relations()){
					if(sigName.equals(r.name())){
						boolean isEmpty = true;
						for(Relation rf: inst.relations()){
							isEmpty = true;
							if(fldName.contains(rf.name())){
								//This field belongs to the signature
								isEmpty = inst.tuples(rf).isEmpty();
								if(!isEmpty)
									break;
							}
						}
						if(isEmpty){
							ret = inst.tuples(r).iterator().next().atom(0);
							break insts;
						}
						break relations;
					}//End of for to check if all fields have empty tuples in the currecnt instance.
				}//End of for to find the sig's raltion coaunterpart.

			}//End of for to iterating the instances		

		return ret;
	}


	/** This method intended to solve the expression with respect to the bound 
	 * @throws Exception */
	public Object eval_woSolveFormula(Sig uSig,final  Expr expr,Command courrentCommand) throws Exception {

		if (expr instanceof Sig) return eval((Sig)expr);
		if (expr instanceof Field) return eval((Field)expr);
		try{			
			List<Instance> ret = new ArrayList<Instance>();
			if(uSig.isUnique != null)
				ret.addAll(getEvalInstaces(expr,uSig,courrentCommand));
			return new Pair<A4Solution,List<Instance>>(this,ret) ;
		} catch(CapacityExceededException ex) {
			ex.printStackTrace();
			throw TranslateAlloyToKodkod.rethrow(ex);
		}
	}

	/** Returns the Kodkod instance represented by this solution; throws an exception if the problem is not yet solved or if it is unsatisfiable. */
	public Instance debugExtractKInstance() throws Err {
		if (!solved) throw new ErrorAPI("This solution is not yet solved, so instance() is not allowed.");
		if (eval==null) throw new ErrorAPI("This solution is unsatisfiable, so instance() is not allowed.");
		return eval.instance().unmodifiableView();
	}

	//===================================================================================================//

	/** Maps a Kodkod formula to an Alloy Expr or Alloy Pos (or null if no such mapping) */
	Object k2pos(Node formula) { return k2pos.get(formula); }

	/** Associates the Kodkod formula to a particular Alloy Expr (if the Kodkod formula is not already associated with an Alloy Expr or Alloy Pos) */
	Formula k2pos(Formula formula, Expr expr) throws Err {
		if (solved) throw new ErrorFatal("Cannot alter the k->pos mapping since solve() has completed.");
		if (formula==null || expr==null || k2pos.containsKey(formula)) return formula;
		k2pos.put(formula, expr);
		if (formula instanceof BinaryFormula) {
			BinaryFormula b = (BinaryFormula)formula;
			if (b.op() == FormulaOperator.AND) { k2pos(b.left(), expr); k2pos(b.right(), expr); }
		}
		return formula;
	}

	/** Associates the Kodkod formula to a particular Alloy Pos (if the Kodkod formula is not already associated with an Alloy Expr or Alloy Pos) */
	Formula k2pos(Formula formula, Pos pos) throws Err {
		if (solved) throw new ErrorFatal("Cannot alter the k->pos mapping since solve() has completed.");
		if (formula==null || pos==null || pos==Pos.UNKNOWN || k2pos.containsKey(formula)) return formula;
		k2pos.put(formula, pos);
		if (formula instanceof BinaryFormula) {
			BinaryFormula b = (BinaryFormula)formula;
			if (b.op() == FormulaOperator.AND) { k2pos(b.left(), pos); k2pos(b.right(), pos); }
		}
		return formula;
	}

	//===================================================================================================//

	/** Associates the Kodkod relation to a particular Alloy Type (if it is not already associated with something) */
	void kr2type(Relation relation, Type newType) throws Err {
		if (solved) throw new ErrorFatal("Cannot alter the k->type mapping since solve() has completed.");
		if (!rel2type.containsKey(relation)) rel2type.put(relation, newType);
	}

	/** Remove all mapping from Kodkod relation to Alloy Type. */
	void kr2typeCLEAR() throws Err {
		if (solved) throw new ErrorFatal("Cannot clear the k->type mapping since solve() has completed.");
		rel2type.clear();
	}

	//===================================================================================================//

	/** Caches a constant pair of Type.EMPTY and Pos.UNKNOWN */
	private Pair<Type,Pos> cachedPAIR = null;

	/** Maps a Kodkod variable to an Alloy Type and Alloy Pos (if no association exists, it will return (Type.EMPTY , Pos.UNKNOWN) */
	Pair<Type,Pos> kv2typepos(Variable var) {
		Pair<Type,Pos> ans=decl2type.get(var);
		if (ans!=null) return ans;
		if (cachedPAIR==null) cachedPAIR=new Pair<Type,Pos>(Type.EMPTY, Pos.UNKNOWN);
		return cachedPAIR;
	}

	/** Associates the Kodkod variable to a particular Alloy Type and Alloy Pos (if it is not already associated with something) */
	void kv2typepos(Variable var, Type type, Pos pos) throws Err {
		if (solved) throw new ErrorFatal("Cannot alter the k->type mapping since solve() has completed.");
		if (type==null) type=Type.EMPTY;
		if (pos==null) pos=Pos.UNKNOWN;
		if (!decl2type.containsKey(var)) decl2type.put(var, new Pair<Type,Pos>(type, pos));
	}

	//===================================================================================================//

	/** Add the given formula to the list of Kodkod formulas, and associate it with the given Pos object (pos can be null if unknown). */
	void addFormula(Formula newFormula, Pos pos) throws Err {
		if (solved) throw new ErrorFatal("Cannot add an additional formula since solve() has completed.");
		if (formulas.size()>0 && formulas.get(0)==Formula.FALSE) return; // If one formula is false, we don't need the others
		if (newFormula==Formula.FALSE) formulas.clear(); // If one formula is false, we don't need the others
		formulas.add(newFormula);
		if (pos!=null && pos!=Pos.UNKNOWN) k2pos(newFormula, pos);
	}

	/** Add the given formula to the list of Kodkod formulas, and associate it with the given Expr object (expr can be null if unknown) */
	void addFormula(Formula newFormula, Expr expr) throws Err {
		if (solved) 
			throw new ErrorFatal("Cannot add an additional formula since solve() has completed.");
		if (formulas.size()>0 && formulas.get(0)==Formula.FALSE) 
			return; // If one formula is false, we don't need the others
		if (newFormula==Formula.FALSE) 
			formulas.clear(); // If one formula is false, we don't need the others
		formulas.add(newFormula);
		if (expr!=null) k2pos(newFormula, expr);
	}

	//===================================================================================================//

	/** Helper class that wraps an iterator up where it will pre-fetch the first element (note: it will not prefetch subsequent elements). */
	private static final class Peeker<T> implements Iterator<T> {
		/** The encapsulated iterator. */
		private Iterator<T> iterator;
		/** True iff we have captured the first element. */
		private boolean hasFirst;
		/** If hasFirst is true, then this is the captured first element. */
		private T first;
		/** Constructrs a Peeker object. */
		private Peeker(Iterator<T> it) {
			iterator = it;
			if (it.hasNext()) { hasFirst=true; first=it.next(); }
		}
		/** {@inheritDoc} */
		public boolean hasNext() {
			return hasFirst || iterator.hasNext();
		}
		/** {@inheritDoc} */
		public T next() {
			if (hasFirst) { hasFirst=false; T ans=first; first=null; return ans; } else return iterator.next();
		}
		/** {@inheritDoc} */
		public void remove() { throw new UnsupportedOperationException(); }
	}

	//===================================================================================================//

	/** Helper method to determine if a given binary relation is a total order over a given unary relation. */
	private static List<Tuple> isOrder(TupleSet b, TupleSet u) {
		// Size check
		final int n = u.size();
		final List<Tuple> list = new ArrayList<Tuple>(n);
		if (b.size() == 0 && n <= 1) return list;
		if (b.size() != n-1) return null;
		// Find the starting element
		Tuple head = null;
		TupleSet right = b.project(1);
		for(Tuple x: u) if (!right.contains(x)) {head = x; break;}
		if (head==null) return null;
		final TupleFactory f = head.universe().factory();
		// Form the list
		list.add(head);
		while(true) {
			// Find head.next
			Tuple headnext = null;
			for(Tuple x: b) if (x.atom(0)==head.atom(0)) { headnext = f.tuple(x.atom(1)); break; }
			// If we've reached the end of the chain, and indeed we've formed exactly n elements (and all are in u), we're done
			if (headnext==null) return list.size()==n ? list : null;
			// If we've accumulated more than n elements, or if we reached an element not in u, then we declare failure
			if (list.size()==n || !u.contains(headnext)) return null;
			// Move on to the next step
			head = headnext;
			list.add(head);
		}
	}

	/** Helper method that chooses a name for each atom based on its most specific sig; (external caller should call this method with s==null and nexts==null) */
	private static void rename (A4Solution frame, PrimSig s, Map<Sig,List<Tuple>> nexts, UniqueNameGenerator un) throws Err {
		if (s==null) {
			for(ExprVar sk:frame.skolems) un.seen(sk.label);
			// Store up the skolems
			List<Object> skolems = new ArrayList<Object>();
			for(Map.Entry<Relation,Type> e: frame.rel2type.entrySet()) {
				Relation r = e.getKey(); if (!frame.eval.instance().contains(r)) continue;
				Type t = e.getValue();   if (t.arity() > r.arity()) continue; // Something is wrong; let's skip it
				while (t.arity() < r.arity()) t = UNIV.type().product(t);
				String n = Util.tail(r.name());
				while(n.length()>0 && n.charAt(0)=='$') n = n.substring(1);
				skolems.add(n);
				skolems.add(t);
				skolems.add(r);
			}
			// Find all suitable "next" or "prev" relations
			nexts = new LinkedHashMap<Sig,List<Tuple>>();
			for(Sig sig:frame.sigs) for(Field f: sig.getFields()) if (f.label.compareToIgnoreCase("next")==0) {
				List<List<PrimSig>> fold = f.type().fold();
				if (fold.size()==1) {
					List<PrimSig> t = fold.get(0);
					if (t.size()==3 && t.get(0).isOne!=null && t.get(1)==t.get(2) && !nexts.containsKey(t.get(1))) {
						TupleSet set = frame.eval.evaluate(frame.a2k(t.get(1)));
						if (set.size()<=1) continue;
						TupleSet next = frame.eval.evaluate(frame.a2k(t.get(0)).join(frame.a2k(f)));
						List<Tuple> test = isOrder(next, set);
						if (test!=null) nexts.put(t.get(1), test);
					} else if (t.size()==2 && t.get(0)==t.get(1) && !nexts.containsKey(t.get(0))) {
						TupleSet set = frame.eval.evaluate(frame.a2k(t.get(0)));
						if (set.size()<=1) continue;
						TupleSet next = frame.eval.evaluate(frame.a2k(f));
						List<Tuple> test = isOrder(next, set);
						if (test!=null) nexts.put(t.get(1), test);
					}
				}
			}
			for(Sig sig:frame.sigs) for(Field f: sig.getFields()) if (f.label.compareToIgnoreCase("prev")==0) {
				List<List<PrimSig>> fold = f.type().fold();
				if (fold.size()==1) {
					List<PrimSig> t = fold.get(0);
					if (t.size()==3 && t.get(0).isOne!=null && t.get(1)==t.get(2) && !nexts.containsKey(t.get(1))) {
						TupleSet set = frame.eval.evaluate(frame.a2k(t.get(1)));
						if (set.size()<=1) continue;
						TupleSet next = frame.eval.evaluate(frame.a2k(t.get(0)).join(frame.a2k(f)).transpose());
						List<Tuple> test = isOrder(next, set);
						if (test!=null) nexts.put(t.get(1), test);
					} else if (t.size()==2 && t.get(0)==t.get(1) && !nexts.containsKey(t.get(0))) {
						TupleSet set = frame.eval.evaluate(frame.a2k(t.get(0)));
						if (set.size()<=1) continue;
						TupleSet next = frame.eval.evaluate(frame.a2k(f).transpose());
						List<Tuple> test = isOrder(next, set);
						if (test!=null) nexts.put(t.get(1), test);
					}
				}
			}
			// Assign atom->name and atom->MostSignificantSig
			for(Tuple t:frame.eval.evaluate(Relation.INTS)) {
				frame.atom2sig.put(t.atom(0), SIGINT); 
			}
			for(Tuple t:frame.eval.evaluate(KK_SEQIDX))     { 
				frame.atom2sig.put(t.atom(0), SEQIDX); 
			}
			for(Tuple t:frame.eval.evaluate(KK_STRING))     { 
				frame.atom2sig.put(t.atom(0), STRING); 
			}
			for(Sig sig:frame.sigs) 
				if (sig instanceof PrimSig && !sig.builtin && ((PrimSig)sig).isTopLevel()) 
					rename(frame, (PrimSig)sig, nexts, un);
			// These are redundant atoms that were not chosen to be in the final instance
			int unused=0;
			for(Tuple tuple:frame.eval.evaluate(Relation.UNIV)) {
				Object atom = tuple.atom(0);
				if (!frame.atom2sig.containsKey(atom)) { 
					frame.atom2name.put(atom, "unused"+unused); unused++; 
				}
			}
			// Add the skolems
			for(int num=skolems.size(), i=0; i<num-2; i=i+3) {
				String n = (String) skolems.get(i);
				while(n.length()>0 && n.charAt(0)=='$') n=n.substring(1);
				Type t = (Type) skolems.get(i+1);
				Relation r = (Relation) skolems.get(i+2);
				frame.addSkolem(un.make("$"+n), t, r);
			}
			return;
		}
		for(PrimSig c: s.children()) rename(frame, c, nexts, un);
		String signame = un.make(s.label.startsWith("this/") ? s.label.substring(5) : s.label);
		List<Tuple> list = new ArrayList<Tuple>();
		for(Tuple t: frame.eval.evaluate(frame.a2k(s))) list.add(t);
		List<Tuple> order = nexts.get(s);
		if (order!=null && order.size()==list.size() && order.containsAll(list)) { list=order; }
		int i = 0;
		for(Tuple t: list) {
			if (frame.atom2sig.containsKey(t.atom(0))) continue; // This means one of the subsig has already claimed this atom.
			String x = t.atom(0).toString();//signame + "$" + i;
			i++;
			frame.atom2sig.put(t.atom(0), s);
			//[VM]
			frame.atom2name.put(t.atom(0),x);
			ExprVar v = ExprVar.make(null, x, s.type());
			TupleSet ts = t.universe().factory().range(t, t);
			Relation r = Relation.unary(x);
			frame.eval.instance().add(r, ts);
			frame.a2k.put(v, r);
			frame.atoms.add(v);
		}
	}

	//===================================================================================================//

	/** Solve for the solution if not solved already; if cmd==null, we will simply use the lowerbound of each relation as its value. */
	A4Solution solve(final A4Reporter rep, Command cmd, Simplifier simp, boolean tryBookExamples) throws Err, IOException {
		// If already solved, then return this object as is
		if (solved) return this;
		// If cmd==null, then all four arguments are ignored, and we simply use the lower bound of each relation
		if (cmd==null) {
			Instance inst = new Instance(bounds.universe());
			for(int max=max(), i=min(); i<=max; i++) {
				Tuple it = factory.tuple(""+i);
				inst.add(i, factory.range(it, it));
			}
			//[VM[ Inclue the extra integers into the visulizer. 
			for(Integer i: exceededInts){
				Tuple it = factory.tuple(String.valueOf(i));
				inst.add(i, factory.range(it, it));

			}
			for(Relation r: bounds.relations()) 
				inst.add(r, bounds.lowerBound(r));
			eval = new Evaluator(inst, solver.options());
			rename(this, null, null, new UniqueNameGenerator());
			solved();
			return this;
		}
		// Otherwise, prepare to do the solve...
		final A4Options opt = originalOptions;
		long time = System.currentTimeMillis();
		rep.debug("Simplifying the bounds...\n");
		if (simp!=null && formulas.size()>0 && !simp.simplify(rep, this, formulas)) 
			addFormula(Formula.FALSE, Pos.UNKNOWN);
		rep.translate(opt.solver.id(), bitwidth, maxseq, solver.options().skolemDepth(), solver.options().symmetryBreaking());
		Formula fgoal = Formula.and(formulas);
		rep.debug("Generating the solution...\n");
		kEnumerator = null;
		Solution sol = null;
		final Reporter oldReporter = solver.options().reporter();
		final boolean solved[] = new boolean[]{true};
		solver.options().setReporter(new AbstractReporter() { // Set up a reporter to catch the type+pos of skolems
			@Override public void skolemizing(Decl decl, Relation skolem, List<Decl> predecl) {
				try {
					Type t=kv2typepos(decl.variable()).a;
					if (t==Type.EMPTY) return;
					for(int i=(predecl==null ? -1 : predecl.size()-1); i>=0; i--) {
						Type pp=kv2typepos(predecl.get(i).variable()).a;
						if (pp==Type.EMPTY) return;
						t=pp.product(t);
					}
					kr2type(skolem, t);
				} catch(Throwable ex) { } // Exception here is not fatal
			}
			@Override public void solvingCNF(int primaryVars, int vars, int clauses) {
				if (solved[0]) return; else solved[0]=true; // initially solved[0] is true, so we won't report the # of vars/clauses
				if (rep!=null) rep.solve(primaryVars, vars, clauses);
			}
		});
		if (!opt.solver.equals(SatSolver.CNF) && !opt.solver.equals(SatSolver.KK) && tryBookExamples) { // try book examples
			A4Reporter r = "yes".equals(System.getProperty("debug")) ? rep : null;
			try { sol = BookExamples.trial(r, this, fgoal, solver, cmd.check); } catch(Throwable ex) { sol = null; }
		}
		solved[0] = false; // this allows the reporter to report the # of vars/clauses
		for(Relation r: bounds.relations()) { 
			formulas.add(r.eq(r)); } // Without this, kodkod refuses to grow unmentioned relations
		fgoal = Formula.and(formulas);

		//System.out.println(debugExtractKInput());
		// Now pick the solver and solve it!
		if (opt.solver.equals(SatSolver.KK)) {
			File tmpCNF = File.createTempFile("tmp", ".java", new File(opt.tempDirectory));
			String out = tmpCNF.getAbsolutePath();
			Util.writeAll(out, debugExtractKInput());
			rep.resultCNF(out);
			return null;
		}
		//System.out.println("\tThe KK bounds inside the solve method of A4Solution\n"+bounds);
		if (opt.solver.equals(SatSolver.CNF)) {
			File tmpCNF = File.createTempFile("tmp", ".cnf", new File(opt.tempDirectory));
			String out = tmpCNF.getAbsolutePath();
			solver.options().setSolver(WriteCNF.factory(out));
			try { sol = solver.solve(fgoal, bounds); } catch(WriteCNF.WriteCNFCompleted ex) { rep.resultCNF(out); return null; }
			// The formula is trivial (otherwise, it would have thrown an exception)
			// Since the user wants it in CNF format, we manually generate a trivially satisfiable (or unsatisfiable) CNF file.
			Util.writeAll(out, sol.instance()!=null ? "p cnf 1 1\n1 0\n" : "p cnf 1 2\n1 0\n-1 0\n");
			rep.resultCNF(out);
			return null;
		}
		if (solver.options().solver()==SATFactory.ZChaffMincost || !solver.options().solver().incremental()) {
			if (sol==null) sol = solver.solve(fgoal, bounds);
		} else {
			kEnumerator = new Peeker<Solution>(solver.solveAll(fgoal, bounds));
			if (sol==null) sol = kEnumerator.next();
		}
		if (!solved[0]) rep.solve(0, 0, 0);
		final Instance inst = sol.instance();
		// To ensure no more output during SolutionEnumeration
		solver.options().setReporter(oldReporter);
		// If unsatisfiable, then retreive the unsat core if desired
		if (inst==null && solver.options().solver()==SATFactory.MiniSatProver) {

			try {
				lCore = new LinkedHashSet<Node>();
				Proof p = sol.proof();
				if (sol.outcome()==UNSATISFIABLE) {
					// only perform the minimization if it was UNSATISFIABLE, rather than TRIVIALLY_UNSATISFIABLE
					int i = p.highLevelCore().size();
					rep.minimizing(cmd, i);
					if (opt.coreMinimization==0) try { p.minimize(new RCEStrategy(p.log())); } catch(Throwable ex) {}
					if (opt.coreMinimization==1) try { p.minimize(new HybridStrategy(p.log())); } catch(Throwable ex) {}
					rep.minimized(cmd, i, p.highLevelCore().size());
				}
				for(Iterator<TranslationRecord> it=p.core(); it.hasNext();) {
					Object n=it.next().node();
					if (n instanceof Formula) lCore.add((Formula)n);
				}
				Map<Formula,Node> map = p.highLevelCore();
				hCore = new LinkedHashSet<Node>(map.keySet());
				hCore.addAll(map.values());
			} catch(Throwable ex) {
				lCore = hCore = null;
			}
		}
		// If satisfiable, then add/rename the atoms and skolems
		if (inst!=null) {
			eval = new Evaluator(inst, solver.options());
			rename(this, null, null, new UniqueNameGenerator());
		}
		// report the result
		solved();
		time = System.currentTimeMillis() - time;
		if (inst!=null) rep.resultSAT(cmd, time, this); else rep.resultUNSAT(cmd, time, this);
		return this;
	}

	//===================================================================================================//

	/** This caches the toString() output. */
	private String toStringCache = null;

	/** Dumps the Kodkod solution into String. */
	@Override public String toString() {
		if (!solved) return "---OUTCOME---\nUnknown.\n";
		if (eval == null) return "---OUTCOME---\nUnsatisfiable.\n";
		String answer = toStringCache;
		if (answer != null) return answer;
		Instance sol = eval.instance();
		StringBuilder sb = new StringBuilder();
		sb.append("---INSTANCE---\n" + "integers={");
		boolean firstTuple = true;
		for(IndexedEntry<TupleSet> e:sol.intTuples()) {
			if (firstTuple) firstTuple=false; else sb.append(", ");
			// No need to print e.index() since we've ensured the Int atom's String representation is always equal to ""+e.index()
			Object atom = e.value().iterator().next().atom(0);
			sb.append(atom2name(atom));
		}
		sb.append("}\n");
		try {
			for(Sig s:sigs) {
				sb.append(s.label).append("=").append(eval(s)).append("\n");
				for(Field f:s.getFields()) sb.append(s.label).append("<:").append(f.label).append("=").append(eval(f)).append("\n");
			}
			for(ExprVar v:skolems) {
				sb.append("skolem ").append(v.label).append("=").append(eval(v)).append("\n");
			}
			return toStringCache = sb.toString();
		} catch(Err er) {
			return toStringCache = ("<Evaluator error occurred: "+er+">");
		}
	}

	//===================================================================================================//

	/** If nonnull, it caches the result of calling "next()". */
	private A4Solution nextCache = null;

	/** If this solution is UNSAT, return itself; else return the next solution (which could be SAT or UNSAT).
	 * @throws ErrorAPI if the solver was not an incremental solver
	 */
	public A4Solution next() throws Err {
		if (!solved) throw new ErrorAPI("This solution is not yet solved, so next() is not allowed.");
		if (eval==null) return this;
		if (nextCache==null) nextCache=new A4Solution(this);
		return nextCache;
	}

	/** Returns true if this solution was generated by an incremental SAT solver. */
	public boolean isIncremental() { return kEnumerator!=null; }

	//===================================================================================================//

	/** The low-level unsat core; null if it is not available. */
	private LinkedHashSet<Node> lCore = null;

	/** This caches the result of lowLevelCore(). */
	private Set<Pos> lCoreCache = null;

	/** If this solution is unsatisfiable and its unsat core is available, then return the core; else return an empty set. */
	public Set<Pos> lowLevelCore() {
		if (lCoreCache!=null) return lCoreCache;
		Set<Pos> ans1 = new LinkedHashSet<Pos>();
		if (lCore!=null) for(Node f: lCore) {
			Object y = k2pos(f);
			if (y instanceof Pos) ans1.add( (Pos)y ); else if (y instanceof Expr) ans1.add( ((Expr)y).span() );
		}
		return lCoreCache = Collections.unmodifiableSet(ans1);
	}

	//===================================================================================================//

	/** The high-level unsat core; null if it is not available. */
	private LinkedHashSet<Node> hCore = null;

	/** This caches the result of highLevelCore(). */
	private Pair<Set<Pos>,Set<Pos>> hCoreCache = null;

	/** If this solution is unsatisfiable and its unsat core is available, then return the core; else return an empty set. */
	public Pair<Set<Pos>,Set<Pos>> highLevelCore() {
		if (hCoreCache!=null) return hCoreCache;
		Set<Pos> ans1 = new LinkedHashSet<Pos>(), ans2 = new LinkedHashSet<Pos>();
		if (hCore!=null) for(Node f: hCore) {
			Object x = k2pos(f);
			if (x instanceof Pos) {
				// System.out.println("F: "+f+" at "+x+"\n"); System.out.flush();
				ans1.add((Pos)x);
			} else if (x instanceof Expr) {
				Expr expr = (Expr)x;
				Pos p = ((Expr)x).span();
				ans1.add(p);
				// System.out.println("F: "+f+" by "+p.x+","+p.y+"->"+p.x2+","+p.y2+" for "+x+"\n\n"); System.out.flush();
				for(Func func: expr.findAllFunctions()) ans2.add(func.getBody().span());
			}
		}
		return hCoreCache = new Pair<Set<Pos>,Set<Pos>>(Collections.unmodifiableSet(ans1), Collections.unmodifiableSet(ans2));
	}

	//===================================================================================================//

	/** Helper method to write out a full XML file. */
	public void writeXML(String filename) throws Err {
		writeXML(filename, null, null);
	}

	/** Helper method to write out a full XML file. */
	public void writeXML(String filename, Iterable<Func> macros) throws Err {
		writeXML(filename, macros, null);
	}

	/** Helper method to write out a full XML file. */
	public void writeXML(String filename, Iterable<Func> macros, Map<String,String> sourceFiles) throws Err {
		PrintWriter out=null;
		try {
			out=new PrintWriter(filename,"UTF-8");
			writeXML(out, macros, sourceFiles);
			if (!Util.close(out)) throw new ErrorFatal("Error writing the solution XML file.");
		} catch(IOException ex) {
			Util.close(out);
			throw new ErrorFatal("Error writing the solution XML file.", ex);
		}
	}

	/** Helper method to write out a full XML file. */
	public void writeXML(A4Reporter rep, String filename, Iterable<Func> macros, Map<String,String> sourceFiles) throws Err {
		PrintWriter out=null;
		try {
			out=new PrintWriter(filename,"UTF-8");
			writeXML(rep, out, macros, sourceFiles);
			if (!Util.close(out)) throw new ErrorFatal("Error writing the solution XML file.");
		} catch(IOException ex) {
			Util.close(out);
			throw new ErrorFatal("Error writing the solution XML file.", ex);
		}
	}

	/** Helper method to write out a full XML file. */
	public void writeXML(PrintWriter writer, Iterable<Func> macros, Map<String,String> sourceFiles) throws Err {
		A4SolutionWriter.writeInstance(null, this, writer, macros, sourceFiles);
		if (writer.checkError()) throw new ErrorFatal("Error writing the solution XML file.");
	}

	/** Helper method to write out a full XML file. */
	public void writeXML(A4Reporter rep, PrintWriter writer, Iterable<Func> macros, Map<String,String> sourceFiles) throws Err {
		A4SolutionWriter.writeInstance(rep, this, writer, macros, sourceFiles);
		if (writer.checkError()) throw new ErrorFatal("Error writing the solution XML file.");
	}
}
