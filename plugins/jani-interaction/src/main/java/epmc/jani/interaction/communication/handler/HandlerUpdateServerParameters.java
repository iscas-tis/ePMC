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

import static epmc.error.UtilError.ensure;

import java.math.BigInteger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.jani.interaction.communication.Backend;
import epmc.jani.interaction.communication.ClientInfo;
import epmc.jani.interaction.error.ProblemsJANIInteraction;
import epmc.options.Option;
import epmc.options.Options;
import epmc.util.UtilJSON;

/**
 * Handler for UpdateServerParameters messages.
 * 
 * @author Ernst Moritz Hahn
 */
public final class HandlerUpdateServerParameters implements Handler {
    public final static String TYPE = "server-parameters";
    private final static String MESSAGE_TYPE = "type";
    private final static String MESSAGE_TYPE_REPLY_SERVER_PARAMETERS = "server-parameters";
    private final static String ID = "id";
    private final static String PARAMETERS = "parameters";
    private final static String VALUE = "value";
    private final static String SUCCESS = "success";
    private final static String ERROR = "error";
    private final static String IDENTIFIER_PATTERN = "[_a-zA-Z][_a-zA-Z0-9\\-]*";

    private final Backend backend;


    public HandlerUpdateServerParameters(Backend backend) {
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
        ensure(backend.clientLoggedIn(client), ProblemsJANIInteraction.JANI_INTERACTION_NOT_LOGGED_IN);
        BigInteger id = UtilJSON.getBigInteger(object, ID);
        ClientInfo clientO = backend.getClientData(client);
        Options clientOptions = clientO.getOptions();
        JsonArray parameters = UtilJSON.getArrayObject(object, PARAMETERS);
        EPMCException problem = null;
        for (JsonObject parameterObject : parameters.getValuesAs(JsonObject.class)) {
            String parameterId = UtilJSON.getMatch(parameterObject, ID, IDENTIFIER_PATTERN);
            parameterId = parameterId.replace('_', '-');
            JsonValue parameterValue = UtilJSON.get(parameterObject, VALUE);
            Option option = clientOptions.getOption(parameterId);
            ensure(option != null, ProblemsJANIInteraction.JANI_INTERACTION_INVALID_OPTION, parameterId);
            ensure(option.isWeb() || backend.isStdio() && option.isGUI(),
                    ProblemsJANIInteraction.JANI_INTERACTION_CHANGE_OPTION_FORBIDDEN, parameterId);
            try {
                option.reset();
                option.parse(parameterValue.toString());
            } catch (EPMCException e) {
                problem = e;
                break;
            }
        }
        JsonObjectBuilder reply = Json.createObjectBuilder()
                .add(MESSAGE_TYPE, MESSAGE_TYPE_REPLY_SERVER_PARAMETERS)
                .add(ID, id)
                .add(SUCCESS, problem == null);
        if (problem != null) {
            reply.add(ERROR, backend.buildUserErrorMessage(problem));
        }
        backend.send(client, reply.build());
    }

}
