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

package epmc.jani.interaction.communication;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import epmc.jani.interaction.options.OptionsJANIInteraction;
import epmc.options.Options;

public final class GUI {
    private final static String ADDRESS_LOCALHOST = "http://localhost:";

    public static void startGUI(Options options) {
        assert options != null;
        int port = options.getInteger(OptionsJANIInteraction.JANI_INTERACTION_WEBSOCKET_SERVER_PORT);
        try {
            Desktop.getDesktop().browse(new URI(ADDRESS_LOCALHOST + port));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
