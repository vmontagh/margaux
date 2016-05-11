package edu.uw.ece.alloy.debugger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;

/**
 * @author vajih
 *
 */
public class PrettyPrintFunction extends PrettyPrintExpression {

	static public String makeString(Func expr) throws Err {

		PrettyPrintFunction obj = new PrettyPrintFunction();

		obj.visit(expr);

		return obj.result.toString().replace("this/", "");
	}

	public void visit(Func fun) throws Err {

		result.append("\n").append(fun.isPred ? "pred" : "fun");
		result.append(" ").append(fun.label).append("[");

		for (int i = 0; i < fun.decls.size(); ++i) {
			for (int j = 0; j < fun.decls.get(i).names.size(); ++j) {
				result.append(fun.decls.get(i).names.get(j));
				if (j < fun.decls.get(i).names.size() - 1) {
					result.append(", ");
				}
			}
			result.append(": ");
			visitThis(fun.decls.get(i).expr);
			if (i < fun.decls.size() - 1) {
				result.append(", ");
			}
		}

		result.append("]");

		if (!fun.isPred) {
			result.append(":");
			visitThis(fun.returnDecl);
		}

		result.append("{\n");
		visitThis(fun.getBody());
		result.append("\n\t}");

	}

}
