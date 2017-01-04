package epmc.graph;

import epmc.graph.Semantics;

public interface SemanticsMarkovChain extends Semantics {
    /**
     * Checks whether this is a discrete or continuous Markov chain semantics.
     * 
     * @return whether this is a discrete or continuous Markov chain semantics
     */
	static boolean isMarkovChain(Semantics semantics) {
		return semantics instanceof SemanticsMarkovChain;
	}
}
