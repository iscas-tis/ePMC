package epmc.graph;

import epmc.graph.Semantics;

public interface SemanticsContinuousTime extends Semantics {
    /**
     * Checks whether this is a continuous-time semantics type.
     * 
     * @return whether this is a continuous-time semantics type
     */
	static boolean isContinuousTime(Semantics semantics) {
		return semantics instanceof SemanticsContinuousTime;
	}
}
