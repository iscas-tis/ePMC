package epmc.filter.messages;

import epmc.messages.Message;

public final class MessagesFilter {
    private final static String MESSAGES_FILTER = "MessagesFilter";
    public final static Message NUM_STATES_IN_FILTER = newMessage().setIdentifier("num-states-in-filter").build();
    public final static Message PRINT_FILTER = newMessage().setIdentifier("print-filter").build();
    public final static Message PRINTING_FILTER_RESULTS = newMessage().setIdentifier("printing-filter-results").build();
    public final static Message PRINTING_ALL_FILTER_RESULTS = newMessage().setIdentifier("printing-all-filter-results").build();

    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_FILTER);
    }
    
	private MessagesFilter() {
	}
}
