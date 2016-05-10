package edu.uw.ece.alloy.debugger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary.Op;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule.Open;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprHasName;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;

public class PropertyDeclaration {

	final public static String LEFT_LABEL = "left";
	final public static String MIDDLE_LABEL = "middle";
	final public static String RIGHT_LABEL = "right";
	final public static String LEFT_FIRST_LABEL = "left_first";
	final public static String LEFT_NEXT_LABEL = "left_next";
	final public static String MIDDLE_FIRST_LABEL = "middle_first";
	final public static String MIDDLE_NEXT_LABEL = "middle_next";
	final public static String RIGHT_FIRST_LABEL = "right_first";
	final public static String RIGHT_NEXT_LABEL = "right_next";

	final private static String[] labels = { LEFT_LABEL, MIDDLE_LABEL,
			RIGHT_LABEL };
	final private static String[] firstLabels = { LEFT_FIRST_LABEL,
			MIDDLE_FIRST_LABEL, RIGHT_FIRST_LABEL };
	final private static String[] nextLabels = { LEFT_NEXT_LABEL,
			MIDDLE_NEXT_LABEL, RIGHT_NEXT_LABEL };

	// Func is an immutable object, so safe to be shared.
	final public Func function;

	public PropertyDeclaration(final Func func) {
		this.function = func;

	}

	/**
	 * The predicate name is supposed to be property name
	 * 
	 * @return
	 */
	public String getPropertyName() {
		return function.label.replace("this/", "");
	}

	public int getRelationArity() {
		return findParams(this.function.decls).get(0).b;
	}

	public boolean isBinaryProperty() {
		return 2 == getRelationArity();
	}

	public boolean isTripple() {
		return 3 == getRelationArity();
	}

	private boolean hasLabel(String label) {
		for (Pair<String, Integer> param : findParams(function.decls)) {
			if (param.a.equals(label))
				return true;
		}
		return false;
	}

	public boolean hasLabel(int nThSig) {
		return hasLabel(labels[nThSig]);
	}

	public boolean hasLeft() {
		return hasLabel(0);
	}

	public boolean hasMiddle() {
		return hasLabel(1);
	}

	public boolean hasRight() {
		return hasLabel(2);
	}

	public boolean hasOrder(int nThSig) {
		return hasLabel(firstLabels[nThSig]) && hasLabel(nextLabels[nThSig]);
	}

	public boolean hasLeftOrder() {
		return hasLabel(LEFT_FIRST_LABEL) && hasLabel(LEFT_NEXT_LABEL);
	}

	public boolean hasMiddleOrder() {
		return hasLabel(MIDDLE_FIRST_LABEL) && hasLabel(MIDDLE_NEXT_LABEL);
	}

	public boolean hasRightOrder() {
		return hasLabel(RIGHT_FIRST_LABEL) && hasLabel(RIGHT_NEXT_LABEL);
	}

	public static int findArity(Expr expr) {

		// Better to check the type to make sure the expr is a set not logical
		// expression.
		if (!(expr instanceof ExprBinary))
			return 1;
		ExprBinary bExpr = (ExprBinary) expr;

		if (!bExpr.op.equals(Op.ARROW))
			return -1;

		int left = findArity(bExpr.left);
		if (left < 0)
			return -1;

		int right = findArity(bExpr.right);
		if (right < 0)
			return -1;

		return left + right;
	}

	/**
	 * Given a list of declarations, it returns the names and its arity
	 * 
	 * @param decls
	 * @return
	 */
	public static List<Pair<String, Integer>> findParams(List<Decl> decls) {
		List<Pair<String, Integer>> result = new LinkedList<Pair<String, Integer>>();

		for (Decl decl : decls) {
			int arity = findArity(decl.expr);
			for (ExprHasName name : decl.names) {
				result.add(new Pair<>(name.label, arity));
			}
		}

		return Collections.unmodifiableList(result);
	}

	/**
	 * The property has to be 1) A predicate not a function. 2) The first argument
	 * has to be the relation. 3) The relation is either binary or ternary for
	 * now. 4) The next two or three arguments are (left->middle->right), (left)
	 * 5) The next pairs are the (left_first, left_next) (middle_first,
	 * middle_next) (right_first, right_next) 6) The next two are
	 * 
	 * @return
	 */
	public boolean isAPropertyDefinition() {
		if (!this.function.isPred)
			return false;

		List<Pair<String, Integer>> params = findParams(function.decls);

		int pointer = 0;

		if (pointer < params.size()
				&& (params.get(pointer).b == 2 || params.get(pointer).b == 3)) {
			++pointer;
		} else {
			return false;
		}

		if (pointer + 2 < params.size()
				&& (params.get(pointer).a.equals(LEFT_LABEL)
						&& params.get(pointer).b.equals(1))
				&& (params.get(pointer + 1).a.equals(MIDDLE_LABEL)
						&& params.get(pointer + 1).b.equals(1))
				&& (params.get(pointer + 2).a.equals(RIGHT_LABEL)
						&& params.get(pointer + 2).b.equals(1))) {
			pointer = pointer + 3;
		} else if (pointer + 1 < params.size()
				&& (params.get(pointer).a.equals(LEFT_LABEL)
						&& params.get(pointer).b.equals(1))
				&& (params.get(pointer + 1).a.equals(MIDDLE_LABEL)
						&& params.get(pointer + 1).b.equals(1))) {
			pointer = pointer + 2;
		} else if (pointer + 1 < params.size()
				&& (params.get(pointer).a.equals(LEFT_LABEL)
						&& params.get(pointer).b.equals(1))
				&& (params.get(pointer + 1).a.equals(RIGHT_LABEL)
						&& params.get(pointer + 1).b.equals(1))) {
			pointer = pointer + 2;
		} else if (pointer + 1 < params.size()
				&& (params.get(pointer).a.equals(MIDDLE_LABEL)
						&& params.get(pointer).b.equals(1))
				&& (params.get(pointer + 1).a.equals(RIGHT_LABEL)
						&& params.get(pointer + 1).b.equals(1))) {
			pointer = pointer + 2;
		} else if (pointer < params.size()
				&& (params.get(pointer).a.equals(LEFT_LABEL)
						&& params.get(pointer).b.equals(1))) {
			pointer = pointer + 1;
		} else if (pointer < params.size()
				&& (params.get(pointer).a.equals(RIGHT_LABEL)
						&& params.get(pointer).b.equals(1))) {
			pointer = pointer + 1;
		} else if (pointer < params.size()
				&& (params.get(pointer).a.equals(MIDDLE_LABEL)
						&& params.get(pointer).b.equals(1))) {
			pointer = pointer + 1;
		} else {
			return pointer == params.size();
		}

		if (pointer < params.size()
				&& params.get(pointer).a.equals(LEFT_FIRST_LABEL)
				&& params.get(pointer).b.equals(1)) {
			if (pointer + 1 < params.size()
					&& params.get(pointer + 1).a.equals(LEFT_NEXT_LABEL)
					&& params.get(pointer + 1).b.equals(2)) {
				pointer = pointer + 2;
			} else {
				return pointer == params.size();
			}
		}

		if (pointer < params.size()
				&& params.get(pointer).a.equals(MIDDLE_FIRST_LABEL)
				&& params.get(pointer).b.equals(1)) {
			if (pointer + 1 < params.size()
					&& params.get(pointer + 1).a.equals(MIDDLE_NEXT_LABEL)
					&& params.get(pointer + 1).b.equals(2)) {
				pointer = pointer + 2;
			} else {
				return pointer == params.size();
			}
		}

		if (pointer < params.size()
				&& params.get(pointer).a.equals(RIGHT_FIRST_LABEL)
				&& params.get(pointer).b.equals(1)) {
			if (pointer + 1 < params.size()
					&& params.get(pointer + 1).a.equals(RIGHT_NEXT_LABEL)
					&& params.get(pointer + 1).b.equals(2)) {
				pointer = pointer + 2;
			} else {
				return pointer == params.size();
			}
		}

		if (pointer == params.size())
			return true;
		else
			return false;
	}

	public static String findSigInField(final Field field, final int nThSig) {
		if (nThSig >= field.type().arity()) {
			throw new IllegalArgumentException("wrong arity");
		}
		return field.type().extract(field.type().arity()).fold().get(0)
				.get(nThSig).label.replace("this/", "");
	}

	public static String findOrderingName(final Field field, final int nThSig,
			final List<Open> opens) {
		final String sigName = findSigInField(field, nThSig);
		for (Open open : opens) {
			if (open.args.size() == 1 && open.filename.equals("util/ordering")
					&& open.args.get(0).equals(sigName)) {
				return open.alias;
			}
		}
		return "";
	}

	@Override
	public String toString() {
		return "PropertyDeclaration [function=" + function + "]";
	}

	
	
}
