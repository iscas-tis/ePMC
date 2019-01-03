package epmc.qmc.messages;

import epmc.messages.Message;

public final class MessagesQMC {
    private final static String MESSAGES_QMC = "MessagesQMC";

    public final static Message QMC_IGNORE_NEG_ALWAYS = newMessage().setIdentifier("qmc-ignore-neg-always").build();
    public final static Message QMC_IGNORE_NEG_BETTER = newMessage().setIdentifier("qmc-ignore-neg-better").build();
    public final static Message QMC_IGNORE_ENGINE = newMessage().setIdentifier("qmc-ignore-engine").build();
    public final static Message PURE_PROB_WITH_DIR = newMessage().setIdentifier("pure-prob-with-dir").build();

    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_QMC);
    }

    private MessagesQMC() {
    }
}
