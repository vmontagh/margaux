-- Binary Tree invariants

-- You can understand the keyword "sig" to mean "class".
-- The syntax of Alloy makes these look like class and field
-- declarations. That intuition is basically correct.
sig Tree { root : Node }
sig Node { 
	left, right : lone Node,
	value : one Int,
}

-- A function with no arguments has a constant value.
-- For example, y = 3 + 4. y always evaluates to 7.
-- In Alloy variables have relational values rather than integer values.
-- So the value of the parent function is a relation that maps each
-- node to its parent node.
-- We can use these constant functions in the same syntactic places
-- where we can use relations (fields). For example, we could write
-- n.parent to get the parent of Node n.
fun parent [] : Node->Node {~(left+right)}


-- invariants of binary search tree
pred well_formed_tree {
    -- TODO: write this predicate
}

-- We want to see trees that are non-empty and well-formed.
pred interesting {
	some Node
	well_formed_tree
}

-- Show us an interesting tree with up to three nodes.
run interesting for 3 but exactly 1 Tree


