package edu.mit.csail.sdg.gen.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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

public final class BoundFactMangler  extends VisitReturn<Expr> {

		final CompModule rootmodule;
		/**Atoms name and the desired sig name*/
		final Map<String, String> newNames;
		final Expr expr;

		/**The atoms that are got accessed from appended fact*/
		List<String> accessed = new ArrayList<String>();
		/**The defined names by let or quantifiers*/
		Set<String> defined = new TreeSet<String>(); 

		public BoundFactMangler(CompModule rootmodule,Map<String, String> newNames, Expr expr){
			this.rootmodule = rootmodule;
			this.newNames = newNames;
			this.expr = expr;
		}

		public Expr replace() throws Err{
			return visitThis(expr);
		}

		public List<String> getAccessedAtoms(){
			return accessed;
		}

		@Override public Expr visit(ExprBadJoin x) throws Err {
			return x;
		}

		@Override
		public Expr visit(ExprBinary x) throws Err {
			if (x.op==ExprBinary.Op.JOIN) {
				return x;
			}else{
				Expr left = visitThis(x.left);
				Expr right = visitThis(x.right);

				return x.op.make(x.pos, x.closingBracket, left, right);
			}
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
			//System.out.println("In call->"+x);
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
			defined.add(left.label);
			Expr sub = visitThis(x.sub);
			return ExprLet.make(x.pos, left, right, sub);
		}

		@Override
		public Expr visit(ExprQt x) throws Err {
			//Put the defined name in the expression in the defined list.
			for(Decl decl:x.decls)
				for(ExprHasName name:decl.names)
					defined.add(name.label);
			//System.out.println("x.sub is->"+x.sub);
			Expr sub = visitThis(x.sub);
			//System.out.println("Here is the expression->"+x.op.make(x.pos, x.closingBracket, x.decls, sub));
			return x.op.make(x.pos, x.closingBracket, x.decls, sub);
		}

		@Override
		public Expr visit(ExprUnary x) throws Err {
			return x.op.make(x.pos, visitThis(x.sub));
		}

		@Override
		public Expr visit(ExprVar x) throws Err {

			String name = x.label;
			if(newNames.containsKey(name) && !name.contains("@") && !defined.contains(name)){
				name = newNames.get(name);
				accessed.add(x.label);
			}
			return ExprVar.make(x.pos, name, x.type());

		}



		@Override
		public Expr visit(Sig x) throws Err {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Expr visit(Field x) throws Err {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Expr visit(Bounds bounds) throws Err {
			// TODO Auto-generated method stub
			return null;
		}

	}

