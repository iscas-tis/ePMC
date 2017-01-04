package epmc.dd;

import epmc.error.Problem;
import epmc.error.UtilError;

// TODO content should be moved to according DD plugins
public final class ProblemsDD {
    private final static String PROBLEMS_DD = "ProblemsDD";
    /** There is insufficient memory to perform an operation in a native DD library. */
    public final static Problem INSUFFICIENT_NATIVE_MEMORY = newProblem("insufficient-native-memory");
    public final static Problem BUDDY_NATIVE_LOAD_FAILED = newProblem("buddy-native-load-failed");
    public final static Problem CACBDD_NATIVE_LOAD_FAILED = newProblem("cacbdd-native-load-failed");
    public final static Problem CUDD_NATIVE_LOAD_FAILED = newProblem("cudd-native-load-failed");
    public final static Problem MEDDLY_NATIVE_LOAD_FAILED = newProblem("meddly-native-load-failed");
    public final static Problem SYLVAN_NATIVE_LOAD_FAILED = newProblem("sylvan-native-load-failed");
    public final static Problem NO_BDD_LIBRARY_AVAILABLE = newProblem("no-bdd-library-available");
    public final static Problem NO_MTBDD_LIBRARY_AVAILABLE = newProblem("no-mtbdd-library-available");
    
    private static Problem newProblem(String name) {
        return UtilError.newProblem(PROBLEMS_DD, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsDD() {
    }
}
