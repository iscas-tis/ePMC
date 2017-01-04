package epmc.value;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Class collecting problems potentially occurring in value basic plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsValueBasic {
    /** Base name of resource file containing module problem descriptions. */
    public final static String PROBLEMS_VALUE_BASIC = "ProblemsValueBasic";
    /** Two values are to be compared. However, they are not equal, but also
     * none of them is smaller or larger than the other.
     * */
    public final static Problem VALUES_INCOMPARABLE = newProblem("values-incomparable");
    /** Given string does not represent a valid value of given type. */
    public final static Problem VALUES_STRING_INVALID_VALUE = newProblem("value-string-invalid-value");
    public final static Problem VALUES_UNSUPPORTED_OPERATION = newProblem("value-unsupported-operation");
    
    /**
     * Create new problem object using module resource file.
     * The name parameter must not be {@code null}.
     * 
     * @param name problem identifier String
     * @return newly created problem identifier
     */
    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(PROBLEMS_VALUE_BASIC, name);
    }
    
    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsValueBasic() {
    }
}
