package epmc.jani.exporter.messages;

import epmc.messages.Message;

/**
 * Messages used in the JANI exporter plugin of EPMC.
 * This class only contains static fields and methods, and thus is protected
 * from being instantiated.
 * 
 * @author Andrea Turrini
 */
public final class MessagesJANIExporter {
    /** Base name of resource bundle for the messages. */
    public final static String MESSAGES_JANI_EXPORTER = "MessagesJANIExporter";
    public final static Message JANI_EXPORTER_MISSING_JANI_FILENAME = newMessage().setIdentifier("jani-exporter-missing-jani-filename").build();
    public final static Message JANI_EXPORTER_UNWRITEABLE_JANI_FILE = newMessage().setIdentifier("jani-exporter-unwriteable-jani-file").build();
    public final static Message JANI_EXPORTER_JANI_MODEL_CREATION = newMessage().setIdentifier("jani-exporter-jani-model-creation").build();
    public final static Message JANI_EXPORTER_JANI_MODEL_CREATION_DONE = newMessage().setIdentifier("jani-exporter-jani-model-creation-done").build();
    public final static Message JANI_EXPORTER_JANI_FILE_CREATION = newMessage().setIdentifier("jani-exporter-jani-file-creation").build();
    public final static Message JANI_EXPORTER_JANI_FILE_CREATION_DONE = newMessage().setIdentifier("jani-exporter-jani-file-creation-done").build();
    
    /**
     * Creates a new message with given identifier with this resource bundle.
     * The parameter may not be {@code null}.
     * 
     * @param message identifier of message to be created
     * @return message created
     */
    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_JANI_EXPORTER);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
	private MessagesJANIExporter() {
	}
}
