package edu.uw.ece.se.sa;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.DirectedGraph;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.TraversalListenerAdapter;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.SubsetSig;
import edu.uw.ece.se.sa.BinaryRelation.Multiplicity;

public class SigsGraph {

	final private DirectedGraph<Sig, Link> dependecyGraph = new DirectedMultigraph<Sig, Link>(Link.class);;

	private final void checkBuiltInSig(String label,Map<String,Sig> sigs){
		if(sigs.containsKey(label) && !dependecyGraph.containsVertex(sigs.get(label))){
			dependecyGraph.addVertex(sigs.get(label));
		}
	}

	private final void extractRelations(Sig sig,Map<String,Sig> sigs,Expr rel,String name){
		if(rel instanceof ExprVar){
			String sigName = ((ExprVar)rel).label;
			checkBuiltInSig(sigName,sigs);
			dependecyGraph.addEdge(sig, 
					sigs.get(sigName),
					new BinaryRelation(
							sig,
							sigs.get(sigName),
							name,
							Multiplicity.values()[((ExprVar)rel).mult])
					);
		}else if(rel instanceof ExprBinary){
			extractRelations( sig,sigs, ((ExprBinary)rel).left,name);
			extractRelations( sig,sigs, ((ExprBinary)rel).right,name);
		}else if(rel instanceof ExprUnary){
			extractRelations( sig,sigs, ((ExprUnary)rel).sub,name);

		}

		/*dependecyGraph.addEdge(sig,sigs.get( relation.expr instanceof ExprUnary?
				((ExprUnary)relation.expr).sub.toString() : relation.expr.toString() ),
				new BinaryRelation(sig, 
						sigs.get( relation.expr instanceof ExprUnary?
								((ExprUnary)relation.expr).sub.toString() : relation.expr.toString() ),
						relation.get().label, 
						Multiplicity.values()[ relation.expr.mult >=0 && relation.expr.mult < Multiplicity.values().length ? relation.expr.mult : 0]));
		 */
	}

	private Map<String,Sig> insertBuiltIns(Map<String,Sig> sigs){
		sigs.put(Sig.UNIV.label, Sig.UNIV);
		sigs.put(Sig.SIGINT.label, Sig.SIGINT);
		sigs.put(Sig.SEQIDX.label, Sig.SEQIDX);
		sigs.put(Sig.STRING.label, Sig.STRING);
		sigs.put(Sig.NONE.label, Sig.NONE);
		return sigs;
	}

	public SigsGraph(LinkedHashMap<Sig,List<Decl>> old2fields, Map<String,Sig> sigs){
		//Extracting the sigs
		List<Sig> sigList= new ArrayList<Sig>(sigs.values());

		for(Sig sig:sigList){			
			dependecyGraph.addVertex(sig);
		}

		sigs = insertBuiltIns(sigs);

		for(Sig sig:sigList){
			//Extracting the relations
			List<Decl> relations = old2fields.get(sig);
			if(relations != null)
				for(Decl relation:relations){
					extractRelations(sig, sigs, relation.expr, relation.get().label);
				}
			//Extracting subsig
			if(	(sig.isSubsig != null) && 
					(sig instanceof PrimSig) &&
					!((PrimSig) sig).parent.builtin){
				Sig parent = sigs.get(((PrimSig) sig).parent.label);//findSig( ((PrimSig) sig).parent);
				dependecyGraph.addEdge(sig, parent, 
						new Extension(sig, parent));
			}


			//Extracting subset
			if(	(sig.isSubset != null) && 
					(sig instanceof SubsetSig) ){
				for(Sig parent:((SubsetSig) sig).parents){
					parent = sigs.get(parent.label);//findSig(parent);
					if(((SubsetSig) sig).exact){
						dependecyGraph.addEdge(sig, parent, 
								new EqualSig(sig, parent));
					}else{
						dependecyGraph.addEdge(sig, parent, 
								new Subset(sig, parent));
					}
				}
			}
		}
	}

	private final Sig findSig(Sig sig){
		for(Sig iSig: dependecyGraph.vertexSet()){
			if(iSig.label.replaceAll("this/", "").compareTo(sig.label.replaceAll("this/","")) == 0){
				return iSig;
			}
		}
		return null;
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


	public void testfindAllReachable(){
		for(Sig sig:dependecyGraph.vertexSet()){
			System.out.println("All reachable sigs from "+sig+" are: "+findAllReachable(sig));
		}
	}

	public void testfindAllReachableRefined(){
		for(Sig sig:dependecyGraph.vertexSet()){
			Set<Sig> sigs = findAllReachable(sig);
			System.out.println("All reachable sigs from "+sig+" are: "+
					refineExtension(refineRemoveItself(sigs,sig),sig));
		}
	}

	public void categorizeSigs(){
		final Map<Sig,Set<Sig>> reachSig = new HashMap<Sig, Set<Sig>>();
		final List<Set<Sig>> categorized = new ArrayList<Set<Sig>>();
		//Removing the  self loop.
		for(Sig sig:dependecyGraph.vertexSet()){
			Set<Sig> sigs = refineRemoveItself(findAllReachable(sig),sig);
			reachSig.put(sig, sigs);
		}
		System.out.println(reachSig);
		int i = 0;
		while(reachSig.size()>0){
			//Removing the super-sig from reachable sigs
			for(Sig sig:reachSig.keySet()){
				reachSig.put(sig, refineExtension(reachSig.get(sig),sig) );
			}
			//All Sig with empty Source are put in meatedlist. 
			List<Sig> list  = new ArrayList<Sig>(reachSig.keySet());
			final Set<Sig> meated = new HashSet<Sig>();
			for(Sig sig:list){
				if(reachSig.get(sig).size() == 0){
					meated.add(sig);
					reachSig.remove(sig);
				}
			}
			
			if(meated.size() == 0)
				break;

			categorized.add( new HashSet<Sig>(meated));
			
			list  = new ArrayList<Sig>(reachSig.keySet());
			for(Sig sig: list){
				reachSig.put(sig, refineMeated(reachSig.get(sig), meated));
			}
			i++;
		}
		System.out.println(categorized);
		
	}

	/**
	 * It removes the sig, from the reachable nodes. 
	 * So, if there was a cycle, we will miss that information
	 * @param sigs
	 * @param sig
	 * @return
	 */
	private final Set<Sig> refineRemoveItself(Set<Sig> sigs, Sig sig){
		sigs.remove(sig);
		return sigs;
	}

	private final Set<Sig> refineExtension(Set<Sig> sigs, Sig sig){
		List<Sig> list =  new ArrayList<Sig>(sigs);
		for(Sig to: list){
			if(dependecyGraph.containsEdge(sig, to) &&
					dependecyGraph.getEdge(sig, to) instanceof Subtype){
				sigs.remove(to);
			}
		}
		return sigs;
	}

	private final Set<Sig> refineMeated(Set<Sig> reached,Set<Sig> tbRemoved){
		List<Sig> list = new ArrayList<Sig>(reached);
		for(Sig to:list){
			if(tbRemoved.contains(to)){
				reached.remove(to);
			}
		}
		return reached;
	}

	public Set<Sig> findAllReachable(Sig sig){
		BreadthFirstIterator<Sig, Link> bfs = new BreadthFirstIterator<Sig, Link>(dependecyGraph,sig);
		Set<Sig> rchblSigs = new HashSet<Sig>();
		while( bfs.hasNext()){
			rchblSigs.add(bfs.next());
		}
		return rchblSigs;
	}

	public String toDot(){
		StringBuffer dot = new StringBuffer();
		for(Link link:dependecyGraph.edgeSet()){
			dot.append("\""+link.getLeft().label+"\"").append("->")
			.append("\""+link.getRight().label+"\"").append("[label=\"")
			.append(link.getLabel()).append("\",arrowhead=\"")
			.append(link.arrow()).append("\"];\n");
		}
		return dot.insert(0, "digraph g {\nrankdir = LR;\nforcelabels=false;\n").append("}").toString();
	}

	public void dotToFile(String path){
		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(path));
			bufferedWriter.write(toDot());
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			//Close the BufferedWriter
			try {
				if (bufferedWriter != null) {
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}


}
