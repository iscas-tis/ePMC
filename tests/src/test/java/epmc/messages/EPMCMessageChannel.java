package epmc.messages;

import epmc.messages.MessageInstance;

public interface EPMCMessageChannel {

    void setTimeStarted(long time);

    void send(MessageInstance instance);
}
