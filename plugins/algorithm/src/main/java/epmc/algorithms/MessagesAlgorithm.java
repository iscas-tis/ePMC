package epmc.algorithms;

import epmc.messages.Message;

public final class MessagesAlgorithm {
    /** Base name of resource bundle for the messages of this plugin. */
    private final static String MESSAGES_ALGORITHM = "MessagesAlgorithm";
    /** Unreliable Fox-Glynn algorithm results. */
    public final static Message FOX_GLYNN_UNRELIABLE_EXP_BELOW_TAU = newMessage().setIdentifier("fox-glynn-unreliable-exp-below-tau").build();
    /** Unreliable Fox-Glynn algorithm results. */
    public final static Message FOX_GLYNN_UNRELIABLE_CANT_BOUND_RIGHT = newMessage().setIdentifier("fox-glynn-unreliable-cant-bound-right").build();
    /** Unreliable Fox-Glynn algorithm results. */
    public final static Message FOX_GLYNN_UNRELIABLE_25 = newMessage().setIdentifier("fox-glynn-unreliable-25").build();
    /** Unreliable Fox-Glynn algorithm results. */
    public final static Message FOX_GLYNN_UNRELIABLE_400 = newMessage().setIdentifier("fox-glynn-unreliable-400").build();
    /** Unreliable Fox-Glynn algorithm results. */
    public final static Message FOX_GLYNN_UNDERFLOW_600 = newMessage().setIdentifier("fox-glynn-underflow-600").build();

    /**
     * Creates a new message with given identifier with this resource bundle.
     * The parameter may not be {@code null}.
     * 
     * @param message identifier of message to be created
     * @return message created
     */
    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_ALGORITHM);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
	private MessagesAlgorithm() {
	}
}
