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

public class CommandJANIHelp implements CommandJANIClient {
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
            return new CommandJANIHelp(this);
        }

        
    }
    
    public final static String IDENTIFIER = "help";
    private final static String JANI_VERSIONS = "jani-versions";
    private final static int JANI_VERSION = 1;
    private final static String X_EPMC_CLIENT = "x-epmc-client";
    private final static String PARAMETERS = "parameters";
    private final static String X_PRECISE_CATEGORIES = "x-precise-categories";
    private final CommandLineOptions options;
    private final String[] args;

    private CommandJANIHelp(Builder builder) {
        assert builder != null;
        this.options = builder.options;
        JsonObjectBuilder request = Json.createObjectBuilder();
        request.add(JANI_VERSIONS, Json.createArrayBuilder().add(JANI_VERSION));
        request.add(X_EPMC_CLIENT, true);
        builder.backend.sendToBackend(builder.client, request.build());
        args = builder.args;
    }

    @Override
    public void sendToClient(Object client, JsonValue message) {
        JsonObject value = UtilJSON.toObject(message);
        JsonArray parameters = UtilJSON.getArray(value, PARAMETERS);
        JsonArray preciseCategories = UtilJSON.getArrayOrNull(value, X_PRECISE_CATEGORIES);
        options.clearValues();
        options.parsePreciseCategories(preciseCategories);
        options.parseOptions(parameters);
        options.setIgnoreUnknownOptions(false);
        options.parse(args);
        System.out.println(UsagePrinterJANI.getUsage(options));
    }

    @Override
    public void logOff(Object who) {
        // TODO Auto-generated method stub
        
    }
}
