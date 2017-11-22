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
import java.rmi.NoSuchObjectException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import epmc.error.EPMCException;
import epmc.jani.interaction.communication.Backend;
import epmc.jani.interaction.communication.ClientInfo;
import epmc.jani.interaction.options.OptionsJANIInteraction;
import epmc.jani.interaction.options.OptionsJANIInteraction.ServerType;
import epmc.jani.interaction.remote.TaskServer;
import epmc.jani.interaction.remote.TaskServerLocal;
import epmc.jani.interaction.remote.TaskServerSameProcess;
import epmc.modelchecker.RawModel;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.util.UtilJSON;

/**
 * Handler for StartAnalysis messages.
 * 
 * @author Ernst Moritz Hahn
 */
public final class HandlerStartAnalysis implements Handler {
    public final static String TYPE = "analysis-start";
    private final static String MESSAGE_TYPE = "type";
    private final static String ANALYSIS_END = "analysis-end";
    private final static String ID = "id";
    private final static String ENGINE = "engine";
    private final static String MODEL = "model";
    private static final String PROPERTIES = "properties";
    private static final String NAME = "name";
    private final static String CHECK = "check";

    private final Backend backend;

    public HandlerStartAnalysis(Backend backend) {
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
        BigInteger id;
        ClientInfo clientDescription = backend.getClientData(client);
        if (clientDescription == null) {
            // TODO
            return;
        }
        id = UtilJSON.getBigInteger(object, ID);
        if (clientDescription.containsAnalysis(id)) {
            // TODO
            return;
        }
        Options enOptions = clientDescription.getOptions();
        String engine = UtilJSON.getIdentifier(object, ENGINE);
        enOptions.getOption(OptionsModelChecker.ENGINE).reset();
        enOptions.getOption(OptionsModelChecker.ENGINE).parse(engine);
        JsonValue model = UtilJSON.getObject(object, MODEL);
        final RawModel rawModel;

        if (model.getValueType() == ValueType.OBJECT) {
            rawModel = prepareJANIModel(model, object);
        } else if (model.getValueType() == ValueType.STRING) {
            assert false;
            // TODO
            rawModel = null;
        } else {
            rawModel = null;
        }
        startTask(rawModel, clientDescription, id);
    }

    private void startTask(RawModel rawModel, ClientInfo clientDescription, BigInteger id) {
        assert rawModel != null;
        assert clientDescription != null;
        assert id != null;
        Object client = clientDescription.getClient();
        Options options = Options.get();
        Options userOptions = clientDescription.getOptions().clone();
        userOptions.set(Options.COMMAND, CHECK);
        final TaskServer server;
        try {
            OptionsJANIInteraction.ServerType serverType = options.get(OptionsJANIInteraction.JANI_INTERACTION_ANALYSIS_SERVER_TYPE);
            if (serverType == ServerType.SAME_PROCESS) {
                server = new TaskServerSameProcess();
                server.start();
            } else if (serverType == ServerType.LOCAL) {
                server = new TaskServerLocal();
                server.start();
            } else {
                assert false : serverType;
            server = null;
            }
            clientDescription.registerAnalysis(id, server);
        } catch (EPMCException e) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                JANIChannel channel = JANIChannel.newChannel(backend, client, id);
                try {
                    // TODO the following is not really thread-safe
                    if (clientDescription.containsAnalysis(id)) {
                        server.execute(userOptions, channel, rawModel, false);
                        sendAnalysisEnd(id);
                        clientDescription.stopAnalysis(id);						
                    }
                } catch (EPMCException e) {
                    clientDescription.stopAnalysis(id);
                }
                try {
                    UnicastRemoteObject.unexportObject(channel, true);
                } catch (NoSuchObjectException e1) {
                    /* We don't care. */
                }
            }

            private void sendAnalysisEnd(BigInteger id) {
                assert id != null;
                JsonValue message = Json.createObjectBuilder()
                        .add(MESSAGE_TYPE, ANALYSIS_END)
                        .add(ID, id)
                        .build();
                backend.send(client, message);
            }
        }).start();
    }

    private RawModel prepareJANIModel(JsonValue model, JsonObject message) {
        assert model != null;
        assert message != null;
        //		Set<String> includePropertiesNames = buildIncludePropertiesNams(message);
        JsonObject modelObject = UtilJSON.toObject(model);
        //		JsonArray includedModelProperties = buildIncludeProperties(modelObject, includePropertiesNames);
        //	JsonObject newModel = rebuildModel(modelObject, includedModelProperties);

        return new RawModelByteArray(new byte[][]{modelObject.toString().getBytes()}, null);
    }

    private Set<String> buildIncludePropertiesNams(JsonObject message) {
        assert message != null;
        JsonArray properties = UtilJSON.getArrayString(message, PROPERTIES);
        Set<String> includeProperties = new HashSet<>();
        for (JsonString string : properties.getValuesAs(JsonString.class)) {
            includeProperties.add(string.getString());
        }
        return includeProperties;
    }

    private JsonArray buildIncludeProperties(JsonObject model, Set<String> include) {
        assert model != null;
        assert include != null;
        JsonArray allModelProperties = UtilJSON.getArrayObject(model, PROPERTIES);
        JsonArrayBuilder includedModelProperties = Json.createArrayBuilder();
        for (JsonObject property : allModelProperties.getValuesAs(JsonObject.class)) {
            String name = UtilJSON.getIdentifier(property, NAME);
            if (!include.contains(name)) {
                continue;
            }
            includedModelProperties.add(property);
        }
        return includedModelProperties.build();
    }

    private JsonObject rebuildModel(JsonObject model, JsonArray newProperties) {
        assert model != null;
        assert newProperties != null;
        JsonObjectBuilder newModel = Json.createObjectBuilder();
        for (Entry<String, JsonValue> entry : model.entrySet()) {
            if (entry.equals(PROPERTIES)) {
                continue;
            }
            newModel.add(entry.getKey(), entry.getValue());
        }
        newModel.add(PROPERTIES, newProperties);
        return newModel.build();
    }
}
