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

package epmc.filter.messages;

import epmc.messages.Message;

public final class MessagesFilter {
    private final static String MESSAGES_FILTER = "MessagesFilter";
    public final static Message NUM_STATES_IN_FILTER = newMessage().setIdentifier("num-states-in-filter").build();
    public final static Message PRINT_FILTER = newMessage().setIdentifier("print-filter").build();
    public final static Message PRINTING_FILTER_RESULTS = newMessage().setIdentifier("printing-filter-results").build();
    public final static Message PRINTING_ALL_FILTER_RESULTS = newMessage().setIdentifier("printing-all-filter-results").build();

    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_FILTER);
    }

    private MessagesFilter() {
    }
}
