sig A{}

sig B{}

sig W{
	r:A,
	v:B}

pred constraintA[aA: set A, bB: set B]{
	all w: W  | (w.r in aA) and (w.v in  bB)
}

inst j{
	A = a+b+c+d+f,
	B = y+x+z
	,exactly 9 W
	//The generate command Without any constraint in either one of below ways
//	W = generate all disj w:W
	//OR
// 	W = generate all w:W | no w' | w.r = w'.r and w.v = w'.v
	//With a constraint
//	W = genreate all w:W | let S=(a+c+f+d) | let SS = (y+z) | constraintA[S,SS]
}{
//I am using 'let' to name the atoms' names. The current version cannot mangle the names passed toprocedure or function
let S=(a+c+f+d) | let SS = (y+z) | constraintA[S,SS]
}

//All the instances of w are distict
pred q{
//	all w: W  | (w.r in A) implies (w.v in  B)
	all disj w, w': W | not (w.r = w'.r and w.v = w'.v) 
}

run q for j
