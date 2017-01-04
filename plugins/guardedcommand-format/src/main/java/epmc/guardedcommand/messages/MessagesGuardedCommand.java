package epmc.guardedcommand.messages;

import epmc.messages.Message;

public final class MessagesGuardedCommand {
    private final static String MESSAGES_GUARDEDCOMMAND = "MessagesGuardedCommand";

    public final static Message GUARDEDCOMMAND_BUILDING_DD_MODEL = newMessage().setIdentifier("guardedcommand-building-dd-model").build();
    public final static Message GUARDEDCOMMAND_BUILDING_DD_MODEL_DONE = newMessage().setIdentifier("guardedcommand-building-dd-model-done").build();
    public final static Message GUARDEDCOMMAND_EXPLORING = newMessage().setIdentifier("guardedcommand-exploring").build();
    public final static Message GUARDEDCOMMAND_EXPLORING_DONE = newMessage().setIdentifier("guardedcommand-exploring-done").build();
    public final static Message GUARDEDCOMMAND_PURE_PROB_WITH_DIR = newMessage().setIdentifier("guardedcommand-pure-prob-with-dir").build();

    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_GUARDEDCOMMAND);
    }

    private MessagesGuardedCommand() {
    }
}
