package epmc.propertysolverltlfairness;

import epmc.messages.Message;

public final class MessagesLTLFairness {
	public final static String MESSAGES_LTL_FAIRNESS = "MessagesLTLFairness";
	
    public final static Message LTL_FAIRNESS_EXPLORING_STATE_SPACE = newMessage().setIdentifier("ltl-fairness-exploring-state-space").build();
    public final static Message LTL_FAIRNESS_EXPLORING_STATE_SPACE_DONE = newMessage().setIdentifier("ltl-fairness-exploring-state-space-done").build();
    public final static Message LTL_FAIRNESS_COMPUTING_END_COMPONENTS_DONE = newMessage().setIdentifier("ltl-fairness-computing-end-components-done").build();
    public final static Message LTL_FAIRNESS_PREPARING_MDP_FOR_ITERATION = newMessage().setIdentifier("ltl-fairness-preparing-mdp-for-iteration").build();
    public final static Message LTL_FAIRNESS_PREPARING_MDP_FOR_ITERATION_DONE = newMessage().setIdentifier("ltl-fairness-preparing-mdp-for-iteration-done").build();
    public final static Message LTL_FAIRNESS_COMPUTING_END_COMPONENTS = newMessage().setIdentifier("ltl-fairness-computing-end-components").build();
    public final static Message LTL_FAIRNESS_NUM_END_COMPONENTS = newMessage().setIdentifier("ltl-fairness-num-end-components").build();

    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_LTL_FAIRNESS);
    }

	private MessagesLTLFairness() {
	}
}
