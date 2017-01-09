package epmc.graph;

import epmc.graph.Semantics;

/**
 * Semantics type for (discrete-time) stochastic Markov games (SMGs).
 */
public enum SemanticsSMG implements SemanticsDiscreteTime, SemanticsNonDet, SemanticsStochastic {
	SMG;
	
    /**
     * Checks whether this is a stochastic Markov game (SMG).
     * 
     * @return whether this is a stochastic Markov game (SMG)
     */
	public static boolean isSMG(Semantics semantics) {
		return semantics instanceof SemanticsSMG;
	}
}
