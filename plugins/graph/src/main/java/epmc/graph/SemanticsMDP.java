package epmc.graph;

import epmc.graph.Semantics;

/**
 * Semantics type for (discrete-time) Markov decision processes (MDPs).
 */
public enum SemanticsMDP implements SemanticsDiscreteTime, SemanticsStochastic, SemanticsNonDet {
	/** Singleton element. */
	MDP;
	
    /**
     * Checks whether this is a Markov decision process (MDP).
     * 
     * @return whether this is a Markov decision process (MDP)
     */
	public static boolean isMDP(Semantics semantics) {
		return semantics instanceof SemanticsMDP;
	}
}
