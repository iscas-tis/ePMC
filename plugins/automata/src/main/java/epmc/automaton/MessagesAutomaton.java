package epmc.automaton;

import epmc.messages.Message;

/**
 * Messages used in the automaton part of EPMC.
 * This class only contains static fields and methods, and thus is protected
 * from being instantiated.
 * 
 * @author Ernst Moritz Hahn
 */
public final class MessagesAutomaton {
    /** Base name of resource bundle for the messages. */
    private final static String MESSAGES_AUTOMATON = "MessagesAutomaton";
    /** Exploring the state space of an automaton product. */
    public final static Message EXPLORING = newMessage().setIdentifier("exploring").build();
    /** Finished exploring the state space of an automaton product. */
    public final static Message EXPLORING_DONE = newMessage().setIdentifier("exploring-done").build();
    
    /** B&uuml;chi automaton for original expression is being computed. */
    public final static Message COMPUTING_ORIG_BUECHI = newMessage().setIdentifier("computing-orig-buechi").build();
    /** B&uuml;chi automaton for negated expression is being computed. */
    public final static Message COMPUTING_NEG_BUECHI = newMessage().setIdentifier("computing-neg-buechi").build();
    /** The computation of the B&uuml;chi automaton for the expression has finished, and it is deterministic. */
    public final static Message COMPUTING_BUECHI_DONE_DET = newMessage().setIdentifier("computing-buechi-done-det").build();
    /** The computation of the B&uuml;chi automaton for the expression has finished, and it is nondeterministic. */
    public final static Message COMPUTING_BUECHI_DONE_NONDET = newMessage().setIdentifier("computing-buechi-done-nondet").build();
    /** The B&uuml;chi automaton for the original expression will be used for further computations. */
    public final static Message USING_ORIG_BUECHI = newMessage().setIdentifier("using-orig-buechi").build();
    /** The B&uuml;chi automaton for the negated expression will be used for further computations. */
    public final static Message USING_NEG_BUECHI = newMessage().setIdentifier("using-neg-buechi").build();

    /**
     * Creates a new message with given identifier with this resource bundle.
     * The parameter may not be {@code null}.
     * 
     * @param message identifier of message to be created
     * @return message created
     */
    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_AUTOMATON);
    }
    
    /**
     * Private constructor to prevent instantiation of this class.
     */
    private MessagesAutomaton() {
    }
}
