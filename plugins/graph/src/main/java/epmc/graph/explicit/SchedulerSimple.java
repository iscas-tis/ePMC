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

import epmc.error.EPMCException;
import epmc.value.ContextValue;

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
public interface SchedulerSimple extends Scheduler, NodeProperty {
    /**
     * Set the decision for a given node.
     * The node must be nonnegative and smaller than the value returned by
     * {@link GraphExplicit#getNumNodes()} of the graph obtained by
     * {@link #getGraph()}. The decision must be nonnegative or equal to
     * {@link Scheduler#UNSET}. It must also be smaller than the value obtained
     * by {@link GraphExplicit#getNumSuccessors()} directly after a call to
     * {@link GraphExplicit#queryNode(int)} on the node parameter of this
     * method. If the decision is set to {@link Scheduler#UNSET}, it means that
     * there is no decision for this node, otherwise the according outgoing edge
     * of the node is selected.
     * 
     * @param node node to which to set the decision
     * @param decision decision to set
     */
    void set(int node, int decision);
    
    /**
     * Get decision for a given node.
     * The node must be nonnegative and smaller than the value returned by
     * {@link GraphExplicit#getNumNodes()} of the graph obtained by
     * {@link #getGraph()}. The decision will be nonnegative or equal to
     * {@link Scheduler#UNSET}. It will also be smaller than the value obtained
     * by {@link GraphExplicit#getNumSuccessors()} directly after a call to
     * {@link GraphExplicit#queryNode(int)} on the node parameter of this
     * method. If the decision is {@link Scheduler#UNSET}, it means that there
     * is no decision for this node, otherwise the according outgoing edge
     * of the node is selected.
     * 
     * @param node node to get decision of
     * @return decision for a given node
     */
    int get(int node);
    
    @Override
    SchedulerSimple clone();

    @Override
    default ContextValue getContextValue() {
        return NodeProperty.super.getContextValue();
    }
    
    default void set(SchedulerSimple other) throws EPMCException {
        assert other != null;
        assert getGraph() == other.getGraph();
        int numNodes = getGraph().getNumNodes();
        for (int node = 0; node < numNodes; node++) {
            set(other.get(node));
        }
    }
    
    default boolean equals(SchedulerSimple other) throws EPMCException {
        assert other != null;
        if (getGraph() != other.getGraph()) {
            return false;
        }
        int numNodes = getGraph().getNumNodes();
        for (int node = 0; node < numNodes; node++) {
            if (get(node) != other.get(node)) {
                return false;
            }
        }
        return true;
    }
}
