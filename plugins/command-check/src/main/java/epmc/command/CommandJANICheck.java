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

package epmc.command;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.jani.interaction.commandline.CommandJANIClient;
import epmc.jani.interaction.commandline.CommandLineOptions;
import epmc.jani.interaction.communication.BackendInterface;
import epmc.util.UtilJSON;

public class CommandJANICheck implements CommandJANIClient {
    public static final class Builder implements CommandJANIClient.Builder {
        private Object client;
        private BackendInterface backend;
        private CommandLineOptions options;
        private String[] args;

        @Override
        public Builder setClient(Object client) {
            this.client = client;
            return this;
        }

        @Override
        public Builder setBackend(BackendInterface backend) {
            this.backend = backend;
            return this;
        }

        @Override
        public Builder setOptions(CommandLineOptions options) {
            this.options = options;
            return this;
        }

        @Override
        public Builder setArgs(String[] args) {
            this.args = args;
            return this;
        }

        @Override
        public CommandJANIClient build() {
            return new CommandJANICheck(this);
        }
    }
    
    public final static String IDENTIFIER = "check";
    private final static String JANI_VERSIONS = "jani-versions";
    private final static int JANI_VERSION = 1;
    private final static String X_EPMC_CLIENT = "x-epmc-client";
    private final static String PARAMETERS = "parameters";
    private final static String X_PRECISE_CATEGORIES = "x-precise-categories";
    private final static String TYPE = "type";
    private final static String CAPABILITIES = "capabilities";
    private final static String SERVER_PARAMETERS = "server-parameters";
    private final static String SUCCESS = "success";
    private final static String ERROR = "error";
    private final static String ANALYSIS_ENGINES = "analysis-engines";
    private final static String ENGINES = "engines";
    private final BackendInterface backend;
    private final CommandLineOptions options;
    private final String[] args;
    private long id;

    private CommandJANICheck(Builder builder) {
        assert builder != null;
        this.options = builder.options;
        JsonObjectBuilder request = Json.createObjectBuilder();
        request.add(JANI_VERSIONS, Json.createArrayBuilder().add(JANI_VERSION));
        request.add(X_EPMC_CLIENT, true);
        builder.backend.sendToBackend(builder.client, request.build());
        backend = builder.backend;
        args = builder.args;
    }

    @Override
    public void sendToClient(Object client, JsonValue message) {
        JsonObject value = UtilJSON.toObject(message);
        switch (UtilJSON.getString(value, TYPE)) {
        case CAPABILITIES:
            parseCapabilities(client, value);
            sendUpdateRequest(client);
            break;
        case SERVER_PARAMETERS:
            parseServerParameters(client, value);
            sendEnginesRequest(client);
            break;
        case ANALYSIS_ENGINES:
            parseAnalysisEngines(client, value);
            break;
        }
//        options.get
//        System.out.println(UsagePrinterJANI.getUsage(options));
    }
    
    private void parseCapabilities(Object client, JsonObject value) {
        JsonArray parameters = UtilJSON.getArray(value, PARAMETERS);
        JsonArray preciseCategories = UtilJSON.getArrayOrNull(value, X_PRECISE_CATEGORIES);
        // TODO check whether server has analyse capabilities
        options.clearValues();
        options.parsePreciseCategories(preciseCategories);
        options.parseOptions(parameters);
    }


    private void sendUpdateRequest(Object client) {
        options.setIgnoreUnknownOptions(false);
        options.parse(args);
        backend.sendToBackend(client, options.getUpdateRequest(getNextId()));
    }

    private void parseServerParameters(Object client, JsonObject value) {
        // TODO check ID
        boolean success = UtilJSON.getBoolean(value, SUCCESS);
        if (!success) {
            System.err.println(UtilJSON.getString(value, ERROR));
            System.exit(1);
        }
    }
    
    private void sendEnginesRequest(Object client) {
        JsonObjectBuilder value = Json.createObjectBuilder();
        value.add(TYPE, ANALYSIS_ENGINES);
        value.add(IDENTIFIER, getNextId());
        backend.sendToBackend(client, value.build());
    }

    private void parseAnalysisEngines(Object client, JsonObject value) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void logOff(Object who) {
        // TODO Auto-generated method stub
        
    }
    
    private long getNextId() {
        id++;
        return id;
    }
}
