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
import edu.mit.csail.sdg.gen.visitor.FieldDecomposer;

public class UniqObjectGeneratorUsingKK extends UniqObjectGenerator {

	final private int PACE;
	
	public UniqObjectGeneratorUsingKK(int PACE){
		this.PACE = PACE;
	}
	
	@Override
	public List<Instance> generate(final Expr expr, final Sig uniqSig, final Command command) throws Exception {
		A4Options options = new A4Options();
		options.solver = A4Options.SatSolver.MiniSatJNI;

		int varPace = PACE;
		Instance sol = null;
		int ExactUpper = varPace;
		int maxUnSAT = Integer.MAX_VALUE;
		
		Command newCommand =  command.change(makeUniqPredicateANDAppnededFact(uniqSig,expr)/*commandExpr*/);
		List<CommandScope> scopes = new ArrayList<CommandScope>(newCommand.scope);
		scopes.add(new CommandScope(Pos.UNKNOWN,uniqSig, true, ExactUpper, ExactUpper, 1));
		newCommand = newCommand.change(ConstList.make(scopes));

		List<Sig> refdSig = extractAllSigs(uniqSig,newCommand.formula);

		do{
			A4Solution ans = TranslateAlloyToKodkod.execute_command_includeInstance(A4Reporter.NOP, refdSig, newCommand, options,sol,uniqSig);

			if(ans.satisfiable()){
				sol = ans.debugExtractKInstance();

				if((maxUnSAT - ExactUpper) == 1)
					break;

				ExactUpper = ((ExactUpper + varPace) >= maxUnSAT) ?  
						ExactUpper + varPace -1 :
							ExactUpper + varPace;

				newCommand = changeCommandScope(
						includeIntoLowerbound(newCommand, uniqSig, sol), uniqSig, ExactUpper);

			}else{
				if( varPace > 1 ){
					maxUnSAT = ExactUpper;
					ExactUpper = (ExactUpper - varPace) + varPace/2;
					varPace = varPace / 2;							
					newCommand = changeCommandScope(newCommand, uniqSig, ExactUpper);
				}else
					break;
			}
		}while(true);

		return solutionDecompser( sol,  uniqSig);	
	}
	

	private Expr makeUniqPredicateANDAppnededFact(final Sig uniqSig, final Expr expr) throws Err{
		//I am trying to make each signature one, then iterativly run the solver.
		ExprHasName u1 =  ExprVar.make(Pos.UNKNOWN,Util.tail(uniqSig.label).toLowerCase()+"_1",uniqSig.type());
		ExprHasName u2 =  ExprVar.make(Pos.UNKNOWN,Util.tail(uniqSig.label).toLowerCase()+"_2",uniqSig.type());
		List<ExprHasName> names = new ArrayList<ExprHasName>();
		names.add(u1);
		names.add(u2);
		edu.mit.csail.sdg.alloy4compiler.ast.Decl decl = 
				new edu.mit.csail.sdg.alloy4compiler.ast.Decl(null,Pos.UNKNOWN, null, names, PIUtil.mult( uniqSig));
		List<edu.mit.csail.sdg.alloy4compiler.ast.Decl> decls = new ArrayList<edu.mit.csail.sdg.alloy4compiler.ast.Decl>();
		decls.add(decl);
		List<Expr> orOprands = new ArrayList<Expr>();
		for(Sig.Field field: uniqSig.getFieldsWithParents() ){
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


		Expr uniqExpr =null;
		uniqExpr = ExprQt.Op.ALL.make(Pos.UNKNOWN, Pos.UNKNOWN, decls, orExpr);



		assert expr instanceof ExprQt : "The appended fact has to be a quantifier ";

		Expr appendedExpr = expr;
		if(expr != null && expr instanceof ExprQt){
			ExprQt exprQT = (ExprQt)expr;
			appendedExpr = ExprQt.Op.ALL.make(exprQT.pos, exprQT.closingBracket, exprQT.decls, exprQT.sub);
			appendedExpr = appendedExpr.resolve_as_formula(null);
		}
		List<ErrorWarning> warnings = new ArrayList();


		//The unique predicate now should be conjucted with the appended fact.
		Expr commandExpr = appendedExpr != null? ExprBinary.Op.AND.make(Pos.UNKNOWN, Pos.UNKNOWN, uniqExpr,appendedExpr):uniqExpr;

		commandExpr = commandExpr.resolve_as_formula(warnings);

		//LoggerUtil.Detaileddebug(this, "The warnings %n%s", warnings);
		//LoggerUtil.Detaileddebug(this, "The command after typechecked %n%s", commandExpr);

		return commandExpr;
	}


	private Command includeIntoLowerbound(final Command oldCommand, final Sig uniqSig, final Instance inst) throws Err{

		final Map<String, CommandScope> scopes = new HashMap<String, CommandScope>();
		final Map<String, Field> fields = new HashMap<String, Field>();
		final Map<String, CommandScope> newScopes = new HashMap<String, CommandScope>();

		String unigSigName = PIUtil.nameSanitizer(PIUtil.tailDot(uniqSig.label));

		for(CommandScope cs: oldCommand.scope){
			String key = PIUtil.nameSanitizer(PIUtil.tailDot(cs.sig.label));
			scopes.put(key, cs);
		}

		for(Field field: uniqSig .getFieldsWithParents()){
			String key = PIUtil.nameSanitizer(PIUtil.tailDot(field.label));
			fields.put(key, field);
		}

		for(Relation rel: inst.relations()){
			//if the relation is the signature itself
			String relName = PIUtil.nameSanitizer(PIUtil.tailDot(rel.name()));
			//if the relation is one of the signature's fields
			if( fields.containsKey(relName) ){

				CommandScope oldScope = scopes.get(relName);
				List<List<Expr>> tuples = convertFieldTupleSetToList(inst.tuples(rel));

				////LoggerUtil.Detaileddebug(this, "The tuples is %s%n The oldCommand is %s%n The sig is: %s%n The instance is: %s",tuples, oldCommand, uniqSig, inst );

				newScopes.put(relName,
						new CommandScope(oldScope==null? oldCommand.pos: oldScope.pos, 
								oldScope==null? new PrimSig(fields.get(relName).label,AttrType.WHERE.make(Pos.UNKNOWN) ): oldScope.sig ,false,
										tuples.size(),
										tuples.size(),1,new ArrayList(), 
										tuples.size(), tuples,
										true, true, false, false) );

			}else if(unigSigName.equals(relName)  ){

				CommandScope oldScope = scopes.get(relName);
				List<Expr> tuples = convertSigTupleSetToList(inst.tuples(rel));

				newScopes.put(relName,
						new CommandScope(oldScope.pos, oldScope.sig ,false, 
								tuples.size(), tuples.size(), 1, 
								tuples, tuples.size())
						);
			}
		}

		scopes.putAll(newScopes);

		return oldCommand.change(ConstList.make(scopes.values()));
	}


	private Command changeCommandScope(Command command, Sig sig, int newUpper) throws ErrorSyntax{
		List<CommandScope> scopes = new ArrayList<CommandScope>();
		String sigName =  PIUtil.nameSanitizer(PIUtil.tailDot(sig.label));
		for(CommandScope cs: command.scope){

			//			//LoggerUtil.Detaileddebug(this,"cs.label=%s \t sig.lable=%s", PIUtil.nameSanitizer(PIUtil.tailDot(cs.sig.label)),
			//					PIUtil.nameSanitizer(PIUtil.tailDot(sig.label)));
			if(PIUtil.nameSanitizer(PIUtil.tailDot(cs.sig.label)).equals(sigName)){
				List<ExprVar> atoms = new ArrayList<>(cs.pAtoms);
				if(atoms.size() < newUpper){
					for(int i =  atoms.size(); i < newUpper; i++ ){
						atoms.add(ExprVar.make(Pos.UNKNOWN, sigName+"$"+i));
					}
				}else{
					for(int i = atoms.size()-1; i >= newUpper;  i--){
						atoms.remove(i);
					}
				}
				//LoggerUtil.Detaileddebug("The newUpper is: %n%d, The new atoms list is: %n\t%s",newUpper, atoms);
				scopes.add(new CommandScope(cs.pos, cs.sig, true, 
						atoms.size(), atoms.size(), 1, 
						atoms,atoms.size(),cs.isSparse));
				//LoggerUtil.Detaileddebug("The added scope is: %n\t%s", scopes.get(scopes.size()-1));
			}else{
				scopes.add(cs);
			}
		}

		return command.change(ConstList.make(scopes));

	}

	private List<Instance> solutionDecompser(Instance sol, Sig uniqSig) throws ErrorType{

		if (sol == null)
			throw new ErrorType("Unstatisfiable appended fact of "+uniqSig.label);

		LoggerUtil.Detaileddebug(this,"The solution for <%s> is: %n%s and",uniqSig,sol);

		Map<Object,List<Object>> uniqSigTupleSets = new HashMap<Object,List<Object>>();  
		Set<Object> uniqSigUni = new HashSet<Object>(); 
		for(Relation r: sol.relations()){
			if(PIUtil.tailDot(r.name()).equals(PIUtil.tailDot(uniqSig.label))){
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

		//LoggerUtil.Detaileddebug(this, "The uniqSigTupleSets is %s", uniqSigTupleSets);

		Map<Object, Instance> retInsts = new IdentityHashMap<Object, Instance>();
		for(Object atom: uniqSigTupleSets.keySet()){
			Universe instUni = new Universe(uniqSigTupleSets.get(atom));
			//LoggerUtil.Detaileddebug(this, "The atom is <%s>%n The uniqSigTupleSets.get(atom) is <%s> ", atom,uniqSigTupleSets.get(atom));
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

						////LoggerUtil.Detaileddebug(this, "The tuple is %s and r is %s and the instUni is %s ", tuple,r,instUni);
						List<Object> atomsList = new ArrayList<Object>();
						boolean invalidTuple = false;
						for(int i= 0; i<tuple.arity();i++){
							//If any atom of this tuple is in uniqSigTupleSets.keys then later it has to be genrated.
							//This can be happened if two sigs from the same super sig is generated.
							////LoggerUtil.Detaileddebug(this, "The tuple atom is %s and r is %s and the instUni is %s ", tuple,r,instUni);
							if( !instUni.contains(tuple.atom(i))){
								invalidTuple = true;
								break;
							}
							atomsList.add(tuple.atom(i));

						}

						if(invalidTuple)
							continue;

						assert atomsList.size()==0:"Is there any tuple with zero arity?";
						tuplesList.add(instUni.factory().tuple(atomsList));
					}
					////LoggerUtil.Detaileddebug(this, "The tupleList is  %s ", tuplesList);
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

	private List<List<Expr>> convertFieldTupleSetToList(final TupleSet tuples){

		final List<List<Expr>> result = new ArrayList<List<Expr>>();

		for(Tuple tuple: tuples){
			List<Expr> tupleList = new ArrayList<Expr>();
			for(int i = 0; i < tuple.arity(); i++){
				tupleList.add(ExprVar.make(Pos.UNKNOWN, tuple.atom(i).toString()) );
			}
			result.add(tupleList);
		}

		return result;
	}

	private List<Object> tupleSet2Atoms(TupleSet tupleSet){
		assert (tupleSet.arity() == 1) : "Tupleset with arity more than 1 cannot be converted to a List";
		List<Object> atoms = new ArrayList<Object>();
		for(Tuple tuple: tupleSet){
			atoms.add(tuple.atom(0));
		}
		return atoms;
	}

	private List<Expr> convertSigTupleSetToList(final TupleSet tuples) throws ErrorSyntax{

		if(tuples.arity() > 1)
			throw new ErrorSyntax("The arity of a sig has to be 1");

		final List<Expr> result = new ArrayList<Expr>();

		for(Tuple tuple: tuples){
			result.add(ExprVar.make(Pos.UNKNOWN, tuple.atom(0).toString()) );
		}

		return result;
	}


	
	

}
