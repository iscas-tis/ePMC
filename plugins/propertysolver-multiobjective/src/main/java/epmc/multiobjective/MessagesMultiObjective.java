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

package epmc.multiobjective;

import epmc.messages.Message;

public final class MessagesMultiObjective {
    public final static String MESSAGES_MULTI_OBJECTIVE = "MessagesMultiObjective";

    public final static Message STARTING_MULTI_OBJECTIVE = newMessage().setIdentifier("starting-multi-objective").build();
    public final static Message DONE_MULTI_OBJECTIVE = newMessage().setIdentifier("done-multi-objective").build();    
    public final static Message STARTING_MAIN_LOOP = newMessage().setIdentifier("starting-main-loop").build();
    public final static Message DONE_MAIN_LOOP = newMessage().setIdentifier("done-main-loop").build();
    public final static Message STARTING_MAIN_LOOP_ITERATION = newMessage().setIdentifier("starting-main-loop-iteration").build();
    public final static Message DONE_MAIN_LOOP_ITERATION = newMessage().setIdentifier("done-main-loop-iteration").build();

    public final static Message STARTING_NORMALISING_FORMULA = newMessage().setIdentifier("starting-normalising-formula").build();
    public final static Message DONE_NORMALISING_FORMULA = newMessage().setIdentifier("done-normalising-formula").build();
    public final static Message STARTING_NESTED_FORMULAS = newMessage().setIdentifier("starting-nested-formulas").build();
    public final static Message DONE_NESTED_FORMULAS = newMessage().setIdentifier("done-nested-formulas").build();

    public final static Message STARTING_PRODUCT = newMessage().setIdentifier("starting-product").build();
    public final static Message DONE_PRODUCT = newMessage().setIdentifier("done-product").build();

    public final static Message CURRENT_BOUND_QUANTITATIVE = newMessage().setIdentifier("current-bound-quantitative").build();
    public final static Message QUALITATIVE_PROPERTY = newMessage().setIdentifier("qualitative-property").build();
    public final static Message QUANTITATIVE_PROPERTY = newMessage().setIdentifier("quantitative-property").build();

    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_MULTI_OBJECTIVE);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private MessagesMultiObjective() {
    }
}
