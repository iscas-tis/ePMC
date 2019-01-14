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

import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeInteger;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueArrayInteger;
import epmc.value.ValueContentIntArray;

/**
 * Sparse graph for DTMCs, CTMCs, interval DTMCs, interval CTMDs, or automata.
 * Allows for different types of memory storage to be used for its data.
 * This is useful if e.g. value iteration shall be performed in native
 * code rather than in Java.
 * 
 * @author Ernst Moritz Hahn
 */
public final class GraphExplicitSparse implements GraphExplicit {

    public final class EdgePropertySparse implements EdgeProperty {
        private final GraphExplicit graph;
        private Value value;
        private ValueArray content;

        EdgePropertySparse(GraphExplicitSparse graph, Type type) {
            assert graph != null;
            assert type != null;
            this.graph = graph;
            this.value = type.newValue();
            TypeArray typeArray = type.getTypeArray();
            this.content = UtilValue.newArray(typeArray, numTotalOut > 0 ? numTotalOut : 1);
        }

        @Override
        public Value get(int currentNode, int successor) {
            int entry = bounds.getInt(currentNode) + successor;
            content.get(value, entry);
            return value;
        }

        @Override
        public void set(int currentNode, int successor, Value value) {
            int entry = bounds.getInt(currentNode) + successor;
            content = ensureSize(content, entry + 1);
            content.set(value, entry);
        }

        public Value getContent() {
            return content;
        }

        @Override
        public Type getType() {
            return value.getType();
        }

        @Override
        public GraphExplicit getGraph() {
            return graph;
        }
    }

    /** Number of nodes of this graph. */
    private int numNodes;
    /** Sum of fanout of all nodes. */
    private int numTotalOut;
    /** Node bounds of this graph, denoting which successors belong to which node. */
    private ValueArrayInteger bounds;
    /** Array of successor nodes. */
    private ValueArrayInteger successors;
    /** Initial nodes of the graph. */
    private final BitSet initNodes;
    /** Graph, node, and edge properties of the graph. */
    private final GraphExplicitProperties properties;
    /** True if the number of nodes and transitions may be modified after creation. */
    private boolean fixedMode;

    /**
     * Create graph so that the number of nodes and edges can be extended later.
     */
    public GraphExplicitSparse() {
        initNodes = UtilBitSet.newBitSetUnbounded();
        properties = new GraphExplicitProperties(this);
        TypeArray typeArrayInteger = TypeInteger.get().getTypeArray();
        bounds = UtilValue.newArray(typeArrayInteger, 1);
        successors = UtilValue.newArray(typeArrayInteger, 1);
    }

    public GraphExplicitSparse(int numNodes, int numTotalOut) {
        initNodes = UtilBitSet.newBitSetUnbounded();
        properties = new GraphExplicitProperties(this);
        this.fixedMode = true;
        this.numNodes = numNodes;
        this.numTotalOut = numTotalOut;
        TypeArray typeArrayInteger = TypeInteger.get().getTypeArray();
        bounds = UtilValue.newArray(typeArrayInteger, numNodes + 1);
        successors = UtilValue.newArray(typeArrayInteger, numTotalOut);

//        addSettableEdgeProperty(CommonProperties.WEIGHT, TypeWeight.get());
//        addNodePropertyConstant(CommonProperties.STATE, UtilValue.newValue(TypeBoolean.get(), true));
    }

    public NodeProperty addNodePropertyConstant(Object name, Value constant) {
        if (getNodeProperties().contains(name)) {
            return getNodeProperty(name);
        }
        NodeProperty property = new NodePropertyConstant(this, constant);
        registerNodeProperty(name, property);
        return property;
    }

    @Override
    public Value addSettableGraphProperty(Object property, Type type)
    {
        assert property != null;
        assert type != null;
        Value value = type.newValue();
        registerGraphProperty(property, value);
        return getGraphProperty(property);
    }

    @Override
    public NodeProperty addSettableNodeProperty(Object name, Type type) {
        assert name != null;
        assert type != null;
        if (getNodeProperties().contains(name)) {
            return getNodeProperty(name);
        }
        NodePropertyGeneral property = new NodePropertyGeneral(this, type);
        registerNodeProperty(name, property);
        return property;
    }

    @Override
    public EdgeProperty addSettableEdgeProperty(Object name, Type type) {
        if (getEdgeProperties().contains(name)) {
            return getEdgeProperty(name);
        }
        EdgeProperty property = new EdgePropertySparse(this, type);
        registerEdgeProperty(name, property);
        return property;
    }

    @Override
    public int getNumSuccessors(int currentNode) {
        return bounds.getInt(currentNode + 1) - bounds.getInt(currentNode);
    }

    @Override
    public int getSuccessorNode(int currentNode, int successor) {
        int entry = bounds.getInt(currentNode) + successor;
        return successors.getInt(entry);
    }

    @Override
    public void computePredecessors(BitSet nodes) {
        this.properties.computePredecessors(nodes);
    }

    @Override
    public void clearPredecessors() {
        this.properties.clearPredecessors();
    }

    public Value getBounds() {
        return bounds;
    }

    public Value getTargets() {
        return successors;
    }

    public int[] getBoundsJava() {
        return ValueContentIntArray.getContent(bounds);
    }

    public int[] getTargetsJava() {
        return ValueContentIntArray.getContent(successors);
    }

    @Override
    public void setSuccessorNode(int currentNode, int succNr, int succNode) {
        assert succNr >= 0;
        assert succNr < bounds.getInt(currentNode + 1) :
            currentNode + " " + succNr + " " + bounds.getInt(currentNode + 1);
        assert succNode >= 0 : succNode;
        assert !fixedMode | succNode < numNodes : succNode + " " + numNodes;
        int entry = bounds.getInt(currentNode) + succNr;
        successors = ensureSize(successors, entry + 1);
        successors.set(succNode, entry);
    }

    @Override
    public void prepareNode(int currentNode, int numSuccessors)
    {
        assert numSuccessors >= 0;
        int from = bounds.getInt(currentNode);
        bounds = ensureSize(bounds, currentNode + 1 + 1);
        bounds.set(from + numSuccessors, currentNode + 1);
        if (!fixedMode) {
            this.numNodes++;
            this.numTotalOut += numSuccessors;
        }
    }

    @Override
    public int computeNumStates() {
        return numNodes;
    }

    public int getNumStates() {
        return numNodes;
    }

    public int getNumTotalOut() {
        return numTotalOut;
    }

    public void setNumStates(int numStates) {
        this.numNodes = numStates;
    }

    @Override
    public BitSet getInitialNodes() {
        return initNodes;
    }

    @Override
    public void explore(BitSet start) {
    }

    @Override
    public String toString() {
        return GraphExporterDOT.toString(this);
    }

    private <T extends ValueArray> T ensureSize(T array, int newSize) {
        if (fixedMode) {
            return array;
        }
        int size = array.size();
        if (newSize <= size) {
            return array;
        }
        while (size < newSize) {
            size *= 2;
        }
        T result = UtilValue.newArray(array.getType(), size);
        Value entry = array.getType().getEntryType().newValue();
        for (int i = 0; i < array.size(); i++) {
            array.get(entry, i);
            result.set(entry, i);
        }
        return result;
    }

    public void clear() {
        assert !fixedMode;
        numNodes = 0;
        numTotalOut = 0;
    }

    @Override
    public GraphExplicitProperties getProperties() {
        return properties;
    }

    @Override
    public EdgePropertySparse getEdgeProperty(Object property) {
        return (EdgePropertySparse) properties.getEdgeProperty(property);
    }

    @Override
    public int getNumNodes() {
        return numNodes;
    }

    @Override
    public void close() {
    }
}
