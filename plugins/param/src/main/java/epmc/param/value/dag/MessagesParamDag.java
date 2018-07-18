package epmc.param.value.dag;

import epmc.messages.Message;

public final class MessagesParamDag {
    /** Base name of resource bundle for the messages. */
    private final static String MESSAGES_PARAM_GRAPH_SOLVER = "MessagesParamDag";
    public final static Message PARAM_DAG_NUM_NODES = newMessage().setIdentifier("param-dag-num-nodes").build();

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
    private MessagesParamDag() {
    }
}
