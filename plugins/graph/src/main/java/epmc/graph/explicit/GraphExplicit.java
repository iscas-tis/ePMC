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

import java.util.Set;

import epmc.expression.Expression;
import epmc.graph.CommonProperties;
import epmc.graph.LowLevel;
import epmc.util.BitSet;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.ValueEnum;
import epmc.value.ValueObject;

// TODO complete documentation

/**
 * Explicit-state graph.
 * 
 * @author Ernst Moritz Hahn
 */
public interface GraphExplicit extends LowLevel {
    /* methods to be implemented by classes implementing the interface */

    /**
     * Get the number of nodes of this graph.
     * 
     * @return number of nodes of this graph
     */
    int getNumNodes();

    /**
     * Get the set of initial nodes of this graph.
     * 
     * @return set of initial nodes of this graph
     */
    BitSet getInitialNodes();

    /**
     * Get number of successors of last node queried by {@link #queryNode(int)}.
     * 
     * @return number of successors of node queried last
     */
    int getNumSuccessors(int node);

    /**
     * Get the successor node with the given number. The method must not be
     * called before {@link #queryNode(int)} has been called. The successor
     * parameter must be nonnegative and strictly smaller than the value of a
     * call to {@link #getNumSuccessors()} after the latest call to
     * {@link #queryNode(int)}.
     * 
     * @param successor
     *            successor number
     * @return successor with the given number of the queried node
     */
    int getSuccessorNode(int node, int successor);

    /**
     * Get the properties of this graph.
     * 
     * @return properties of this graph
     */
    GraphExplicitProperties getProperties();

    // default fail / convenience

    default void computePredecessors(BitSet states) {
        getProperties().computePredecessors(states);
    }

    default void clearPredecessors() {
        getProperties().clearPredecessors();
    }

    default void explore(BitSet start) {
        getProperties().explore(start);
    }

    default void registerGraphProperty(Object property, Type type) {
        getProperties().registerGraphProperty(property, type);
    }

    default void setGraphProperty(Object property, Value value) {
        getProperties().setGraphProperty(property, value);
    }

    default void registerNodeProperty(Object propertyName, NodeProperty property) {
        getProperties().registerNodeProperty(propertyName, property);
    }

    default NodeProperty getNodeProperty(Object property) {
        return getProperties().getNodeProperty(property);
    }

    default Set<Object> getNodeProperties() {
        return getProperties().getNodeProperties();
    }

    default void registerEdgeProperty(Object propertyName, EdgeProperty property) {
        getProperties().registerEdgeProperty(propertyName, property);
    }

    default EdgeProperty getEdgeProperty(Object property) {
        return getProperties().getEdgeProperty(property);
    }

    default Set<Object> getEdgeProperties() {
        return getProperties().getEdgeProperties();
    }

    default void removeGraphProperty(Object property) {
        getProperties().removeGraphProperty(property);
    }

    default void removeNodeProperty(Object property) {
        getProperties().removeNodeProperty(property);
    }

    default void removeEdgeProperty(Object property) {
        getProperties().removeEdgeProperty(property);
    }

    default Set<Object> getGraphProperties() {
        return getProperties().getGraphProperties();
    }

    default Value getGraphProperty(Object property) {
        return getProperties().getGraphProperty(property);
    }

    default Value addSettableGraphProperty(Object property, Type type) {
        assert false;
        return null;
    }

    default NodeProperty addSettableNodeProperty(Object property, Type type) {
        assert false;
        return null;
    }

    default EdgeProperty addSettableEdgeProperty(Object property, Type type) {
        assert false;
        return null;
    }

    default void setSuccessorNode(int node, int succNr, int succState) {
        assert false;
    }

    default void prepareNode(int node, int numSuccessors) {
        assert false;
    }

    default NodeProperty addSettableNodeProperty(Type type) {
        return addSettableNodeProperty(new Object(), type);
    }

    default EdgeProperty addSettableEdgeProperty(Type type) {
        return addSettableEdgeProperty(new Object(), type);
    }

    default int computeNumStates() {
        int numStates = 0;
        NodeProperty isState = getNodeProperty(CommonProperties.STATE);
        assert isState != null;
        int numNodes = getNumNodes();
        for (int node = 0; node < numNodes; node++) {
            if (isState.getBoolean(node)) {
                numStates++;
            }
        }
        return numStates;
    }

    default void computePredecessors() {
        getProperties().computePredecessors();
    }

    default Type getGraphPropertyType(Object property) {
        return getGraphProperty(property).getType();
    }

    default <T> T getGraphPropertyObject(Object property) {
        assert property != null;
        ValueObject graphProperty = ValueObject.as(getGraphProperty(property));
        if (graphProperty == null) {
            return null;
        }
        return graphProperty.getObject();
    }

    default <T extends Enum<?>> T getGraphPropertyEnum(Object property) {
        assert property != null;
        return ValueEnum.as(getGraphProperty(property)).getEnum();
    }

    default boolean getGraphPropertyBoolean(Object property) {
        assert property != null;
        return ValueBoolean.as(getGraphProperty(property)).getBoolean();
    }

    default void setGraphProperty(Object property, Object object) {
        assert property != null;
        assert object != null;
        ValueObject.as(getGraphProperty(property)).set(object);
    }

    default Type getNodePropertyType(Object property) {
        assert property != null;
        NodeProperty nodeProperty = getNodeProperty(property);
        if (nodeProperty == null) {
            return null;
        }
        return nodeProperty.getType();
    }

    default Type getEdgePropertyType(Object property) {
        assert property != null;
        EdgeProperty edgeProperty = getEdgeProperty(property);
        if (edgeProperty == null) {
            return null;
        }
        return edgeProperty.getType();
    }

    default void registerGraphProperty(Object propertyName, Value value) {
        assert propertyName != null;
        assert value != null;
        registerGraphProperty(propertyName, value.getType());
        setGraphProperty(propertyName, value);
    }

    default void registerGraphProperty(Object propertyName, Type type, Object object) {
        registerGraphProperty(propertyName, type);
        setGraphProperty(propertyName, object);
    }

    default void explore() {
        explore(getInitialNodes());
    }

    @Override
    default StateSetExplicit newInitialStateSet() {
        return new StateSetExplicit(this, getInitialNodes());
    }

    /**
     * Get the successor number of the given successor state. The node parameter
     * must be a node which is a successor of the currently queried state. The
     * method will then return the successor number of this node.
     * 
     * @param node
     *            a successor node of the currently queried node
     * @return successor number of this node
     */
    default int getSuccessorNumber(int fromNode, int toNode) {
        assert fromNode >= 0;
        assert fromNode < getNumNodes();
        assert toNode >= 0;
        assert toNode < getNumNodes();
        for (int succNr = 0; succNr < getNumSuccessors(fromNode); succNr++) {
            if (getSuccessorNode(fromNode, succNr) == toNode) {
                return succNr;
            }
        }
        return -1;
    }

    @Override
    default Type getType(Expression expression) {
        Value graphProperty = getGraphProperty(expression);
        if (graphProperty != null) {
            return graphProperty.getType();
        }
        NodeProperty nodeProperty = getNodeProperty(expression);
        if (nodeProperty != null) {
            return nodeProperty.getType();
        }
        EdgeProperty edgeProperty = getEdgeProperty(expression);
        if (edgeProperty != null) {
            return edgeProperty.getType();
        }
        return null;
    }
}
