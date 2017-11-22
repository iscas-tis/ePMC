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

import java.math.BigInteger;

import javax.json.JsonObject;

import epmc.error.EPMCException;
import epmc.jani.interaction.communication.Backend;
import epmc.jani.interaction.communication.ClientInfo;
import epmc.util.UtilJSON;

/**
 * Handler for StopAnalysis messages.
 * 
 * @author Ernst Moritz Hahn
 */
public final class HandlerStopAnalysis implements Handler {
    public final static String TYPE = "analysis-stop";
    private final static String ID = "id";

    private final Backend backend;

    public HandlerStopAnalysis(Backend backend) {
        assert backend != null;
        this.backend = backend;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void handle(Object client, JsonObject object) {
        assert client != null;
        assert object != null;
        ClientInfo clientDescription = backend.getClientData(client);
        if (clientDescription == null) {
            return;
        }
        BigInteger id = null;
        try {
            id = UtilJSON.getBigInteger(object, ID);
        } catch (EPMCException e) {
            return;
        }
        clientDescription.stopAnalysis(id);

        // TODO
    }
}
