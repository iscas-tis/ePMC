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

package epmc.jani.interaction.command;

import static epmc.error.UtilError.*;

import java.io.IOException;
import java.net.BindException;

import epmc.jani.interaction.communication.GUI;
import epmc.jani.interaction.communication.Server;
import epmc.jani.interaction.communication.StandardStream;
import epmc.jani.interaction.error.ProblemsJANIInteraction;
import epmc.jani.interaction.messages.MessagesJANIInteraction;
import epmc.jani.interaction.options.JANIInteractionIO;
import epmc.jani.interaction.options.OptionsJANIInteraction;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.CommandTask;
import epmc.modelchecker.Log;
import epmc.modelchecker.ModelChecker;
import epmc.options.Options;

/**
 * Command to start JANI interaction mode.
 * 
 * @author Ernst Moritz Hahn
 */
public final class CommandTaskJaniInteractionStartServer implements CommandTask {
    /** Integer value to denote no timeout. */
    private final static int NO_TIMEOUT = 0;
    /** Unique identifier of JANI interaction server start command. */
    public final static String IDENTIFIER = "jani-interaction-start-server";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
    }

    @Override
    public void executeInClientBeforeServer() {
        JANIInteractionIO type = Options.get().get(OptionsJANIInteraction.JANI_INTERACTION_TYPE);
        switch (type) {
        case STDIO:
            StandardStream standard = new StandardStream(System.in);
            standard.start();
            break;
        case WEBSOCKETS:
            Server server = new Server();
            try {
                server.start(NO_TIMEOUT);
            } catch (BindException e) {
                fail(ProblemsJANIInteraction.JANI_INTERACTION_SERVER_BIND_FAILED,
                        Options.get().getInteger(OptionsJANIInteraction.JANI_INTERACTION_WEBSOCKET_SERVER_PORT));
            } catch (IOException e) {
                fail(ProblemsJANIInteraction.JANI_INTERACTION_SERVER_IO_PROBLEM,
                        Options.get().getInteger(OptionsJANIInteraction.JANI_INTERACTION_WEBSOCKET_SERVER_PORT));
            }
            getLog().send(MessagesJANIInteraction.JANI_INTERACTION_SERVER_STARTED,
                    Options.get().getInteger(OptionsJANIInteraction.JANI_INTERACTION_WEBSOCKET_SERVER_PORT));
            if (Options.get().getBoolean(OptionsJANIInteraction.JANI_INTERACTION_START_GUI)) {
                GUI.startGUI(Options.get());
            }
            try {
                System.in.read();
            } catch (IOException e) {
            }
            server.stop();
            getLog().send(MessagesJANIInteraction.JANI_INTERACTION_SERVER_STOPPED);
            break;
        default:
            assert false;
            break;		
        }
        // TODO Auto-generated method stub
    }

    @Override
    public boolean isRunOnServer() {
        return false;
    }
    
    private Log getLog() {
        return Options.get().get(OptionsMessages.LOG);
    }
}
