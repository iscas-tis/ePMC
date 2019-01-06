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

package epmc.prism.exporter.messages;

import epmc.messages.Message;

/**
 * Messages used in the PRISM exporter plugin of EPMC.
 * This class only contains static fields and methods, and thus is protected
 * from being instantiated.
 * 
 * @author Andrea Turrini
 */
public final class MessagesPRISMExporter {
    /** Base name of resource bundle for the messages. */
    public final static String MESSAGES_PRISM_EXPORTER = "MessagesPRISMExporter";
    public final static Message PRISM_EXPORTER_MISSING_PRISM_MODEL_FILENAME = newMessage().setIdentifier("prism-exporter-missing-prism-model-filename").build();
    public final static Message PRISM_EXPORTER_MISSING_PRISM_PROPERTIES_FILENAME = newMessage().setIdentifier("prism-exporter-missing-prism-properties-filename").build();
    public final static Message PRISM_EXPORTER_UNWRITABLE_PRISM_MODEL_FILE = newMessage().setIdentifier("prism-exporter-unwritable-prism-model-file").build();
    public final static Message PRISM_EXPORTER_UNWRITABLE_PRISM_PROPERTIES_FILE = newMessage().setIdentifier("prism-exporter-unwritable-prism-properties-file").build();
    public final static Message PRISM_EXPORTER_PRISM_MODEL_CREATION = newMessage().setIdentifier("prism-exporter-prism-model-creation").build();
    public final static Message PRISM_EXPORTER_PRISM_MODEL_CREATION_DONE = newMessage().setIdentifier("prism-exporter-prism-model-creation-done").build();
    public final static Message PRISM_EXPORTER_PRISM_FILE_CREATION = newMessage().setIdentifier("prism-exporter-prism-file-creation").build();
    public final static Message PRISM_EXPORTER_PRISM_FILE_CREATION_DONE = newMessage().setIdentifier("prism-exporter-prism-file-creation-done").build();

    /**
     * Creates a new message with given identifier with this resource bundle.
     * The parameter may not be {@code null}.
     * 
     * @param message identifier of message to be created
     * @return message created
     */
    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_PRISM_EXPORTER);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private MessagesPRISMExporter() {
    }
}
