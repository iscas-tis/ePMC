package epmc.dd;

import epmc.messages.Message;

/**
 * Messages specific to the DD module.
 * This class contains only messages independent of DD libraries. Messages which
 * are specific for a given DD library are found in the plugin providing support
 * for that library.
 * 
 * @author Ernst Moritz Hahn
 */
public final class MessagesDD {
    /** Base name of resource file for message description. */
    public final static String MESSAGES_DD = "MessagesDD";
    
    /** Total time spent in routines operating on decision diagrams. */
    public final static Message DD_TOTAL_TIME = newMessage().setIdentifier("dd-total-time").build();
    /** Total time spent converting decision diagrams from one libary to another. */
    public final static Message DD_CONVERSION_TIME = newMessage().setIdentifier("dd-conversion-time").build();

    /**
     * Construct a new message for the DD module.
     * The result will be a message object with the base name set to the one of
     * the DD module and with a name as specified. The parameter may not be
     * {@code null}.
     * 
     * @param message base name of the message
     * @return message constructed
     */
    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_DD);
    }
    
    /**
     * Private constructor to prevent instantiation of this class.
     */
    private MessagesDD() {
    }
}
