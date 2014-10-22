/**
 * 
 */
package edu.mit.csail.sdg.gen.visitor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4compiler.ast.Bounds;
import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
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
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.ast.VisitReturn;

/**
 * @author vajih
 *
 */
public final class MakeDOTCallGraph extends VisitReturn<Expr> {

	final Stack<Map<String,Expr>> callStack = new Stack<>(); 
	final HashSet<String> operators = new HashSet<>();
	final Stack<String> stack = new Stack<String>();
	final String namePrefix = "N";
	final String nameSep = "_";
	final StringBuilder result = new StringBuilder();


	int nameCounter = 0;


	private MakeDOTCallGraph(String init){
		stack.push(init);
	}

	static public String makeString(String init, Expr expr) throws Err{

		MakeDOTCallGraph obj = new MakeDOTCallGraph(init);

		obj.visitThis(expr);

		return obj.result.toString().replace("this/", "");
	}

	private String name(String name){
		return name+nameSep+nameCounter++;
	}

	@Override
	public Expr visit(ExprBinary x) throws Err {

		//System.out.println("ExprBinary:\t"+x);

		stack.push( make_node_connection(x.op.name() ) );

		visitThis(x.left);
		visitThis(x.right);

		stack.pop();

		return x;
	}

	@Override
	public Expr visit(ExprList x) throws Err {

		//System.out.println("ExprList:\t"+x);

		stack.push( make_node_connection(x.op.name() ) );

		for(Expr arg: x.args){
			visitThis(arg);
		}

		stack.pop();
		return x;
	}

	@Override
	public Expr visit(ExprCall x) throws Err {

		int size = callStack.size();
		System.out.println("ExprCall:\t"+x);
		System.out.println("CallStack on Entry:\t"+callStack+"\n");


		StringBuilder sb = new StringBuilder();
		ArrayList<String> decls = new ArrayList<String>();

		sb.append(x.fun.toString()).append("[");

		for(Decl decl: x.fun.decls){

			for(ExprHasName name: decl.names){
				sb.append(name.label).append(',');
				decls.add(name.label);
			}

			if(decl.names.size() > 0)
				sb.setCharAt(sb.length()-1, ':');
			else
				sb.append(':');

			sb.append(decl.expr.toString());
		}
		sb.append(']');

		callStack.push(new HashMap<String,Expr>());

		int i = 0;
		for(Expr arg: x.args){
			callStack.peek().put(decls.get(i++), arg );
		}


		String n = name( sb.toString() );

		if(!stack.empty()){
			result.append(String.format("\"%s\" -> \"%s\" [label=\"%s\"];%n", stack.peek(), n, x.args.toString()));
		}

		result.append(String.format("\"%s\"  [label=\"%s\"];%n", n, sb.toString()));

		//visitThis(x.fun.returnDecl);

		stack.push(n);

		visitThis(x.fun.getBody());

		stack.pop();
		callStack.pop();
		System.out.println("CallStack on Exit ExprCall:\t"+callStack+"\n\n");

		if(size != callStack.size()){
			System.err.println(String.format("Size was %d but changed to %d in Call", size, callStack.size()));
			System.err.flush();
			System.exit(-10);
		}

		return x;
	}

	@Override
	public Expr visit(ExprConstant x) throws Err {

		//System.out.println("ExprConstant:\t"+x);

		stack.push( make_node_connection(x.op.name() ) );

		return x;
	}

	@Override
	public Expr visit(ExprITE x) throws Err {

		stack.push( make_node_connection( "ITE" ) );

		visitThis(x.cond);

		visitThis(x.left);

		visitThis(x.right);

		stack.pop();

		return x;
	}

	@Override
	public Expr visit(ExprLet x) throws Err {

		int size = callStack.size();
		System.out.println("ExprLet:\t"+x);
		System.out.println("CallStack on Entry:\t"+callStack+"\n");

		String cs = "0->"+callStack.size()+" empty ="+callStack.empty();
		cs += "\n1->"+callStack.toString();

		boolean wasEmpty = callStack.empty();
		if(wasEmpty)
			callStack.push(new HashMap<String,Expr>());

		cs  = cs+ "\n2->" +callStack.toString();
		Expr oldExpr = callStack.peek().get(x.var.label);

		callStack.peek().put(x.var.label, x.expr);
		cs  = cs+ "\n3->" +callStack.toString();
		visitThis(x.sub);
		cs  = cs+ "\n4->" +callStack.toString();

		if(!wasEmpty)
			if(oldExpr != null)
				callStack.peek().put(x.var.label, oldExpr);
			else
				callStack.peek().remove(x.var.label);
		else
			callStack.clear();

		cs  = cs+ "\n3->" +callStack.toString();

		System.out.println("CallStack on Exit ExprLet:\t"+callStack +"\n\n");
		if(size != callStack.size()){
			System.err.println(String.format("Size was %d but changed to %d in Let and cs is %n%s", size, callStack.size(), cs));
			System.err.flush();
			System.exit(-10);
		}

		return x;
	}

	@Override
	public Expr visit(ExprQt x) throws Err {

		int size = callStack.size();

		System.out.println("ExprQt:\t"+x);
		System.out.println("CallStack on Entry:\t"+callStack+"\n");
		String oldCallStack = callStack.toString();
		stack.push( make_node_connection( x.op.name() ) );

		HashMap<String,Expr> oldNames = new HashMap<String,Expr>();

		stack.push( make_node_connection( ":" ) );

		//		if(callStack.empty())
		//			callStack.push(oldNames);

		for(Decl decl: x.decls){

			StringBuilder sb = new StringBuilder();
			for(ExprHasName name: decl.names){
				sb.append(name.label).append(",");

				if(!callStack.empty()){
					if( callStack.peek().containsKey(name.label) ){
						oldNames.put( name.label, callStack.peek().get(name.label) );
					}
					callStack.peek().remove(name.label);
				}
				//callStack.peek().put(name.label, decl.expr );
			}

			stack.push( make_node_connection( sb.substring(0, sb.length()-1) ) );

			visitThis(decl.expr);

			stack.pop();
		}

		stack.pop();

		visitThis(x.sub);

		/*for(Decl decl: x.decls){
			for(ExprHasName name: decl.names){
				callStack.peek().remove(name.label);
			}
		}*/

		if(!callStack.empty())
			callStack.peek().putAll(oldNames);

		stack.pop();

		System.out.println("CallStack on Exit ExprQt:\t"+callStack+"\n\n");
		if(size != callStack.size()){
			System.err.println(String.format("Size was %d but changed to  %d in QT %s", size, callStack.size(),oldCallStack));
			System.err.flush();
		}


		return x;
	}

	@Override
	public Expr visit(ExprUnary x) throws Err {

		/*String n = name(x.op.name());

		result.append(String.format("\"%s\" -> \"%s\";%n", stack.peek(), n));


		stack.push( n );*/
		//System.out.println("ExprUnary:\t"+x);

		if(x.op.equals(x.op.NOOP))
			visitThis(x.sub);
		else{
			stack.push( make_node_connection(x.op.name() ) );

			visitThis(x.sub);

			stack.pop();
		}
		return x;
	}

	@Override
	public Expr visit(ExprVar x) throws Err {

		//String n = name(x.label);
		//System.out.println("ExprVar:\t"+x);

		if(!callStack.empty() && callStack.peek().containsKey(x.label) && !callStack.peek().get(x.label).toString().equals(x.label) )
			visitThis( callStack.peek().get(x.label) );
		else
			make_node_connection(x.label);
		//result.append(String.format("\"%s\" -> \"%s\";%n", stack.peek(), n));

		return x;
	}

	@Override
	public Expr visit(Sig x) throws Err {

		//System.out.println("Sig:\t"+x);

		make_node_connection(x.label);

		return x;
	}

	@Override
	public Expr visit(Field x) throws Err {

		//System.out.println("Field:\t"+x);

		make_node_connection(x.label);

		return x;
	}

	@Override
	public Expr visit(Bounds bounds) throws Err {
		System.out.println("boun is called");
		return null;
	}

	private String make_node_connection(String label){
		String n = name(label);

		result.append(String.format("\"%s\" -> \"%s\";%n", stack.peek(), n));
		result.append(String.format("\"%s\"  [label=\"%s\"];%n", n, label));

		return n;
	}




}
