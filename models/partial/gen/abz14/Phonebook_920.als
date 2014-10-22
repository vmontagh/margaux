module tour/addressBook3d ----- this is the final model in fig 2.18

abstract sig Target { }
sig Addr extends Target { }
abstract sig Name extends Target { }

sig Alias, Group extends Name { }

abstract sig Bool {}

one sig True, False extends Bool{}

uniq 
sig Book {
	p: Bool,
	names: set  Name,
	addr: names->some Target
} {
	no n: Name | n in n.^addr
	all a: Alias | lone a.addr
	p = True iff (all n: names | some /*lookup [b,n]*/  (n.^(addr) & Addr))
//	all b,b':Book|((b.addr=b'.addr) implies b=b')
}


pred add [b, b': Book, n: Name, t: Target] { 
	t in Addr or some (n.^(b.addr) & Name&t) 
	b'.addr = b.addr - n->t
	b != b'
}

pred del [b, b': Book, n: Name, t: Target] { 
	b != b'
	no b.addr.n or some n.(b.addr) - t
	b'.addr = b.addr - n->t
} 

/*assert InsertORRemove{
	all b:Book|some n:Name,t:Target,b':Book| add [b,b',n,t] or del[b,b',n,t])} 
	
check InsertORRemove for 0 but exactly 2 Loc, exactly 2 Alias, exactly 0 Group*/

pred InsertORRemove[]{
	not( all b:Book|some n:Name,t:Target,b':Book| add [b,b',n,t] or del[b,b',n,t]) 

} 


inst i{0,exactly 3 Addr, exactly 2 Alias,exactly 1 Group}

run InsertORRemove for i
