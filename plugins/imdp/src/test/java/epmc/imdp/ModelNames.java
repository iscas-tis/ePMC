package epmc.imdp;

// TODO maybe add philosophers model

public final class ModelNames {
    /** Prefix of path where models are stored. */
    private final static String PREFIX = "epmc/imdp/";

    public final static String TOY = PREFIX + "toy.nm";

    public final static String COIN = PREFIX + "coin%d.nm";

    public final static String ZEROCONF_DL = PREFIX + "zeroconf_dl_%d.nm";

    public final static String PHIL_NOFAIR = PREFIX + "phil-nofair%d.nm";

    /** Robot case study from Morteza (flat model) */
    public final static String ROBOT = PREFIX + "robot_IMDP.txt";

    /** Robot case study from Morteza journal version (flat model) */
    public final static String ROBOT_JOURNAL = PREFIX + "robot_IMDP_journal.txt";

    public final static String ROBOT_WAREHOUSE_P1 = PREFIX + "robot_warehouse_P1.txt";

    public final static String ROBOT_WAREHOUSE_P1P2P3_ANYORDER = PREFIX + "robot_warehouse_P1P2P3_anyorder.txt";

    public final static String ROBOT_WAREHOUSE_P1P2P3_STRICT = PREFIX + "robot_warehouse_P1P2P3_strict.txt";

    /** Running example from so-be-submitted QEST 2017 paper (slightly
     * modified, because time-bounded properties not supported yet.)
     */
    public final static String RUNNING_EXAMPLE_QEST_2017 = PREFIX + "running_example_qest_2017.txt";

    public final static String TOUR_GUIDE = PREFIX + "tour_guide.prism";
    public final static String TOUR_GUIDE_CONFIGURATION_1 = PREFIX + "tour_guide_configuration_1.prism";
    public final static String TOUR_GUIDE_CONFIGURATION_2 = PREFIX + "tour_guide_configuration_2.prism";

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ModelNames() {
    }
}
