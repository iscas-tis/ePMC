/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

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
    public static final String MESSAGES_JANI_EXPORTER = "MessagesJANIExporter";
    public static final Message JANI_EXPORTER_MISSING_JANI_FILENAME = newMessage().setIdentifier("jani-exporter-missing-jani-filename").build();
    public static final Message JANI_EXPORTER_UNWRITABLE_JANI_FILE = newMessage().setIdentifier("jani-exporter-unwritable-jani-file").build();
    public static final Message JANI_EXPORTER_ALREADY_EXISTING_JANI_FILE_ABORT = newMessage().setIdentifier("jani-exporter-already-existing-jani-file-abort").build();
    public static final Message JANI_EXPORTER_ALREADY_EXISTING_JANI_FILE_HELP = newMessage().setIdentifier("jani-exporter-already-existing-jani-file-help").build();
    public static final Message JANI_EXPORTER_ALREADY_EXISTING_JANI_FILE_OVERWRITE = newMessage().setIdentifier("jani-exporter-already-existing-jani-file-overwrite").build();
    public static final Message JANI_EXPORTER_JANI_MODEL_CREATION = newMessage().setIdentifier("jani-exporter-jani-model-creation").build();
    public static final Message JANI_EXPORTER_JANI_MODEL_CREATION_DONE = newMessage().setIdentifier("jani-exporter-jani-model-creation-done").build();
    public static final Message JANI_EXPORTER_JANI_FILE_CREATION = newMessage().setIdentifier("jani-exporter-jani-file-creation").build();
    public static final Message JANI_EXPORTER_JANI_FILE_CREATION_DONE = newMessage().setIdentifier("jani-exporter-jani-file-creation-done").build();

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
