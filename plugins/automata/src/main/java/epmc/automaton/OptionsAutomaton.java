package epmc.automaton;

/**
 * Options of automaton plugin of EPMC.
 * 
 * @author Ernst Moritz Hahn
 */
public enum OptionsAutomaton {
    /** Base name of resource bundle. */
    OPTIONS_AUTOMATON,
    AUTOMATON_CATEGORY,
    /** number of states reserved for DD-based automata ({@link Integer}) */
    AUTOMATON_DD_MAX_STATES,
    AUTOMATON_BUILDER,
    AUTOMATON_SPOT_LTL2TGBA_CMD,
    AUTOMATON_SUBSUME_APS,
    AUTOMATON_DET_NEG,
    AUTOMATA_REPLACE_NE,
    AUTOMATON_CLASS,
    ;

    public static enum Ltl2BaAutomatonBuilder {
        SPOT
    }

    public static enum Ltl2BaDetNeg {
        NEVER,
        BETTER,
        ALWAYS
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private OptionsAutomaton() {
    }
}
