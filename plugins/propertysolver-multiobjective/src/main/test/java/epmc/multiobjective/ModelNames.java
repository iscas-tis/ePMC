package epmc.multiobjective;

/**
 * Filenames of test models for multiobjective solver.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ModelNames {
	/** Directory containing the model files. */
    private final static String PREFIX = "epmc/multiobjective/";
    
    public final static String MULTI_OBJECTIVE_SIMPLE = PREFIX + "multiObjectiveSimple.prism";
    public final static String MULTI_OBJECTIVE_SIMPLE_REWARDS = PREFIX + "multiObjectiveSimpleRewards.prism";
    public final static String DINNER_REDUCED_PROB_BOUNDED_1 = PREFIX + "dinnerReducedProb-bounded-1.prism";
    public final static String DINNER_REDUCED_BOUNDED_1 = PREFIX + "dinnerReduced-bounded-1.prism";

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ModelNames() {
    }
}
