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

package epmc.jani.interaction.messages;

import epmc.messages.Message;

/**
 * Messages used in the JANI interaction plugin of EPMC.
 * This class only contains static fields and methods, and thus is protected
 * from being instantiated.
 * 
 * @author Ernst Moritz Hahn
 */
public final class MessagesJANIInteraction {
    /** Base name of resource bundle for the messages. */
    public final static String MESSAGES_JANI_INTERACTION = "MessagesJANIInteraction";
    public final static Message JANI_INTERACTION_SERVER_STARTED = newMessage().setIdentifier("jani-interaction-server-started").build();
    public final static Message JANI_INTERACTION_SERVER_STOPPED = newMessage().setIdentifier("jani-interaction-server-stopped").build();
    public final static Message JANI_INTERACTION_SENT_TO_SERVER = newMessage().setIdentifier("jani-interaction-sent-to-server").build();
    public final static Message JANI_INTERACTION_SENT_BY_SERVER = newMessage().setIdentifier("jani-interaction-sent-by-server").build();

    /**
     * Creates a new message with given identifier with this resource bundle.
     * The parameter may not be {@code null}.
     * 
     * @param message identifier of message to be created
     * @return message created
     */
    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_JANI_INTERACTION);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private MessagesJANIInteraction() {
    }
}
