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

package epmc.graphsolver.iterative;

import epmc.messages.Message;

/**
 * Messages used in the iterative graph solver of EPMC.
 * This class only contains static fields and methods, and thus is protected
 * from being instantiated.
 * 
 * @author Ernst Moritz Hahn
 */
public final class MessagesGraphSolverIterative {
    /** Base name of resource bundle for the messages. */
    private final static String MESSAGES_GRAPHSOLVER_ITERATIVE = "MessagesGraphSolverIterative";
    public final static Message ITERATING = newMessage().setIdentifier("iterating").build();
    public final static Message ITERATING_DONE = newMessage().setIdentifier("iterating-done").build();
    public final static Message ITERATING_PROGRESS_UNBOUNDED = newMessage().setIdentifier("iterating-progress-unbounded").build();
    public final static Message ITERATING_PROGRESS_BOUNDED = newMessage().setIdentifier("iterating-progress-bounded").build();

    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_GRAPHSOLVER_ITERATIVE);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private MessagesGraphSolverIterative() {
    }
}
