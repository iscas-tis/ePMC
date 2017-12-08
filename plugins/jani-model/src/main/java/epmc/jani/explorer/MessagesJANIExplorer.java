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

package epmc.jani.explorer;

import epmc.messages.Message;

/**
 * Messages used in the explorer part of JANI.
 * This class only contains static fields and methods, and thus is protected
 * from being instantiated.
 * 
 * @author Ernst Moritz Hahn
 */
public final class MessagesJANIExplorer {
    /** Base name of resource bundle for the messages. */
    private final static String MESSAGES_JANI_EXPLORER = "MessagesJANIExplorer";    
    public final static Message START_BUILDING_EXPLORER = newMessage().setIdentifier("start-building-explorer").build();
    public final static Message DONE_BUILDING_EXPLORER = newMessage().setIdentifier("done-building-explorer").build();
    public final static Message START_BUILDING_INITIAL_STATES_EXPLORER = newMessage().setIdentifier("start-building-initial-states-explorer").build();
    public final static Message DONE_BUILDING_INITIAL_STATES_EXPLORER = newMessage().setIdentifier("done-building-initial-states-explorer").build();

    /**
     * Creates a new message with given identifier with this resource bundle.
     * The parameter may not be {@code null}.
     * 
     * @param message identifier of message to be created
     * @return message created
     */
    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_JANI_EXPLORER);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private MessagesJANIExplorer() {
    }
}
