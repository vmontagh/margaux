one sig Product {
	f1: one FeatureA,		--mandatory feature
	f2: lone FeatureB,		--optional feature

	totalM1: one Int,
	totalM2: one Int
}{
	totalM1 = plus[f1.m1, f2.m1]
	totalM2 = plus[f1.m2, f2.m2]
}

abstract sig Feature {
	--Metrics
	m1: one Int,
	m2: one Int
}

sig FeatureA, FeatureB extends Feature {}

inst config {
	exactly 1 Product,
	6 Int,
	FeatureA = A1 + A2,
	FeatureB = B1 + B2,
	
	--Assign metric values
	m1 = A1->5 + A2->4 +
			  B1->3 + B2->3,
	m2 = A1->4 + A2->2 +
			  B1->5 + B2->2
}

objectives opt1 {
	maximize (Product.totalM1),
	minimize (Product.totalM2)
}

pred show {}
run show for config optimize opt1
