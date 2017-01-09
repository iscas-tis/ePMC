package epmc.graph;

import epmc.graph.Semantics;

/**
 * Semantics type for Markov automata (MA).
 */
public enum SemanticsMA implements SemanticsContinuousTime, SemanticsNonDet, SemanticsStochastic {
	/** Singleton element. */
	MA;
	
    /**
     * Checks whether this is a Markov automaton (MA).
     * 
     * @return whether this is a Markov automaton (MA)
     */
	public static boolean isMA(Semantics semantics) {
		return semantics instanceof SemanticsMA;
	}
}
