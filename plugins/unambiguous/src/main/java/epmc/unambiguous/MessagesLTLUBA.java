package epmc.unambiguous;

import epmc.messages.Message;
import epmc.messages.UtilMessages;

public final class MessagesLTLUBA {
	
	public final static String MESSAGES_LTL_UBA = "MessagesLTLUBA";
	public final static Message LTL_UBA_NUM_ECC_STATES = newMessage("ltl-uba-num-recurrent-states");
	public final static Message LTL_UBA_BUILD_DONE = newMessage("ltl-uba-build-done");
	public final static Message LTL_UBA_SCC_ACCEPTED = newMessage("ltl-uba-scc-accepted");
	public final static Message LTL_UBA_NUM_SCC_CHECKED = newMessage("ltl-uba-num-scc-checked");
	public final static Message LTL_UBA_POSITIVE_SCC_CHECKED = newMessage("ltl-uba-postive-scc-checked");
	public final static Message LTL_UBA_NUM_MODEL_STATES = newMessage("ltl-uba-num-model-states");
	public final static Message LTL_UBA_NUM_AUTOMATON_STATES = newMessage("ltl-uba-num-automaton-states");
	public final static Message LTL_UBA_NUM_PRODUCT_STATES = newMessage("ltl-uba-num-product-states");
	public final static Message LTL_UBA_EXPLORING_STATE_SPACE = newMessage("ltl-uba-exploring-state-space");
	public final static Message LTL_UBA_EXPLORING_STATE_SPACE_DONE = newMessage("ltl-uba-exploring-state-space-done");
    public final static Message LTL_UBA_COMPUTING_END_COMPONENTS = newMessage("ltl-uba-computing-end-components");
    public final static Message LTL_UBA_COMPUTING_END_COMPONENTS_DONE = newMessage("ltl-uba-computing-end-components-done");
    public final static Message LTL_UBA_COMPUTING_ZERO_STATES_DONE = newMessage("ltl-uba-computing-zero-states-done");
    public final static Message LTL_UBA_COMPUTING_ZERO_STATES = newMessage("ltl-uba-computing-zero-states");
    public final static Message LTL_UBA_LP_SOLVING = newMessage("ltl-uba-lp-solving");
    public final static Message LTL_UBA_LP_SOLVING_DONE = newMessage("ltl-uba-lp-solving-done");
    public final static Message LTL_UBA_CONSTRUCT_LP_EQUATION_DONE = newMessage("ltl-uba-construct-lp-done");

	private static Message newMessage(String message) {
    	assert message != null;
        return UtilMessages.newMessage().setBundle(MESSAGES_LTL_UBA).setIdentifier(message).build();
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
	private MessagesLTLUBA() {
	}

}
