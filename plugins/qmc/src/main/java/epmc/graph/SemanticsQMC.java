package epmc.graph;

import epmc.graph.Semantics;
import epmc.graph.SemanticsDTMC;
import epmc.graph.SemanticsDiscreteTime;
import epmc.graph.SemanticsMarkovChain;

/**
 * Semantics type for quantum Markov chains (QMCs).
 */
public enum SemanticsQMC implements SemanticsDTMC,  SemanticsDiscreteTime, SemanticsMarkovChain {
    /** Singleton element. */
    QMC;

    /**
     * Checks whether this is a quantum Markov chain (QMC).
     * 
     * @return whether this is a quantum Markov chain (QMC)
     */
    static boolean isQMC(Semantics semantics) {
        return semantics instanceof SemanticsQMC;
    }
}
