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
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.jani.interaction.communication.Backend;
import epmc.jani.interaction.error.ProblemsJANIInteraction;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Option;
import epmc.options.OptionTypeMap;
import epmc.options.Options;
import epmc.util.UtilJSON;

// TODO check names when online again.
/**
 * Handler for QueryAnalysisEngines messages.
 * 
 * @author Ernst Moritz Hahn
 */
public final class HandlerQueryAnalysisEngines implements Handler {
    /** Type of messages this handler handles. */
    public final static String TYPE = "analysis-engines";
    private final static String ID = "id";
    /** Type field of JANI messages. */
    private final static String MESSAGE_TYPE = "type";
    /** Reply about analysis engines message type identifier. */
    private final static String MESSAGE_TYPE_REPLY_ANALYSIS_ENGINES = "analysis-engines";
    private final static String REPLY_ANALYSIS_ENGINES_ENGINES = "engines";
    private final static String NAME = "name";
    /** String denoting major version. */
    private final static String VERSION_MAJOR = "major";
    /** String denoting minor version. */
    private final static String VERSION_MINOR = "minor";
    /** String denoting revision version. */
    private final static String VERSION_REVISION = "revision";
    private final static String VERSION = "version";

    /** Backend in which this handler is used. */
    private final Backend backend;
    private final JsonValue engines;

    public HandlerQueryAnalysisEngines(Backend backend) {
        assert backend != null;
        this.backend = backend;
        engines = buildJsonEngines();
    }

    @Override
    public String getType() {
        return TYPE;
    }

    private JsonValue buildJsonEngines() {
        Option engines = Options.get().getOption(OptionsModelChecker.ENGINE);
        OptionTypeMap<Class<?>> optionType = engines.getType();
        JsonArrayBuilder result = Json.createArrayBuilder();
        for (String id : optionType.getKeys()) {
            JsonObjectBuilder engine = Json.createObjectBuilder();
            engine.add(ID, id);
            engine.add(NAME, id);
            JsonObjectBuilder version = Json.createObjectBuilder()
                    .add(VERSION_MAJOR, 0)
                    .add(VERSION_MINOR, 0)
                    .add(VERSION_REVISION, 0);
            engine.add(VERSION, version);
            result.add(engine);
        }
        return result.build();
    }

    @Override
    public void handle(Object client, JsonObject object) {
        assert client != null;
        assert object != null;
        ensure(backend.clientLoggedIn(client),
                ProblemsJANIInteraction.JANI_INTERACTION_NOT_LOGGED_IN);
        BigInteger id = null;
        id = UtilJSON.getBigInteger(object, ID);
        JsonObjectBuilder reply = Json.createObjectBuilder();
        reply.add(MESSAGE_TYPE, MESSAGE_TYPE_REPLY_ANALYSIS_ENGINES);
        reply.add(ID, id);
        reply.add(REPLY_ANALYSIS_ENGINES_ENGINES, this.engines);
        backend.send(client, reply.build());
    }
}
