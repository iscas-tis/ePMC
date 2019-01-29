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

package epmc.automaton.hoa;

import epmc.messages.Message;

public final class MessagesHoa {
    /** Base name of resource bundle for the messages. */
    private final static String MESSAGES_HOA = "MessagesHoa";
    /** Exploring the state space of an automaton product. */
    public final static Message HOA_UNKNOWN_ACC_NAME = newMessage().setIdentifier("hoa-unknown-acc-name").build();

    /**
     * Creates a new message with given identifier with this resource bundle.
     * The parameter may not be {@code null}.
     * 
     * @param message identifier of message to be created
     * @return message created
     */
    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_HOA);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private MessagesHoa() {
    }
}
