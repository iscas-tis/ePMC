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

package epmc.graph;

import epmc.messages.Message;

public final class MessagesGraph {
    public static final String MESSAGES_GRAPH = "MessagesGraph";

    public final static Message CONVERTING_DD_GRAPH_TO_EXPLICIT = newMessage().setIdentifier("converting-dd-graph-to-explicit").build();
    public final static Message CONVERTING_DD_GRAPH_TO_EXPLICIT_DONE = newMessage().setIdentifier("converting-dd-graph-to-explicit-done").build();
    public static final Message BUILD_MODEL_START = newMessage().setIdentifier("build-model-start").build();
    public static final Message BUILD_MODEL_STATES_EXPLORED = newMessage().setIdentifier("build-model-states-explored").build();
    public static final Message BUILD_MODEL_NEXT_PHASE = newMessage().setIdentifier("build-model-next-phase").build();
    public static final Message BUILD_MODEL_DONE = newMessage().setIdentifier("build-model-done").build();

    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_GRAPH);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private MessagesGraph() {
    }
}
