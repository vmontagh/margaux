abstract sig S {r: set S} 
one sig S0,S1 extends S{}
fact {r=S0->S1+S1->S0}
run {}