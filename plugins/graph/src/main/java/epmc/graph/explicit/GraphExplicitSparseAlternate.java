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

import java.nio.ByteBuffer;
import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeHasNativeArray;
import epmc.value.TypeInteger;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueArrayInteger;
import epmc.value.ValueContentIntArray;
import epmc.value.ValueContentMemory;

// for MDPs, CTMDPs, EDTMCs, ECTMCs, turn-based two-player games;
// for value iteration

public class GraphExplicitSparseAlternate implements GraphExplicit {
    public final class NodePropertySparseNondetConstant implements NodeProperty {
        private final GraphExplicit graph;
        private final Value constant;

        NodePropertySparseNondetConstant(GraphExplicitSparseAlternate graph, Value constant) {
            assert graph != null;
            assert constant != null;
            this.graph = graph;
            this.constant = UtilValue.clone(constant);
        }
        
        @Override
        public Value get(int node) {
            return constant;
        }
        
        @Override
        public void set(int node, Value value) throws EPMCException {
            assert value != null;
        }

        @Override
        public Type getType() {
            return constant.getType();
        }

        @Override
        public GraphExplicit getGraph() {
            return graph;
        }
    }

    public final class NodePropertySparseNondetRanged implements NodeProperty {
        private final GraphExplicit graph;
        private final int[] ranges;
        private final Value[] constants;
        private final Value helper;

        NodePropertySparseNondetRanged(GraphExplicitSparseAlternate graph, int[] ranges, Type type) {
            assert graph != null;
            assert ranges != null;
            assert type != null;
            this.graph = graph;
            this.ranges = ranges;
            this.constants = new Value[ranges.length];
            for (int i = 0; i < ranges.length; i++) {
                this.constants[i] = type.newValue();
            }
            this.helper = type.newValue();
        }
        
        @Override
        public Value get(int currentNode) {
            int index = Arrays.binarySearch(ranges, currentNode);
            if (index < 0) {
                index = -index - 1;
            } else {
                index++;
            }
            return constants[index];
        }
        
        @Override
        public void set(int currentNode, Value value) throws EPMCException {
            assert value != null;
            int index = Arrays.binarySearch(ranges, currentNode);
            if (index < 0) {
                index = -index - 1;
            } else {
                index++;
            }
            constants[index].set(value);
        }

        @Override
        public Type getType() {
            return helper.getType();
        }

        @Override
        public GraphExplicit getGraph() {
            return graph;
        }
    }

    public interface EdgePropertySparseNondet extends EdgeProperty {

        default EdgePropertySparseNondetOnlyNondet asSparseNondetOnlyNondet() {
            return (EdgePropertySparseNondetOnlyNondet) this;
        }
        
        public void setForState(Value value, int succNr);

        public void setForNonDet(Value value, int interSuccNr);
    }
    
    public final class EdgePropertySparseNondetGeneral implements EdgePropertySparseNondet {
        private final GraphExplicit graph;
        private final Value value;
        private ValueArray content;
        private ValueArray contentND;
		private int nextND;
		private int nextS;

        EdgePropertySparseNondetGeneral(GraphExplicitSparseAlternate graph, Type type) {
            assert graph != null;
            assert type != null;
            this.graph = graph;
            this.value = type.newValue();
            TypeArray typeArray = forNative
                    ? TypeHasNativeArray.getTypeNativeArray(type)
                    : type.getTypeArray();
            this.content = UtilValue.newArray(typeArray, 1);
            this.contentND = UtilValue.newArray(typeArray, 1);
        }
        
        @Override
        public Value get(int currentNode, int successor) {
            if (currentNode < numStates) {
            	int entryNr = stateBounds.getInt(currentNode) + successor;
                content = ensureSize(content, entryNr + 1);
            	content.get(value, entryNr);
            } else {
            	int entryNr = nondetBounds.getInt(currentNode - numStates) + successor;
            	contentND = ensureSize(contentND, entryNr + 1);
            	contentND.get(value, entryNr);
            }
            return value;
        }

        @Override
        public void set(int currentNode, int successor, Value value) {
        	if (currentNode < numStates) {
                int entryNr = stateBounds.getInt(currentNode) + successor;
                content = ensureSize(content, entryNr + 1);
                content.set(value, entryNr);
        	} else {
                int entryNr = nondetBounds.getInt(currentNode - numStates) + successor;
                contentND = ensureSize(contentND, entryNr + 1);
                contentND.set(value, entryNr);
        	}
        }

        @Override
        public Type getType() {
            return value.getType();
        }
        
        public Value getContent() {
            return content;
        }

        @Override
        public GraphExplicit getGraph() {
            return graph;
        }

        @Override
        public void setForState(Value value, int succNr) {
            content = ensureSize(content, nextS + 1);
            content.set(value, nextS);
            nextS++;
        }

        @Override
        public void setForNonDet(Value value, int interSuccNr) {
//            System.out.println("SN  " + currentNode + " " + interSuccNr + " " + entryNr + " " + value);
            contentND = ensureSize(contentND, nextND + 1);
            contentND.set(value, nextND);
            nextND++;
        }
        
    }

    public final class EdgePropertySparseNondetOnlyNondet implements EdgePropertySparseNondet {
        private final GraphExplicit graph;
        private final Value value;
        private ValueArray content;

        EdgePropertySparseNondetOnlyNondet(GraphExplicitSparseAlternate graph, Type type) {
            assert graph != null;
            assert type != null;
            this.graph = graph;
            this.value = type.newValue();
            TypeArray typeArray = forNative
                    ? TypeHasNativeArray.getTypeNativeArray(type)
                    : type.getTypeArray();
            assert typeArray != null : type + " " + forNative;
            this.content = UtilValue.newArray(typeArray, numProb);
        }
        
        private int getEntryNumber(int currentNode, int successor) {
            assert successor >= 0;
            assert nondetBounds.getInt(currentNode - numStates) + successor
            < nondetBounds.getInt(currentNode - numStates + 1)
            : currentNode + " " + successor + " " +
            nondetBounds.getInt(currentNode - numStates) + " " +
            nondetBounds.getInt(currentNode - numStates + 1) + " " +
            numStates;
            return nondetBounds.getInt(currentNode - numStates) + successor;
        }
        
        @Override
        public Value get(int currentNode, int successor) {
            if (currentNode < numStates) {
                ValueAlgebra.asAlgebra(value).set(-1);
            } else {
                int entryNr = getEntryNumber(currentNode, successor);
                content = ensureSize(content, entryNr + 1);
                content.get(value, entryNr);
            }
            return value;
        }

        @Override
        public void set(int currentNode, int successor, Value value) {
            if (currentNode < numStates) {
                
            } else {
                int entryNr = getEntryNumber(currentNode, successor);
                content = ensureSize(content, entryNr + 1);
                content.set(value, entryNr);
            }
        }
        
        @Override
        public Type getType() {
            return value.getType();
        }
        
        public Value getContent() {
            return content;
        }

        @Override
        public GraphExplicit getGraph() {
            return graph;
        }

        @Override
        public void setForState(Value value, int succNr) {
        }

        @Override
        public void setForNonDet(Value value, int successor) {
//            int entryNr = getEntryNumber(successor);
            int entryNr = nondetBounds.getInt(numNondet - 1) + successor;
            content = ensureSize(content, entryNr + 1);
            content.set(value, entryNr);
        }
    }
    
    private final ContextValue contextValue;
    private final boolean forNative;
    private int numStates;
    private int numNondet;
    private int numProb;
    private ValueArrayInteger stateBounds;
    private ValueArrayInteger nondetBounds;
    private ValueArrayInteger successors;
    private int maxNumSuccessors;
    private int lastStatePrepared = -1;
    private int lastNondetPrepared = -1;
    private final BitSet initNodes;
    private final GraphExplicitProperties properties;
    private boolean fixedMode;

    public GraphExplicitSparseAlternate(ContextValue contextValue, boolean forNative) {
        this.initNodes = UtilBitSet.newBitSetUnbounded();
        properties = new GraphExplicitProperties(this, contextValue);
        this.forNative = forNative;
        this.contextValue = contextValue;
        assert contextValue != null;
        TypeArray typeArrayInteger = forNative
                ? TypeInteger.get(contextValue).getTypeArrayNative()
                : TypeInteger.get(contextValue).getTypeArray();
        stateBounds = UtilValue.newArray(typeArrayInteger, 2);
        nondetBounds = UtilValue.newArray(typeArrayInteger, 2);
        successors = UtilValue.newArray(typeArrayInteger, 1);
        int successorsTotalSize = successors.size();
        for (int index = 0; index < successorsTotalSize; index++) {
        	successors.set(-1, index);
        }

    }
    
    public GraphExplicitSparseAlternate(ContextValue contextValue, boolean forNative,
            int numStates, int numNondet, int numProb) {
        this.initNodes = UtilBitSet.newBitSetUnbounded();
        properties = new GraphExplicitProperties(this, contextValue);
        this.fixedMode = true;
        this.forNative = forNative;
        this.numStates = numStates;
        this.numNondet = numNondet;
        this.numProb = numProb;
        this.contextValue = contextValue;
        TypeArray typeArrayInteger = forNative
                ? TypeInteger.get(contextValue).getTypeArrayNative()
                : TypeInteger.get(contextValue).getTypeArray();
        stateBounds = UtilValue.newArray(typeArrayInteger, numStates + 1);
        nondetBounds = UtilValue.newArray(typeArrayInteger, numNondet + 1);
        successors = UtilValue.newArray(typeArrayInteger, numProb);
        int successorsTotalSize = successors.size();
        for (int index = 0; index < successorsTotalSize; index++) {
        	successors.set(-1, index);
        }
    }

    public void computeMaxNumSuccessors() {
        for (int state = 0; state < numStates; state++) {
            int size = stateBounds.getInt(state + 1) - stateBounds.getInt(state);
            maxNumSuccessors = Math.max(maxNumSuccessors, size);
        }
        for (int nondet = 0; nondet < numNondet; nondet++) {
            int size = nondetBounds.getInt(nondet + 1) - nondetBounds.getInt(nondet);
            maxNumSuccessors = Math.max(maxNumSuccessors, size);
        }
    }

    @Override
    public int getNumSuccessors(int node) {
        assert node >= 0 : node;
        assert !fixedMode | node < numStates + numNondet : node + " " + numStates + " " + numNondet;
        if (node < numStates) {
            int from = stateBounds.getInt(node);
            int to = stateBounds.getInt(node + 1);
            return to - from;
        } else {
        	nondetBounds = ensureSize(nondetBounds, node - numStates + 1 + 1);
            int from = nondetBounds.getInt(node - numStates);
            int to = nondetBounds.getInt(node - numStates + 1);
            return to - from;
        }
    }

    @Override
    public int getSuccessorNode(int currentNode, int successor) {
        assert successor >= 0;
        if (currentNode < numStates) {
            return stateBounds.getInt(currentNode) + successor + numStates;
        } else {
            int succNr = nondetBounds.getInt(currentNode - numStates) + successor;
            return successors.getInt(succNr);
        }
    }

    @Override
    public void computePredecessors(BitSet states) throws EPMCException {
        this.properties.computePredecessors(states);
    }

    @Override
    public void clearPredecessors() {
        this.properties.clearPredecessors();
    }

    @Override
    public void prepareNode(int currentNode, int numSuccessors) throws EPMCException {
        assert numSuccessors >= 0;
        if (currentNode < numStates) {
            assert lastStatePrepared + 1 == currentNode : lastStatePrepared + " " + currentNode;
            lastStatePrepared++;
            int from = stateBounds.getInt(currentNode);
            stateBounds = ensureSize(stateBounds, currentNode + 1 + 1);
            stateBounds.set(from + numSuccessors, currentNode + 1);
        } else {
            assert !fixedMode || lastNondetPrepared + 1 == currentNode - numStates : lastNondetPrepared + " " + (currentNode - numStates);
            lastNondetPrepared++;
            int from = nondetBounds.getInt(currentNode - numStates);
            nondetBounds = ensureSize(nondetBounds, currentNode - numStates + 1 + 1);
            nondetBounds.set(from + numSuccessors, currentNode - numStates + 1);
        }
    }
    
    @Override
    public void setSuccessorNode(int currentNode, int succNr, int succNode) {
        assert succNr >= 0 : succNr;
        assert succNode >= 0 : succNode;
        if (currentNode < numStates) {
            assert succNode >= numStates : succNode + " " + numStates + " " + numStates + " " + currentNode;
            assert succNr >= 0;
        } else {
            assert !fixedMode || succNode < numStates : currentNode + " " + succNode + " " + numStates;
            int entryNr = nondetBounds.getInt(currentNode - numStates) + succNr;
            successors = ensureSize(successors, entryNr + 1);
            successors.set(succNode, entryNr);
        }
    }
    
    @Override
    public Value addSettableGraphProperty(Object property, Type type)
            throws EPMCException {
        assert property != null;
        assert type != null;
        Value value = type.newValue();
        registerGraphProperty(property, value);
        return getGraphProperty(property);
    }

    @Override
    public NodeProperty addSettableNodeProperty(Object property, Type type) {
        assert property != null;
        assert type != null : property;
        NodeProperty nodeProperty;
        if (getNodeProperties().contains(property)) {
            return getNodeProperty(property);
        }
//        if (fixedMode && property == CommonProperties.STATE) {
  //          nodeProperty = new NodePropertySparseNondetRanged(this, new int[]{numStates, numStates + numNondet}, type);
    //    } else {
            nodeProperty = new NodePropertyGeneral(this, type, forNative);
     //   }
        registerNodeProperty(property, nodeProperty);
        return nodeProperty;
    }
    
    @Override
    public EdgePropertySparseNondet addSettableEdgeProperty(Object property, Type type) {
        assert property != null;
        assert type != null;
        EdgePropertySparseNondet edgeProperty;
        if (getEdgeProperties().contains(property)) {
            return getEdgeProperty(property);
        }
        if (property == CommonProperties.WEIGHT) {
            edgeProperty = new EdgePropertySparseNondetOnlyNondet(this, type);
        } else {
            edgeProperty = new EdgePropertySparseNondetGeneral(this, type);
        }
        registerEdgeProperty(property, edgeProperty);
        return edgeProperty;
    }
   
    public int getNumNondet() {
        return numNondet;
    }
    
    public boolean isNative() {
        return forNative;
    }

    public ValueArrayInteger getStateBounds() {
        return stateBounds;
    }

    public int[] getStateBoundsJava() {
        return ValueContentIntArray.getContent(stateBounds);
    }
    
    public ByteBuffer getStateBoundsNative() {
        return ValueContentMemory.getMemory(stateBounds);
    }
    
    public int[] getNondetBoundsJava() {
        return ValueContentIntArray.getContent(nondetBounds);
    }
    
    public ByteBuffer getNondetBoundsNative() {
        return ValueContentMemory.getMemory(nondetBounds);
    }
    
    public Value getTargets() {
        return successors;
    }
    
    public int[] getTargetsJava() {
        return ValueContentIntArray.getContent(successors);
    }
    
    public ByteBuffer getTargetsNative() {
        return ValueContentMemory.getMemory(successors);
    }
    
    @Override
    public BitSet getInitialNodes() {
        return initNodes;
    }

    @Override
    public Options getOptions() {
        return contextValue.getOptions();
    }
    
    @Override
    public ContextValue getContextValue() {
        return contextValue;
    }
    
    @Override
    public void explore(BitSet start) throws EPMCException {
    }

    @Override
    public int computeNumStates() {
        return numStates;
    }

    public void setNumStates(int numStates) {
        this.numStates = numStates;
    }
    
    @Override
    public String toString() {
        return GraphExporterDOT.toString(this);
    }
    
    private <T extends ValueArray> T ensureSize(T array, int newSize) {
        if (fixedMode) {
//            return;
        }
        int size = array.size();
        if (newSize <= size) {
            return array;
        }
        if (size == 0) {
            size = 1;
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

    public void prepareState(int numSuccessors) {
        int bound = stateBounds.getInt(numStates) + numSuccessors;
        stateBounds = ensureSize(stateBounds, numStates + 1 + 1);
        stateBounds.set(bound, numStates + 1);
        numStates++;
    }

    public void prepareNondet(int numSuccessors) {
        int bound = nondetBounds.getInt(numNondet) + numSuccessors;
        nondetBounds = ensureSize(nondetBounds, numNondet + 1 + 1);
        nondetBounds.set(bound, numNondet + 1);
        numNondet++;
    }

    public void setNondetSuccessor(int succNr, int state) {
        int offset = nondetBounds.getInt(numNondet - 1) + succNr;
        successors = ensureSize(successors, offset + 1);
        successors.set(state, offset);
    }
    
    public void clear() {
        numStates = 0;
        numNondet = 0;
        stateBounds.set(0, 1);
    }

    @Override
    public GraphExplicitProperties getProperties() {
        return properties;
    }
    
    @Override
    public EdgePropertySparseNondet getEdgeProperty(Object property) {
        return (EdgePropertySparseNondet) properties.getEdgeProperty(property);
    }

    @Override
    public int getNumNodes() {
        return numStates + numNondet;
    }
    
	@Override
	public void close() {
	}
}
