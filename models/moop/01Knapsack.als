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
	6 Int,
	Item = apple + orange + pear + banana + peach,
	weight = apple->2 + orange->4 + pear->2 +
					banana->5 + peach->3,
	value = apple->6 + orange->3 + pear->5 +
					banana->4 + peach->3
}

pred knap1 { Knapsack.totalWeight < 10 }

objectives o { maximize Knapsack.totalValue }

run knap1 for contents1 optimize o
