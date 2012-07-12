package edu.mit.csail.sdg.alloy4compiler.ast;

import java.util.ArrayList;

import edu.mit.csail.sdg.alloy4.Err;

/**
 * An ObjBlock is an syntax node representing the top-most non-terminal in the
 * part of the Alloy4 grammar that describes an Alloy4 objectives block. Each
 * block will have a name and a set of objective declarations (ObjDecls).
 * 
 * Note that the accept method is part of our implementation of the visitor
 * design pattern.
 * 
 * @version 1.0 (2012-03-19)
 * @author Steven Stewart (s26stewa@uwaterloo.ca)
 * 
 */
public class ObjBlock {
	public String name; // the name of the objectives block
	public ArrayList<ObjDecl> decls;

	public ObjBlock(String name, ArrayList<ObjDecl> d) {
		this.name = name;
		this.decls = d;
	}

	final <T> T accept(VisitReturn<T> visitor) throws Err {
		return visitor.visit(this);
	}
}
