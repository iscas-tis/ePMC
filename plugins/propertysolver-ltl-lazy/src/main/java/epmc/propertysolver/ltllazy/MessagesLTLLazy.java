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

package epmc.propertysolver.ltllazy;

import epmc.messages.Message;

public final class MessagesLTLLazy {
    public final static String MESSAGES_LTL_LAZY = "MessagesLTLLazy";

    public final static Message LTL_LAZY_INITIALISING_AUTOMATON_AND_PRODUCT_MODEL = newMessage().setIdentifier("initialising-automaton-and-product-model").build();
    public final static Message LTL_LAZY_INITIALISING_AUTOMATON_AND_PRODUCT_MODEL_DONE = newMessage().setIdentifier("initialising-automaton-and-product-model-done").build();
    public final static Message LTL_LAZY_INITIALISING_AUTOMATON_AND_PRODUCT_MODEL_SUBSET = newMessage().setIdentifier("initialising-automaton-and-product-model-subset").build();
    public final static Message LTL_LAZY_INITIALISING_AUTOMATON_AND_PRODUCT_MODEL_SUBSET_DONE = newMessage().setIdentifier("initialising-automaton-and-product-model-subset-done").build();
    public final static Message LTL_LAZY_INITIALISING_AUTOMATON_AND_PRODUCT_MODEL_BREAKPOINT = newMessage().setIdentifier("initialising-automaton-and-product-model-breakpoint").build();
    public final static Message LTL_LAZY_INITIALISING_AUTOMATON_AND_PRODUCT_MODEL_BREAKPOINT_DONE = newMessage().setIdentifier("initialising-automaton-and-product-model-breakpoint-done").build();
    public final static Message LTL_LAZY_INITIALISING_AUTOMATON_AND_PRODUCT_MODEL_RABIN = newMessage().setIdentifier("initialising-automaton-and-product-model-rabin").build();
    public final static Message LTL_LAZY_INITIALISING_AUTOMATON_AND_PRODUCT_MODEL_RABIN_DONE = newMessage().setIdentifier("initialising-automaton-and-product-model-rabin-done").build();
    public final static Message LTL_LAZY_EXPLORING_STATE_SPACE = newMessage().setIdentifier("exploring-state-space").build();
    public final static Message LTL_LAZY_EXPLORING_STATE_SPACE_DONE = newMessage().setIdentifier("exploring-state-space-done").build();
    public final static Message LTL_LAZY_COMPUTING_END_COMPONENTS_INCREMENTALLY = newMessage().setIdentifier("computing-end-components-incrementally").build();
    public final static Message LTL_LAZY_COMPUTING_END_COMPONENTS_INCREMENTALLY_DONE = newMessage().setIdentifier("computing-end-components-incrementally-done").build();
    public final static Message LTL_LAZY_DECIDING_COMPONENT = newMessage().setIdentifier("deciding-component").build();
    public final static Message LTL_LAZY_DECIDING_COMPONENT_DONE = newMessage().setIdentifier("deciding-component-done").build();
    public final static Message LTL_LAZY_SKIPPING_COMPONENT = newMessage().setIdentifier("skipping-component").build();
    public final static Message LTL_LAZY_DECIDING_SUBSET = newMessage().setIdentifier("deciding-subset").build();
    public final static Message LTL_LAZY_DECIDING_SUBSET_DONE_ACCEPT = newMessage().setIdentifier("deciding-subset-done-accept").build();
    public final static Message LTL_LAZY_DECIDING_SUBSET_DONE_REJECT = newMessage().setIdentifier("deciding-subset-done-reject").build();
    public final static Message LTL_LAZY_DECIDING_SUBSET_DONE_UNDECIDED = newMessage().setIdentifier("deciding-subset-done-undecided").build();
    public final static Message LTL_LAZY_DECIDING_BREAKPOINT = newMessage().setIdentifier("deciding-breakpoint").build();
    public final static Message LTL_LAZY_DECIDING_BREAKPOINT_DONE_ACCEPT = newMessage().setIdentifier("deciding-breakpoint-done-accept").build();
    public final static Message LTL_LAZY_DECIDING_BREAKPOINT_DONE_REJECT = newMessage().setIdentifier("deciding-breakpoint-done-reject").build();
    public final static Message LTL_LAZY_DECIDING_BREAKPOINT_DONE_UNDECIDED = newMessage().setIdentifier("deciding-breakpoint-done-undecided").build();
    public final static Message LTL_LAZY_DECIDING_RABIN = newMessage().setIdentifier("deciding-rabin").build();
    public final static Message LTL_LAZY_DECIDING_RABIN_DONE_ACCEPT = newMessage().setIdentifier("deciding-rabin-done-accept").build();
    public final static Message LTL_LAZY_DECIDING_RABIN_DONE_REJECT = newMessage().setIdentifier("deciding-rabin-done-reject").build();
    public final static Message LTL_LAZY_DECIDING_BREAKPOINT_SINGLETONS = newMessage().setIdentifier("deciding-breakpoint-singletons").build();
    public final static Message LTL_LAZY_DECIDING_BREAKPOINT_SINGLETONS_DONE_ACCEPT = newMessage().setIdentifier("deciding-breakpoint-singletons-done-accept").build();
    public final static Message LTL_LAZY_DECIDING_BREAKPOINT_SINGLETONS_DONE_REJECT = newMessage().setIdentifier("deciding-breakpoint-singletons-done-reject").build();
    public final static Message LTL_LAZY_COMPUTING_END_COMPONENTS = newMessage().setIdentifier("computing-end-components").build();
    public final static Message LTL_LAZY_COMPUTING_END_COMPONENTS_DONE = newMessage().setIdentifier("computing-end-components-done").build();
    public final static Message LTL_LAZY_NUM_END_COMPONENTS = newMessage().setIdentifier("num-end-components").build();
    public final static Message LTL_LAZY_NUM_ECC_STATES = newMessage().setIdentifier("num-ecc-states").build();

    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_LTL_LAZY);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private MessagesLTLLazy() {
    }
}
