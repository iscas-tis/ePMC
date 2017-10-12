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

import epmc.graph.explorer.Explorer;
import epmc.graph.explorer.ExplorerNode;
import epmc.util.BitStoreableToNumber;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeArrayGeneric;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueContentIntArray;

/**
 * Node property to store explorer nodes of underlying explorer.
 * 
 * @author Ernst Moritz Hahn
 */
final class NodePropertyExplorerNode implements NodeProperty {
    private final class TypeExplorerNode implements Type {
        @Override
        public ValueExplorerNode newValue() {
            return new ValueExplorerNode();
        }

        @Override
        public TypeArray getTypeArray() {
            return typeArray;
        }
    }

    private final class ValueExplorerNode implements Value {
        /** String containing "null". */
        private final static String NULL = "null";
        /** Index of node in {@link NodePropertyExplorerNode#nodeStore}. */
        private int node;

        @Override
        public Type getType() {
            return type;
        }

        @Override
        public String toString() {
            if (node == -1) {
                return NULL;
            }
            NodePropertyExplorerNode.this.nodeStore.fromNumber(
                    NodePropertyExplorerNode.this.node, this.node);
            return NodePropertyExplorerNode.this.node.toString();
        }
    }

    private final class TypeArrayExplorerNode implements TypeArray {
        private final static String ARRAY_INDICATOR = "[](explorer-node)";
        private final Type entryType;

        protected TypeArrayExplorerNode(Type entryType) {
            assert entryType != null;
            this.entryType = entryType;
        }

        @Override
        public ValueArray newValue() {
            return new ValueArrayExplorerNode(this);
        }

        @Override
        public Type getEntryType() {
            return entryType;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TypeArrayExplorerNode)) {
                return false;
            }
            TypeArrayExplorerNode other = (TypeArrayExplorerNode) obj;
            return this.getEntryType().equals(other.entryType);
        }

        @Override
        public int hashCode() {
            int hash = 0;
            hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
            hash = getEntryType().hashCode() + (hash << 6) + (hash << 16) - hash;
            return hash;
        }

        @Override
        public TypeArray getTypeArray() {
            return ContextValue.get().makeUnique(new TypeArrayGeneric(this));
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(getEntryType());
            builder.append(ARRAY_INDICATOR);
            return builder.toString();
        }
    }

    private final class ValueArrayExplorerNode implements ValueArray, ValueContentIntArray {
        /** Content of the array, storing indices of explorer nodes. */
        private int[] content;
        private final TypeArrayExplorerNode type;
        private int size;

        ValueArrayExplorerNode(TypeArrayExplorerNode type) {
            assert type != null;
            this.type = type;
            this.content = new int[0];
        }

        @Override
        public void set(Value value, int index) {
            assert value != null;
            assert index >= 0;
            assert index < size();
            ValueExplorerNode other = (ValueExplorerNode) value;
            content[index] = other.node;
        }

        @Override
        public void get(Value value, int index) {
            assert value != null;
            assert index >= 0;
            assert index < size();
            int entry = content[index];
            ValueExplorerNode other = (ValueExplorerNode) value;
            other.node = entry;
        }

        @Override
        public int[] getIntArray() {
            return content;
        }

        @Override
        public int hashCode() {
            int hash = size();
            for (int entryNr = 0; entryNr < size(); entryNr++) {
                int entry = content[entryNr];
                hash = entry + (hash << 6) + (hash << 16) - hash;
            }
            return hash;
        }

        @Override
        public TypeArrayExplorerNode getType() {
            return type;
        }

        @Override
        public void setSize(int size) {
            content = new int[size];
            this.size = size;
        }

        @Override
        public int size() {
            return size;
        }
    }

    /** Graph this node property belongs to. */
    private final GraphExplicit graph;
    /** Explorer node used as return value. */
    private final ExplorerNode node;
    /** Type of this node property. */
    private final TypeExplorerNode type;
    private final TypeArrayExplorerNode typeArray;

    /** Value used as return value. */
    private final ValueExplorerNode value;
    /** Node store to read nodes from. */
    private BitStoreableToNumber nodeStore;
    /** Last state of graph for which we obtained a node. */
    private int lastNode = -1;
    /** Number of states of the graph to which this node property belongs. */
    private int numStates;

    /**
     * Construct new explorer node graph property.
     * None of the parameters may be {@code null}.
     * 
     * @param graph graph to which this property belongs
     * @param explorer explorer graph was constructed from
     * @param nodeStore storage of the nodes of the explorer
     */
    NodePropertyExplorerNode(GraphExplicit graph, Explorer explorer, BitStoreableToNumber nodeStore) {
        assert graph != null;
        assert explorer != null;
        assert nodeStore != null;
        this.graph = graph;
        this.node = explorer.newNode();
        this.type = new TypeExplorerNode();
        this.typeArray = new TypeArrayExplorerNode(type);
        this.value = type.newValue();
        this.nodeStore = nodeStore;
    }

    void setNumStates(int numStates) {
        assert numStates >= 0;
        this.numStates = numStates;
    }

    @Override
    public GraphExplicit getGraph() {
        return graph;
    }

    @Override
    public Value get(int graphNode) {
        if (graphNode >= numStates) {
            value.node = -1;
            lastNode = graphNode;
            return value;
        }
        if (lastNode != graphNode) {
            value.node = graphNode;
            nodeStore.fromNumber(node, graphNode);
            lastNode = graphNode;
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getObject(int graphNode) {
        if (graphNode >= numStates) {
            value.node = -1;
            lastNode = graphNode;
            return null;
        }
        if (lastNode != graphNode) {
            value.node = graphNode;
            nodeStore.fromNumber(node, graphNode);
            lastNode = graphNode;
        }
        return (T) node;
    }

    @Override
    public void set(int node, Value value) {
        assert value != null;
        assert false;
    }

    @Override
    public Type getType() {
        return type;
    }
}
