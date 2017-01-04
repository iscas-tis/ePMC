package epmc.graph;

import epmc.graph.Semantics;

/**
 * Semantics type for discrete-time Markov chains (DTMCs).
 */
public interface SemanticsDTMC extends SemanticsDiscreteTime, SemanticsStochastic, SemanticsMarkovChain {
    /**
     * Checks whether this is a discrete-time Markov chain (DTMC).
     * 
     * @return whether this is a discrete-time Markov chain (DTMC)
     */
	public static boolean isDTMC(Semantics semantics) {
		return semantics instanceof SemanticsDTMC;
	}
}
