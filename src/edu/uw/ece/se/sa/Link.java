package edu.uw.ece.se.sa;

import org.jgrapht.graph.DefaultEdge;

import edu.mit.csail.sdg.alloy4compiler.ast.Sig;

public abstract class Link extends DefaultEdge{
	final private Sig left,right;
	final private String label;
	protected enum ARROW {normal,none,empty,vee};
	
	public Link(Sig left, Sig right, String label) {
		this.left = left;
		this.right = right;
		this.label = label;
	}

	public Sig getLeft() {
		return left;
	}

	public Sig getRight() {
		return right;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return "Link [left=" + left + ", right=" + right + ", label=" + label
				+ "]";
	}
	
	public abstract String arrow(); 
	
}
