package edu.uw.ece.alloy.debugger.filters;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.mit.csail.sdg.alloy4.Err;
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

/**
 * @author vajih
 *
 */
public final class FieldsExtractorVisitor extends VisitReturn<Expr> {

	final private Map<Sig.Field, Integer> fields;

	private FieldsExtractorVisitor() {
		this.fields = new HashMap<Sig.Field, Integer>();
	}

	static public synchronized int getReferencedCountField(Expr expr, Sig.Field field) throws Err {
		FieldsExtractorVisitor obj = new FieldsExtractorVisitor();
		obj.visitThis(expr);
		return obj.fields.containsKey(field) ? obj.fields.get(field): 0;
	}

	static public synchronized Set<Sig.Field> getReferencedFields(Expr expr) throws Err {

		FieldsExtractorVisitor obj = new FieldsExtractorVisitor();

		obj.visitThis(expr);

		return obj.fields.keySet();
	}

	@Override
	public Expr visit(ExprBinary x) throws Err {

		visitThis(x.left);
		visitThis(x.right);

		return x;
	}

	@Override
	public Expr visit(ExprList x) throws Err {
		for (Expr arg : x.args) {
			visitThis(arg);
		}

		return x;
	}

	@Override
	public Expr visit(ExprCall x) throws Err {
		for (Decl decl : x.fun.decls) {
			visitThis(decl.expr);
		}

		for (Expr arg : x.args) {
			visitThis(arg);
		}

		visitThis(x.fun.getBody());

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
		visitThis(x.sub);

		return x;
	}

	@Override
	public Expr visit(ExprQt x) throws Err {
		for (Decl decl : x.decls) {
			visitThis(decl.expr);
		}

		visitThis(x.sub);

		return x;
	}

	@Override
	public Expr visit(ExprUnary x) throws Err {
		visitThis(x.sub);

		return x;
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
		fields.put(x, fields.containsKey(x) ? fields.get(x) + 1 : 1);
		return x;
	}

	public Expr visit(Bounds bounds) throws Err {
		return null;
	}

}
