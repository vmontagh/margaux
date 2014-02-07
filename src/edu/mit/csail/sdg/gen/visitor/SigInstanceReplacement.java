package edu.mit.csail.sdg.gen.visitor;

import java.util.ArrayList;
import java.util.List;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ConstList.TempList;
import edu.mit.csail.sdg.alloy4compiler.ast.Bounds;
import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBadJoin;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprCall;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprConstant;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprHasName;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprITE;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprLet;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprList;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprQt;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.VisitReturn;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;

public final class SigInstanceReplacement  extends VisitReturn<Expr> {

	final CompModule rootmodule;
	/**Atoms name and the desired sig name*/
	final Sig oldName;
	final ExprHasName newName;
	final Expr expr;
	boolean replaced = false;


	public SigInstanceReplacement(CompModule rootmodule,Sig oldName, ExprHasName newName, Expr expr){
		this.rootmodule = rootmodule;
		this.newName = newName;
		this.oldName = oldName;
		this.expr = expr;
	}

	public Expr replace() throws Err{
		Expr ret = visitThis(expr);
		return ret;
	}



	@Override public Expr visit(ExprBadJoin x) throws Err {
		return x;
	}

	@Override
	public Expr visit(ExprBinary x) throws Err {
		Expr left;
		Expr right;
		left = visitThis(x.left);
		right = visitThis(x.right);
		return x.op.make(x.pos, x.closingBracket, left, right);
	}

	@Override
	public Expr visit(ExprList x) throws Err {
		TempList<Expr> temp = new TempList<Expr>(x.args.size());
		for(int i=0; i<x.args.size(); i++) {
			temp.add(visitThis(x.args.get(i)));
		}
		return ExprList.make(x.pos, x.closingBracket, x.op, temp.makeConst());
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
		Expr f = visitThis(x.cond);
		Expr a = visitThis(x.left);
		Expr b = visitThis(x.right);
		return ExprITE.make(x.pos, f, a, b);
	}

	@Override
	public Expr visit(ExprLet x) throws Err {
		Expr right = visitThis(x.expr);

		ExprVar left = ExprVar.make(x.var.pos, x.var.label, right.type());
		//Put the defined name in the expression in the defined list.
		Expr sub = visitThis(x.sub);
		return ExprLet.make(x.pos, left, right, sub);
	}

	@Override
	public Expr visit(ExprQt x) throws Err {
		List<Decl> declz = new ArrayList<Decl>();
		for(Decl decl:x.decls){
			Expr declExpr = visitThis(decl.expr);
			declz.add(new Decl(decl.isPrivate, decl.disjoint, decl.disjoint2, decl.names, declExpr));
		}
		Expr ret = x.op.make(x.pos, x.closingBracket, declz, visitThis(x.sub));
		return ret;
	}

	@Override
	public Expr visit(ExprUnary x) throws Err {
		return x.op.make(x.pos, visitThis(x.sub));
	}

	@Override
	public Expr visit(ExprVar x) throws Err {
		String name = x.label;
		return ExprVar.make(x.pos, name, x.type());

	}



	@Override
	public Expr visit(Sig x) throws Err {
		if(x.label.equals(oldName.label)){
			replaced = true;
			return newName;
		}
		else 
			return x;
	}

	@Override
	public Expr visit(Field x) throws Err {
		return x;
	}

	@Override
	public Expr visit(Bounds bounds) throws Err {
		return null;
	}
}


