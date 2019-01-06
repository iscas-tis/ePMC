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

package epmc.qmc.exporter.messages;

import epmc.messages.Message;

public final class MessagesQMCExporter {
    private final static String MESSAGES_QMC_EXPORTER = "MessagesQMCExporter";

    public final static Message QMC_EXPORTER_UNKNOWN_EXPORT_TO_TARGET = newMessage().setIdentifier("qmc-exporter-unknown-export-to-target").build();

    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_QMC_EXPORTER);
    }

    private MessagesQMCExporter() {
    }
}
