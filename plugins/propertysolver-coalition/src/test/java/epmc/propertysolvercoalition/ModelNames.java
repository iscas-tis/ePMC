package epmc.propertysolvercoalition;

/**
 * Name of test case models for the coalition solver (2 1/2 player) plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ModelNames {
	/** Prefix of path where models are stored. */
    private final static String PREFIX = "epmc/propertysolvercoalition/";
    
    /** Two robots model from our paper. */
    public final static String ROBOTS = PREFIX + "robots.prism";
    /** Small version of the robot model from our paper. */
    public final static String ROBOTS_SMALL = PREFIX + "robots-small.prism";

    public final static String ROBOTS_MODIFIED = PREFIX + "robots-modified.prism";

    public final static String ROBOTS_MODIFIED_SMALL = PREFIX + "robots-modified-small.prism";

    public final static String ROBOTS_MODIFIED_MEDIUM = PREFIX + "robots-modified-medium.prism";

    public final static String ROBOTS_QUANTITATIVE = PREFIX + "robots-quantitative.prism";

    public final static String ROBOTS_SMALL4 = PREFIX + "robots-small4.prism";

    public final static String TWO_INVESTORS = PREFIX + "two_investors.prism";

    /**
     * Private constructor to prevent instantiation of this class.
     */
	private ModelNames() {
	}
}
