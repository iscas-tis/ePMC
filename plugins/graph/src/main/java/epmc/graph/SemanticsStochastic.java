package epmc.graph;

import epmc.graph.Semantics;

public interface SemanticsStochastic extends Semantics {
    /**
     * Checks whether this is a stochastic/probabilistic semantics type.
     * 
     * @return whether this is a stochastic/probabilistic semantics type
     */
	static boolean isStochastic(Semantics semantics) {
		return semantics instanceof SemanticsStochastic;
	}
}
