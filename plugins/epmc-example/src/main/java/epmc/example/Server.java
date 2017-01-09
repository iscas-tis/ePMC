package epmc.example;

import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.*;

import fi.iki.elonen.NanoWSD;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;

public class Server extends NanoWSD{
	private final boolean debug;
	private static final Logger LOG = Logger.getLogger(Server.class.getName());
	
	public Server(int port, boolean debug) {
        super(port);
        this.debug = debug;
    }

	@Override
	protected WebSocket openWebSocket(IHTTPSession handshake) {
		return new DebugWebSocket(this, handshake);		
	}
	
	private static class DebugWebSocket extends WebSocket {

        private final Server server;

        public DebugWebSocket(Server server, IHTTPSession handshakeRequest) {
            super(handshakeRequest);
            this.server = server;
        }

        @Override
        protected void onOpen() {
        }

        @Override
        protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
            if (server.debug) {
                System.out.println("C [" + (initiatedByRemote ? "Remote" : "Self") + "] " + (code != null ? code : "UnknownCloseCode[" + code + "]")
                        + (reason != null && !reason.isEmpty() ? ": " + reason : ""));
            }
        }

        @Override
        protected void onMessage(WebSocketFrame message) {
            try {
                message.setUnmasked();
                String strmsg = message.getTextPayload();
                JsonReader reader = Json.createReader(new StringReader(strmsg));
                JsonObject obj = reader.readObject();
                if (!obj.getString("method").equals("breathe")) {
                	System.out.println(obj);
                }
                sendFrame(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPong(WebSocketFrame pong) {
            if (server.debug) {
                System.out.println("P " + pong);
            }
        }

        @Override
        protected void onException(IOException exception) {
            Server.LOG.log(Level.SEVERE, "exception occured", exception);
        }

        @Override
        protected void debugFrameReceived(WebSocketFrame frame) {
            if (server.debug) {
                System.out.println("R " + frame);
            }
        }

        @Override
        protected void debugFrameSent(WebSocketFrame frame) {
            if (server.debug) {
                System.out.println("S " + frame);
            }
        }
    }
}
