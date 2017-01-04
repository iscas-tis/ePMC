package epmc.algorithms;

import epmc.algorithms.explicit.ComponentsExplicit;

public final class UtilAlgorithms {    
    public static ComponentsExplicit newComponentsExplicit() {
        return new ComponentsExplicit();
    }
    
    /**
     * Private constructor to prevent instantiation of this class.
     */
    private UtilAlgorithms() {
    }
}
