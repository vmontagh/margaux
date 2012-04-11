package edu.mit.csail.sdg.alloy4compiler.ast;

/**
 * An ObjDecl is a syntax node representing the non-terminal ObjDecl.
 * 
 * Note that the accept method is part of our implementation of the visitor
 * design pattern.
 * 
 * @version 1.0 (2012-03-19)
 * @author Steven Stewart (s26stewa@uwaterloo.ca)
 * 
 */
public class ObjDecl {
	public Expr e;
	public boolean maximize;

	public ObjDecl(boolean max, Expr e) {
		this.maximize = max;
		this.e = e;
	}

	public void accept(VisitQuery<?> v) {

	}
}
