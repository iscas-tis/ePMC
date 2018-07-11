package epmc.jani.interaction.communication;

import javax.json.JsonValue;

public interface BackendInterface {
    void sendToBackend(Object client, JsonValue message);
}
