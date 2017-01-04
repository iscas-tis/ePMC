package epmc.modelchecker.messages;

import epmc.messages.Message;

/**
 * Messages used in the model checker part of EPMC.
 * This class only contains static fields and methods, and thus is protected
 * from being instantiated.
 * 
 * @author Ernst Moritz Hahn
 */
public final class MessagesModelChecker {
    /** Base name of resource bundle for the messages. */
    private final static String MESSAGES_MODEL_CHECKER = "MessagesModelChecker";    
    /** Model checking has started. */
    public final static Message MODEL_CHECKING = newMessage().setIdentifier("model-checking").build();
    /** A given property is going to be analysed. */
    public final static Message ANALYSING_PROPERTY = newMessage().setIdentifier("analysing-property").build();
    /** Model checking finished. */
    public final static Message MODEL_CHECKING_DONE = newMessage().setIdentifier("model-checking-done").build();

    /**
     * Creates a new message with given identifier with this resource bundle.
     * The parameter may not be {@code null}.
     * 
     * @param message identifier of message to be created
     * @return message created
     */
    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_MODEL_CHECKER);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private MessagesModelChecker() {
    }
}
