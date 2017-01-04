package epmc.graph;

import epmc.graph.Semantics;

public interface SemanticsDiscreteTime extends Semantics {
    /**
     * Checks whether this is a discrete-time semantics type.
     * 
     * @return whether this is a discrete-time semantics type
     */
	static boolean isDiscreteTime(Semantics semantics) {
		return semantics instanceof SemanticsDiscreteTime;
	}
}
