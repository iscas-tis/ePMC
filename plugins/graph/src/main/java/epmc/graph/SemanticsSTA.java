package epmc.graph;

import epmc.graph.Semantics;

/**
 * Semantics type for stochastic timed automata (STAs).
 */
public enum SemanticsSTA implements SemanticsNonDet, SemanticsStochastic {
	/** Singleton element. */
	STA;
	
    /**
     * Checks whether this is a stochastic timed automaton (STA).
     * 
     * @return whether this is a stochastic timed automaton (STA)
     */
	public static boolean isSTA(Semantics semantics) {
		return semantics instanceof SemanticsSTA;
	}
}
