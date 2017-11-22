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

/**
 * Stores the name and port of an RMI server.
 * 
 * @author Ernst Moritz Hahn
 */
public final class RMIConnectionData {
    /** name of RMI server */
    private final String name;
    /** port of RMI server */
    private final int port;

    /**
     * Constructs new data set of RMI connection data.
     * The server name must not be {@code null}. The port must be a positive
     * integer. Value 0 is not allowed, because the port must have indeed been
     * fixed at this point.
     * 
     * @param name name to store
     * @param port port to store
     */
    RMIConnectionData(String name, int port) {
        assert name != null;
        assert port > 0;
        this.name = name;
        this.port = port;
    }

    /**
     * Obtain server name.
     * 
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Obtain port number.
     * 
     * @return port number
     */
    public int getPort() {
        return port;
    }
}
