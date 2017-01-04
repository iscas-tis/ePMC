package epmc.command;

import epmc.messages.Message;

/**
 * Messages used in the lumping command plugin of EPMC.
 * This class only contains static fields and methods, and thus is protected
 * from being instantiated.
 * 
 * @author Ernst Moritz Hahn
 */
public final class MessagesCommandExplore {
    /** Base name of resource bundle for the messages. */
    private final static String MESSAGES_COMMAND_EXPLORE = "MessagesCommandExplore";

    /** Exploring the state space of a model. */
    public final static Message EXPLORING = newMessage().setIdentifier("exploring").build();
    /** Finished exploring the state space of a model. */
    public final static Message EXPLORING_DONE = newMessage().setIdentifier("exploring-done").build();

    /**
     * Creates a new message with given identifier with this resource bundle.
     * The parameter may not be {@code null}.
     * 
     * @param message identifier of message to be created
     * @return message created
     */
    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_COMMAND_EXPLORE);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private MessagesCommandExplore() {
    }
}
