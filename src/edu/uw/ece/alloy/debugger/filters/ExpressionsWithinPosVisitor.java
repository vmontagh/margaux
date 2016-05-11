package edu.uw.ece.alloy.debugger.filters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
import edu.mit.csail.sdg.alloy4compiler.ast.VisitReturn;
import edu.uw.ece.alloy.util.Utils;

/**
 * Given A position, find all the sub expressions that are in that pos.
 * 
 * @author vajih
 *
 */
public class ExpressionsWithinPosVisitor extends VisitReturn<Expr> {

	final public Pos pos;
	final private Set<Expr> exprs;

	private ExpressionsWithinPosVisitor(Pos pos) {
		this.pos = pos;
		exprs = new HashSet<>();
	}

	public static List<Expr> findAllExprsWithinPos(Pos pos, Expr expr)
			throws Err {
		ExpressionsWithinPosVisitor ewpv = new ExpressionsWithinPosVisitor(pos);
		ewpv.visitThis(expr);
		return Collections.unmodifiableList(new ArrayList<>(ewpv.exprs));
	}

	public static List<Sig.Field> findAllFieldsWithinPos(Pos pos, Expr expr)
			throws Err {
		List<Expr> exprs = findAllExprsWithinPos(pos, expr);
		List<Sig.Field> result = new LinkedList<>();
		for (Expr e : exprs)
			result.addAll(FieldsExtractorVisitor.getReferencedFields(e));

		return Collections.unmodifiableList(result);
	}

	@Override
	public Expr visit(ExprBinary x) throws Err {
		if (Utils.posWithin(x.pos, pos)) {
			exprs.add(x);
		} else {
			visitThis(x.left);
			visitThis(x.right);
		}
		return x;
	}

	@Override
	public Expr visit(ExprList x) throws Err {
		if (Utils.posWithin(x.pos, pos)) {
			exprs.add(x);
		} else {
			for (Expr arg : x.args) {
				visitThis(arg);
			}
		}
		return x;
	}

	@Override
	public Expr visit(ExprCall x) throws Err {
		if (Utils.posWithin(x.pos, pos)) {
			exprs.add(x);
		} else {
			for (Decl decl : x.fun.decls) {
				visitThis(decl.expr);
			}
			visitThis(x.fun.getBody());
		}
		return x;
	}

	@Override
	public Expr visit(ExprConstant x) throws Err {
		if (Utils.posWithin(x.pos, pos)) {
			exprs.add(x);
		}
		return x;
	}

	@Override
	public Expr visit(ExprITE x) throws Err {
		if (Utils.posWithin(x.pos, pos)) {
			exprs.add(x);
		} else {
			visitThis(x.cond);
			visitThis(x.left);
			visitThis(x.right);
		}
		return x;
	}

	@Override
	public Expr visit(ExprLet x) throws Err {
		if (Utils.posWithin(x.pos, pos)) {
			exprs.add(x);
		} else {
			visitThis(x.expr);
			visitThis(x.sub);
		}
		return x;
	}

	@Override
	public Expr visit(ExprQt x) throws Err {
		if (Utils.posWithin(x.pos, pos)) {
			exprs.add(x);
		} else {
			for (Decl decl : x.decls) {
				visitThis(decl.expr);
			}
			visitThis(x.sub);
		}
		return x;
	}

	@Override
	public Expr visit(ExprUnary x) throws Err {
		if (Utils.posWithin(x.pos, pos)) {
			exprs.add(x);
		} else {
			visitThis(x.sub);
		}
		return x;
	}

	@Override
	public Expr visit(ExprVar x) throws Err {
		if (Utils.posWithin(x.pos, pos)) {
			exprs.add(x);
		}
		return x;
	}

	@Override
	public Expr visit(Sig x) throws Err {
		if (Utils.posWithin(x.pos, pos)) {
			exprs.add(x);
		}
		return x;
	}

	@Override
	public Expr visit(Field x) throws Err {
		if (Utils.posWithin(x.pos, pos)) {
			exprs.add(x);
		}
		return x;
	}

	public Expr visit(Bounds bounds) throws Err {
		return bounds;
	}

}