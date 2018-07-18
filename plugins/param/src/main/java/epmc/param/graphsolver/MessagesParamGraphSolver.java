package epmc.param.graphsolver;

import epmc.messages.Message;

/**
 * Messages used in graph solver part of the parametric analysis.
 * This class only contains static fields and methods, and thus is protected
 * from being instantiated.
 * 
 * @author Ernst Moritz Hahn
 */
public final class MessagesParamGraphSolver {
    /** Base name of resource bundle for the messages. */
    private final static String MESSAGES_PARAM_GRAPH_SOLVER = "MessagesParamGraphSolver";
    public final static Message PARAM_GRAPH_ELIMINATOR_START = newMessage().setIdentifier("param-graph-eliminator-start").build();
    public final static Message PARAM_GRAPH_ELIMINATOR_DONE = newMessage().setIdentifier("param-graph-eliminator-done").build();
    public final static Message PARAM_BUILD_MUTABLE_GRAPH_START = newMessage().setIdentifier("param-build-mutable-graph-start").build();
    public final static Message PARAM_BUILD_MUTABLE_GRAPH_DONE = newMessage().setIdentifier("param-build-mutable-graph-done").build();    
    public final static Message PARAM_ELIMINATION_START = newMessage().setIdentifier("param-elimination-start").build();
    public final static Message PARAM_ELIMINATION_DONE = newMessage().setIdentifier("param-elimination-done").build();
    public final static Message PARAM_ELIMINATION_PROGRESS = newMessage().setIdentifier("param-elimination-progress").build();
    public final static Message PARAM_COLLECT_RESULTS_START = newMessage().setIdentifier("param-collect-results-start").build();
    public final static Message PARAM_COLLECT_RESULTS_DONE = newMessage().setIdentifier("param-collect-results-done").build();

    /**
     * Creates a new message with given identifier with this resource bundle.
     * The parameter may not be {@code null}.
     * 
     * @param message identifier of message to be created
     * @return message created
     */
    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_PARAM_GRAPH_SOLVER);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private MessagesParamGraphSolver() {
    }
}
