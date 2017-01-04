package epmc.util;

import epmc.error.Problem;
import epmc.error.UtilError;

public final class ProblemsJSON {
    private final static String PROBLEMS_JSON = "ProblemsJSON";
    
    /** The given element may occur only once at given context but appears at least twice. */
    public final static Problem JSON_ELEMENT_ONLY_ONCE = newProblem("json-element-only-once");
    /** Element at given position should be a JSON value string but is not. */
    public final static Problem JSON_NOT_VALUE_STRING = newProblem("json-not-value-string");
    /** Element should be one of a given set of strings, but is not. */
    public final static Problem JSON_VALUE_ONE_OF = newProblem("json-value-one-of");
    /** Element should be integral number but is not. */
    public final static Problem JSON_NOT_VALUE_INTEGER = newProblem("json-not-value-integer");
    /** Element should be integral number representable as Java int but is not. */
    public final static Problem JSON_NOT_VALUE_INTEGER_JAVA = newProblem("json-not-value-integer-java");
    /** Element should be boolean but is not. */
    public final static Problem JSON_NOT_VALUE_BOOLEAN = newProblem("json-not-value-boolean");
    /** JSON value should be array but is not */
    public static final Problem JSON_VALUE_ARRAY = newProblem("json-value-array");
    /** JSON value should be object but is not */
    public static final Problem JSON_VALUE_OBJECT = newProblem("json-value-object");;
    /** There should only be one JSON object with given identifier value in
     * given context, but there are at least two. */
    public static final Problem JSON_ELEMENT_UNIQUE = newProblem("json-element-unique");
    /** A given JSON element is required but missing. */
    public static final Problem JSON_ELEMENT_REQUIRED = newProblem("json-element-required");
    /** Given string is not a valid identifier. */
    public static final Problem JSON_INVALID_IDENTIFIER = newProblem("json-invalid-identifier");
    /** Given string does not match the given pattern. */
    public static final Problem JSON_DOES_NOT_MATCH = newProblem("json-does-not-match");
    /** General JSON error. */
    public static final Problem JSON_CANNOT_PARSE = newProblem("json-cannot-parse");
    /** JSON entry should be equal to given value but is not. */
    public static final Problem JSON_NOT_EQUALS = newProblem("json-not-equals");
    /** Element at given position should be a JSON value string but is not. */
    public static final Problem JSON_NOT_VALUE_NUMBER = newProblem("json-not-value-number");
    /** Element should be value string or start object but is not. */
    public static final Problem JSON_NOT_VALUE_STRING_OR_OBJECT = newProblem("json-not-value-string-or-object");
    /** Input is not well-formed JSON. */
    public static final Problem JSON_MALFORMED_JSON = newProblem("json-malformed-json");

    /**
     * Create new problem object using plugin resource file.
     * The name parameter must not be {@code null}.
     * 
     * @param name problem identifier String
     * @return newly created problem identifier
     */
    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(PROBLEMS_JSON, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsJSON() {
    }
}
