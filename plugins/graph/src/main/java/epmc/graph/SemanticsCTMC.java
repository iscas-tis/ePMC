package epmc.graph;

import epmc.graph.Semantics;

/**
 * Semantics type for continuous-time Markov chains (CTMCs).
 */
public enum SemanticsCTMC implements SemanticsStochastic, SemanticsContinuousTime, SemanticsMarkovChain {
	/** Singleton element. */
	CTMC;
	
    /**
     * Checks whether this is a continuous-time Markov chain (CTMC).
     * 
     * @return whether this is a continuous-time Markov chain (CTMC)
     */
	public static boolean isCTMC(Semantics semantics) {
		return semantics instanceof SemanticsCTMC;
	}
}
