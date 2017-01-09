package epmc.messages;

import epmc.error.Problem;
import epmc.error.UtilError;

public final class ProblemsMessages {
    private final static String PROBLEMS_MESSAGES = "ProblemsMessages";
    public final static Problem REMOTE = newProblem("remote");
    public final static Problem CHANNEL_FAILURE = newProblem("channel-failure");
    
    private static Problem newProblem(String name) {
        return UtilError.newProblem(PROBLEMS_MESSAGES, name);
    }
    
    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsMessages() {
    }
}
