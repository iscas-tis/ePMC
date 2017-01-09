package epmc.graph;

import epmc.graph.Semantics;

/**
 * Semantics type for continuous-time Markov decision processes (CTMDPs).
 */
public enum SemanticsCTMDP implements SemanticsStochastic, SemanticsContinuousTime, SemanticsNonDet {
	/** Singleton element. */
	CTMDP;
	
    /**
     * Checks whether this is a continuous-time Markov decision process (CTMDP).
     * 
     * @return whether this is a continuous-time Markov decision process (CTMDP)
     */
	public static boolean isCTMDP(Semantics semantics) {
		return semantics instanceof SemanticsCTMDP;
	}
}
