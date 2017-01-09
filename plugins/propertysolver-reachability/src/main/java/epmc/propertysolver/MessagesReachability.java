package epmc.propertysolver;

import epmc.messages.Message;

public class MessagesReachability {
	public final static String MESSAGES_REACHABILITY = "MessagesReachability";
	
    public final static Message REACHABILITY_NUM_ONE_STATES = newMessage().setIdentifier("reachability-num-one-states").build();
    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_REACHABILITY);
    }

	private MessagesReachability() {
	}
}
