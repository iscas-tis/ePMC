package epmc.graphsolver.lp;

import epmc.messages.Message;

/**
 * Messages used in the LP graph solver plugin of EPMC.
 * This class only contains static fields and methods, and thus is protected
 * from being instantiated.
 * 
 * @author Ernst Moritz Hahn
 */
public final class MessagesGraphSolverLP {
    /** Base name of resource bundle for the messages. */
    private final static String MESSAGES_GRAPHSOLVER_LP = "MessagesGraphSolverLP";
    public final static Message PREPARING_MDP_FOR_ITERATION = newMessage().setIdentifier("preparing-mdp-for-iteration").build();

    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_GRAPHSOLVER_LP);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private MessagesGraphSolverLP() {
    }
}
