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

import java.util.Arrays;

import epmc.error.EPMCException;
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
        public ContextValue getContext() {
            return NodePropertyExplorerNode.this.graph.getContextValue();
        }

        @Override
        public Type clone() {
            TypeExplorerNode clone = new TypeExplorerNode();
            return clone;
        }

        @Override
        public ValueExplorerNode newValue() {
            return new ValueExplorerNode();
        }
        
        @Override
        public TypeArray getTypeArray() {
            return typeArray;
        }

		@Override
		public boolean canImport(Type type) {
	        assert type != null;
	        if (this == type) {
	            return true;
	        }
	        return false;
		}
    }

    private final class ValueExplorerNode implements Value {
        /** String containing "null". */
        private final static String NULL = "null";
        /** Index of node in {@link NodePropertyExplorerNode#nodeStore}. */
        private int node;
        /** Whether the value is immutable. */
        private boolean immutable;
        
        @Override
        public Value clone() {
            ValueExplorerNode clone = new ValueExplorerNode();
            clone.node = node;
            return clone;
        }

        @Override
        public Type getType() {
            return type;
        }

        @Override
        public void setImmutable() {
            this.immutable = true;
        }

        @Override
        public boolean isImmutable() {
            return immutable;
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
        
        @Override
        public void set(Value value) {
            assert value != null;
            assert value instanceof ValueExplorerNode;
            ValueExplorerNode other = (ValueExplorerNode) value;
            this.node = other.node;
        }

		@Override
		public int compareTo(Value other) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double distance(Value other) throws EPMCException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean isEq(Value other) throws EPMCException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void set(String value) throws EPMCException {
			// TODO Auto-generated method stub
			
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
        public TypeArray clone() {
            return this;
        }

        @Override
        public ValueArray newValue() {
            return new ValueArrayExplorerNode(this);
        }

		@Override
		public ContextValue getContext() {
			return entryType.getContext();
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
	        return getContext().makeUnique(new TypeArrayGeneric(this));
	    }

	    @Override
	    public String toString() {
	        StringBuilder builder = new StringBuilder();
	        builder.append(getEntryType());
	        builder.append(ARRAY_INDICATOR);
	        return builder.toString();
	    }
    }
    
    private final class ValueArrayExplorerNode extends ValueArray implements ValueContentIntArray {
        /** Content of the array, storing indices of explorer nodes. */
        private int[] content;
		private final TypeArrayExplorerNode type;
		private boolean immutable;

        ValueArrayExplorerNode(TypeArrayExplorerNode type) {
        	assert type != null;
        	this.type = type;
            this.content = new int[0];
        }
        
        @Override
        public ValueArrayExplorerNode clone() {
            ValueArrayExplorerNode clone = (ValueArrayExplorerNode) getType().newValue();
            clone.set(this);
            return clone;
        }

        @Override
        protected void setDimensionsContent() {
            assert !isImmutable();
            if (this.content.length < getTotalSize()) {
                content = new int[getTotalSize()];
            }
        }
        
        @Override
        public void set(Value value, int index) {
            assert !isImmutable();
            assert value != null;
            assert getType().getEntryType().canImport(value.getType());
            assert index >= 0;
            assert index < getTotalSize();
            ValueExplorerNode other = (ValueExplorerNode) value;
            content[index] = other.node;
        }

        @Override
        public void get(Value value, int index) {
            assert value != null;
            assert value.getType().canImport(getType().getEntryType());
            assert index >= 0;
            assert index < getTotalSize();
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
            int hash = Arrays.hashCode(getDimensions());
            for (int entryNr = 0; entryNr < getTotalSize(); entryNr++) {
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
        public void setImmutable() {
        	immutable = true;
        }
        
        @Override
        public boolean isImmutable() {
        	return immutable;
        }

		@Override
		public void set(String value) throws EPMCException {
			// TODO Auto-generated method stub
			
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
     * @throws EPMCException thrown in case of problems
     */
    NodePropertyExplorerNode(GraphExplicit graph, Explorer explorer, BitStoreableToNumber nodeStore) throws EPMCException {
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
    public Value get(int graphNode) throws EPMCException {
//        int graphNode = graph.getQueriedNode();
        if (graphNode >= numStates) {
            value.node = -1;
            lastNode = graphNode;
            return value;
        }
        System.out.println("NS " + numStates);
        if (lastNode != graphNode) {
            value.node = graphNode;
            nodeStore.fromNumber(node, graphNode);
            lastNode = graphNode;
        }
        return value;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getObject(int graphNode) throws EPMCException {
//        int graphNode = graph.getQueriedNode();
        if (graphNode >= numStates) {
            value.node = -1;
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
    public void set(int node, Value value) throws EPMCException {
        assert value != null;
        assert false;
    }

    @Override
    public Type getType() {
        return type;
    }
}
