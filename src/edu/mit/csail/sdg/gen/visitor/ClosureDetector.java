package edu.mit.csail.sdg.gen.visitor;

import java.util.Set;

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

public final class  ClosureDetector extends VisitReturn<Expr>{

		private  Sig.Field clsredField,clsrField;
		private   Expr clsrExpr,expr;
		
		public ClosureDetector(final Expr expr) throws Err{
			this.clsredField = null;
			this.clsrField = null;
			this.clsrExpr = null;
			this.expr = null;
			if(expr!=null){
				visitThis(expr);
				ClosureAssignmentExpressionRemover caer = 
						new ClosureAssignmentExpressionRemover(expr, this.clsrExpr);
				this.expr = caer.getRemovedPredicate();
			}
		}
		
		//returns 'sigma' ins cl=^sgima
		public final Sig.Field getClosuredField(){
			return this.clsredField;
		}
		
		//returns 'cl' in cl=^sigma
		public final Sig.Field getClosureField(){
			return this.clsrField;
		}
		
		public final Expr getRemovedStatement(){
			return this.expr;
		}
		
		//returns the closure statement
		public final Expr getClosureStatement(){
			return this.clsrExpr;
		}
		
		@Override
		public Expr visit(ExprBinary x) throws Err {
			if(((ExprBinary)x).op.equals(ExprBinary.Op.EQUALS)){
				FieldDecomposer fd = new FieldDecomposer();
				Expr rhs = visitThis(((ExprBinary)x).right);
				Expr lhs = ((ExprBinary)x).left.typecheck_as_formula();
				Set<Sig.Field> lhsFlds = fd.extractFieldsInExpr(lhs);
				if(rhs!=null && lhsFlds.size() ==1){//The rhs is a closure//This rule is not absloutly complete.
					Set<Sig.Field> rhsFlds = fd.extractFieldsInExpr(rhs);
					Sig.Field lhsFld = lhsFlds.iterator().next();
					for(Sig.Field fld: rhsFlds){
						//The lhs and a filed in the rhs are in the same type.
						if(fld.type().equals(lhsFld.type())){
							this.clsredField = fld;
							this.clsrField = lhsFld;
							break;
						}
					}
					this.clsrExpr = x;
				}
			}else if(((ExprBinary)x).op.equals(ExprBinary.Op.AND)){
				visitThis(x.left);
				visitThis(x.right);
			}
			return null;
		}

		@Override
		public Expr visit(ExprList x) throws Err {
			for(Expr arg:x.args)
				visitThis(arg);
			return null;
		}

		@Override
		public Expr visit(ExprCall x) throws Err {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Expr visit(ExprConstant x) throws Err {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Expr visit(ExprITE x) throws Err {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Expr visit(ExprLet x) throws Err {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Expr visit(ExprQt x) throws Err {
			visitThis(x.sub);
			return null;
		}

		@Override
		public Expr visit(ExprUnary x) throws Err {
			if(!(x.op.equals(ExprUnary.Op.RCLOSURE) || x.op.equals(ExprUnary.Op.CLOSURE))){
				visitThis(x.sub);
				return null;
			}else{
				return x.typecheck_as_formula();
			}
			
		}

		@Override
		public Expr visit(ExprVar x) throws Err {
			// TODO Auto-generated method stub
			return null;
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
