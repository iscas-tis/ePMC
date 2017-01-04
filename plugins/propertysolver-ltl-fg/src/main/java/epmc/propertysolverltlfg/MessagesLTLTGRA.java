package epmc.propertysolverltlfg;

import epmc.messages.Message;

public final class MessagesLTLTGRA {
	public final static String MESSAGES_LTL_TGRA = "MessagesLTLTGRA";
	
    public final static Message LTL_TGRA_EXPLORING_STATE_SPACE = newMessage().setIdentifier("ltl-tgra-exploring-state-space").build();
    public final static Message LTL_TGRA_EXPLORING_STATE_SPACE_DONE = newMessage().setIdentifier("ltl-tgra-exploring-state-space-done").build();
    public final static Message LTL_TGRA_COMPUTING_END_COMPONENTS_DONE = newMessage().setIdentifier("ltl-tgra-computing-end-components-done").build();
    public final static Message LTL_TGRA_PREPARING_MDP_FOR_ITERATION = newMessage().setIdentifier("ltl-tgra-preparing-mdp-for-iteration").build();
    public final static Message LTL_TGRA_PREPARING_MDP_FOR_ITERATION_DONE = newMessage().setIdentifier("ltl-tgra-preparing-mdp-for-iteration-done").build();
    public final static Message LTL_TGRA_COMPUTING_END_COMPONENTS = newMessage().setIdentifier("ltl-tgra-computing-end-components").build();
    public final static Message LTL_TGRA_NUM_END_COMPONENTS = newMessage().setIdentifier("ltl-tgra-num-end-components").build();
    public final static Message LTL_TGRA_FIRST_MEC_COMPONENTS = newMessage().setIdentifier("ltl-tgra-fisrt-compute-mec-components").build();

    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_LTL_TGRA);
    }

	private MessagesLTLTGRA() {
	}
}
