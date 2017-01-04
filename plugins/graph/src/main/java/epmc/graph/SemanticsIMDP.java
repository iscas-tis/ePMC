package epmc.graph;

import epmc.graph.Semantics;

/**
 * Semantics type for interval Markov decision processes (IMDPs).
 */
public enum SemanticsIMDP implements SemanticsDiscreteTime, SemanticsNonDet, SemanticsStochastic {
	/** Singleton element. */
	IMDP;

    /**
     * Checks whether this is an interval Markov decision process (IMDP).
     * 
     * @return whether this is an interval Markov decision process (IMDP)
     */
	public static boolean isIMDP(Semantics semantics) {
		return semantics instanceof SemanticsIMDP;
	}
}
