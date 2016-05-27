package edu.uw.ece.alloy.debugger.filters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.javafx.fxml.expression.UnaryExpression;

import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprList;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary;

/**
 * @author vajih
 *
 */
public class Decompose {

	/**
	 * * Given an expression it decomposes them into a set of expression that are
	 * conjuncted. E.g. (a or b) and c and d ~> (a or b)
	 */
	public static List<Expr> decomposetoConjunctions(Expr expr) {
		List<Expr> result = new ArrayList<>();

		Expr tmpExpr = expr;

		while ((tmpExpr instanceof ExprUnary)
				&& (((ExprUnary) tmpExpr).op.equals(ExprUnary.Op.NOOP))) {
			tmpExpr = ((ExprUnary) tmpExpr).sub;
		}

		// a list is expected to be seen. The operator is AND
		if (!(tmpExpr instanceof ExprList)
				|| !(((ExprList) tmpExpr).op.equals(ExprList.Op.AND))) {
			result.add(tmpExpr);
		} else {
			result.addAll(((ExprList) tmpExpr).args);
		}

		return Collections.unmodifiableList(result);
	}

	/**
	 * * Given an expression it decomposes them into a set of expression that are
	 * conjuncted. E.g. (a or b) and c and d ~> [(a or b), c, d]
	 */
	public static Pair<List<Expr>, List<Expr>> decomposetoImplications(
			Expr expr) {

		Expr tmpExpr = expr;

		while ((tmpExpr instanceof ExprUnary)
				&& (((ExprUnary) tmpExpr).op.equals(ExprUnary.Op.NOOP))) {
			tmpExpr = ((ExprUnary) tmpExpr).sub;
		}

		// a list is expected to be seen. The operator is AND
		if (!(tmpExpr instanceof ExprList)
				|| !(((ExprList) tmpExpr).op.equals(ExprList.Op.AND))
				|| !(((ExprList) tmpExpr).args.size() == 1)) {
			// The model is not in the form of M=>P so return in the form of
			// conjunctions
			return new Pair<List<Expr>, List<Expr>>(decomposetoConjunctions(tmpExpr),
					Collections.emptyList());
		}

		tmpExpr = ((ExprList) tmpExpr).args.get(0);

		while ((tmpExpr instanceof ExprUnary)
				&& (((ExprUnary) tmpExpr).op.equals(ExprUnary.Op.NOOP))) {
			tmpExpr = ((ExprUnary) tmpExpr).sub;
		}

		// if the check is in the form of P=Q, then the given formula is
		// in the form of !(P=>Q) in order to find counter example.

		if ((tmpExpr instanceof ExprUnary)
				&& (((ExprUnary) tmpExpr).op.equals(ExprUnary.Op.NOT))) {
			tmpExpr = ((ExprUnary) tmpExpr).sub;
		}

		while ((tmpExpr instanceof ExprUnary)
				&& (((ExprUnary) tmpExpr).op.equals(ExprUnary.Op.NOOP))) {
			tmpExpr = ((ExprUnary) tmpExpr).sub;
		}

		if (!(tmpExpr instanceof ExprBinary)
				|| !(((ExprBinary) tmpExpr).op.equals(ExprBinary.Op.IMPLIES))) {
			throw new RuntimeException(
					"The expression is expected to be like P=>Q but :\n"
							+ tmpExpr.getClass() + "\n" + tmpExpr + "\n" + tmpExpr);
		}

		return new Pair<List<Expr>, List<Expr>>(
				decomposetoConjunctions(((ExprBinary) tmpExpr).left),
				decomposetoConjunctions(((ExprBinary) tmpExpr).right));
	}

}
