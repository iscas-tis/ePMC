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

import java.rmi.RemoteException;

import epmc.modelchecker.RawModel;
import epmc.options.Options;

/**
 * Interface to store several types of server connections.
 * Classes implementing this interface could for instance be processes running
 * on the local machine as well as processes running remotely.
 * The servers this class represents are used to perform one or more analysis
 * for a given client. The servers represented by this class are under the
 * under the control of the client. In particular, the client can start and
 * stop such servers. Thus, this class is <emph>not</emph> meant to represent
 * e.g. webservers which should stay up and be used by several users.
 * It is useful to run analysis processes in separate processes as supported by
 * this class rather than in threads e.g. because
 * <ul>
 * <li>they can be safely terminated before finishing, and</li>
 * <li>they can be run on remote computers.</li>
 * </ul>
 *
 * @author Ernst Moritz Hahn
 */
public interface TaskServer {
    /**
     * Start given server process.
     * The method may not be called several times, not even if
     * {@link #stop} was called afterwards. Instead, a new task server object
     * should be created to restart the server.
     * 
     */
    void start();

    /**
     * Terminate given server process.
     * The method may only be called after {@link #start()} has been called.
     * It may only be called once for a given task server object.
     * 
     */
    void stop();

    /**
     * Obtain interface to execute tasks on server.
     * This method may only be called once {@link #start()} has been called. It
     * may not be called after {@link #stop()} has been called.
     * 
     * @return interface to execute tasks on server.
     */
    JANIRemote getServer();

    /**
     * Execute task on server
     * This method may only be called once {@link #start()} has been called. It
     * may not be called after {@link #stop()} has been called.
     * 
     * @param userOptions options to send to the server
     * @param channel message channel to use
     * @param model model to analyse
     * @param ignoreConnectionErrors whether to silently ignore connection problems
     */
    default void execute(Options userOptions, EPMCChannel channel, RawModel model, boolean ignoreConnectionErrors) {
        assert userOptions != null;
        assert channel != null;
        assert model != null;
        userOptions = userOptions.clone();
        try {
            getServer().execute(model, userOptions, channel);
        } catch (RemoteException e) {
            fail(ProblemsRemote.REMOTE_EXECUTE_REMOTE_EXCEPTION, e.getMessage(), e);
        }
    }
}
