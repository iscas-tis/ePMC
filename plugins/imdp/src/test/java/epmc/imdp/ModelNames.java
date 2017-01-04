package epmc.imdp;

// TODO maybe add philosophers model

public final class ModelNames {
	/** Prefix of path where models are stored. */
    private final static String PREFIX = "epmc/imdp/";

    public final static String TOY = PREFIX + "toy.nm";
    
    public final static String COIN = PREFIX + "coin%d.nm";
    
    public final static String ZEROCONF_DL = PREFIX + "zeroconf_dl_%d.nm";

    public final static String PHIL_NOFAIR = PREFIX + "phil-nofair%d.nm";

    /**
     * Private constructor to prevent instantiation of this class.
     */
	private ModelNames() {
	}
}
