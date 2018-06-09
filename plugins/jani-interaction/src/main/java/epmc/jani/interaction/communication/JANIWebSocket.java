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

import java.io.IOException;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.jani.interaction.messages.MessagesJANIInteraction;
import epmc.jani.interaction.options.OptionsJANIInteraction;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;
import epmc.util.UtilJSON;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoWSD.WebSocket;
import fi.iki.elonen.NanoWSD.WebSocketFrame;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;

/**
 * WebSocket to handle JANI requests.
 * 
 * @author Ernst Moritz Hahn
 */
final class JANIWebSocket extends WebSocket {
    private final static String SEC_WEBSOCKET_KEY = "sec-websocket-key";
    private final static String INVALID_JSON = "(invalid JSON possibly containing sensitive data)";
    private final static String PASSWORD_REMOVED = "(password removed from output)";
    private final static String PASSWORD = "password";
    private final static String QUOT_PASSWORD = "\"password\"";

    private final Backend backend;
    private final boolean printMessages;

    public JANIWebSocket(Backend backend, IHTTPSession handshakeRequest) {
        super(handshakeRequest);
        assert backend != null;
        assert handshakeRequest != null;
        this.backend = backend;
        this.printMessages = Options.get().getBoolean(OptionsJANIInteraction.JANI_INTERACTION_PRINT_MESSAGES);
    }

    @Override
    protected void onOpen() {
    }

    @Override
    protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
    }

    @Override
    protected void onMessage(WebSocketFrame message) {
        assert message != null;
        if (message.getTextPayload() == null) {
            return;
        }
        printMessage(message);
        backend.sendToBackend(this, UtilJSON.read(message.getTextPayload()));
    }

    private void printMessage(WebSocketFrame message) {
        assert message != null;
        if (!printMessages) {
            return;
        }
        String messageText = message.getTextPayload();
        try {
            JsonStructure value = UtilJSON.read(messageText);
            JsonObjectBuilder builder = Json.createObjectBuilder();
            if (value instanceof JsonObject) {
                JsonObject object = (JsonObject) value;
                for (Entry<String, JsonValue> entry : object.entrySet()) {
                    String key = entry.getKey();
                    JsonValue v = entry.getValue();
                    if (key.equals(PASSWORD)) {
                        v = UtilJSON.toStringValue(PASSWORD_REMOVED);
                    }
                    builder.add(key, v);
                }
                messageText = builder.build().toString();
            }
        } catch (EPMCException e) {
            if (messageText.contains(QUOT_PASSWORD)) {
                messageText = INVALID_JSON;
            }
        }
        getLog().send(MessagesJANIInteraction.JANI_INTERACTION_SENT_TO_SERVER,
                getHandshakeRequest().getHeaders().get(SEC_WEBSOCKET_KEY),
                messageText);
    }

    @Override
    protected void onPong(WebSocketFrame pong) {
    }

    @Override
    protected void onException(IOException exception) {
    }

    private Log getLog() {
        return Options.get().get(OptionsMessages.LOG);
    }
}
