/**
* This model is created for elaborating the mutation idea.
**/

sig Node{
	nxt:  set  Node}

pred acyclic{
	all n: Node| !( n in n.^(nxt) )
}

pred structuralConstraint{
	all n:Node | one n.nxt
}

pred lowerBoud[]{
//	some nxt //Node
}

pred listModel[]{
	structuralConstraint
	acyclic
	lowerBoud
}

pred allReachable[]{
	lone n: Node| Node = n.*(nxt)
}

//run genBinaryTree for 3

run
{
	(structuralConstraint and
	acyclic and
	lowerBoud) implies allReachable 
} for 3
