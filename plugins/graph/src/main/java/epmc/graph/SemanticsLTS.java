package epmc.graph;

import epmc.graph.Semantics;

/**
 * Semantics type for labeled-transition systems (LTSs).
 */
public enum SemanticsLTS implements SemanticsDiscreteTime, SemanticsNonDet {
	/** Singleton element. */
	LTS;
	
    /**
     * Checks whether this is a labeled transition system (LTS).
     * 
     * @return whether this is a labeled transition system (LTS)
     */
	public static boolean isLTS(Semantics semantics) {
		return semantics instanceof SemanticsLTS;
	}
}
