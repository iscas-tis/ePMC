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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.jani.interaction.UserManager;
import epmc.jani.interaction.communication.Backend;
import epmc.jani.interaction.communication.ClientInfo;
import epmc.jani.interaction.database.Database;
import epmc.jani.interaction.error.ProblemsJANIInteraction;
import epmc.jani.interaction.options.OptionsJANIInteraction;
import epmc.options.Category;
import epmc.options.Option;
import epmc.options.OptionType;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeEnum;
import epmc.options.OptionTypeInteger;
import epmc.options.OptionTypeMap;
import epmc.options.OptionTypeReal;
import epmc.options.Options;
import epmc.util.UtilJSON;

/**
 * Handler for Authenticate messages.
 * 
 * @author Ernst Moritz Hahn
 */
public final class HandlerAuthenticate implements Handler {
    private final static boolean ADD_EXTENDED_INFO = true;
    /** Type of messages this handler handles. */
    public final static String TYPE = "authenticate";
    private final static String AUTHENTICATE_LOGIN = "login";
    private final static String AUTHENTICATE_PASSWORD = "password";
    private final static String JANI_VERSIONS = "jani-versions";
    private final static String JANI_VERSION = "jani-version";
    private final static String CAPABILITIES = "capabilities";
    private final static String CAPABILITIES_EXTENSIONS = "extensions";
    private final static String CAPABILITIES_METADATA = "metadata";
    private final static String CAPABILITIES_METADATA_NAME = "name";
    private final static String CAPABILITIES_METADATA_VERSION = "version";
    private final static String CAPABILITIES_PARAMETERS = "parameters";
    private final static String CAPABILITIES_ROLES = "roles";
    private final static String CAPABILITIES_ROLES_ANALYSE = "analyse";
    private final static String CAPABILITIES_ROLES_TRANSFORM = "transform";
    private final static String ID = "id";
    private final static String NAME = "name";
    private final static String IS_GLOBAL = "is-global";
    private final static String OPTION_TYPE = "type";
    private final static String DEFAULT_VALUE = "default-value";
    private final static String OPTION_TYPE_BOOL = "bool";
    private final static String OPTION_TYPE_INT = "int";
    private final static String OPTION_TYPE_REAL = "real";
    private final static String OPTION_TYPE_STRING = "string";
    private final static String OPTION_X_PRECISE_TYPE = "x-precise-type";
    private final static String OPTION_X_PRECISE_CATEGORY = "x-precise-category";
    private final static String ENGINE = "engine";
    private static final String CATEGORY = "category";
    private final static String EXTENSIONS = "extensions";
    private final static String X_PRECISE_CATEGORIES = "x-precise-categories";
    private final static String CAT_IDENTIFIER = "id";
    private final static String CAT_DESCRIPTION = "name";
    private final static String CAT_PARENT = "parent";

    /**
     * Option to be excluded.
     * Options are excluded for instance because they are can be set by other
     * mechanisms.
     */
    private final static Set<String> EXCLUDE_OPTIONS;
    static {
        Set<String> excludeOptions = new LinkedHashSet<>();
        excludeOptions.add(ENGINE);
        EXCLUDE_OPTIONS = Collections.unmodifiableSet(excludeOptions);
    }

    private final JsonValue capabilitiesParameters;
    private final JsonArray extendedCategories;
    /** Backend in which this handler is used. */
    private final Backend backend;
    private final UserManager userManager;

    public HandlerAuthenticate(Backend backend) {
        assert backend != null;
        this.backend = backend;
        capabilitiesParameters = buildCapabilitiesParameters();
        extendedCategories = ADD_EXTENDED_INFO
                ? buildExtendedCategories() : null;
        Database authentification = backend.getPermanentStorage();
        userManager = new UserManager(authentification);
    }

    private JsonArray buildExtendedCategories() {
        JsonArrayBuilder result = Json.createArrayBuilder();
        for (Category category : Options.get().getAllCategories().values()) {
            JsonObjectBuilder categoryObject = Json.createObjectBuilder();
            categoryObject.add(CAT_IDENTIFIER, category.getIdentifier());
            categoryObject.add(CAT_DESCRIPTION, category.getShortDescription());
            if (category.getParent() != null) {
                categoryObject.add(CAT_PARENT, category.getParent().getIdentifier());
            }
            result.add(categoryObject);
        }
        return result.build();
    }

    @Override
    public String getType() {
        return TYPE;
    }

    private JsonValue buildCapabilitiesParameters() {
        assert backend != null;
        JsonArrayBuilder result = Json.createArrayBuilder();
        for (Option option : Options.get().getAllOptions().values()) {
            if (!(option.isGUI() && backend.isStdio())
                    && !option.isWeb()) {
                continue;
            }
            if (EXCLUDE_OPTIONS.contains(option.getIdentifier())) {
                continue;
            }
            JsonObjectBuilder parameter = Json.createObjectBuilder();
            parameter.add(ID, option.getIdentifier());
            parameter.add(NAME, option.getShortDescription());
            parameter.add(IS_GLOBAL, option.isWeb());
            parameter.add(OPTION_TYPE, buildType(option.getType()));
            if (ADD_EXTENDED_INFO) {
                parameter.add(OPTION_X_PRECISE_TYPE, option.getTypeInfo());
            }
            parameter.add(DEFAULT_VALUE, buildDefault(option.getType(), option.getDefault()));
            if (option.getCategory() != null) {
                parameter.add(CATEGORY, option.getCategory().getShortDescription());
            }
            if (ADD_EXTENDED_INFO && option.getCategory() != null) {
                parameter.add(OPTION_X_PRECISE_CATEGORY, option.getCategory().getIdentifier());
            }
            result.add(parameter);
        }
        return result.build();
    }

    private JsonValue buildType(OptionType type) {
        assert type != null;
        if (type instanceof OptionTypeBoolean) {
            return UtilJSON.toStringValue(OPTION_TYPE_BOOL);
        } else if (type instanceof OptionTypeInteger) {
            return UtilJSON.toStringValue(OPTION_TYPE_INT);			
        } else if (type instanceof OptionTypeReal) {
            return UtilJSON.toStringValue(OPTION_TYPE_REAL);
        } else if (type instanceof OptionTypeEnum) {
            JsonArrayBuilder result = Json.createArrayBuilder();
            OptionTypeEnum typeEnum = (OptionTypeEnum) type;
            for (String key : typeEnum.getKeys()) {
                result.add(key);
            }
            return result.build();
        } else if (type instanceof OptionTypeMap<?>) {
            JsonArrayBuilder result = Json.createArrayBuilder();
            OptionTypeMap<?> typeMap = (OptionTypeMap<?>) type;
            for (String key : typeMap.getKeys()) {
                result.add(key);
            }
            return result.build();
        } else {
            return UtilJSON.toStringValue(OPTION_TYPE_STRING);
        }
    }

    private JsonValue buildDefault(OptionType type, Object defaultValue) {
        if (defaultValue == null) {
            return UtilJSON.getNull();
        } else if (type instanceof OptionTypeBoolean) {
            return UtilJSON.toBooleanValue(defaultValue);
        } else if (type instanceof OptionTypeInteger) {
            return UtilJSON.toNumberValue(defaultValue);
        } else if (type instanceof OptionTypeReal) {
            return UtilJSON.toNumberValue(defaultValue);
        } else {
            return UtilJSON.toStringValue(type.unparse(defaultValue));
        }
    }

    /**
     * Handle authentication request from client.
     * None of the parameters may be {@code null}.
     * 
     * @param client client trying to authenticate
     * @param message message by which client is trying to connect
     */
    @Override
    public void handle(Object client, JsonObject message) {
        assert client != null;
        assert message != null;
        checkMessage(client, message);
        checkAvailableVersions(message);
        login(client, message);
        JsonObject reply = buildReply(client);
        backend.send(client, reply);
    }

    private void checkMessage(Object client, JsonObject message) {
        assert message != null;
        Map<Object, ClientInfo> clients = backend.getClients();
        boolean stdio = backend.isStdio();
        ensure(!clients.containsKey(client),
                ProblemsJANIInteraction.JANI_INTERACTION_ALREADY_LOGGED_IN);
        ensure(!stdio || !message.containsKey(AUTHENTICATE_LOGIN),
                ProblemsJANIInteraction.JANI_INTERACTION_STD_IO_LOGIN_PROVIDED);
        ensure(stdio
                || Options.get().getBoolean(OptionsJANIInteraction.JANI_INTERACTION_WEBSOCKET_ANONYMOUS_LOGINS)
                || message.containsKey(AUTHENTICATE_LOGIN),
                ProblemsJANIInteraction.JANI_INTERACTION_WEBSOCKET_NO_LOGIN);
        ensure(message.containsKey(AUTHENTICATE_LOGIN)
                == message.containsKey(AUTHENTICATE_PASSWORD),
                ProblemsJANIInteraction.JANI_INTERACTION_NO_PASSWORD);
        ensure(!message.containsKey(AUTHENTICATE_LOGIN)
                || message.get(AUTHENTICATE_LOGIN).getValueType()
                == JsonValue.ValueType.STRING,
                ProblemsJANIInteraction.JANI_INTERACTION_INVALID_MESSAGE);
        ensure(!message.containsKey(AUTHENTICATE_PASSWORD)
                || message.get(AUTHENTICATE_PASSWORD).getValueType()
                == JsonValue.ValueType.STRING,
                ProblemsJANIInteraction.JANI_INTERACTION_INVALID_MESSAGE);
    }

    private void checkAvailableVersions(JsonObject message) {
        BigInteger ourJaniVersion = backend.getOurJaniVersion();
        BigInteger[] clientVersions = UtilJSON.getArrayBigInteger(message, JANI_VERSIONS);
        boolean found = false;
        for (BigInteger clientVersion : clientVersions) {
            if (clientVersion.equals(ourJaniVersion)) {
                found = true;
            }
        }
        ensure(found, ProblemsJANIInteraction.JANI_INTERACTION_NO_ACCORDING_VERSION_FOUND);
    }

    private void login(Object client, JsonObject message) {
        String login = null;
        String password = null;
        if (message.containsKey(AUTHENTICATE_LOGIN)) {
            login = UtilJSON.getStringOrNull(message, AUTHENTICATE_LOGIN);
            password = UtilJSON.getStringOrNull(message, AUTHENTICATE_PASSWORD);
        }
        int id = -1;
        if (login == null) {

        } else {
            id = userManager.checkLogin(login, password);
            ensure(id != -1, ProblemsJANIInteraction.JANI_INTERACTION_LOGIN_DATA);
        }
        Set<String> extensions = UtilJSON.getStringSetOrEmpty(message, EXTENSIONS);
        backend.registerClient(client, id, extensions);
    }

    private JsonObject buildReply(Object client) {
        BigInteger ourJaniVersion = backend.getOurJaniVersion();
        JsonObject metadata = Json.createObjectBuilder()
                .add(CAPABILITIES_METADATA_NAME, backend.getToolName())
                .add(CAPABILITIES_METADATA_VERSION, backend.getOurServerVersion())
                .build();
        JsonArrayBuilder extensions = Json.createArrayBuilder();
        for (String extension: backend.getClientData(client).getExtensions()) {
            extensions.add(extension);
        }
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add(TYPE, CAPABILITIES)
        .add(JANI_VERSION, ourJaniVersion)
        .add(CAPABILITIES_EXTENSIONS, extensions.build())
        .add(CAPABILITIES_METADATA, metadata);
        if (ADD_EXTENDED_INFO) {
            builder.add(X_PRECISE_CATEGORIES, this.extendedCategories);
        }
        builder.add(CAPABILITIES_PARAMETERS, capabilitiesParameters)
        .add(CAPABILITIES_ROLES, Json.createArrayBuilder().add(CAPABILITIES_ROLES_ANALYSE).add(CAPABILITIES_ROLES_TRANSFORM));
        return builder.build();
    }

}
