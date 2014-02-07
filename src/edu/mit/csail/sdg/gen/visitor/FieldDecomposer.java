package edu.mit.csail.sdg.gen.visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Bounds;
import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBadJoin;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprCall;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprConstant;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprITE;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprLet;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprList;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprQt;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.VisitReturn;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;

//A visitor class to decompose a field expression, which should be ExprBinary to the Signatures or other relations.
//For example A->B->C decomposed into [A,B,C]
//
public final class FieldDecomposer extends VisitReturn<Expr> {

	//final Expr expr;
	List<Expr> sigs = null;
	List<ExprUnary.Op> mults = null;
	Set<Sig.Field> fields = null;

	public FieldDecomposer() {
	}

	public Set<Sig.Field> extractFieldsInExpr(Expr expr) throws Err{
		fields = new HashSet<Sig.Field>();
		if(expr == null)
			return fields;
		sigs=null;
		mults=null;
		visitThis( expr);
		return this.fields;
	}

	public Set<Sig> extractSigsInExpr(Expr expr) throws Err{
		sigs = new ArrayList();
		if(expr == null)
			return new HashSet();
		fields = null;
		mults=null;
		visitThis( expr);
		Map<String, Sig> resultMap = new HashMap<String, Sig>();

		for(Expr sig : sigs){
			if(sig instanceof Sig){
				resultMap.put(((Sig)sig).label,(Sig)sig);
			}
		}
		return new HashSet(resultMap.values());
	}

	public List<Expr> extractFieldsItems(Sig.Field field) throws Err{
		fields =null;
		sigs = new ArrayList<Expr>();
		mults = new ArrayList<ExprUnary.Op>();
		visitThis( field.decl().expr);
		return this.sigs;
	}

	public Set<Sig> extractSigsFromFields(Collection<Field> fields) throws Err{
		Set<Sig> sigs = new HashSet<Sig>();
		for(Sig.Field field:fields){
			//LoggerUtil.Detaileddebug(this, "The field is %s %n The sigs are: %s", field, extractFieldsItems(field));

			for(Expr sig:extractFieldsItems(field)){
				if(sig instanceof Sig)
					sigs.add((Sig)sig);
				else if(sig instanceof Sig.Field){
					Collection<Field> subFields = new ArrayList<Sig.Field>();
					subFields.add((Field)sig);
					sigs.addAll(extractSigsFromFields(subFields));
				}
			}
		}
		return sigs;
	}


	public List<ExprUnary.Op> getMultiplicities(Sig.Field fld) throws Err{
		fields =null;
		sigs = new ArrayList<Expr>();
		mults = new ArrayList<ExprUnary.Op>();
		visitThis( fld.decl().expr);
		return this.mults;
	}


	@Override public Expr visit(ExprBadJoin x) throws Err {
		visitThis(x.left);
		visitThis(x.right);
		return x;
	}

	@Override
	public Expr visit(ExprBinary x) throws Err {
		if(mults!=null)
			switch(((ExprBinary)x).op){
			case LONE_ARROW_LONE:
				mults.add( ExprUnary.Op.LONEOF);
				mults.add(ExprUnary.Op.LONEOF);
				break;
			case LONE_ARROW_ONE:
				mults.add(ExprUnary.Op.LONEOF);
				mults.add(ExprUnary.Op.ONEOF);
				break;
			case LONE_ARROW_ANY:
				mults.add(ExprUnary.Op.LONE);
				mults.add(ExprUnary.Op.NOOP);
				break;				
			case LONE_ARROW_SOME:
				mults.add(ExprUnary.Op.LONEOF);
				mults.add(ExprUnary.Op.SOME);
				break;	

			case ONE_ARROW_ONE:
				mults.add(ExprUnary.Op.ONEOF);
				mults.add(ExprUnary.Op.ONEOF);
				break;
			case ONE_ARROW_LONE:
				mults.add(ExprUnary.Op.ONEOF);
				mults.add(ExprUnary.Op.LONEOF);
				break;
			case ONE_ARROW_SOME:
				mults.add(ExprUnary.Op.ONEOF);
				mults.add(ExprUnary.Op.SOMEOF);
				break;
			case ONE_ARROW_ANY:
				mults.add(ExprUnary.Op.ONEOF);
				mults.add(ExprUnary.Op.NOOP);
				break;

			case SOME_ARROW_ONE:
				mults.add(ExprUnary.Op.SOMEOF);
				mults.add(ExprUnary.Op.ONEOF);
				break;
			case SOME_ARROW_LONE:
				mults.add(ExprUnary.Op.SOMEOF);
				mults.add(ExprUnary.Op.LONE);
				break;
			case SOME_ARROW_SOME:
				mults.add(ExprUnary.Op.SOMEOF);
				mults.add(ExprUnary.Op.SOMEOF);
				break;
			case SOME_ARROW_ANY:
				mults.add(ExprUnary.Op.SOMEOF);
				mults.add(ExprUnary.Op.NOOP);
				break;

			case ANY_ARROW_ONE:
				mults.add(ExprUnary.Op.NOOP);
				mults.add(ExprUnary.Op.ONEOF);
				break;
			case ANY_ARROW_LONE:
				mults.add(ExprUnary.Op.NOOP);
				mults.add(ExprUnary.Op.LONEOF);
				break;
			case ANY_ARROW_SOME:
				mults.add(ExprUnary.Op.NOOP);
				mults.add(ExprUnary.Op.SOMEOF);
				break;
			case ARROW:
				mults.add( ExprUnary.Op.NOOP);
				mults.add(ExprUnary.Op.NOOP);
				break;


			}
		visitThis(x.left);
		visitThis(x.right);
		return x;
	}

	@Override
	public Expr visit(ExprList x) throws Err {
		for(int i=0; i<x.args.size(); i++) {
			visitThis(x.args.get(i));
		}
		return x;
	}

	@Override
	public Expr visit(ExprCall x) throws Err {

		return x;
	}

	@Override
	public Expr visit(ExprConstant x) throws Err {

		return x;
	}

	@Override
	public Expr visit(ExprITE x) throws Err {
		visitThis(x.cond);
		visitThis(x.left);
		visitThis(x.right);
		return x;
	}

	@Override
	public Expr visit(ExprLet x) throws Err {
		visitThis(x.expr);
		visitThis(x.var);
		visitThis(x.sub);
		return x;
	}

	@Override
	public Expr visit(ExprQt x) throws Err {
		for(Decl decl:x.decls){
			//LoggerUtil.Detaileddebug(this, "The decl expr is: %s and the class is %s", decl.expr, decl.expr.getClass());
			visitThis(decl.expr);
		}
		visitThis(x.sub);
		return x;
	}

	@Override
	public Expr visit(ExprUnary x) throws Err {
		if(x.op.equals(ExprUnary.Op.SETOF) 
				|| x.op.equals(ExprUnary.Op.LONEOF)
				|| x.op.equals(ExprUnary.Op.ONEOF)
				|| x.op.equals(ExprUnary.Op.SOMEOF))
			if(mults!=null) mults.add( x.op);
		//LoggerUtil.Detaileddebug(this, "The unary of %s is: %s and the class is %s",x ,x.sub, x.sub.getClass());
		visitThis(x.sub);
		return x;
	}

	@Override
	public Expr visit(ExprVar x) throws Err {
		if(sigs!=null && !x.label.equals("this")) sigs.add(x);
		return x;

	}



	@Override
	public Expr visit(Sig x) throws Err {
		//LoggerUtil.Detaileddebug(this, "The passed Sig is %s %nThe sigs is already contains %s", x,sigs);
		if(sigs!=null) sigs.add(x);
		return x;
	}

	@Override
	public Expr visit(Field x) throws Err {
		if(sigs!=null) sigs.add(x);
		if(fields!=null) fields.add(x);
		return x;
	}

	@Override
	public Expr visit(Bounds x) throws Err {
		return null;
	}
}


