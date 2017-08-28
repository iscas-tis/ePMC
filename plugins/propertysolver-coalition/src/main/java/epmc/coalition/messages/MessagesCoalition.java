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

package epmc.coalition.messages;

import epmc.messages.Message;

/**
 * Messages used in the coalition solver plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class MessagesCoalition {
    /** Base name of resource bundle of this plugin. */
    public final static String MESSAGES_COALITION = "MessagesCoalition";
    /** Number of nodes in the original model. */
    public final static Message COALITION_MODEL_NODES = newMessage().setIdentifier("coalition-model-nodes").build();
    /** Building product start. */
    public final static Message COALITION_PRODUCT_START = newMessage().setIdentifier("coalition-product-start").build();
    /** Building product done. */
    public final static Message COALITION_PRODUCT_DONE = newMessage().setIdentifier("coalition-product-done").build();
    /** Number of colors in the game. */
    public final static Message COALITION_NUMBER_COLORS = newMessage().setIdentifier("coalition-number-colors").build();
    /** Solving the game using method. */
    public final static Message COALITION_SOLVING_USING = newMessage().setIdentifier("coalition-solving-using").build();
    /** Number of player even/odd nodes. */
    public final static Message COALITION_SOLVING_DONE = newMessage().setIdentifier("coalition-solving-done").build();
    /** Number of recursive calls to McNaughton algorithm. */
    public final static Message COALITION_SCHEWE_MCNAUGHTON_CALLS = newMessage().setIdentifier("coalition-schewe-mcnaughton-calls").build();
    /** Starting gadget construction to be able to use algorithms for nonprobabilistic games. */
    public final static Message COALITION_GADGET_TRANSFORM_START = newMessage().setIdentifier("coalition-gadget-transform-start").build();
    /** Gadget construction to be able to use algorithms for nonprobabilistic games done. */
    public final static Message COALITION_GADGET_TRANSFORM_DONE = newMessage().setIdentifier("coalition-gadget-transform-done").build();
    /** Starting transformation construction solver. */
    public final static Message COALITION_GADGET_START = newMessage().setIdentifier("coalition-gadget-start").build();
    /** Done transformation construction solver. */
    public final static Message COALITION_GADGET_DONE = newMessage().setIdentifier("coalition-gadget-done").build();	
    /** Starting Jurdzinski's algorithm. */
    public final static Message COALITION_JURDZINSKI_START = newMessage().setIdentifier("coalition-jurdzinski-start").build();
    /** Done Jurdzinski's algorithm. */
    public final static Message COALITION_JURDZINSKI_DONE = newMessage().setIdentifier("coalition-jurdzinski-done").build();
    /** Starting to lift nodes in Jurdzinski's algorithm. */
    public final static Message COALITION_JURDZINSKI_LIFTING_START = newMessage().setIdentifier("coalition-jurdzinski-lifting-start").build();
    /** Done lifting nodes in Jurdzinski's algorithm. */
    public final static Message COALITION_JURDZINSKI_LIFTING_DONE = newMessage().setIdentifier("coalition-jurdzinski-lifting-done").build();
    /** Starting to compute predecessors for Jurdzinski's algorithm. */
    public final static Message COALITION_JURDZINSKI_COMPUTE_PREDECESSORS_START = newMessage().setIdentifier("coalition-jurdzinski-compute-predecessors-start").build();
    /** Done computing predecessors for Jurdzinski's algorithm. */
    public final static Message COALITION_JURDZINSKI_COMPUTE_PREDECESSORS_DONE = newMessage().setIdentifier("coalition-jurdzinski-compute-predecessors-done").build();
    /** Starting nonstochastic McNaughton algorithm. */
    public final static Message COALITION_NONSTOCHASTIC_MCNAUGHTON_START = newMessage().setIdentifier("coalition-nonstochastic-mcnaughton-start").build();
    /** Done nonstochastic McNaughton algorithm. */
    public final static Message COALITION_NONSTOCHASTIC_MCNAUGHTON_DONE = newMessage().setIdentifier("coalition-nonstochastic-mcnaughton-done").build();
    /** Starting stochastic McNaughton algorithm. */
    public final static Message COALITION_STOCHASTIC_MCNAUGHTON_START = newMessage().setIdentifier("coalition-stochastic-mcnaughton-start").build();
    /** Done stochastic McNaughton algorithm. */
    public final static Message COALITION_STOCHASTIC_MCNAUGHTON_DONE = newMessage().setIdentifier("coalition-stochastic-mcnaughton-done").build();

    public final static Message COALITION_QUANTITATIVE_SCHEWE_START = newMessage().setIdentifier("coalition-quantitative-schewe-start").build();
    public final static Message COALITION_QUANTITATIVE_SCHEWE_DONE = newMessage().setIdentifier("coalition-quantitative-schewe-done").build();
    public final static Message COALITION_QUANTITATIVE_SCHEWE_INITIALISE_START = newMessage().setIdentifier("coalition-quantitative-schewe-initialise-start").build();
    public final static Message COALITION_QUANTITATIVE_SCHEWE_INITIALISE_DONE = newMessage().setIdentifier("coalition-quantitative-schewe-initialise-done").build();
    public final static Message COALITION_QUANTITATIVE_SCHEWE_IMPROVE_START = newMessage().setIdentifier("coalition-quantitative-schewe-improve-start").build();
    public final static Message COALITION_QUANTITATIVE_SCHEWE_IMPROVE_DONE = newMessage().setIdentifier("coalition-quantitative-schewe-improve-done").build();

    /**
     * Construct a new message for the coalition solver.
     * The base name of the resource bundle of this plugin will be used to
     * construct the message.
     * The message parameter must not be {@code null}.
     * 
     * @return new message builder for the coalition solver
     */
    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_COALITION);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private MessagesCoalition() {
    }
}
