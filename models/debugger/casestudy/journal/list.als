/**
* This model is created for elaborating the mutation idea.
**/

sig Node{
	next:  set  Node}

pred acyclic{
	all n: Node| !( n in n.^(next) )
}

pred structuralConstraint{
	all n:Node | one n.next
}

pred lowerBoud[]{
	some Node
}

pred listModel[]{
	structuralConstraint
	acyclic
	lowerBoud
}

pred allReachable[]{
	some n: Node| Node = n.*(next)
}

//run genBinaryTree for 3

run {
	(structuralConstraint and
	acyclic and
	lowerBoud) implies allReachable 
} for 3
