/* 2012-03-31, Steven Stewart (s26stewa@uwaterloo.ca)

	Terminology used in this specification:
		Product - a configurable artifact/entity
						(e.g., computer, software, bicycle)
		Feature - an available component for a product
						(e.g. hard drive, algorithm, wheels)
		Metric - a quantifiable characteristic of a feature
						(e.g., cost, performance, bandwidth)
		Objective - a goal to be achieved when configuring a product
						  with features, that either maximizes or minimizes
						  one or more metrics							
*/

/* A product can be configured with some number of features;
	e.g., feature1, feature2, and so on. We can specify
	a different multiplicity for each feature. For example, if we
	use "lone" instead of "one", then the feature would be
	optional. */
sig Product {
	feature1: one F1,
	feature2: one F2,
	feature3: some F3,
	feature4: lone F4
} {
	--A product has a capacity of 2 for feature3
	#(feature3 & F3) <= 2
}

/* Features available to a Product, each of which are
	characterized by metrics m1, m2, etc. */
abstract sig Feature {
	--Metrics (i.e., cost, performance, ...)
	m1: Int,
	m2: Int
}
sig F1, F2, F3, F4 extends Feature {}


/* The partial instance block describes the bounds on our
	relational variables. In other words, it specifies what 
	features are available for a product, and assigns values 
	to these features for each metric. */
inst inventory {
	exactly 1 Product, 	--explore possible configs of one product
	7 Int,					 	--large enough integers for our metrics

	--inventory of options for each feature
	F1 = F1O1 + F1O2 + F1O3,
	F2 = F2O1 + F2O2 + F2O3,
	F3 = F3O1 + F3O2 + F3O3,
	F4 = F4O1 + F4O2,

	--assignment of values to metrics for each option
	m1 = F1O1->10 + F1O2->15 + F1O3->5 +
				F2O1->4 + F2O2->16 + F2O3->8 +
				F3O1->7 + F3O2->6 + F3O3->5 +
				F4O1->6 + F4O2->4,
	m2 = F1O1->5 + F1O2->7 + F1O3->3 +
				F2O1->8 + F2O2->5 + F2O3->2 +
				F3O1->3 + F3O2->2 + F3O3->4 +
				F4O1->2 + F4O2->4
}

objectives config {
	/* Maximize first metric for each feature */
	maximize [Product.feature1.m1],
	maximize [Product.feature2.m1],
	maximize [Product.feature3.m1],
	maximize [Product.feature4.m1],

	/* Minimize second metric for each feature */
	minimize [Product.feature1.m2],
	minimize [Product.feature2.m2],
	minimize [Product.feature3.m2],
	minimize [Product.feature4.m2]
}

pred show {}

run show for inventory optimize config

