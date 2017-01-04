package epmc.graph;

import epmc.graph.Semantics;

public interface SemanticsNonDet extends Semantics {
    /**
     * Checks whether this semantics contains nondeterminism.
     * 
     * @return whether this semantics contains nondeterminism
     */
	static boolean isNonDet(Semantics semantics) {
		return semantics instanceof SemanticsNonDet;
	}
}
