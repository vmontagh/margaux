/**
* This model is created for elaborating the mutation idea.
**/

sig Node{
	left:  set  Node,
	right: set  Node}

pred acyclic{
	all n: Node| !( n in n.^(right + left) )
}

pred distinctChildren{
	all n: Node| n.right != n.left
}

pred structuralConstraint{
	all n:Node | one n.left
	all n:Node | one n.right
}

pred lowerBoud{
	gt[#Node, 1]
}

pred genBinaryTree{
	structuralConstraint
	acyclic
	distinctChildren
	lowerBoud
}

pred allReachable{
	some n: Node| Node = n.*(left+right)
}

run genBinaryTree for 3
check {genBinaryTree implies allReachable }for 3
