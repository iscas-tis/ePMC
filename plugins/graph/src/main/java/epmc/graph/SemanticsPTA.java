package epmc.graph;

import epmc.graph.Semantics;

/**
 * Semantics type for probabilistic timed automata (PTAs).
 */
public enum SemanticsPTA implements SemanticsNonDet, SemanticsStochastic {
	/** Singleton element. */
	PTA;
	
    /**
     * Checks whether this is a probabilistic timed automaton (PTA).
     * 
     * @return whether this is a probabilistic timed automaton (PTA)
     */
	public static boolean isPTA(Semantics semantics) {
		return semantics instanceof SemanticsPTA;
	}
}
