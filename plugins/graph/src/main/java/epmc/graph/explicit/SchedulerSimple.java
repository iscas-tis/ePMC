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

package epmc.graph.explicit;

import epmc.graph.Scheduler;

/**
 * Simple scheduler.
 * A simple scheduler is a function mapping each node of a graph to an outgoing
 * edge of this node. Each time the state is entered, the scheduler chooses the
 * edge chosen for the node, such that the successor is the post node of the
 * edge. Schedulers of this type thus do not depend on the past history of the
 * process and do not use randomisation. See for instance
 * Christel Baier and Joost-Pieter Katoen, Principles of Model Checking,
 * p. 847, Definition 10.96. Memoryless Scheduler.
 * 
 * @author Ernst Moritz Hahn
 */
public interface SchedulerSimple extends Scheduler {
    /**
     * Get decision for a given state.
     * The state must be a valid index in the graph the scheduler refers to.
     * 
     * @param state node to get decision of
     * @return decision for a given node
     */
    int getDecision(int state);
}
