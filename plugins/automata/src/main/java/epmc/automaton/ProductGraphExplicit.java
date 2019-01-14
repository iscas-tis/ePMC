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

package epmc.automaton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import epmc.expression.Expression;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitProperties;
import epmc.graph.explicit.NodeProperty;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.TypeObject;
import epmc.value.TypeObject.StorageType;
import epmc.value.Value;
import epmc.value.ValueInteger;
import epmc.value.ValueObject;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

public final class ProductGraphExplicit implements GraphExplicit {

    @FunctionalInterface
    public interface NextAutomatonState {
        boolean move(int node);
    }

    /**
     * Product graph explicit automaton builder.
     * We use the builder pattern, because
     * <ul>
     * <li>the number of parameters of the constructor we sometimes need
     * is already quite large, and the call to the constructor would be
     * hard to understand without the builder pattern usage,</li>
     * <li>we do not want to introduce dozends of different constructors,
     * or apply the telescoping constructor anti pattern, which would make
     * maintenance difficult,</li>
     * <li>it is very likely that new parameters will be added, which would
     * make it hard to maintain compatiblity to placed in the code already
     * using the explicit product graph.</li>
     * </ul>
     * 
     * @author Ernst Moritz Hahn
     */
    public final static class Builder {
        private GraphExplicit model;
        private BitSet modelIinitNodes;
        private Automaton automaton;
        private int automatonInitState = -1;
        private final List<Object> graphProperties = new ArrayList<>();
        private final List<Object> nodeProperties = new ArrayList<>();
        private final List<Object> edgeProperties = new ArrayList<>();
        private int modelInitialNode = -1;
        private boolean manual;
        private NextAutomatonState nextAutomatonState;

        public Builder setModel(GraphExplicit model) {
            this.model = model;
            return this;
        }

        private GraphExplicit getModel() {
            return model;
        }

        public Builder setModelInitialNodes(BitSet modelInitNodes) {
            this.modelIinitNodes = modelInitNodes;
            return this;
        }

        private int getModelInitialNode() {
            return modelInitialNode;
        }

        public Builder setModelInitialNode(int initialNode) {
            this.modelInitialNode = initialNode;
            return this;
        }

        private BitSet getModelIinitialNodes() {
            return modelIinitNodes;
        }

        public Builder setAutomaton(Automaton automaton) {
            this.automaton = automaton;
            return this;
        }

        private Automaton getAutomaton() {
            return automaton;
        }

        public Builder setAutomatonInitialState(int automatonInitialState) {
            this.automatonInitState = automatonInitialState;
            return this;
        }

        private int getAutomatonInitialState() {
            return automatonInitState;
        }

        public Builder addGraphProperty(Object property) {
            graphProperties.add(property);
            return this;
        }

        public Builder addGraphProperties(Collection<Object> properties) {
            graphProperties.addAll(properties);
            return this;
        }

        private List<Object> getGraphProperties() {
            return graphProperties;
        }

        public Builder addNodeProperty(Object property) {
            nodeProperties.add(property);
            return this;
        }

        public Builder addNodeProperties(Collection<? extends Object> properties) {
            nodeProperties.addAll(properties);
            return this;
        }

        private List<Object> getNodeProperties() {
            return nodeProperties;
        }

        public Builder addEdgeProperty(Object property) {
            edgeProperties.add(property);
            return this;
        }

        public Builder addEdgeProperties(Collection<Object> properties) {
            edgeProperties.addAll(properties);
            return this;
        }

        private List<Object> getEdgeProperties() {
            return edgeProperties;
        }

        public Builder setManual(boolean manual) {
            this.manual = manual;
            return this;
        }

        public Builder setManual() {
            this.manual = true;
            return this;
        }

        private boolean isManual() {
            return manual;
        }

        public void setNextAutomatonState(NextAutomatonState nextAutomatonState) {
            this.nextAutomatonState = nextAutomatonState;
        }

        private NextAutomatonState getNextAutomatonState() {
            return nextAutomatonState;
        }

        public ProductGraphExplicit build() {
            return new ProductGraphExplicit(this);
        }
    }

    private final class NodePropertyDerived implements NodeProperty {
        private final GraphExplicit graph;
        private final NodeProperty from;

        NodePropertyDerived(GraphExplicit graph, NodeProperty from) {
            assert graph != null;
            assert from != null;
            this.graph = graph;
            this.from = from;
        }

        @Override
        public Value get(int node) {
            return from.get(getModelNode(node));
        }

        @Override
        public void set(int node, Value value) {
            assert false;
        }

        @Override
        public Type getType() {
            return from.getType();
        }

        @Override
        public GraphExplicit getGraph() {
            return graph;
        }
    }

    private final class NodePropertySettable implements NodeProperty {
        private final GraphExplicit graph;
        private final Value value;

        NodePropertySettable(GraphExplicit graph, Value value) {
            assert graph != null;
            assert value != null;
            this.graph = graph;
            this.value = value;
        }

        @Override
        public Value get(int node) {
            if (queriedNode != node) {
                long combined = numberToCombined[node];
                queryNode(combined);
                queriedNode = node;
            }
            return value;
        }

        @Override
        public void set(int node, Value value) {
            assert false;
        }

        @Override
        public Type getType() {
            return value.getType();
        }

        @Override
        public GraphExplicit getGraph() {
            return graph;
        }

        @Override
        public void set(int node, Object object) {
            assert object != null;
            assert ValueObject.is(value);
            assert false;
        }    

        @Override
        public void set(int node, int value) {
            assert false;
        }    

        @Override
        public void set(int node, Enum<?> object) {
            assert object != null;
            assert false;
        }

    }

    private final class EdgePropertySettable implements EdgeProperty {
        private final GraphExplicit graph;
        private final Value[] value;

        EdgePropertySettable(GraphExplicit graph, Value[] value) {
            assert graph != null;
            assert value != null;
            this.graph = graph;
            this.value = value;
        }

        @Override
        public Value get(int node, int succNr) {
            if (queriedNode != node) {
                long combined = numberToCombined[node];
                queryNode(combined);
                queriedNode = node;
            }
            return value[succNr];
        }

        @Override
        public void set(int node, int succ, Value value) {
            assert false;
        }

        @Override
        public Type getType() {
            return value[0].getType();
        }

        @Override
        public GraphExplicit getGraph() {
            return graph;
        }
    }

    private final class EdgePropertyDerived implements EdgeProperty {
        private final GraphExplicit graph;
        private final EdgeProperty from;

        EdgePropertyDerived(ProductGraphExplicit graph, EdgeProperty from) {
            assert graph != null;
            assert from != null;
            this.graph = graph;
            this.from = from;
        }

        @Override
        public Value get(int node, int successor) {
            assert successor >= 0 : successor;
            assert successor < numSuccessors : successor;
            if (automaton.isDeterministic()) {
                return from.get(getModelNode(node), successor);
            } else {
                return from.get(getModelNode(node), successor % model.getNumSuccessors(node));                
            }
        }

        @Override
        public void set(int node, int succ, Value value) {
            assert false;
        }

        @Override
        public Type getType() {
            return from.getType();
        }

        @Override
        public GraphExplicit getGraph() {
            return graph;
        }
    }

    private final GraphExplicit model;
    private final Long2IntOpenHashMap combinedToNumber = new Long2IntOpenHashMap();
    private long[] numberToCombined = new long[1];
    private int numberToCombinedSize;
    private int numNodes;
    private final Automaton automaton;
    private int[] successorNodes;
    private long[] manualSuccessorNodes;
    private int numSuccessors = -1;

    private final ValueObject propAutomatonValue ;
    private final ValueObject[] propAutomatonValues;
    private final ValueInteger propNodeModelValue;
    private final ValueObject automatonState;
    private final NodeProperty[] expressionProps;
    private final Value[] queryArray;
    private final NodeProperty isModelPropState;
    private final BitSet initStates;
    private final long[] manualInitNodes;
    private final GraphExplicitProperties properties;
    private final boolean manualEnumeration;
    private final NextAutomatonState nextAutomatonState;
    private int queriedNode = -1;

    private ProductGraphExplicit(Builder builder) {
        assert builder != null;
        assert builder.getModel() != null;
        assert builder.getAutomaton() != null;
        combinedToNumber.defaultReturnValue(-1);
        manualEnumeration = builder.isManual();
        initStates = UtilBitSet.newBitSetUnbounded();
        properties = new GraphExplicitProperties(this);
        TypeObject typeModel = new TypeObject.Builder()
                .setClazz(GraphExplicit.class)
                .build();
        TypeObject typeAutomaton = new TypeObject.Builder()
                .setClazz(Automaton.class)
                .build();
        this.model = builder.getModel();
        int automatonNode = builder.getAutomatonInitialState();
        if (automatonNode == -1) {
            automatonNode = builder.getAutomaton().getInitState();
        }
        BitSet modelInitNodes = builder.getModelIinitialNodes();
        if (modelInitNodes == null) {
            modelInitNodes = UtilBitSet.newBitSetUnbounded();
            assert builder.getModelInitialNode() >= 0;
            modelInitNodes.set(builder.getModelInitialNode());
        }
        if (manualEnumeration) {
            manualInitNodes = new long[modelInitNodes.cardinality()];
            int initStateNr = 0;
            for (int node = modelInitNodes.nextSetBit(0); node >= 0; node = modelInitNodes.nextSetBit(node+1)) {
                manualInitNodes[initStateNr] = combine(node, automatonNode);
                initStateNr++;
            }
        } else {
            for (int node = modelInitNodes.nextSetBit(0); node >= 0; node = modelInitNodes.nextSetBit(node+1)) {
                initStates.set(combineToNode(node, automatonNode));
            }
            manualInitNodes = null;
        }
        this.automaton = builder.getAutomaton();
        int reservedSuccessors = Integer.MAX_VALUE;
        if (reservedSuccessors == Integer.MAX_VALUE) {
            reservedSuccessors = 1024;
        }
        if (manualEnumeration) {
            this.manualSuccessorNodes = new long[reservedSuccessors];
        } else {
            this.successorNodes = new int[reservedSuccessors];
        }
        for (Object propName : builder.getGraphProperties()) {
            Value value = builder.getModel().getGraphProperty(propName);
            registerGraphProperty(propName, value);
        }
        registerGraphProperty(CommonProperties.MODEL_GRAPH, typeModel, builder.getModel());
        registerGraphProperty(CommonProperties.AUTOMATON, typeAutomaton, builder.getAutomaton());

        for (Object propName : builder.getNodeProperties()) {
            NodeProperty origProp = builder.getModel().getNodeProperty(propName);
            assert origProp != null : propName;
            NodeProperty derivedProp = new NodePropertyDerived(this, origProp);
            registerNodeProperty(propName, derivedProp);
        }

        for (Object propName : builder.getEdgeProperties()) {
            EdgeProperty origProp = builder.getModel().getEdgeProperty(propName);
            assert origProp != null : propName;
            EdgeProperty derivedProp = new EdgePropertyDerived(this, origProp);
            registerEdgeProperty(propName, derivedProp);
        }
        if (builder.getAutomaton().isDeterministic()) {
            propAutomatonValue = new TypeObject.Builder()
                    .setClazz(Object.class)
                    .setStorageClass(StorageType.NUMERATED_IDENTITY)
                    .build()
                    .newValue();
            NodePropertySettable propAutomatonValueProperty = new NodePropertySettable(this, propAutomatonValue);
            registerNodeProperty(CommonProperties.AUTOMATON_LABEL, propAutomatonValueProperty);
            propAutomatonValues = null;
        } else {
            int num = 1024;
            propAutomatonValues = new ValueObject[num];

            for (int i = 0; i < num; i++) {
                propAutomatonValues[i] = new TypeObject.Builder()
                        .setClazz(Object.class)
                        .setStorageClass(StorageType.NUMERATED_IDENTITY)
                        .build()
                        .newValue();
            }
            EdgePropertySettable propAutomatonValueProperty = new EdgePropertySettable(this, propAutomatonValues);
            registerEdgeProperty(CommonProperties.AUTOMATON_LABEL, propAutomatonValueProperty);

            propAutomatonValue = null;
        }
        propNodeModelValue = TypeInteger.get().newValue();
        NodePropertySettable propNodeModelValueProperty = new NodePropertySettable(this, propNodeModelValue);
        registerNodeProperty(CommonProperties.NODE_MODEL, propNodeModelValueProperty);
        automatonState = new TypeObject.Builder()
                .setClazz(Object.class)
                .setStorageClass(StorageType.NUMERATED_IDENTITY)
                .build().newValue();
        NodePropertySettable automatonStateProperty = new NodePropertySettable(this, automatonState);
        registerNodeProperty(CommonProperties.NODE_AUTOMATON, automatonStateProperty);

        Expression[] expressions = builder.getAutomaton().getExpressions();
        queryArray = new Value[expressions.length];
        expressionProps = new NodeProperty[expressions.length];
        for (int exprNr = 0; exprNr < expressions.length; exprNr++) {
            expressionProps[exprNr] = builder.getModel().getNodeProperty(expressions[exprNr]);
            assert expressionProps[exprNr] != null : expressions[exprNr] + " " + expressions[exprNr].getClass();
        }
        this.isModelPropState = builder.getModel().getNodeProperty(CommonProperties.STATE);
        if (builder.getNextAutomatonState() == null) {
            this.nextAutomatonState = isModelPropState::getBoolean;
        } else {
            this.nextAutomatonState = builder.getNextAutomatonState();
        }
    }

    int getModelNode(int node) {
        long combined = numberToCombined[node];
        return combinedToModelNode(combined);
    }

    private void queryNode(long combined) {
        int modelNode = combinedToModelNode(combined);
        int propNodeAutomatonValue = combinedToAutomatonNode(combined);

        propNodeModelValue.set(modelNode);
        Object oState = automaton.numberToState(propNodeAutomatonValue);
        automatonState.set(oState); /* set automaton state and get atomic proposition labeling below */
        for (int exprNr = 0; exprNr < expressionProps.length; exprNr++) {
            assert expressionProps[exprNr] != null;
            queryArray[exprNr] = expressionProps[exprNr].get(modelNode);
        }
        automaton.queryState(queryArray, propNodeAutomatonValue);
        int numModelSuccessors = model.getNumSuccessors(modelNode);
        numSuccessors = 0;
        if (automaton.isDeterministic()) {
            propAutomatonValue.set(automaton.numberToLabel(automaton.getSuccessorLabel()));
        } else if (nextAutomatonState.move(modelNode)) { /*LY add following code */
            int succNr = 0;
            int numAutomatonSuccessors = automaton.getNumberSuccessors();
            for (int autSuccNr = 0; autSuccNr < numAutomatonSuccessors; autSuccNr++) {
                Object automatonLabel = automaton.numberToLabel(automaton.getSuccessorLabel(autSuccNr));
                for (int modelSuccNr = 0; modelSuccNr < numModelSuccessors; modelSuccNr++) {
                    propAutomatonValues[succNr].set(automatonLabel); 
                    succNr++;
                }
            }
        }

        if (nextAutomatonState.move(modelNode)) {
            if (automaton.isDeterministic()) {
                int succStateAutomatonNumber = automaton.getSuccessorState();
                for (int succNr = 0; succNr < numModelSuccessors; succNr++) {
                    int succModel = model.getSuccessorNode(modelNode, succNr);
                    addSuccessor(succModel, succStateAutomatonNumber);
                }
            } else {
                int numAutomatonSuccessors = automaton.getNumberSuccessors();
                for (int autSuccNr = 0; autSuccNr < numAutomatonSuccessors; autSuccNr++) {
                    int succStateAutomatonNumber = automaton.getSuccessorState(autSuccNr);
                    for (int modelSuccNr = 0; modelSuccNr < numModelSuccessors; modelSuccNr++) {
                        int succModel = model.getSuccessorNode(modelNode, modelSuccNr);
                        addSuccessor(succModel, succStateAutomatonNumber);
                    }
                }
            }
        } else {
            for (int succNr = 0; succNr < numModelSuccessors; succNr++) {
                int succModel = model.getSuccessorNode(modelNode, succNr);
                addSuccessor(succModel, propNodeAutomatonValue);
            }
        }
    }

    private void addSuccessor(int modelNode, int automatonNode) {
        if (manualEnumeration) {
            manualSuccessorNodes[numSuccessors] = combine(modelNode, automatonNode);
        } else {
            int node = combineToNode(modelNode, automatonNode);
            successorNodes[numSuccessors] = node;
        }
        numSuccessors++;
        reserveSuccessors();
    }

    public void addNodeManually(long combined, int node) {

    }

    @Override
    public int getNumSuccessors(int node) {
        assert node >= 0 : node;
        if (queriedNode != node) {
            long combined = numberToCombined[node];
            queryNode(combined);
            this.queriedNode = node;
        }
        return numSuccessors;
    }

    public long getManualSuccessorNode(int successor) {
        return manualSuccessorNodes[successor];
    }

    @Override
    public int getSuccessorNode(int node, int successor) {
        assert node >= 0 : node;
        if (queriedNode != node) {
            long combined = numberToCombined[node];
            queryNode(combined);
            this.queriedNode = node;
        }
        return successorNodes[successor];
    }

    @Override
    public void computePredecessors(BitSet nodes) {
        assert false;
    }

    @Override
    public void clearPredecessors() {
        assert false;
    }

    public static long combine(int modelState, int automatonState) {
        return (((long) modelState) << 32) | (automatonState);
    }

    public int combineToNode(int modelState, int automatonState) {
        return combineToNode(combine(modelState, automatonState));
    }

    public int combineToNode(long both) {
        int newValue = numberToCombinedSize;
        int value = combinedToNumber.putIfAbsent(both, newValue);
        if (value == -1) {
            ensureSize();
            numberToCombined[numberToCombinedSize] = both;
            numberToCombinedSize++;
            value = newValue;
            numNodes = value + 1;
        }
        return value;
    }

    public int combinedToModelNode(long state) {
        state >>>= 32;
        return (int) state;
    }

    public int combinedToAutomatonNode(long state) {
        state &= 0xFFFFL;
        return (int) state;
    }

    private void reserveSuccessors() {
        if (manualEnumeration) {
            int succLength = manualSuccessorNodes.length;        
            if (succLength >= numSuccessors) {
                return;
            }
            while (succLength < numSuccessors) {
                succLength *= 2;
            }
            manualSuccessorNodes = Arrays.copyOf(manualSuccessorNodes, succLength);
        } else {
            int succLength = successorNodes.length;        
            if (succLength >= numSuccessors) {
                return;
            }
            while (succLength < numSuccessors) {
                succLength *= 2;
            }
            successorNodes = Arrays.copyOf(successorNodes, succLength);
        }
    }

    public long[] getManualInitNodes() {
        return manualInitNodes;
    }

    @Override
    public BitSet getInitialNodes() {
        return initStates;
    }

    @Override
    public void explore(BitSet start) {
        properties.explore(start);
    }

    @Override
    public Set<Object> getGraphProperties() {
        return properties.getGraphProperties();
    }

    @Override
    public Value getGraphProperty(Object property) {
        return properties.getGraphProperty(property);
    }

    @Override
    public void registerGraphProperty(Object propertyName, Type type) {
        properties.registerGraphProperty(propertyName, type);
    }

    @Override
    public void setGraphProperty(Object property, Value value)
    {
        properties.setGraphProperty(property, value);
    }

    @Override
    public void registerNodeProperty(Object propertyName,
            NodeProperty property) {
        properties.registerNodeProperty(propertyName, property);
    }

    @Override
    public NodeProperty getNodeProperty(Object property) {
        return properties.getNodeProperty(property);
    }

    @Override
    public Set<Object> getNodeProperties() {
        return properties.getNodeProperties();
    }

    @Override
    public void registerEdgeProperty(Object propertyName,
            EdgeProperty property) {
        properties.registerEdgeProperty(propertyName, property);
    }

    @Override
    public EdgeProperty getEdgeProperty(Object property) {
        return properties.getEdgeProperty(property);
    }

    @Override
    public Set<Object> getEdgeProperties() {
        return properties.getEdgeProperties();
    }

    @Override
    public void removeGraphProperty(Object property) {
        properties.removeGraphProperty(property);
    }

    @Override
    public void removeNodeProperty(Object property) {
        properties.removeNodeProperty(property);
    }

    @Override
    public void removeEdgeProperty(Object property) {
        properties.removeEdgeProperty(property);
    }

    @Override
    public GraphExplicitProperties getProperties() {
        return properties;
    }

    @Override
    public int getNumNodes() {
        return numNodes;
    }

    private void ensureSize() {
        assert numberToCombinedSize >= 0;
        int size = numberToCombined.length;
        if (numberToCombinedSize < size) {
            return;
        }
        int newSize = size;
        while (newSize <= numberToCombinedSize) {
            newSize *= 2;
        }
        numberToCombined = Arrays.copyOf(numberToCombined, newSize);
    }

    @Override
    public void close() {
    }

    @Override
    public Type getType(Expression expression) {
        assert expression != null;
        return model.getType(expression);
    }
}
