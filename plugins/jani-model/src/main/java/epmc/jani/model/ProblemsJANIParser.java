package epmc.jani.model;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Class collecting problems potentially occurring in parser part of JANI plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsJANIParser {
	/** Base name of resource file containing plugin problem descriptions. */
    private final static String PROBLEMS_JANI_PARSER = "ProblemsJANIParser";
    
	/** Version number should be larger or equal to 1. */
    public final static Problem JANI_PARSER_VERSION_NUMBER_WRONG = newProblem("jani-parser-version-number-wrong");
	/** JANI input is not well-formed JSON. */
	public static final Problem JANI_PARSER_MALFORMED_JSON = newProblem("jani-parser-malformed-json");
	/** Automaton variable shadows global variable. */
	public static final Problem JANI_PARSER_VARIABLE_SHADOWS_GLOBAL = newProblem("jani-parser-variable-shadows-global");
	/** Automaton variable shadows constant definition. */
	public static final Problem JANI_PARSER_VARIABLE_SHADOWS_CONSTANT = newProblem("jani-parser-variable-shadows-constant");
	/** Wrong arity for given operator. */
	public static final Problem JANI_PARSER_WRONG_ARITY = newProblem("jani-parser-wrong-arity");
	/** Array "elements" of parallel composition has invalid size (!= 2). */
	public static final Problem JANI_PARSER_PARALLEL_ELEMENTS_WRONG_SIZE = newProblem("jani-parser-parallel-elements-wrong-size");
	/** Arrays "from" and "to" of rename compositions have different sizes. */
	public static final Problem JANI_PARSER_RENAME_DIFFERENT_SIZES = newProblem("jani-parser-rename-different-sizes");
	/** Incompatible types in assignment. */
	public static final Problem JANI_PARSER_ASSIGNMENT_TYPE_ERROR = newProblem("jani-parser-assignment-type-error");
	/** Right-side of assignment is type-inconsistent. */
	public static final Problem JANI_PARSER_ASSIGNMENT_INCONSISTENT = newProblem("jani-parser-assignment-inconsistent");
	/** Invalid JSON value type when parsing expression. */
	public static final Problem JANI_PARSER_EXPRESSION_INVALID_JSON_VALUE_TYPE = newProblem("jani-parser-expression-invalid-json-value-type");
	/** Invalid JSON value type when parsing constant value. */
	public static final Problem JANI_PARSER_CONSTANT_VALUE_INVALID_JSON_VALUE_TYPE = newProblem("jani-parser-value-invalid-json-constant-value-type");
	/** Destinations must contain at least one destination. */
	public static final Problem JANI_PARSER_DESTINATIONS_NOT_EMPTY = newProblem("jani-parser-destinations-not-empty");
	/** Constants and global variables must be disjoint. */
	public static final Problem JANI_PARSER_DISJOINT_GLOBALS_CONSTANTS = newProblem("jani-parser-disjoint-globals-constants");
	/** Global variables should be present if and only if initial states are present. */
	public static final Problem JANI_PARSER_GLOBAL_VARIABLES_INITIAL_STATES = newProblem("jani-parser-global-variables-initial-states");
	/** Automaton variables should be present if and only if initial states are present. */
	public static final Problem JANI_PARSER_AUTOMATON_VARIABLES_INITIAL_STATES = newProblem("jani-parser-automaton-variables-initial-states");
	/** Automaton variables should be present if and only if initial states are present. */
	public static final Problem JANI_PARSER_COMPOSITION_DIFFERENT_SIZES = newProblem("jani-parser-composition-different-sizes");
	
	/**
	 * Create new problem object using plugin resource file.
	 * The name parameter must not be {@code null}.
	 * 
	 * @param name problem identifier String
	 * @return newly created problem identifier
	 */
    private static Problem newProblem(String name) {
    	assert name != null;
    	return UtilError.newProblem(PROBLEMS_JANI_PARSER, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsJANIParser() {
    }
}
