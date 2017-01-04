package epmc.graph;

import epmc.graph.Semantics;

/**
 * Semantics type for timed automata (TAs).
 */
public enum SemanticsTA implements SemanticsNonDet {
	/** Singleton element. */
	TA;
	
    /**
     * Checks whether this is a timed automaton (TA).
     * 
     * @return whether this is a timed automaton (TA)
     */
	public static boolean isTA(Semantics semantics) {
		return semantics instanceof SemanticsTA;
	}
}
