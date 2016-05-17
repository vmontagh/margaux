package edu.uw.ece.alloy.debugger.mutate;

/**
 * The class represents an oracle that knows what examples are acceptable and
 * what are not. It can interact with human, or use a correct model to reject or
 * accept a given example.
 * 
 * @author vajih
 *
 */
public interface Oracle {
	/**
	 * Take an alloy model of an example, e.g.
	 * "some disj n_0, n_1: univ| (n_0 + n_1) = Node and n_0 -> n_0 in next" It
	 * returns whether the model is intended.
	 * 
	 * @param example
	 * @return
	 */
	boolean isIntended(String example);
}
