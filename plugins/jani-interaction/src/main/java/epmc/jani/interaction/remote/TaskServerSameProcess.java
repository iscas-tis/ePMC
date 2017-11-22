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

import static epmc.error.UtilError.fail;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Task server which runs in the same process as the one who starts it.
 * Running the task server in the same process as the starting process means
 * that it cannot be easily stopped and might also crash the client. Thus, it
 * should only be used for testing, or for the command line, in which anyway no
 * further reaction is possible after a crash.
 * 
 * @author Ernst Moritz Hahn
 */
public final class TaskServerSameProcess implements TaskServer {
    /** Remote server object for this task server. */
    private JANIServer server;

    @Override
    public void start() {
        try {
            server = new JANIServer(null, 0);
        } catch (RemoteException e) {
            fail(ProblemsRemote.REMOTE, e);
        }
    }

    @Override
    public void stop() {
        try {
            UnicastRemoteObject.unexportObject(server, true);
        } catch (NoSuchObjectException e) {
            // we don't care
        }
    }

    @Override
    public JANIRemote getServer() {
        assert server != null;
        return server;
    }
}
