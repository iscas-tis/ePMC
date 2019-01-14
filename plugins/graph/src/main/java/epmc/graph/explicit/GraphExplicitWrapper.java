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

import static epmc.error.UtilError.ensure;

import java.util.ArrayList;
import java.util.List;

import epmc.graph.CommonProperties;
import epmc.graph.OptionsTypesGraph;
import epmc.graph.ProblemsGraph;
import epmc.graph.options.OptionsGraph;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeObject;
import epmc.value.TypeObject.StorageType;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueSetString;

// class which takes an existing graph and can be used to speed up computation
// of successors and state attributes by caching

public final class GraphExplicitWrapper implements GraphExplicit {
    /** Empty string. */
    private final static String EMPTY = "";

    private final static class DummyName {
        @Override
        public String toString() {
            return EMPTY;
        }
    }

    private final class NodePropertyWrapperDerived implements NodeProperty {
        private final GraphExplicit graph;
        private final Type typeEntry;
        private ValueArray content;
        private final Value helper;
        private final NodeProperty inner;

        NodePropertyWrapperDerived(GraphExplicitWrapper graph, NodeProperty inner) {
            assert graph != null;
            assert inner != null;
            this.graph = graph;
            this.typeEntry = inner.getType();
            TypeArray typeArray = typeEntry.getTypeArray();
            content = UtilValue.newArray(typeArray, 1);
            this.helper = typeEntry.newValue();
            this.inner = inner;
            for (int node = queriedNodes.nextSetBit(0); node >= 0; node = queriedNodes.nextSetBit(node+1)) {
                update(node, node);
            }
        }

        @Override
        public Value get(int currentNode) {
            queryNode(currentNode);
            content = UtilValue.ensureSize(content, currentNode + 1);
            content.get(helper, currentNode);
            return helper;
        }

        @Override
        public void set(int currentNode, Value value) {
            assert value != null;
            content = UtilValue.ensureSize(content, currentNode + 1);
            content.set(value, currentNode);
        }

        void update(int currentNode, int innerNode) {
            set(currentNode, inner.get(innerNode));
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

    private final class EdgePropertyWrapperSettable implements EdgeProperty {
        private final GraphExplicit graph;
        private final Type typeEntry;
        private ValueArray content;
        private final Value helper;

        EdgePropertyWrapperSettable(GraphExplicitWrapper graph, Type type) {
            assert graph != null;
            assert type != null;
            this.graph = graph;
            this.typeEntry = type;
            TypeArray typeArray = typeEntry.getTypeArray();
            content = UtilValue.newArray(typeArray, 1);
            this.helper = typeEntry.newValue();
        }

        @Override
        public Value get(int currentNode, int successor) {
            queryNode(currentNode);
            assert successor >= 0;
            int entryNr = getCachedSuccessorEntry(currentNode, successor);
            content = UtilValue.ensureSize(content, entryNr + 1);
            content.get(helper, entryNr);
            return helper;
        }

        @Override
        public void set(int currentNode, int successor, Value value) {
            assert value != null;
            assert successor >= 0;
            int entryNr = getCachedSuccessorEntry(currentNode, successor);
            content = UtilValue.ensureSize(content, entryNr + 1);
            content.set(value, entryNr);
        }

        @Override
        public void set(int currentNode, int successor, String value) {
            assert value != null;
            assert successor >= 0;
            ValueSetString.as(helper).set(value);
            set(currentNode, successor, helper);
        }

        @Override
        public Type getType() {
            return typeEntry;
        }

        @Override
        public GraphExplicit getGraph() {
            return graph;
        }
    }

    private final class EdgePropertyWrapperDerived implements EdgeProperty {
        private final GraphExplicit graph;
        private final Type typeEntry;
        private ValueArray content;
        private final Value helper;
        private final EdgeProperty inner;

        EdgePropertyWrapperDerived(GraphExplicitWrapper graph, EdgeProperty inner) {
            assert graph != null;
            assert inner != null;
            this.graph = graph;
            this.typeEntry = inner.getType();
            TypeArray typeArray = typeEntry.getTypeArray();
            content = UtilValue.newArray(typeArray, 1);
            this.helper = typeEntry.newValue();
            this.inner = inner;
            for (int node = queriedNodes.nextSetBit(0); node >= 0; node = queriedNodes.nextSetBit(node+1)) {
                update(node);
            }
        }

        @Override
        public Value get(int currentNode, int successor) {
            assert successor >= 0;
            int entryNr = getCachedSuccessorEntry(currentNode, successor);
            content = UtilValue.ensureSize(content, entryNr + 1);
            content.get(helper, entryNr);
            return helper;
        }

        @Override
        public void set(int currentNode, int successor, Value value) {
            assert value != null;
            assert successor >= 0;
            int entryNr = getCachedSuccessorEntry(currentNode, successor);
            content = UtilValue.ensureSize(content, entryNr + 1);
            content.set(value, entryNr);
        }

        @Override
        public Type getType() {
            return typeEntry;
        }

        void update(int currentNode) {
            for (int succ = 0 ; succ < numSuccessors; succ++) {
                set(currentNode, succ, inner.get(currentNode, succ));
            }
        }

        @Override
        public GraphExplicit getGraph() {
            return graph;
        }
    }


    private final static int DEFAULT_NUM_SUCCESSORS = 1024;

    private final GraphExplicit innerGraph;

    private final BitSet queriedNodes;

    private boolean cache;

    private List<NodePropertyWrapperDerived> derivedNodeProps = new ArrayList<>();
    private List<EdgePropertyWrapperDerived> derivedEdgeProps = new ArrayList<>();

    private int currentNode;
    private int numSuccessors;
    private int[] currentSuccessorNodes;

    private int maxNumSuccessors = Integer.MAX_VALUE;

    private int otfNextSuccessorPlace;
    private final IntArrayList otfSuccessorsStart = new IntArrayList();
    private OptionsTypesGraph.WrapperGraphSuccessorsSize sizeType;
    private final IntArrayList otfSuccorsSizeInt = new IntArrayList();
    private final ShortArrayList otfSuccorsSizeShort = new ShortArrayList();
    private final ByteArrayList otfSuccorsSizeByte = new ByteArrayList();

    private IntArrayList cachedSuccessorNodes = new IntArrayList();

    private final BitSet initNodes;

    private final GraphExplicitProperties properties;

    /* constructors */

    private GraphExplicitWrapper(GraphExplicit innerGraph, boolean cache) {
        this.initNodes = UtilBitSet.newBitSetUnbounded();
        this.queriedNodes = UtilBitSet.newBitSetUnbounded();
        properties = new GraphExplicitProperties(this);
        this.innerGraph = innerGraph;
        if (innerGraph != null) {
            this.maxNumSuccessors = DEFAULT_NUM_SUCCESSORS;
            TypeObject innerGraphType = new TypeObject.Builder()
                    .setClazz(innerGraph.getClass())
                    .setStorageClass(StorageType.DIRECT)
                    .build();
            properties.registerGraphProperty(CommonProperties.INNER_GRAPH, innerGraphType);
            Value graphValue = innerGraphType.newValue(innerGraph);
            properties.setGraphProperty(CommonProperties.INNER_GRAPH, graphValue);
        } else {
            this.maxNumSuccessors = DEFAULT_NUM_SUCCESSORS;
        }
        if (innerGraph != null) {
            initNodes.or(innerGraph.getInitialNodes());
        }
        this.cache = cache;

        numSuccessors = this.maxNumSuccessors;
        if (numSuccessors == Integer.MAX_VALUE) {
            numSuccessors = DEFAULT_NUM_SUCCESSORS;
        }
        currentSuccessorNodes = new int[1];
        ensureSuccessorsSize();


        sizeType = computeSizeType(maxNumSuccessors);

        otfNextSuccessorPlace = 0;

        BitSet oldQueried = queriedNodes.clone();
        queriedNodes.clear();
        for (int node = oldQueried.nextSetBit(0); node >= 0; node = oldQueried.nextSetBit(node+1)) {
            queryNode(node);
        }
    }

    public GraphExplicitWrapper(GraphExplicit innerGraph) {
        this(innerGraph, true);
    }

    public GraphExplicitWrapper() {
        this(null, true);
    }

    /* methods for configuration before start of exploration */

    public void addDerivedGraphProperty(Object property) {
        assert property != null;
        assert innerGraph != null;
        Value value = innerGraph.getGraphProperty(property);
        if (property != CommonProperties.INNER_GRAPH) {
            registerGraphProperty(property, value);
        }
    }

    public void addAllDerivedGraphProperties() {
        assert innerGraph != null;
        for (Object property : innerGraph.getGraphProperties()) {
            addDerivedGraphProperty(property);
        }
    }

    public void addDerivedGraphProperties(Iterable<?> properties) {
        assert innerGraph != null;
        for (Object property : properties) {
            addDerivedGraphProperty(property);
        }
    }

    @Override
    public Value addSettableGraphProperty(Object property, Type type) {
        assert property != null;
        assert type != null;
        Value value = type.newValue();
        registerGraphProperty(property, value);
        return getGraphProperty(property);
    }

    public NodeProperty addDerivedNodeProperty(Object property) {
        assert property != null;
        assert innerGraph != null;
        NodeProperty innerProp = innerGraph.getNodeProperty(property);
        NodePropertyWrapperDerived prop = new NodePropertyWrapperDerived(this, innerProp);
        derivedNodeProps.add(prop);
        registerNodeProperty(property, prop);
        return prop;
    }

    public EdgeProperty addDerivedEdgeProperty(Object property) {
        assert property != null;
        assert innerGraph != null;
        EdgeProperty innerProp = innerGraph.getEdgeProperty(property);
        EdgePropertyWrapperDerived prop = new EdgePropertyWrapperDerived(this, innerProp);
        derivedEdgeProps.add(prop);
        registerEdgeProperty(property, prop);
        return prop;
    }

    @Override
    public NodeProperty addSettableNodeProperty(Object property, Type type) {
        if (property == null) {
            property = new DummyName();
        }
        assert type != null;
        NodeProperty result = new NodePropertyGeneral(this, type);
        registerNodeProperty(property, result);
        return result;
    }

    @Override
    public EdgeProperty addSettableEdgeProperty(Object property, Type type) {
        if (property == null) {
            property = new DummyName();
        }
        assert type != null;
        EdgePropertyWrapperSettable result = new EdgePropertyWrapperSettable(this, type);
        registerEdgeProperty(property, result);
        return result;
    }

    private static OptionsTypesGraph.WrapperGraphSuccessorsSize computeSizeType(int maxNumSuccessors) {
        assert maxNumSuccessors >= 0;
        int numSuccessors = maxNumSuccessors;
        if (numSuccessors == Integer.MAX_VALUE) {
            numSuccessors = DEFAULT_NUM_SUCCESSORS;
        }
        /* prepare storage of successors size */
        OptionsTypesGraph.WrapperGraphSuccessorsSize sizeType = Options.get().getEnum(OptionsGraph.WRAPPER_GRAPH_SUCCESSORS_SIZE);
        switch (sizeType) {
        case INT:
            ensure(numSuccessors <= Integer.MAX_VALUE, ProblemsGraph.WRAPPER_GRAPH_SUCCESSORS_SIZE_TOO_SMALL);
            break;
        case SHORT:
            ensure(numSuccessors <= Short.MAX_VALUE, ProblemsGraph.WRAPPER_GRAPH_SUCCESSORS_SIZE_TOO_SMALL);
            break;
        case BYTE:
            ensure(numSuccessors <= Byte.MAX_VALUE, ProblemsGraph.WRAPPER_GRAPH_SUCCESSORS_SIZE_TOO_SMALL);
            break;
        case SMALLEST:
            if (maxNumSuccessors <= Byte.MAX_VALUE) {
                sizeType = OptionsTypesGraph.WrapperGraphSuccessorsSize.BYTE;
            } else if (maxNumSuccessors <= Short.MAX_VALUE) {
                sizeType = OptionsTypesGraph.WrapperGraphSuccessorsSize.SHORT;
            } else {
                sizeType = OptionsTypesGraph.WrapperGraphSuccessorsSize.INT;
            }
            break;
        default:
            assert false;
            break;
        }
        // TODO fix this
        return OptionsTypesGraph.WrapperGraphSuccessorsSize.INT;
        //        return sizeType;
    }

    private void queryNode(int node) {
        assert node >= 0 : node;
        currentNode = node;
        if (innerGraph != null && (!cache || !queriedNodes.get(node))) {
            numSuccessors = innerGraph.getNumSuccessors(node);
            assert numSuccessors >= 0;
            ensureSuccessorsSize();
            for (int succNr = 0; succNr < numSuccessors; succNr++) {
                int innerSuccessorNode = innerGraph.getSuccessorNode(node, succNr);
                assert innerSuccessorNode >= 0;
                currentSuccessorNodes[succNr] = innerSuccessorNode;
            }

            prepareCachedSuccessors(node, numSuccessors);
            for (NodePropertyWrapperDerived property : derivedNodeProps) {
                property.update(node, node);
            }
            for (EdgePropertyWrapperDerived property : derivedEdgeProps) {
                property.update(node);
            }
            if (cache) {
                storeNode();
            }
        } else {
            numSuccessors = getCachedNumSuccessors(node);
            if (numSuccessors == -1) {
                return;
            }
            for (int succNr = 0; succNr < numSuccessors; succNr++) {
                int entryNr = getCachedSuccessorEntry(node, succNr);
                int chachedSuccNode = cachedSuccessorNodes.getInt(entryNr);
                currentSuccessorNodes[succNr] = chachedSuccNode;
            }
        }
        queriedNodes.set(node);
    }

    @Override
    public void prepareNode(int currentNode, int numSuccessors) {
        assert innerGraph == null;
        queryNode(currentNode);
        assert numSuccessors >= 0;
        queriedNodes.set(currentNode);
        this.numSuccessors = numSuccessors;
        ensureSuccessorsSize();
        prepareCachedSuccessors(currentNode, numSuccessors);
        while (cachedSuccessorNodes.size() <= otfNextSuccessorPlace) {
            cachedSuccessorNodes.add(-1);
        }
    }

    @Override
    public void setSuccessorNode(int currentNode, int succNr, int succState) {
        assert succState >= 0;
        currentSuccessorNodes[succNr] = succState;
        int succEntry = getCachedSuccessorEntry(currentNode, succNr);
        cachedSuccessorNodes.set(succEntry, succState);
    }

    private void storeNode() {
        int fromNode = otfSuccessorsStart.getInt(currentNode);
        while (cachedSuccessorNodes.size() <= fromNode + numSuccessors) {
            cachedSuccessorNodes.add(-1);
        }
        for (int succNr = 0; succNr < numSuccessors; succNr++) {
            int succEntry = getCachedSuccessorEntry(currentNode, succNr);
            int succState = currentSuccessorNodes[succNr];
            assert succState >= 0;
            cachedSuccessorNodes.set(succEntry, succState);
        }
    }

    private void ensureSuccessorsSize() {
        if (currentSuccessorNodes.length < numSuccessors) {
            currentSuccessorNodes = new int[currentSuccessorNodes.length * 2];
            int reserve = 1;
            while (reserve < numSuccessors) {
                reserve <<= 1;
            }
            currentSuccessorNodes = new int[reserve];
        }
    }

    private int getCachedSuccessorEntry(int node, int succNr) {
        assert node >= 0;
        assert node < otfSuccessorsStart.size() : node + " " + otfSuccessorsStart.size();
        return otfSuccessorsStart.getInt(node) + succNr;
    }

    private int getCachedNumSuccessors(int node) {
        assert node >= 0 : node;
        int result;
        switch (sizeType) {
        case INT:
            if (node >= otfSuccorsSizeInt.size()) {
                return -1;
            }
            result = otfSuccorsSizeInt.getInt(node);
            assert result >= 0 : node;
            break;
        case SHORT:
            if (node >= otfSuccorsSizeShort.size()) {
                return -1;
            }
            result = otfSuccorsSizeShort.getShort(node);
            assert result >= 0 : node;
            break;
        case BYTE:
            if (node >= otfSuccorsSizeByte.size()) {
                return -1;
            }
            result = otfSuccorsSizeByte.getByte(node);
            assert result >= 0;
            break;
        default:
            assert false;
            result = -1;
            break;
        }
        assert result >= 0 : node;
        return result;
    }

    private void prepareCachedSuccessors(int node, int numSuccessors) {
        assert numSuccessors >= 0;
        while (otfSuccessorsStart.size() <= node) {
            otfSuccessorsStart.add(-1);
            switch (sizeType) {
            case INT:
                otfSuccorsSizeInt.add(-1);
                break;
            case SHORT:
                otfSuccorsSizeShort.add((short) -1);
                break;
            case BYTE:
                otfSuccorsSizeByte.add((byte) -1);
                break;
            default:
                assert false;
                break;
            }
        }

        int fromNode = otfSuccessorsStart.getInt(node);
        if (fromNode == -1) {
            otfSuccessorsStart.set(node, otfNextSuccessorPlace);            
            otfNextSuccessorPlace += numSuccessors;
            switch (sizeType) {
            case INT:
                otfSuccorsSizeInt.set(node, numSuccessors);
                break;
            case SHORT:
                otfSuccorsSizeShort.set(node, (short) numSuccessors);
                break;
            case BYTE:
                otfSuccorsSizeByte.set(node, (byte) numSuccessors);
                break;
            default:
                assert false;
                break;
            }
        }
    }

    @Override
    public int getNumSuccessors(int node) {
        queryNode(node);
        return numSuccessors;
    }

    @Override
    public int getSuccessorNode(int node, int successorNumber) {
        queryNode(node);
        assert successorNumber < numSuccessors : successorNumber + " " + numSuccessors;
        assert currentSuccessorNodes[successorNumber] >= 0 : currentNode + " " + successorNumber;
        return currentSuccessorNodes[successorNumber];
    }

    @Override
    public void computePredecessors() {
        this.properties.computePredecessors();
    }

    @Override
    public void computePredecessors(BitSet states) {
        this.properties.computePredecessors(states);
    }

    @Override
    public void clearPredecessors() {
        this.properties.clearPredecessors();
    }

    /* functions to efficiently store entries of different types */

    public void addDerivedNodeProperties(Iterable<?> properties)
    {
        assert properties != null;
        for (Object property : properties) {
            addDerivedNodeProperty(property);
        }
    }

    public void addDerivedEdgeProperties(Iterable<Object> properties)
    {
        assert properties != null;
        for (Object property : properties) {
            addDerivedEdgeProperty(property);
        }
    }

    @Override
    public BitSet getInitialNodes() {
        return initNodes;
    }

    @Override
    public String toString() {
        return GraphExporterDOT.toString(this);
    }

    @Override
    public GraphExplicitProperties getProperties() {
        return properties;
    }

    @Override
    public int getNumNodes() {
        return queriedNodes.length();
    }

    @Override
    public void close() {
    }
}
