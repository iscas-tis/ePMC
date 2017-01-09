package epmc.graph;

import epmc.messages.Message;

public final class MessagesGraph {
    public static final String MESSAGES_GRAPH = "MessagesGraph";
    
    public final static Message CONVERTING_DD_GRAPH_TO_EXPLICIT = newMessage().setIdentifier("converting-dd-graph-to-explicit").build();
    public final static Message CONVERTING_DD_GRAPH_TO_EXPLICIT_DONE = newMessage().setIdentifier("converting-dd-graph-to-explicit-done").build();
    public static final Message BUILD_MODEL_START = newMessage().setIdentifier("build-model-start").build();
    public static final Message BUILD_MODEL_STATES_EXPLORED = newMessage().setIdentifier("build-model-states-explored").build();
    public static final Message BUILD_MODEL_NEXT_PHASE = newMessage().setIdentifier("build-model-next-phase").build();
    public static final Message BUILD_MODEL_DONE = newMessage().setIdentifier("build-model-done").build();

    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_GRAPH);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private MessagesGraph() {
    }
}
