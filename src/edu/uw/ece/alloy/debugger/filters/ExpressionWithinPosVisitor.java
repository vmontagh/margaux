/**
 * 
 */
package edu.uw.ece.alloy.debugger.filters;

import java.util.List;
import java.util.ArrayList;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pos;
import edu.mit.csail.sdg.alloy4compiler.ast.Bounds;
import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
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
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.uw.ece.alloy.util.Utils;
import edu.mit.csail.sdg.alloy4compiler.ast.VisitReturn;

/**
 * The class is for finding an expression in a given pos. 
 * A opposed to ExpressionsWithinPosVisitor, this class does not
 * decompose an expression to minor parts.
 * @author vajih
 *
 */
public class ExpressionWithinPosVisitor extends VisitReturn<Expr> {

	final public Pos pos;

	private ExpressionWithinPosVisitor(Pos pos){
		this.pos = pos;
	}

	public static final Expr findExprWhitinPos(Pos pos, Expr expr) throws Err{
		ExpressionWithinPosVisitor ewpv = new ExpressionWithinPosVisitor(pos);
		return ewpv.visitThis(expr);

	}

	@Override
	public Expr visit(ExprBinary x) throws Err {
		if (!Utils.posWithin(x.pos, pos)){
			Expr left = visitThis(x.left);
			if (left != null)
				return left;
			return visitThis(x.right);
		}
		return x;
	}

	@Override
	public Expr visit(ExprList x) throws Err {
		if (!Utils.posWithin(x.pos, pos)){
			List<Expr> exprList = new ArrayList<>();
			for(Expr arg: x.args){
				Expr visitedArg = visitThis(arg); 
				if (visitedArg != null){
					exprList.add(visitedArg);
				}
			}

			if (exprList.isEmpty()){
				return null;
			}else if (exprList.size() == 1){
				return exprList.get(0);
			}else{
				return ExprList.make(x.pos, x.closingBracket, x.op, exprList);
			}
		}
		return x;	
	}

	@Override
	public Expr visit(ExprCall x) throws Err {
		if (!Utils.posWithin(x.pos, pos)){
			return visitThis(x.fun.getBody());
		}
		return x;
	}

	@Override
	public Expr visit(ExprConstant x) throws Err {
		System.out.println("const->"+ x);
		return x;
	}

	@Override
	public Expr visit(ExprITE x) throws Err {
		if (!Utils.posWithin(x.pos, pos)){
			Expr cond = visitThis(x.cond);
			if (cond!=null)
				return cond;
			Expr left = visitThis(x.left);
			if (left!=null)
				return left;
			Expr right = visitThis(x.right);
			return right;
		}
		return x;
	}

	@Override
	public Expr visit(ExprLet x) throws Err {
		if (!Utils.posWithin(x.pos, pos)){
			Expr expr = visitThis(x.expr);
			if (expr!=null)
				return expr;
			return visitThis(x.sub);
		}
		return x;
	}

	@Override
	public Expr visit(ExprQt x) throws Err {
		// A quantifier is not decomposable.
		if (!Utils.posWithin(x.pos, pos)){
			for(Decl decl: x.decls){
				visitThis(decl.expr);
			}
			return visitThis(x.sub); 
		}
		return x;
	}

	@Override
	public Expr visit(ExprUnary x) throws Err {
		if (!Utils.posWithin(x.pos, pos)){
			// remove the operator
			return visitThis(x.sub);
		}
		return x;	
	}

	@Override
	public Expr visit(ExprVar x) throws Err {
		if (!Utils.posWithin(x.pos, pos)){
			return null;
		}
		return x;
	}

	@Override
	public Expr visit(Sig x) throws Err {
		if (!Utils.posWithin(x.pos, pos)){
			return null;
		}
		return x;
	}

	@Override
	public Expr visit(Field x) throws Err {
		if (!Utils.posWithin(x.pos, pos)){
			return null;
		}
		return x;
	}

	@Override
	public Expr visit(Bounds bounds) throws Err {
		// TODO Auto-generated method stub
		return null;
	}

}
