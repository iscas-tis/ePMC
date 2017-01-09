package epmc.graph;

import epmc.graph.Semantics;

/**
 * Semantics type for interactive Markov chains (IMCs).
 */
public enum SemanticsIMC implements SemanticsContinuousTime, SemanticsNonDet, SemanticsStochastic {
	/** Singleton element. */
	IMC;
	
    /**
     * Checks whether this is an interactive Markov chain (IMC).
     * 
     * @return whether this is an interactive Markov chain (IMC)
     */
	public static boolean isIMC(Semantics semantics) {
		return semantics instanceof SemanticsIMC;
	}
}
