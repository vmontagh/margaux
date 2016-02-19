module binary_implication

open property_structure as ps

abstract sig bin_prop extends prop{}

one sig  bijection  extends bin_prop{} 
one sig  complete  extends bin_prop{} 
one sig  preorder  extends bin_prop{} 
one sig  equivalence  extends bin_prop{} 
one sig  partialOrder  extends bin_prop{} 
one sig  totalOrder  extends bin_prop{} 
one sig  rootedAll  extends bin_prop{} 
one sig  rootedOne  extends bin_prop{} 
one sig  stronglyConnected  extends bin_prop{} 
one sig  function  extends bin_prop{} 
one sig  reflexive  extends bin_prop{} 
one sig  acyclic  extends bin_prop{} 
one sig  empty  extends bin_prop{} 
one sig  functional  extends bin_prop{} 
one sig  total  extends bin_prop{} 
one sig  weaklyConnected  extends bin_prop{} 
one sig  transitive  extends bin_prop{} 
one sig  symmetric  extends bin_prop{} 
one sig  antisymmetric  extends bin_prop{} 
one sig  irreflexive  extends bin_prop{} 


fact implication{
	functional + function + total = bijection.imply
	weaklyConnected = complete.imply
	reflexive + transitive + total = preorder.imply
	reflexive + transitive + preorder + symmetric + total = equivalence.imply
	reflexive + transitive + preorder + antisymmetric + total = partialOrder.imply
	partialOrder + weaklyConnected + reflexive + rootedOne + transitive + complete + preorder + antisymmetric + total = totalOrder.imply
	weaklyConnected + stronglyConnected + total = rootedAll.imply
	weaklyConnected = rootedOne.imply
	weaklyConnected + total + rootedAll = stronglyConnected.imply
	functional + total = function.imply
	total = reflexive.imply
	irreflexive + antisymmetric = acyclic.imply
	acyclic + transitive + irreflexive + functional + antisymmetric + symmetric = empty.imply
	no functional.imply
	no total.imply
	no weaklyConnected.imply
	no transitive.imply
	no symmetric.imply
	no antisymmetric.imply
	no irreflexive.imply
}


run {}
