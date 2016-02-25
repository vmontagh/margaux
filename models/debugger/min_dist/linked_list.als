sig Node {next: lone Node}

pred isAcyclic[nodes: set Node, next1: Node -> Node] {
	all node : nodes | node not in node.^next1
}

pred singleParent[nodes: set Node, next1: Node -> Node] {
	// all n1, n2 : nodes | n1 != n2 => n1.next1 != n2.next1
	all disj n1, n2 : nodes | n1.next1 != n2.next1
}

pred includeInstance[nodes: set Node, next1: Node -> Node] {
	Node.next1 in nodes
	next1.Node in nodes
}

pred structuralConstraint[nodes: set Node, next1: Node -> Node] {
	all node : nodes | lone node.next1
}

pred isInstance[nodes: set Node, next1: Node -> Node] {
	includeInstance[nodes, next1]
	structuralConstraint[nodes, next1]
}

pred isListInstance[nodes: set Node, next1: Node -> Node] {
	isInstance[nodes, next1]
	isAcyclic[nodes, next1]
	singleParent[nodes, next1]
}

/**
* delta = |List1 - List2|
*
* List1 = List2 + delta OR
* List2 = List1 + delta
*/
pred isDiffInstance[n1: set Node, next1: Node -> Node, 
				n2: set Node, next2: Node -> Node,
				diffNodes: set Node, diffNext: Node -> Node] {
	
	isInstance[n1, next1]
	isInstance[n2, next2]

	n1 != n2 implies ((n1 = n2 + diffNodes) or (n2 = n1 + diffNodes)) else no diffNodes
	next1 != next2 implies ((next1 = next2 + diffNext) or (next2 = next1 + diffNext)) else no diffNext
	
}

run {
	isListInstance[Node, next]
} for 10
