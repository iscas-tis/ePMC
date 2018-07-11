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

import static epmc.error.UtilError.ensure;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import epmc.error.EPMCException;
import epmc.jani.interaction.Analyse;
import epmc.main.options.UtilOptionsEPMC;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.RawModel;
import epmc.options.Options;
import epmc.plugin.UtilPlugin;

/**
 * JANI RMI server.
 * 
 * @author Ernst Moritz Hahn
 */
public final class JANIServer extends UnicastRemoteObject implements JANIRemote {
    /** serial version ID for this class - 1L as I don't know any better */
    private static final long serialVersionUID = 1L;
    /** name of exit command */
    public final static String EXIT = "exit";

    /**
     * Construct new EPMC server
     * 
     * @param name name of server
     * @param port port on which to start server
     * @throws RemoteException thrown in case of communication failure
     */
    public JANIServer(String name, int port) throws RemoteException {
        super(port);
    }

    @Override
    public void execute(RawModel rawModel, Options options, EPMCChannel channel) throws RemoteException {
        try {
            String commandName = options.getString(Options.COMMAND);
            ensure(commandName != null, ProblemsRemote.REMOTE_NO_COMMAND_GIVEN);
            if (commandName.equals(EXIT)) {
                UnicastRemoteObject.unexportObject(this, true);
                return;
            }
            UtilOptionsEPMC.prepareOptions(options);
            ensure(channel != null, ProblemsRemote.REMOTE_CHANNEL_MISSING);
            UtilPlugin.loadPlugins(options);
            LogJANI log = new LogJANI(options, channel);
            options.set(OptionsMessages.LOG, log);
            Analyse.execute(rawModel, options, log);
        } catch (EPMCException e) {
            channel.send(e);
        }
    }

}
