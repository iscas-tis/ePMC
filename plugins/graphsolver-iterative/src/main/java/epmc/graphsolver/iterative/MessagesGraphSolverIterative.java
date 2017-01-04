package epmc.graphsolver.iterative;

import epmc.messages.Message;

/**
 * Messages used in the iterative graph solver of EPMC.
 * This class only contains static fields and methods, and thus is protected
 * from being instantiated.
 * 
 * @author Ernst Moritz Hahn
 */
public final class MessagesGraphSolverIterative {
    /** Base name of resource bundle for the messages. */
    private final static String MESSAGES_GRAPHSOLVER_ITERATIVE = "MessagesGraphSolverIterative";
    public final static Message ITERATING = newMessage().setIdentifier("iterating").build();
    public final static Message ITERATING_DONE = newMessage().setIdentifier("iterating-done").build();

    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_GRAPHSOLVER_ITERATIVE);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private MessagesGraphSolverIterative() {
    }
}
