
sig Node {next: lone Node}


pred deltaNode[node: set Node, node': set Node, node'': set Node] {
    node != node' implies (node'' = node' - node and node'' + node = node') else no node''
}

pred deltaNode_next[node_next: Node->Node, node_next': Node->Node, node_next'': Node->Node] {
    node_next != node_next' implies (node_next'' = node_next' - node_next and node_next'' + node_next = node_next') else no node_next''
}

pred structuralConstraints [node: set Node, node_next: Node->Node] {
    (all p_this: one node | (lone (p_this . node_next) && ((p_this . node_next) in node)))
    ((node_next . univ) in node)
}

pred includeInstance [node: set Node, node_next: Node->Node] {
    (node_next.Node) in Node
    (Node.node_next) in Node
}

pred isInstance [node: set Node, node_next: Node->Node] {
    includeInstance[Node, node_next]
    structuralConstraints[Node, node_next]
}

pred findMarginalInstances[] {
    some node, node', node'': set Node, node_next, node_next', node_next'': set Node->Node | {
            (
            isInstance[node, node_next]
            and isInstance[node', node_next']
            and deltaNode[node, node', node'']
            and deltaNode_next[node_next, node_next', node_next'']
            )
        and
            all node1, node1', node1'': set Node, node_next1, node_next1', node_next1'': set Node->Node | {
                    (
                    isInstance[node1, node_next1]
                    and isInstance[node1', node_next1']
                    and deltaNode[node1, node1', node1'']
                    and deltaNode_next[node_next1, node_next1', node_next1'']
                    )
                implies
                    (
                    sigma[#node'', #node_next''] <= sigma[#node1'', #node_next1'']
                    )
            }
    }
}

fun sigma [a1, a2: Int] : Int {
    a1.add[a2]
}

run findMarginalInstances
