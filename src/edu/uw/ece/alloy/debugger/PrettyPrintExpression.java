package edu.uw.ece.alloy.debugger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

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
import edu.mit.csail.sdg.alloy4compiler.ast.VisitReturn;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;

public class PrettyPrintExpression  extends VisitReturn<Expr>  {


	final protected StringBuilder result = new StringBuilder();


	int nameCounter = 0;


	protected PrettyPrintExpression(){
	}

	static public String makeString(Expr expr) throws Err{

		PrettyPrintExpression obj = new PrettyPrintExpression();

		obj.visitThis(expr);

		return obj.result.toString().replace("this/", "");
	}


	@Override
	public Expr visit(ExprBinary x) throws Err {

		//System.out.println("ExprBinary:\t"+x);

		//stack.push( make_node_connection(x.op.name() ) );
		
		//Parenthesis are for priority
		result.append(" ( ");
		
		visitThis(x.left);
		
		result.append("  ").append(x.op).append("  ");
		
		visitThis(x.right);

		result.append(" ) ");
		//stack.pop();

		return x;
	}

	@Override
	public Expr visit(ExprList x) throws Err {

		//System.out.println("ExprList:\t"+x);

		result.append("(");

		for ( int i = 0; i < x.args.size() ; ++i){
			result.append(" (");
			visitThis(x.args.get(i));
			result.append(" )");
			if(i < x.args.size() - 1)
				result.append(" ").append(x.op.name().toLowerCase());
		}		
		
		result.append(" )");
		return x;
	}

	@Override
	public Expr visit(ExprCall x) throws Err {

		result.append(" ").append(x.fun.label).append("[");
		
		for(int i = 0; i < x.args.size(); ++i){
			visitThis( x.args.get(i) );
			if( i < x.args.size() - 1 ){
				result.append(", ");
			}
		}
		
		result.append(" ]");
		return x;
	}

	@Override
	public Expr visit(ExprConstant x) throws Err {

		//System.out.println("ExprConstant:\t"+x);
		
		//stack.push( make_node_connection(x.op.name() ) );
		result.append(" ").append(x);

		return x;
	}

	@Override
	public Expr visit(ExprITE x) throws Err {

		result.append(" ( ");
		
		result.append(" ( ");
		visitThis(x.cond);
		result.append(" ) ");
		
		result.append(" ").append("=>");

		result.append(" ( ");
		visitThis(x.left);
		result.append(" ) ");
		
		result.append(" ").append("else ");
		
		result.append(" ( ");
		visitThis(x.right);
		result.append(" ) ");
		
		result.append(" ) ");
		return x;
	}

	@Override
	public Expr visit(ExprLet x) throws Err {

		result.append("let ");
		visitThis(x.var);
		
		result.append("= ");
		visitThis(x.expr);
		
		result.append("| ");
		visitThis(x.sub);

		return x;
	}
	
	
	public static void main(String ... args){
		int[] ints = {1,2,3,4};
		
		for(int i = 0; i < ints.length; ++i){
			System.out.print(i);
			if( i < ints.length - 1)
				System.out.print(",");
		}
	}

	@Override
	public Expr visit(ExprQt x) throws Err {

		if(x.op.equals(ExprQt.Op.COMPREHENSION))
			result.append("{");
		else
			result.append( x.op );
		
		for(int j =0; j < x.decls.size(); ++j){
			Decl decl = x.decls.get(j);
			for(int i = 0; i < decl.names.size(); ++i){
				visitThis(decl.names.get(i));
				if( i < decl.names.size() - 1)
					result.append(", ");
			}
			
			result.append(":");
			visitThis(decl.expr);
			if( j < x.decls.size() - 1)
				result.append(", ");
		}
		
		result.append("| ");
		visitThis(x.sub);

		if(x.op.equals(ExprQt.Op.COMPREHENSION))
			result.append("}");
		
		return x;
	}

	@Override
	public Expr visit(ExprUnary x) throws Err {

		/*String n = name(x.op.name());

		result.append(String.format("\"%s\" -> \"%s\";%n", stack.peek(), n));


		stack.push( n );*/
		//System.out.println("ExprUnary:\t"+x);
		
		if( !Arrays.asList(ExprUnary.Op.NOOP, ExprUnary.Op.LONEOF, ExprUnary.Op.SOMEOF,
				ExprUnary.Op.ONEOF, ExprUnary.Op.SETOF, ExprUnary.Op.EXACTLYOF).contains(x.op) ){
			result.append(" ").append(x.op);

			result.append("(");
			visitThis(x.sub);
			result.append(")");

		}else{
			visitThis(x.sub);
		}
		
		return x;
	}

	@Override
	public Expr visit(ExprVar x) throws Err {

		result.append(" ").append(x.label);
		return x;
	}

	@Override
	public Expr visit(Sig x) throws Err {

		result.append(" ").append(x.label);
		return x;
	}

	@Override
	public Expr visit(Field x) throws Err {

		result.append(" (");
		
		visitThis(x.sig);
		
		result.append(ExprBinary.Op.DOMAIN).append(x.label);
		
		result.append(") ");
		return x;
	}

	@Override
	public Expr visit(Bounds bounds) throws Err {
		throw new RuntimeException("Forbiden Call to Bounds");
	}

	
}
