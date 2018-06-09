package epmc.jani.interaction.communication;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import javax.json.JsonValue;

import epmc.plugin.PluginInterface;

public final class SimpleAsynchronous implements BackendInterface, BackendFeedback {
    private final BackendInterface backend;
    private final LinkedBlockingDeque<JsonValue> clientToBackend = new LinkedBlockingDeque<>();
    private final LinkedBlockingDeque<JsonValue> backendToCliend = new LinkedBlockingDeque<>();
    private BackendFeedback client;
    private Thread clientToBackendThread;
    private Thread backendToClientThread;
    private boolean done;
    
    public SimpleAsynchronous(List<Class<? extends PluginInterface>> plugins) {
        backend = new Backend(this, plugins);
    }
    
    public void setClient(BackendFeedback client) {
        this.client = client;
    }

    public void start() {
        clientToBackendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!done) {
                    JsonValue line = null;
                    try {
                        line = clientToBackend.take();
                        backend.sendToBackend(this, line);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        backendToClientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!done) {
                    JsonValue line = null;
                    try {
                        line = backendToCliend.take();
                        client.sendToClient(this, line);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        clientToBackendThread.start();
        backendToClientThread.start();
    }
    
    @Override
    public void sendToBackend(Object client, JsonValue message) {
        try {
            clientToBackend.put(message);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendToClient(Object client, JsonValue message) {
        try {
            backendToCliend.put(message);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void logOff(Object who) {
        done = true;
        clientToBackendThread.interrupt();
        backendToClientThread.interrupt();
    }
}
