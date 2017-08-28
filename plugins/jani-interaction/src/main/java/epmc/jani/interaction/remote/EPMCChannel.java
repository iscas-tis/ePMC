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

package epmc.jani.interaction.remote;

import java.rmi.RemoteException;

import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.messages.Message;

/**
 * RMI remote channel to send data back from the server to the client.
 * The data sent back from the server includes exceptions, model checking
 * results, and messages.
 * 
 * @author Ernst Moritz Hahn
 */
public interface EPMCChannel extends java.rmi.Remote {
    // TODO maybe get rid of this function and just always use absolute time
    void setTimeStarted(long time) throws RemoteException;

    void send(EPMCException exception) throws RemoteException;

    void send(String name, JsonValue result) throws RemoteException;    

    /**
     * Send back a status message to the client.
     * 
     * @param time time message was created
     * @param key message type specification
     * @param arguments arguments of message
     * @throws RemoteException thrown in case of connection problems
     */
    void send(long time, Message key, String... arguments) throws RemoteException;
}
