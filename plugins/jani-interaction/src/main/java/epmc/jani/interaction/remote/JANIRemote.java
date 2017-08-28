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

import epmc.modelchecker.RawModel;
import epmc.options.Options;

/**
 * RMI interface for EPMC server.
 * 
 * @author Ernst Moritz Hahn
 */
public interface JANIRemote extends java.rmi.Remote {
    /**
     * Execute task on EPMC server.
     * 
     * @param model unparsed model to check
     * @param options options used
     * @param channel channel to send information back to client
     * @return result of model checking process
     * @throws RemoteException thrown in case of connection problems
     */
    public void execute
    (RawModel model, Options options, EPMCChannel channel)
            throws RemoteException;
}
