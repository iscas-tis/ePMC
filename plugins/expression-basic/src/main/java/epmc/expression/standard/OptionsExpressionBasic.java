package epmc.expression.standard;

// TODO complete documentation

public enum OptionsExpressionBasic {
    /** Base name of resource file for options description. */
    OPTIONS_EXPRESSION_BASIC,

    /**
     * Store already constructed decision diagrams in cache when converting
     * expressions to decision diagrams. */
    DD_EXPRESSION_CACHE,
    /**
     * Use a bitvector representation when converting expressions to decision
     * diagrams to avoid having to construct MTBDDs with very many terminal
     * nodes.
     */
    DD_EXPRESSION_VECTOR,

    EXPRESSION_EVALUTOR_EXPLICIT_CLASS,
    EXPRESSION_EXPRESSION_TO_CODE_CLASS,
    EXPRESSION_EVALUTOR_DD_CLASS,
    EXPRESSION_SIMPLIFIER_CLASS,
}
