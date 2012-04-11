/* 0-1 Knapsack */

one sig Knapsack {
	items: set Item,
	totalWeight: one Int,
	totalValue: one Int
} {
	totalWeight =  (sum i:items | i.weight)
	totalValue = (sum i:items | i.value)
}

sig Item {	weight: one Int, value: one Int }

inst contents1 {
	4 sparse Int,
	Item = apple + orange + pear + banana + peach,
	weight = apple->20 + orange->40 + pear->20 +
					banana->50 + peach->30,
	value = apple->60 + orange->30 + pear->50 +
					banana->40 + peach->30
}

pred knap1 { Knapsack.totalWeight < 100 }

//objectives o { maximize Knapsack.totalValue }

run knap1 for contents1 //optimize o
