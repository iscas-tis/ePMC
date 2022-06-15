package epmc.graph;

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
