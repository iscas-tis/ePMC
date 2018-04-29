package epmc.imdp.options;

/**
 * Class collecting options used for IMDP plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public enum OptionsIMDPLump {
    /** Base name of resource file for options description. */
    OPTIONS_IMDP_LUMP,
    /** Category for IMDP lumping options. */
    IMDP_LUMP_CATEGORY,
    /** Method to use for lumping, e.g. state-wise, block-wise lumping. */
    IMDP_LUMP_METHOD,
    /** Whether to avoid comparing states to theirselves. */
    IMDP_NO_SELF_COMPARE,
    /** Data structure to use for caching. */
    IMDP_LP_CACHE_TYPE,
    /** For block-based lumping, this option controls the method to split a
     * block. */
    IMDP_SPLIT_BLOCK_METHOD,
    /** Whether to normalise signatures of states. */
    IMDP_SIGNATURE_NORMALISE,
    /** Whether to cache before normalisation of problem set  */
    IMDP_PROBLEM_SET_CACHE_BEFORE_NORMALISATION,
    /** Whether to cache after normalisation of problem set. */
    IMDP_PROBLEM_SET_CACHE_AFTER_NORMALISATION,
    /** Shortcut for cooperative mode: check whether defender does not have
     * any action which does only assign positive probability to successor
     * classes to which defender assigns positive probability as well.*/
    IMDP_SHORTCUT_ZERO_ACTIONS,
    /** Shortcut for cooperative mode: before normalisation, check whether
     * challenger action can be simulated by a single defender action, thus
     * avoiding to construct an LP. */
    IMDP_SHORTCUT_SINGLE_ACTION_BEFORE_NORMALISATION,
    /** Shortcut for cooperative mode: after normalisation, check whether
     * challenger action can be simulated by a single defender action, thus
     * avoiding to construct LP. */
    IMDP_SHORTCUT_SINGLE_ACTION_AFTER_NORMALISATION,
    /** Shortcut for cooperative mode: check whether minimal probability
     * assigned by challenger to a given class is already so high that there
     * is no single action from which can assign enough probability. */
    IMDP_SHORTCUT_UNSIMULABLE_BLOCK,
    ;
}
