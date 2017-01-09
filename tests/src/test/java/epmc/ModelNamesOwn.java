package epmc;

public final class ModelNamesOwn {
    private final static String PREFIX = "epmc/";

    public final static String ZEROCONF_SIMPLE = PREFIX + "zeroconf.prism";
    public final static String CHAIN = PREFIX + "chain.prism";
    public final static String CLUSTER_DTMC_1 = PREFIX + "clusterDTMC1.prism";
    public final static String SIMPLE = PREFIX + "simple.prism";
    public final static String SIMPLE_QUEUE = PREFIX + "simple_queue.prism";
    public final static String VERYLARGE = PREFIX + "verylarge.prism";
    public final static String ER12_1 = PREFIX + "er12-1.prism";
    public final static String RANDOM_GOOGLE = PREFIX + "random_google.prism";

    public final static String MULTI_OBJECTIVE_SIMPLE = PREFIX + "multiObjectiveSimple.prism";
    public final static String MULTI_OBJECTIVE_SIMPLE_REWARDS = PREFIX + "multiObjectiveSimpleRewards.prism";
    
    public final static String ROBOT_ONE_DIR = PREFIX + "robotOneDir.prism";
    public final static String ROBOT_REDUCED = PREFIX + "robotReduced.prism";
    
    public final static String MA_SINGLEMODULE = PREFIX + "ma-singlemodule.prism";
    public final static String MA_SINGLEMODULE_TWORATE = PREFIX + "ma-singlemodule-tworate.prism";
    public final static String MA_TWOMODULES = PREFIX + "ma-twomodules.prism";
    public final static String MA_DISABLING_RATE = PREFIX + "ma-disabling-rate.prism";
    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ModelNamesOwn() {
    }
}
