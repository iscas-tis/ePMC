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
