package epmc.imdp.messages;

import epmc.messages.Message;

public final class MessagesIMDPModel {
    private final static String MESSAGES_IMDP_MODEL = "MessagesIMDPModel";

    public final static Message BUILDING_DD_MODEL = newMessage().setIdentifier("building-dd-model").build();
    public final static Message BUILDING_DD_MODEL_DONE = newMessage().setIdentifier("building-dd-model-done").build();
    public final static Message EXPLORING = newMessage().setIdentifier("exploring").build();
    public final static Message EXPLORING_DONE = newMessage().setIdentifier("exploring-done").build();
    public final static Message PURE_PROB_WITH_DIR = newMessage().setIdentifier("pure-prob-with-dir").build();

    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_IMDP_MODEL);
    }

    private MessagesIMDPModel() {
    }
}
