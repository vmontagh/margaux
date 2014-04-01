package edu.mit.csail.sdg.gen.alloy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import kodkod.ast.Relation;
import kodkod.instance.Instance;
import kodkod.instance.Tuple;
import kodkod.instance.TupleSet;
import kodkod.instance.Universe;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorSyntax;
import edu.mit.csail.sdg.alloy4.ErrorType;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.Pos;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.CommandScope;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprHasName;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprList;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprQt;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Attr.AttrType;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;
import edu.mit.csail.sdg.gen.LoggerUtil;
import edu.mit.csail.sdg.gen.visitor.ClosureDetector;
import edu.mit.csail.sdg.gen.visitor.FieldDecomposer;


public class UniqObjectGeneratorUsingSAT extends UniqObjectGenerator {

	private final boolean symmetryOff;
	private final String ATOM_SEP = "$";
	private final String PARTIAL_SEP = "%";

	public UniqObjectGeneratorUsingSAT(boolean symmetryOff, CompModule world){
		this.symmetryOff = symmetryOff;
	}


	/* (non-Javadoc)
	 * @see edu.mit.csail.sdg.gen.alloy.UniqObjectGenerator#getEvalInstaces(edu.mit.csail.sdg.alloy4compiler.ast.Expr, edu.mit.csail.sdg.alloy4compiler.ast.Sig, edu.mit.csail.sdg.alloy4compiler.ast.Command, edu.mit.csail.sdg.alloy4compiler.parser.CompModule)
	 */
	@Override
	public List<Instance> generate(final Expr expr, final Sig uniqSig, final Command command) throws Exception{

		uniqSig.isOne = Pos.UNKNOWN;

		List<Instance> insts = new ArrayList<Instance>();

		//I am trying to make each signature one, then iteratively run the solver.
		Command newCommand = command.change(expr!=null?expr:ExprList.make(Pos.UNKNOWN, Pos.UNKNOWN, ExprList.Op.AND, new ArrayList<Expr>()));

		List<CommandScope> scopes = new ArrayList<CommandScope>(newCommand.scope)  ;
		ClosureDetector clsrDtctr = new ClosureDetector(expr);

		if(clsrDtctr.getClosureField() != null){
			//The the new sig list contians all the sigs before, but the altered unique sig. The closured field in the
			//unique labeled field is changed,which is possible because the fields is mutable in the Sig definition.
			//It has a huge side effect. So, the solution is to set the upper bound of the clousred field to empty.

			CommandScope emptyClsrd = new CommandScope(clsrDtctr.getClosureField().pos,
					new PrimSig(clsrDtctr.getClosureField().label, AttrType.WHERE.make(clsrDtctr.getClosureField().pos))
			, true, 0, 0, 1, new ArrayList() , 0); 
			scopes.add(emptyClsrd);
			newCommand.change(ConstList.make(scopes) );
		}

		A4Options options = new A4Options();
		options.solver = A4Options.SatSolver.MiniSatJNI;
		options.symmetry = symmetryOff ? 0 : 20;

		List<Sig> refdSig = extractAllSigs(uniqSig,newCommand.formula);

		A4Solution ans = TranslateAlloyToKodkod.execute_command(A4Reporter.NOP, refdSig, newCommand, options);

		if (! ans.satisfiable())
			throw new ErrorType("Unstatisfiable appended fact of "+uniqSig.label+" at "+expr.pos);			
		do{
			Instance sol = ans.debugExtractKInstance();
			insts.add(sol);
			ans = ans.next();
		}while(ans.satisfiable());

		uniqSig.isOne = null;

		return mergeInstance(insts, uniqSig);

	}

	/**
	 * 'updateTupleSet' takes an old tupleset and prepend the new atom  within the new universe.
	 * E.g. b$1 == a$1->b$1
	 * @param oldTS
	 * @param oldAtom
	 * @param newAtom
	 * @param add 
	 * @param uni
	 * @return
	 */
	private final TupleSet updateTupleSet(final TupleSet oldTS, final String newAtom, final Universe universe, boolean add){
		
		List<Tuple> tuples = new ArrayList<Tuple>();

		int arity = oldTS.arity() + 1;
		
		for(Tuple tuple: oldTS){
			List<String> newTuple = new ArrayList<String>();
	
			if(add)	newTuple.add(newAtom);
			
			for(int i = 0; i < tuple.arity(); i++){
				String atom = tuple.atom(i).toString();
				int iA = atom.indexOf(ATOM_SEP);
				int iN = newAtom.indexOf(ATOM_SEP);
				if(  atom.substring(0, iA < 0 ? atom.length() : iA ).equals(newAtom.substring(0, iN < 0 ? newAtom.length() : iN )) ){
					newTuple.add(newAtom);
				}else
					newTuple.add( tuple.atom(i).toString());
			}

			tuples.add(universe.factory().tuple(newTuple));
		}
		
		return tuples.size() == 0 ? universe.factory().noneOf(arity) : universe.factory().setOf(tuples) ;
	}


	private final boolean hasTheSameName(final Sig uniqSig, final String  relationName){
		if(relationName.contains(uniqSig.label)){
			return true;
		}else if(uniqSig instanceof PrimSig){
			PrimSig pus = (PrimSig)uniqSig;
			for(PrimSig parent: pus.allPrimSigParent())
				if(relationName.contains(parent.label))
					return true;
		}
		return false;
	}
	
	private final List<Instance> mergeInstance(final List<Instance> insts, final Sig uniqSig){

		final String uniqSigAtom = PIUtil.nameSanitizer(uniqSig.label);
		final String uniqSigInitialAtom = uniqSigAtom+ATOM_SEP+"0";
		final HashMap<String, Field> fields = new HashMap<>();
		
		for(Field field: uniqSig.getFieldsWithParents()){
			fields.put(field.toString().replace("field (", "").replace(")", "").replace(" <: ", "."), field);
		}
		
		//change the atoms of uniqSig and make a new tuple
		int unigSigNum = 0;

		List<Instance> result = new ArrayList<Instance>();

		for(Instance inst: insts){

			final Set<String> atoms = new HashSet<String>();

			for(Object atom: inst.universe())
				if(! uniqSigInitialAtom.equals(atom.toString()))
					atoms.add(atom.toString());

			atoms.add(uniqSigAtom+ATOM_SEP+unigSigNum);

			//LoggerUtil.debug("atoms: %s %nthe instance is:%s", atoms, inst);
			
			final Universe kkUniv = new Universe(atoms);
			Instance newInstance = new Instance(kkUniv);
			Set<String> inserted = new HashSet<String>();

			for(Relation relation: inst.relations()){
				
				if(inserted.contains(relation.name())) continue;
				
				if(hasTheSameName(uniqSig, relation.name())
						/*relation.name().contains(uniqSig.label)*/){//This is field of uniqSig
										
					TupleSet tupleSet = updateTupleSet(inst.tuples(relation),uniqSigAtom+ATOM_SEP+unigSigNum, kkUniv,
							fields.containsKey(relation.name()) ? fields.get(relation.name()).type().arity() > inst.tuples(relation).arity():true);
					
					newInstance.add(Relation.nary(relation.name(), tupleSet.arity()), tupleSet);

				}else if(! (relation.name().contains(PARTIAL_SEP) | relation.name().contains(ATOM_SEP) )){//Anything else except the atoms

					newInstance.add(relation,changeUniverseOfTupleSet(inst.tuples(relation), kkUniv  ));

				}
				inserted.add(relation.name());
			}

			result.add(newInstance);

			unigSigNum++;
		}

		return result;
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

}
