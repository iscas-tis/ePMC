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

package epmc.command;

import epmc.messages.Message;

/**
 * @author Ernst Moritz Hahn
 */
public final class MessagesCommandCheck {
    /** Base name of resource file for message description. */
    public final static String MESSAGES_COMMAND_CHECK = "MessagesCommandCheck";
    
    public final static Message COMMAND_CHECK_RESULT_IS = newMessage().setIdentifier("command-check-result-is").build();

    
    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_COMMAND_CHECK);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private MessagesCommandCheck() {
    }
}
