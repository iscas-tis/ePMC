/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

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
