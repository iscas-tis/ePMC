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

package epmc.prism.messages;

import epmc.messages.Message;

public final class MessagesPRISM {
    private final static String MESSAGES_PRISM = "MessagesPRISM";

    public final static Message BUILDING_DD_MODEL = newMessage().setIdentifier("building-dd-model").build();
    public final static Message BUILDING_DD_MODEL_DONE = newMessage().setIdentifier("building-dd-model-done").build();
    public final static Message EXPLORING = newMessage().setIdentifier("exploring").build();
    public final static Message EXPLORING_DONE = newMessage().setIdentifier("exploring-done").build();
    public final static Message PURE_PROB_WITH_DIR = newMessage().setIdentifier("pure-prob-with-dir").build();
    public final static Message START_PARSING = newMessage().setIdentifier("start-parsing").build();
    public final static Message DONE_PARSING = newMessage().setIdentifier("done-parsing").build();

    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_PRISM);
    }

    private MessagesPRISM() {
    }
}
