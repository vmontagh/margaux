package edu.uw.ece.alloy.visitor;

import java.util.ArrayList;
import java.util.List;

import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Bounds;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
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

public final class ClosureAssignmentExpressionRemover extends VisitReturn<Expr>{

		final private Expr rm;
		final private Expr rmed;
		
		public ClosureAssignmentExpressionRemover(final Expr expr, final Expr rm) throws Err{
			this.rm = rm;
			this.rmed = visitThis(expr);
		}
		
		
		public final Expr getRemovedPredicate(){
			return this.rmed;
		}
		
		@Override
		public Expr visit(ExprBinary x) throws Err {
			if(x.equals(rm))
				return null;
			else if(((ExprBinary)x).op.equals(ExprBinary.Op.AND)){
				Expr lhs = visitThis(x.left);
				Expr rhs = visitThis(x.right);
				if(lhs!=null && rhs!=null)
					return x.op.make(x.pos, x.closingBracket, rhs, rhs);
				else if(rhs != null)
					return rhs;
				else if(lhs !=null)
					return lhs;
			}
			return x;
		}

		@Override
		public Expr visit(ExprList x) throws Err {
			List<Expr> list = new ArrayList<Expr>();
			for(Expr arg: x.args){
				Expr ret = visitThis(arg);
				if(ret !=null)
					list.add(ret);
			}
			return list.size()==1  ?list.get(0) : ExprList.make(x.pos, x.closingBracket, x.op, ConstList.make(list));
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
			return x.make(x.pos,visitThis( x.cond), visitThis( x.left), visitThis( x.right));
		}

		@Override
		public Expr visit(ExprLet x) throws Err {
			return x.make(x.pos, x.var, visitThis(x.expr), visitThis(x.sub));
		}

		@Override
		public Expr visit(ExprQt x) throws Err {
			Expr sub = visitThis(x.sub);
			if(sub!=null)
				return x.op.make(x.pos, x.closingBracket, x.decls, sub);
			else
				return null;
		}

		@Override
		public Expr visit(ExprUnary x) throws Err {
			Expr sub = visitThis( x.sub);
			return sub !=null ? x.op.make(x.pos, visitThis( x.sub)) : null;
		}

		@Override
		public Expr visit(ExprVar x) throws Err {
			return x;
		}

		@Override
		public Expr visit(Sig x) throws Err {
			return x;
		}

		@Override
		public Expr visit(Field x) throws Err {
			return x;
		}

		@Override
		public Expr visit(Bounds bounds) throws Err {
			return bounds;
		}
		
	}

