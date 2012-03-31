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
	e.g., feature1, feature2, and so on. In this model, each feature
	appears once in a product configuration, but we could specify
	a different multiplicity. For example, if we use "lone" instead of
	"one", then the feature would be optional. */
sig Product {
	feature1: one F1,
	feature2: one F2,

	/* We add these declarations for three reasons:
		(1) it is handy to have the totals for our objectives appear
			 in the visualizer
		(2) it is not possible to combine values using plus or mult
			 from within an objectives block (yet)
		(3) this reduces the number of objective declarations in the
			 objectives block, which may yield better performance
			 (although experimentation is required)

		Thus, for each metric in declared in the Feature sig, we have
		a "totalMetric" singleton relation specified here.
	*/
	totalMetric1: one Int,
	totalMetric2: one Int
} {
	
	--Unable to use plus function in objectives block, so this is the workaround
	totalMetric1 = plus[feature1.m1, feature2.m1]
	totalMetric2 = plus[feature1.m2, feature2.m2]
}

/* Features available to a Product, each of which are
	characterized by metrics m1, m2, etc. */
abstract sig Feature {
	--Metrics (i.e., cost, performance, ...)
	m1: Int,
	m2: Int
}
sig F1, F2 extends Feature {}

/* The partial instance block describes the bounds on our
	relational variables. In other words, it specifies what 
	features are available for a product, and assigns values 
	to these features for each metric. */
inst inventory {
	exactly 1 Product, 	--explore possible configs of one product
	6 Int,					 	--large enough integers for our metrics

	--inventory of options for each feature
	F1 = F1O1 + F1O2 + F1O3,
	F2 = F2O1 + F2O2 + F2O3,

	--assignment of values to metrics for each option
	m1 = F1O1->10 + F1O2->15 + F1O3->5 +
				F2O1->4 + F2O2->16 + F2O3->8,
	m2 = F1O1->5 + F1O2->7 + F1O3->3 +
				F2O1->8 + F2O2->5 + F2O3->2
}

objectives config {
	/* Maximize first metric for each feature */
		maximize[Product.totalMetric1],

	/* Minimize second metric for each feature */
		minimize[Product.totalMetric2]
}

pred show {}

run show for inventory optimize config

