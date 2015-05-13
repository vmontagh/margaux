/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.analyse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.uw.ece.alloy.graph.DijkstraSP;
import edu.uw.ece.alloy.graph.DirectedEdge;
import edu.uw.ece.alloy.graph.EdgeWeightedDigraph;
import edu.uw.ece.alloy.util.Utils;
import fj.data.Stream;

/**
 * This graph condenses the graph representing the implication relations. 
 * @author vajih
 *
 */
public class GraphCondenser {

	final private int 	 nodeCount = 181;
	final private String implicationCSVPath = "/Users/vajih/Documents/Papers/papers/debugger/reviews/data/relational_analyzer/csv/implies.csv";
	/*an item in the array is either -1, itself, or other value.
	 * -1 means the node is deleted. 
	 * itself means it is not replaced by other node
	 * other value means the node has been replaced by other node. e.g if a<=>B then b is replaced by a
	 */
	final private int    replacedNode[] = new int[nodeCount];
	final private Set<Integer>   sparceMatrixImplication[] = new Set[nodeCount];

	final private String inconsistentCSVPath = "/Users/vajih/Documents/Papers/papers/debugger/reviews/data/relational_analyzer/csv/incons.csv";
	final private Set<Integer>   sparceMatrixInconsistency[] = new Set[nodeCount];

	final private String vaccucityCSVPath = "/Users/vajih/Documents/Papers/papers/debugger/reviews/data/relational_analyzer/csv/vac.csv";
	final private Set<Integer> vacuities = new HashSet<Integer>();

	final private String legendsCSVPath = "/Users/vajih/Documents/Papers/papers/debugger/reviews/data/relational_analyzer/csv/legends.csv";
	final private String legends[] = new String[nodeCount];

	final private String dotOutput = "/Users/vajih/Documents/Papers/papers/debugger/reviews/data/relational_analyzer/dots/%s.java.out.dot";


	/*
	 * Each index in implicationClasses contains a list of other nodes having iff to it. 
	 * For example, implicationClasses[0]={1,2} means 0<=>1, 0<=>2, and 1<=>2
	 * It is expected to see  implicationClasses[1]={0,2} and implicationClasses[2]={1,0}
	 */
	final private TreeSet<Integer>    implicationClasses[] = new TreeSet[nodeCount];
	/*
	 * After processing implicationClasses, the iff-class reps are stored in confirmingReplacedNode
	 * which has to be equal to replacedNode. 
	 */
	final private int confirmingReplacedNode[] = new int[nodeCount];
	final private String iffCSVPath = "/Users/vajih/Documents/Papers/papers/debugger/reviews/data/relational_analyzer/csv/iff.csv";


	/**
	 * 
	 */
	public GraphCondenser() {
		Stream.range(0, nodeCount).forEach(
				i->{
					replacedNode[i] = i;
					sparceMatrixImplication[i]   = new ConcurrentSkipListSet<Integer>();
					sparceMatrixInconsistency[i] = new ConcurrentSkipListSet<Integer>();
					implicationClasses[i] = new TreeSet<>();
				});

		loadData();

	}



	private void loadData(){

		Utils.readFile(new File(vaccucityCSVPath), new Consumer<List<String>>() {
			@Override
			public void accept(List<String> t) {
				assert t.size() == 2;
				assert t.get(0) == t.get(1);

				vacuities.add(Integer.valueOf(t.get(0)));
			}} );

		Utils.readFile(new File(implicationCSVPath), new Consumer<List<String>>() {
			@Override
			public void accept(List<String> t) {
				assert t.size() == 2;

				final int from = Integer.valueOf(t.get(0));
				final int to   = Integer.valueOf(t.get(1));
				//System.out.println(from+"->"+to);
				sparceMatrixImplication[from].add(to);		
			}} );

		Utils.readFile(new File(legendsCSVPath), new Consumer<List<String>>() {
			@Override
			public void accept(List<String> t) {
				assert t.size() == 2;

				final int id = Integer.valueOf(t.get(0));
				final String name   = t.get(1);
				//System.out.println(from+"->"+to);
				legends[id] = name; 		
			}} );

		Utils.readFile(new File(iffCSVPath), new Consumer<List<String>>() {
			@Override
			public void accept(List<String> t) {
				assert t.size() == 2;

				final int from = Integer.valueOf(t.get(0));
				final int to   = Integer.valueOf(t.get(1));
				implicationClasses[from].add(to);
				//insert the node as well. It makes the comparison and
				//rep slection easier.
				implicationClasses[from].add(from);
			}} );

		Utils.readFile(new File(inconsistentCSVPath), new Consumer<List<String>>() {
			@Override
			public void accept(List<String> t) {
				assert t.size() == 2;

				final int from = Integer.valueOf(t.get(0));
				final int to   = Integer.valueOf(t.get(1));
				sparceMatrixInconsistency[from].add(to);
			}} );


	}

	public final String makeHTMLLegends(){
		StringBuilder result = new StringBuilder();

		result.append("{ rank = sink;\nLegend [shape=none, margin=0, label=<").append("\n");
		result.append("\t").append("<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"4\">").append("\n");
		result.append("\t\t").append("<TR><TD COLSPAN=\"2\"><B>Legend</B></TD></TR>").append("\n");

		Stream.range(1, nodeCount).forEach(

				i->{
					String key = ""+i;
					String value = legends[i];
					//System.out.println(value);
					String fontStart = "";
					String fontEnd = "";
					if(vacuities.contains(i)){
						fontStart = "<FONT COLOR=\"red\">";
						fontEnd = "</FONT>";
					}else 
						if(replacedNode[i] != i){
							key = key+"&rarr;"+replacedNode[i];
							fontStart = "<FONT COLOR=\"green\">";
							fontEnd = "</FONT>";
						}
					result.append("\t\t").append("<TR><TD>").append(fontStart).append(key).append(fontEnd).append("</TD>");
					result.append("<TD>").append(fontStart).append(value).append(fontEnd).append("</TD></TR>").append("\n");					
				}

				);
		result.append("\t").append("</TABLE>>];}").append("\n");
		return result.toString();
	}

	public final void process(){
		//The vacuities have to be removed
		vacuities.stream().forEach(i->replacedNode[i] = -1);

		/* traversing the nodes from 0 to nodeCount
		 * and find loop backs, i.e. a->b and b->a.
		 * In this case b is removed and all its appearances
		 * are replaces by a. replacedNode[b] = a.
		 */
		Stream.range(1, nodeCount).forEach(
				from->{
					if(replacedNode[from] == -1){
						sparceMatrixImplication[from].clear();;
					}else  {
						sparceMatrixImplication[from].stream().forEach(
								to->{
									sparceMatrixImplication[from].remove(to);
									//to->from and from->to
									if( sparceMatrixImplication[to].contains(from)){
										replacedNode[to] = from;
										sparceMatrixImplication[to].remove(from);
										sparceMatrixImplication[from].addAll(sparceMatrixImplication[to]);
										sparceMatrixImplication[to].clear();
									}else if(replacedNode[to] != -1){
										sparceMatrixImplication[from].add(replacedNode[to]);
									}
								}
								);
					}
				});
	}


	/**
	 * Given the analyzed iff statements, stored in iffCSVPath, the reps-classes computed in
	 * confirmingReplacedNode and replacedNode have to be equal.  
	 */
	public boolean confirmProcessValidity(){

		//Check the transitivity of the implication relations
		for(int i = 1; i < implicationClasses.length; ++i){

			Set<Integer> iffSet = implicationClasses[i];

			for(Integer j: iffSet){
				if( ! implicationClasses[j].containsAll(iffSet) ){

					System.err.printf("Invalid total order state %s-%s: \n\t%s \n\t%s%n", i,j, iffSet, implicationClasses[j]);

					implicationClasses[j].removeAll(iffSet);
					System.out.println(implicationClasses[j]);
					return false;
				}
			}
		}

		return true;
		//System.out.println(implicationClasses[1]);
	}

	public final void condese(){

		
		
		//TODO
		if(!confirmProcessValidity()) throw new RuntimeException();
		for(int node = 1; node < implicationClasses.length; ++node){
			if(vacuities.contains(node)){
				replacedNode[node] = -1;
			}else{
				replacedNode[node] = implicationClasses[node].isEmpty() ? node : implicationClasses[node].first();
			}
		}

		for(int i = 1; i < replacedNode.length; ++i){
			//System.out.print(i+"->"+replacedNode[i]+", ");
		}

	}

	/**
	 * Precondition: replacedNode contains the information about vacuities, i.e. nodes are vacuite are -1.
	 * Node that are grouped have the rep. i.e. in a group {1,2,3}, {1} is the rep, and the replacedNode
	 * would be [1,1,1].
	 * 
	 * Postcondition: all condensed nodes and vacuite are removed from the the matrix.
	 * The output is meaningful by both replacedNode and sparceMatrix.
	 */
	public final void condeseByGrouping(Set<Integer>  sparceMatrix[]){

		condese();

		//System.out.println();

		for(int from = 1; from < sparceMatrix.length; ++from){


			//System.out.println("BEFORE> "+from+"->"+sparceMatrix[from]);
			//removing the vacuite nodes
			if( replacedNode[from] == -1 ){
				sparceMatrix[from].clear();
				continue;
			}
			//redundant nodes
			if( replacedNode[from] != from ){
				sparceMatrix[from].clear();
				continue;
			}

			if( replacedNode[from] == from ){
				for(int to: sparceMatrix[from]){
					if(replacedNode[to] == -1 ){
						sparceMatrix[from].remove(to);
						continue;
					}
					sparceMatrix[from].remove(to);
					sparceMatrix[from].add(replacedNode[to]);

				}
				sparceMatrix[from].remove(from);
			}



			//System.out.println("AFTER> "+from+"->"+sparceMatrix[from]);

		}

		//System.out.println();
		for(int i = 1; i < sparceMatrix.length; ++i){
			//System.out.println(i+"->"+sparceMatrix[i]);
		}

	}

	public final String generateEdges(final String edgeAttribute, final Set<Integer>   sparceMatrix[]){
		final StringBuilder dot = new StringBuilder();
		IntStream.range(1, nodeCount).filter(i->replacedNode[i]==i).forEach(i-> sparceMatrix[i].stream().forEach(j->dot.append("\t").append(i).append("->").append(j).append(edgeAttribute).append(";\n")));
		return (dot.toString());
	}


	public final String generateDot(final String legend, final String edges){

		//IntStream.range(1, nodeCount).filter(i->replacedNode[i]==i).forEach(i->System.out.printf("%s-> \t%s%n", i, sparceMatrix[i]));
		final StringBuilder dot = new StringBuilder();
		dot.append("digraph lattice {\n");
		dot.append(edges);
		dot.append(legend);
		dot.append("}");

		return (dot.toString());

	}

	public final String generateDotImplication(){
		condeseByGrouping(sparceMatrixImplication);
		return generateDot(makeHTMLLegends(), generateEdges("", sparceMatrixImplication));
	}

	public final String generateDotInconsistencies(){


		System.out.println(Arrays.asList(sparceMatrixInconsistency) );
		condeseByGrouping(sparceMatrixInconsistency);
		for(int i = 1; i < sparceMatrixInconsistency.length; ++i){
			System.out.println(i+"->"+sparceMatrixInconsistency[i]);
		}
		System.out.println(Arrays.asList(sparceMatrixInconsistency) );
		return generateDot(makeHTMLLegends(), generateEdges(" [dir=none]", sparceMatrixInconsistency));
	}

	public final String generateDotImplicationPlusInconsistencies(){
		condeseByGrouping(sparceMatrixImplication);
		condeseByGrouping(sparceMatrixInconsistency);
		return generateDot(/*makeHTMLLegends()*/"{ rank = sink;\nLegend [shape=none, margin=0, label=<Blue line: Implications\nRed Lines: Inconsistencies\nBlack Line: Nodes circles>]}",  generateEdges("[dir=none color=\"red\"]", sparceMatrixInconsistency) + generateEdges(" [color=\"blue\"]", sparceMatrixImplication) );
	}





	public final void implicationIsAcyclic(){
		condeseByGrouping(sparceMatrixImplication);

		
		
		
		
		
		System.out.println(isCyclic(sparceMatrixImplication));
		
		System.out.println(  findSources(sparceMatrixImplication)  );
		System.out.println( decodeToLegends( findSources(sparceMatrixImplication) ) );
		
		System.out.println(  findSinks(sparceMatrixImplication)  );
		System.out.println( decodeToLegends( findSinks(sparceMatrixImplication) ) );
		
		findLongestPath(sparceMatrixImplication);

		System.out.println("Live nodes: "+findAllLiveNodes().size());
		
		int node = findNodeWithMostOutgoingEdges(sparceMatrixImplication);
		System.out.printf("Max outgoing:%s (%s) %n",legends[node],  node );
		
		node = findNodeWithMostincomeEdges(sparceMatrixImplication);
		System.out.printf("Max Incoming:%s (%s) %n",legends[node],  node );
		
		
		
		
	}



	/**
	 * Sources are the nodes does not have any input edge in sparceMatrix
	 * @return
	 */
	public List<Integer> findSources(final Set<Integer>   sparceMatrix[]){

		boolean notHasInput[] = new boolean[sparceMatrix.length];
		Arrays.fill(notHasInput, true);

		for(int from = 1; from < sparceMatrix.length; ++from){
			if(vacuities.contains(from)){
				notHasInput[from] = false;
				continue;
			}
			if(replacedNode[from] != from){ 
				notHasInput[from] = false;
				continue;
			}
			Set<Integer> Tos = sparceMatrix[from];
			for(int to: Tos){
				notHasInput[to] = false;
			}
		}



		List<Integer> result = new LinkedList<>();

		Stream.range(1, nodeCount).forEach(
				i->{
					//System.out.println(i+"->"+notHasInput[i]);
					if(notHasInput[i]) result.add(i);
				}
				);

		return Collections.unmodifiableList(result);

	}

	public List<String> decodeToLegends(final List<Integer> nodes){
		return nodes.stream().map(node->legends[node]).collect(Collectors.toList());
	}

	public List<Integer> findSinks(final Set<Integer>   sparceMatrix[]){

		boolean notHasOutput[] = new boolean[sparceMatrix.length];
		Arrays.fill(notHasOutput, true);

		for(int from = 1; from < sparceMatrix.length; ++from){
			if(vacuities.contains(from)){
				notHasOutput[from] = false;
				continue;
			}
			if(replacedNode[from] != from){ 
				notHasOutput[from] = false;
				continue;
			}
			Set<Integer> Tos = sparceMatrix[from];
			if(!Tos.isEmpty())
				notHasOutput[from] = false;
		}

		List<Integer> result = new LinkedList<>();

		Stream.range(1, nodeCount).forEach(
				i->{
					if(notHasOutput[i]) result.add(i);
				}
				);

		return Collections.unmodifiableList(result);

	}


	public boolean isCyclic(final Set<Integer>   sparceMatrix[]){
		boolean visited[] = new boolean[sparceMatrix.length];
		Arrays.fill(visited, false);

		Stack<Integer> stack = new Stack<>();

		for(int source: findSources(sparceMatrix)){
			stack.push(source);
		}

		while(!stack.isEmpty()){
			Integer from = stack.pop();
			if ( visited[from] ) {
				return false;
			}
			for(Integer to: sparceMatrix[from]){
				stack.push(to);
			}

		}

		return true;
	}

	private List<Integer> findAllLiveNodes(){
		List<Integer> list = new ArrayList<>();
		for(int node = 1; node < replacedNode.length; ++node){
			if(replacedNode[node] == node)
				list.add(node);
		}
		return Collections.unmodifiableList(list);
	}

	public int findLongestPath(final Set<Integer>   sparceMatrix[]){

		if( !isCyclic(sparceMatrix) ) return -1;

		HashMap<Integer, Integer> coder = new HashMap<>();
		HashMap<Integer, Integer> decoder = new HashMap<>();

		int node = 0;
		for(int vNode: findAllLiveNodes()){
			coder.put(vNode, node);
			decoder.put(node, vNode);
			node++;
		}

		EdgeWeightedDigraph graph = new EdgeWeightedDigraph(node);

		for(int from: findAllLiveNodes()){
			for(int to: sparceMatrix[from]){
				graph.addEdge(new DirectedEdge(coder.get(from), coder.get(to), -1));
			}
		}

		for(int source: findSources(sparceMatrix)){
			for( int sink: findSinks(sparceMatrix) )
			System.out.printf("%s \t\t %s(%s) ~> %s(%s)\n",  (new DijkstraSP(graph, coder.get(source))).distTo(coder.get(sink)) , legends[source], source,legends[sink], sink);
		}

		//while()

		return 0;
	}

	//Outdegree
	public int findNodeWithMostOutgoingEdges(final Set<Integer>   sparceMatrix[]){
		int maxOutgoingIndex = 0;
		int maxOutgoingValue = Integer.MIN_VALUE;
		for(int node: findAllLiveNodes()){
			if(maxOutgoingValue < sparceMatrix[node].size()){
				maxOutgoingIndex = node;
				maxOutgoingValue = sparceMatrix[maxOutgoingIndex].size();
			}
		}
		System.out.println("Max Outgoing edges: "+maxOutgoingValue);
		return maxOutgoingIndex;
	}
	
	//Indegree
	public int findNodeWithMostincomeEdges(final Set<Integer>   sparceMatrix[]){
		int incomes[] = new int[nodeCount];
		Arrays.fill(incomes, 0);
		
		for(int from: findAllLiveNodes()){
			for(int to: sparceMatrix[from]){
				incomes[to]++;
			}
		}
		
		int maxIncomeIndex = 0;
		int maxIncomeValue = Integer.MIN_VALUE;
		for(int node = 1; node < incomes.length; ++node){
			if(maxIncomeValue< incomes[node]){
				maxIncomeValue = incomes[node];
				maxIncomeIndex = node;
			}
		}
		System.out.println("Max Income edges: "+maxIncomeValue);
		return maxIncomeIndex;
	}
	
	/**
	 * @param args
	 * @throws Err 
	 */
	public static void main(String[] args) throws Err {

		GraphCondenser gc = new GraphCondenser();
		gc.confirmProcessValidity();
		//gc.condese();
		Util.writeAll(String.format(gc.dotOutput, "incosnt"), gc.generateDotInconsistencies());
		Util.writeAll(String.format(gc.dotOutput, "imply"),gc.generateDotImplication());
		Util.writeAll(String.format(gc.dotOutput, "inconst.imply"), gc.generateDotImplicationPlusInconsistencies());

		gc.implicationIsAcyclic();
		
		System.out.println();


	}

}
