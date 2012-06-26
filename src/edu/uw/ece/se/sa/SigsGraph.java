package edu.uw.ece.se.sa;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DirectedMultigraph;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.uw.ece.se.sa.BinaryRelation.Multiplicity;

public class SigsGraph {

	final private DirectedGraph<Sig, Link> dependecyGraph = new DirectedMultigraph<Sig, Link>(Link.class);;
	
	public SigsGraph(LinkedHashMap<Sig,List<Decl>> old2fields, Map<String,Sig> sigs){
		//Extracting the sigs
		List<Sig> sigList= new ArrayList<Sig>(sigs.values());
		
		for(Sig sig:sigList){
			dependecyGraph.addVertex(sig);
		}
		
		//Extracting the relations
		for(Sig sig:sigList){
			List<Decl> relations = old2fields.get(sig);
			if(relations != null)
			for(Decl relation:relations){
				System.out.println("mult of "+ relation.expr+" is:"+relation.expr.mult);

				dependecyGraph.addEdge(sig,sigs.get( relation.expr instanceof ExprUnary?
						((ExprUnary)relation.expr).sub.toString() : relation.expr.toString() ),
						new BinaryRelation(sig, 
								sigs.get( relation.expr instanceof ExprUnary?
										((ExprUnary)relation.expr).sub.toString() : relation.expr.toString() ),
								relation.get().label, 
								Multiplicity.values()[ relation.expr.mult >=0 && relation.expr.mult < Multiplicity.values().length ? relation.expr.mult : 0]));
			}
		}
	}
	
	
	public String printGrpah(){
		return dependecyGraph.toString();
	}
	
    public static void main(String[] args) throws Err {
    	DirectedGraph<Sig, Link> graph = new DirectedMultigraph<Sig, Link>(Link.class);
    	
    	Sig A = new PrimSig("A");
    	graph.addVertex(A);
    	Sig B = new PrimSig("B");
    	graph.addVertex(B);
    	Sig C = new PrimSig("C");
    	graph.addVertex(C);
    	
    	graph.addEdge(A, B, new BinaryRelation(A, B, "r", Multiplicity.ONE));
    	graph.addEdge(A, B, new BinaryRelation(B, C, "r2", Multiplicity.SET));

    	
    	
    	System.out.println(graph);
    }
	
}
