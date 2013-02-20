// boilerplate stuff, used for every isomorphism check
abstract sig Node { }
abstract sig Var extends Node {}
abstract sig Op extends Node {}
abstract sig And, Or, Not extends Op {}
sig Graph1, Graph2 in Node {}
fact { 
    Node = Graph1 + Graph2 
    Var in Graph1
    Var in Graph2
    no (Graph1-Var) & (Graph2-Var)
}

one sig Mapping { map : Graph1 lone -> lone Graph2 }
fact { 
    let v = (Var->Var & iden) | let m = Mapping.map | let n=m-v {
        v in m
        no n.Var
        no Var.n
    }
}

fun subst[m : Graph1->Graph2, e : Graph1->Graph1] : Graph2->Graph2 {
    { s, t : Graph2 | some p, q : Graph1 | {
        p->q in e 
        p->s in m
        q->t in m }}
}

pred solve[] {
    subst[Mapping.map, edge1] = edge2   
}
run solve for 0


// stuff below this line is generated from the two dot files
one sig A, B, D, E, X, Y extends Var {}
one sig Or10a, Or4b extends Or {}
one sig And5a, And5b, And11a, And11b extends And {}
fact {
    Graph1 = Or10a + And5a + And11a + Var
    Graph2 = Or4b + And5b + And11b + Var
}

fun edge1[] : Node->Node {
    And5a -> X + 
    A -> Or10a + 
    B -> Or10a + 
    D -> And5a + 
    Or10a -> And5a +
    And11a -> Y +
    E -> And11a +
    Or10a -> And11a
}
fun edge2[] : Node->Node {
    And5b -> X + 
    A -> Or4b + 
    B -> Or4b + 
    D -> And5b + 
    Or4b -> And5b +
    And11b -> Y +
    E -> And11b +
    Or4b -> And11b
}
