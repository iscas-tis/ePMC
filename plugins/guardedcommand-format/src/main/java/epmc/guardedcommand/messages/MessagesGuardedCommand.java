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

package epmc.guardedcommand.messages;

import epmc.messages.Message;

public final class MessagesGuardedCommand {
    private final static String MESSAGES_GUARDEDCOMMAND = "MessagesGuardedCommand";

    public final static Message GUARDEDCOMMAND_BUILDING_DD_MODEL = newMessage().setIdentifier("guardedcommand-building-dd-model").build();
    public final static Message GUARDEDCOMMAND_BUILDING_DD_MODEL_DONE = newMessage().setIdentifier("guardedcommand-building-dd-model-done").build();
    public final static Message GUARDEDCOMMAND_EXPLORING = newMessage().setIdentifier("guardedcommand-exploring").build();
    public final static Message GUARDEDCOMMAND_EXPLORING_DONE = newMessage().setIdentifier("guardedcommand-exploring-done").build();
    public final static Message GUARDEDCOMMAND_PURE_PROB_WITH_DIR = newMessage().setIdentifier("guardedcommand-pure-prob-with-dir").build();

    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_GUARDEDCOMMAND);
    }

    private MessagesGuardedCommand() {
    }
}
