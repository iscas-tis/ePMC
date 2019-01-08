package epmc.qmc;

public final class ModelNames {
    private final static String PREFIX = "epmc/qmc/";
    private final static String EXTENSION_MODEL = ".prism";
    private final static String EXTENSION_PROPERTIES = ".props";

    public final static String KEY_DISTRIBUTION_MODEL = PREFIX + "key-distribution" + EXTENSION_MODEL;
    public final static String KEY_DISTRIBUTION_PROPERTIES = PREFIX + "key-distribution" + EXTENSION_PROPERTIES;
    public final static String LOOP_MODEL = PREFIX + "loop" + EXTENSION_MODEL;
    public final static String LOOP_PROPERTIES = PREFIX + "loop" + EXTENSION_PROPERTIES;
    public final static String LOOP_ALLOPERATORS_MODEL = PREFIX + "loop-alloperators" + EXTENSION_MODEL;
    public final static String LOOP_ALLOPERATORS_PROPERTIES = PREFIX + "loop-alloperators" + EXTENSION_PROPERTIES;
    public final static String LOOP_ALTERNATIVE_MODEL = PREFIX + "loop-alternative" + EXTENSION_MODEL;
    public final static String LOOP_ALTERNATIVE_PROPERTIES = PREFIX + "loop-alternative" + EXTENSION_PROPERTIES;
    public final static String SUPERDENSE_CODING_MODEL = PREFIX + "superdense-coding" + EXTENSION_MODEL;
    public final static String SUPERDENSE_CODING_PROPERTIES = PREFIX + "superdense-coding" + EXTENSION_PROPERTIES;

    private ModelNames() {
    }
}
