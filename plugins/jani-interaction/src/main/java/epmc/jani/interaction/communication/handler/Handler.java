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

package epmc.jani.interaction.communication.handler;

import javax.json.JsonObject;

import epmc.error.EPMCException;

/**
 * Handler for methods of a given type.
 * 
 * @author Ernst Moritz Hahn
 */
public interface Handler {
    /**
     * Get the message type the handler shall be used for.
     * 
     * @return message type the handler shall be used for
     */
    String getType();

    /**
     * Handle the given message.
     * None of the parameters may be {@code null}.
     * This method is used to handle a message sent by a given client. The
     * backend must take care to forward the messages to the correct handler.
     * In case this method throws an {@link EPMCException}, the backend is
     * supposed to close the connection with the client with the error message
     * specified in the exception.
     * 
     * @param client client which sent the message
     * @param message message sent to the backend
     */
    void handle(Object client, JsonObject message);
}
