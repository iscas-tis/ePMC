package epmc.main.messages;

import epmc.messages.Message;

/**
 * Messages used in the main part of EPMC.
 * This class only contains static fields and methods, and thus is protected
 * from being instantiated.
 * 
 * @author Ernst Moritz Hahn
 */
public final class MessagesEPMC {
    /** Base name of resource bundle for the messages. */
    private final static String MESSAGES_EPMC = "MessagesEPMC";    
    /**
     * Assertions are enabled.
     * Thus, the tool will run somewhat slower, but error messages in case of
     * crashes are more useful than with disabled assertions. The tool should
     * not be run with assertions enabled for performance evaluation.
     */
    public final static Message ASSERTIONS_ENABLED = newMessage().setIdentifier("assertions-enabled").build();
    /**
     * Assertions are disabled.
     * Thus, the tool will run faster than with assertions enabled. However,
     * error messages in case of crashes will be less useful. The tool should be
     * run without assertions enabled for performance evaluation.
     */
    public final static Message ASSERTIONS_DISABLED = newMessage().setIdentifier("assertions-disabled").build();
    /** Prints the SVN revision of EPMC, if built using Maven. */
    public final static Message RUNNING_EPMC_REVISION = newMessage().setIdentifier("running-epmc-revision").build();

    /**
     * Creates a new message with given identifier with this resource bundle.
     * The parameter may not be {@code null}.
     * 
     * @param message identifier of message to be created
     * @return message created
     */
    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_EPMC);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private MessagesEPMC() {
    }
}
