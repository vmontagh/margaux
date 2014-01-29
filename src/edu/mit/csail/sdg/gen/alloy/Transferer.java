package edu.mit.csail.sdg.gen.alloy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorSyntax;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Pos;
import edu.mit.csail.sdg.alloy4compiler.ast.Attr.AttrType;
import edu.mit.csail.sdg.alloy4compiler.ast.Bounds;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.CommandScope;
import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprConstant;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprHasName;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprList;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprQt;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Tuple;
import edu.mit.csail.sdg.alloy4compiler.ast.Type;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.gen.LoggerUtil;

public class Transferer {

	final private Command oldCommand, alterdCommand;
	final private List<A4Solution> counterExamples;

	private Transferer(final Command oldCommand, final List<A4Solution> counterExamples, boolean invert) throws Err{
		if(counterExamples.isEmpty()){

			this.oldCommand = null;
			this.counterExamples = null;
			this.alterdCommand = null;

			return;
		}

		this.oldCommand = oldCommand;
		this.counterExamples = new ArrayList<>(counterExamples);


		Set<String> skolems = new HashSet<String>();
		Sig skolemSig = null;

		Expr newFact = null;

		for(A4Solution ce: this.counterExamples){
			if(null == newFact){
				newFact = InstanceToPredicate(ce);
			}else{
				newFact = newFact.and(InstanceToPredicate(ce));
			}

			Pair<String,Sig> counterExample = getFirstSkolem(ce);
			if(counterExample.a != null){
				skolemSig =  counterExample.b;
				skolems.add(counterExample.a);
			}
		}

		LoggerUtil.debug(this, "The intermediate fact is %s:", newFact);
		if(null != skolemSig  && null != newFact){

			Expr newFormula =  (invert? this.oldCommand.formula.not(): this.oldCommand.formula).and(newFact);
			Pair<Expr,List<ExprConstant>> cardPair = makeTuplesNumberConstraints(this.oldCommand, skolemSig);

			newFormula = newFormula.and(cardPair.a);

			LoggerUtil.debug(this, "The formula is->%s%n", newFormula);
			List<ErrorWarning> reportedWarning = new ArrayList<ErrorWarning>();
			newFormula = newFormula.resolve_as_formula(reportedWarning);
			//LoggerUtil.debug(this,"%s %s", reportedWarning.toString(), newFormula.typecheck_as_formula());
			//System.exit(-10);

			this.alterdCommand = addIntegers(removeAtoms(oldCommand.change(newFormula), skolems, skolemSig),cardPair.b);
		}else{
			this.alterdCommand = oldCommand;
		}
	}

	public static Command computeNewCommandByNegation(final Command oldCommand, final List<A4Solution> counterExamples) throws Err{
		return (new Transferer(oldCommand, counterExamples,true)).computeCommand();
	}

	public static Command computeNewCommand(final Command oldCommand, final List<A4Solution> counterExamples) throws Err{
		return (new Transferer(oldCommand, counterExamples,false)).computeCommand();
	}

	private Command computeCommand(){
		return alterdCommand;
	}

	private  Expr makeQuntifiedExpression(final ExprVar v, final A4Solution ans) throws Err{

		Map<String, Sig> sigs = new HashMap<String, Sig>();

		for(Sig sig: ans.getAllReachableSigs()){
			sigs.put(PIUtil.nameSanitizer(sig.label),sig);

		}

		Expr fact = null;

		String skolemName = PIUtil.nameSanitizer(ans.eval(v).toString());
		Type skolemType =  v.type();
		String skolemTypeName = PIUtil.nameSanitizer(skolemType.toString());
		//Type name ->(Type name -> created ExprVar )
		Map<String,Map<String, ExprHasName>> vars = new HashMap<String,Map<String, ExprHasName>>();
		Map<String, ExprHasName> map = new HashMap<String, ExprHasName>();
		map.put(skolemName, ExprVar.make(Pos.UNKNOWN, skolemTypeName, sigs.get(skolemTypeName).type()) );
		vars.put(skolemTypeName, map);

		//Making the body
		Map<String, List<Expr>> body = new HashMap<String, List<Expr>>();
		Map<String, Field> fields = new HashMap<String, Sig.Field>();


		for(Field f:sigs.get(skolemTypeName).getFields()){

			fields.put(f.label, f);
			body.put(f.label, new ArrayList<Expr>());

			for(A4Tuple tuple: ans.eval(f)){
				if(PIUtil.nameSanitizer(tuple.atom(0)).equals(skolemName)){

					LoggerUtil.debug(this,PIUtil.nameSanitizer(tuple.atom(0)));

					Expr tupleExpr = null;
					//for every tuple, if the first atom is in the skolem var, then the tuple is not added 
					for(int i=0; i < tuple.arity(); i++){

						String sigI = PIUtil.nameSanitizer(tuple.sig(i).label);
						String varName = PIUtil.nameSanitizer(tuple.atom(i)); 

						if(!vars.containsKey( sigI ))
							vars.put(sigI, new TreeMap<String, ExprHasName>());
						
						ExprHasName atomExpr = vars.get(sigI).containsKey(varName) ? 
												vars.get(sigI).get(varName) : 
													ExprVar.make(Pos.UNKNOWN, varName , tuple.sig(i).type()  );

						if(!vars.get(sigI).containsKey(varName))
							vars.get(sigI).put(varName, atomExpr);
						
						if(0==i){
							tupleExpr = atomExpr;
						}else{
							tupleExpr = tupleExpr.product(atomExpr);
							tupleExpr = tupleExpr.resolve_as_set(new ArrayList());
						}
					}

					LoggerUtil.debug(this, "tupleExpr is: %s",tupleExpr);

					assert body.containsKey( f.label ) && tupleExpr!=null;
					body.get(f.label).add(tupleExpr);
				}
			}
		}

		List<Expr> bodyExprsList = new ArrayList<Expr>();

		for(String fld: body.keySet()){
			if (body.get(fld).size() == 0){

				
				
				
				LoggerUtil.debug(this, "%s %s %n%s",skolemName,vars.get(skolemTypeName), vars.get(skolemTypeName).get(skolemName), vars);
				
				
				
				bodyExprsList.add(
						vars.get(skolemTypeName).get(skolemName)
								.join(ExprUnary.Op.NOOP.make(Pos.UNKNOWN, fields.get(fld)) ).no());

			}else{
				Expr lhs = body.get(fld).get(0);					
				for(Expr tuple: body.get(fld)){
					lhs = lhs.plus(tuple);
					lhs.resolve_as_set(new ArrayList());
				}

				Expr inExpr = lhs.in(fields.get(fld));
				inExpr = inExpr.resolve_as_formula(new ArrayList());
				bodyExprsList.add(inExpr );
			}
		}

		//Declarations
		fact =  ExprList.make(Pos.UNKNOWN, Pos.UNKNOWN, ExprList.Op.AND, bodyExprsList);
		for(String type: vars.keySet()){
			assert vars.get(type).size() > 0;
			LoggerUtil.debug(this, "The type is %s %s", type,sigs.get(type));
			if(vars.get(type).size() == 1){
				fact = fact.forOne(new Decl(null,null,null,
						new ArrayList<ExprHasName>( vars.get(type).values()),
						ExprUnary.Op.NOOP.make(Pos.UNKNOWN, sigs.get(type)).oneOf()))
						.resolve_as_formula(new ArrayList<ErrorWarning>());
			}else{
				fact = fact.forSome(new Decl(null,null,null,
						new ArrayList<ExprHasName>( vars.get(type).values()),
						ExprUnary.Op.NOOP.make(Pos.UNKNOWN, sigs.get(type))))
						.resolve_as_formula(new ArrayList<ErrorWarning>());
			}

		}


		LoggerUtil.debug(this, "This returned fact is:%s %n%s", fact, fact.errors);
		//System.exit(-10);

		return fact;

	}

	private  Expr InstanceToPredicate(A4Solution ans) throws Err{

		Expr result = null;



		for(ExprVar v:ans.getAllSkolems()) {

			assert result != null : "Only one skolemized variable has to be found!";

			Expr fact = makeQuntifiedExpression(v,ans);

			if(null == result){
				result = fact;
			}else{
				result = ExprBinary.Op.AND.make(null, null, result, fact);
			}
		}

		return result;
	}


	private  Map<String,Field> getFields(Sig sig){
		Map<String,Field> fields = new HashMap<String, Sig.Field>();

		for(Sig.Field field: sig.getFields()){
			fields.put(field.label, field);
		}

		return fields;
	}

	private  CommandScope changeScope(final CommandScope cs, final Set<String> toBeRemovedAtoms, final Sig sig ){

		if(cs==null)
			return cs;

		Map<String,Field> fields = getFields(sig);

		//If the scope is defined for fields
		if(fields.containsKey(PIUtil.nameSanitizer(cs.sig.label))){
			List<List<Expr>> pFields = new ArrayList<List<Expr>>();
			for(List<Expr> tuple: cs.pFields){
				if(!toBeRemovedAtoms.contains(PIUtil.nameSanitizer(tuple.get(0).toString())))
					pFields.add(new ArrayList<Expr>(tuple));
			}
			return new CommandScope(cs.pos, cs.sig, false, pFields.size(), pFields.size(), 
					cs.increment, cs.pAtoms, cs.pAtomsLowerLastIndex, pFields, 
					true, true, false, cs.isSparse);
		}


		//If the scope is defined for the signature as set of atoms
		if(sig.label.equals(cs.sig.label)){
			List<ExprVar> pAtoms = new ArrayList<ExprVar>();
			for(ExprVar atom: cs.pAtoms){
				if(!toBeRemovedAtoms.contains(PIUtil.nameSanitizer(atom.label)))
					pAtoms.add(atom);
			}
			return new CommandScope(cs.pos, cs.sig, false, pAtoms.size(), pAtoms.size(), 
					cs.increment, pAtoms, cs.pAtomsLowerLastIndex, cs.pFields, 
					true, true, false, cs.isSparse);

		}

		return cs;

	}

	private  Command removeAtoms(final Command cmd,
			final Set<String> toBeRemovedAtoms, final Sig sig){

		List<CommandScope> nList = new ArrayList<CommandScope>();

		for(CommandScope cs: cmd.scope){
			//if this command scope belongs to any field of the signature, then all tuples refereing the 
			//any atom in toBeRemovedAtoms has to be removed.
			nList.add(changeScope(cs,toBeRemovedAtoms,sig));
		}

		return new Command( cmd.pos, cmd.label, cmd.check, cmd.overall, cmd.bitwidth, 
				cmd.maxseq, cmd.expects, nList, cmd.additionalExactScopes, cmd.formula, 
				cmd.parent,cmd.isSparse,new Bounds(cmd.bound.pos, cmd.bound.label, nList));
	}

	private  Pair<Expr,List<ExprConstant>> makeTuplesNumberConstraints(final Command cmd,
			final Sig sig){
		Expr result = null;

		Map<String,Field> fields = getFields(sig);
		Set<Integer> numbers =  new TreeSet();
		for(CommandScope cs: cmd.scope){
			if(fields.containsKey(PIUtil.nameSanitizer(cs.sig.label))){


				Expr cardExpr = ExprUnary.Op.NOOP.make(Pos.UNKNOWN, fields.get(PIUtil.nameSanitizer(cs.sig.label))).cardinality().equal(ExprConstant.makeNUMBER(cs.endingScope));

				numbers.add(cs.endingScope );
				if(null == result){
					result = cardExpr;
				}else{
					result = result.and(cardExpr);
				}
			}
		}

		List<ExprConstant> exprNumbers = new ArrayList<ExprConstant>();

		for(Integer i: numbers){

			exprNumbers.add((ExprConstant) ExprConstant.makeNUMBER(i));

		}

		return new Pair<Expr,List<ExprConstant>>(result,exprNumbers);
	}


	private  Command addIntegers(Command cmd, List<ExprConstant> ints) throws Err{

		int bitWidth = cmd.bitwidth==-1? 2 : cmd.bitwidth;

		LoggerUtil.debug(this, "addIntegers is called for add %s to %n%s",ints,cmd);
		List<CommandScope> scope = new ArrayList<>( cmd.scope);

		scope.add(new CommandScope(Pos.UNKNOWN, Sig.SPARSE_INT, false, 
				ints.size(), ints.size(), 1, 
				ints, ints.size(),true));
		Bounds newBoud = new Bounds(cmd.bound.pos,cmd.bound.label,scope );
		return new Command(cmd.pos, cmd.label,cmd.check, cmd.overall, bitWidth, cmd.maxseq, 
				cmd.expects, scope, cmd.additionalExactScopes, cmd.formula, cmd.parent, true, newBoud);

	}


	private  Pair<String,Sig> getFirstSkolem(A4Solution ans) throws Err{
		for(ExprVar v:ans.getAllSkolems()) 
			for(Sig sig: ans.getAllReachableSigs())
				if(PIUtil.nameSanitizer(sig.label).equals(PIUtil.nameSanitizer(v.type().toString())))
					return new Pair<String, Sig>(PIUtil.nameSanitizer(ans.eval(v).toString()), sig);
		return new Pair<String, Sig>(null,null);
	}

	

}
