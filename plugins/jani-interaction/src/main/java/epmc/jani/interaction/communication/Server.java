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
import java.io.InputStream;

import javax.json.JsonValue;

import epmc.jani.interaction.messages.MessagesJANIInteraction;
import epmc.jani.interaction.options.OptionsJANIInteraction;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;

/**
 * Server which provides HTML JANI GUI and waits for WebSocket connections.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Server extends NanoWSD implements BackendFeedback  {
    private final static String DOTDOT = "..";
    private final static String SLASH = "/";
    private final static String SLASH_INDEX_DOT_HTML = "/index.html";
    private final static String SLASH_PAGE = "/page";
    private final static String SEC_WEBSOCKET_KEY = "sec-websocket-key";

    private final Backend backend;
    private final boolean printMessages;

    public Server() {
        super(getPort());
        backend = new Backend(this, null);
        printMessages = Options.get().getBoolean(OptionsJANIInteraction.JANI_INTERACTION_PRINT_MESSAGES);
    }

    /**
     * Read the port to use for this server.
     * 
     * @return port to use for this server
     */
    private static int getPort() {
        return Options.get().getInteger(OptionsJANIInteraction.JANI_INTERACTION_WEBSOCKET_SERVER_PORT);
    }

    /* methods of backend feedback */

    @Override
    public void sendToClient(Object client, JsonValue message) {
        assert client != null;
        assert message != null;
        assert client instanceof WebSocket;
        WebSocket socket = (WebSocket) client;
        if (printMessages) {
            getLog().send(MessagesJANIInteraction.JANI_INTERACTION_SENT_BY_SERVER,
                    socket.getHandshakeRequest().getHeaders().get(SEC_WEBSOCKET_KEY),
                    message);
        }
        try {
            socket.send(message.toString());
        } catch (IOException e) {
        }
    }

    @Override
    public void logOff(Object client) {
        assert client != null;
        assert client instanceof WebSocket;
        WebSocket socket = (WebSocket) client;
        try {
            // TODO might be more specific here
            socket.close(CloseCode.NormalClosure, "", true);
        } catch (IOException e) {
        }
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        return new JANIWebSocket(backend, handshake);
    }

    @Override
    protected Response serveHttp(IHTTPSession session) {
        String uri = session.getUri();
        if (uri.contains(DOTDOT)) {
            return NanoHTTPD.newChunkedResponse(Status.NOT_FOUND, MIME_HTML, null);
        }
        if (uri.equals(SLASH)) {
            uri = SLASH_INDEX_DOT_HTML;
        }
        String resourceName = SLASH_PAGE + uri;
        InputStream in = getClass().getResourceAsStream(resourceName);
        if (in == null) {
            return NanoHTTPD.newChunkedResponse(Status.NOT_FOUND, MIME_HTML, null);
        }
        String type = getMimeTypeForFile(uri);
        return NanoHTTPD.newChunkedResponse(Status.OK, type, in);
    }

    /**
     * Get log used.
     * 
     * @return log used
     */
    private Log getLog() {
        return Options.get().get(OptionsMessages.LOG);
    }
}
