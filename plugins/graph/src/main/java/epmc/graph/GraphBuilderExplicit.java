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

package epmc.graph;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.List;

import epmc.error.EPMCException;
import epmc.graph.Semantics;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitSparse;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graph.explicit.NodeProperty;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArrayInteger;
import epmc.value.ValueObject;

// TODO class would profit from some code review

public final class GraphBuilderExplicit {
    private GraphExplicit inputGraph;
    private GraphExplicit outputGraph;
    private ValueArrayInteger inputToOutputNodes;
    private ValueArrayInteger outputToInputNodes;
    private final List<Object> derivedGraphProperties = new ArrayList<>();
    private final List<Object> derivedNodeProperties = new ArrayList<>();
    private final List<Object> derivedEdgeProperties = new ArrayList<>();
    private final List<BitSet> sinks = new ArrayList<>();
    private boolean uniformise;
    private boolean reorder;
    private boolean backward;
    private GraphBuilderMode graphBuilderMode;
    private boolean built;
    private List<BitSet> parts;

    public boolean isBuilt() {
        return built;
    }

    public GraphBuilderExplicit setInputGraph(GraphExplicit graph) {
        assert !isBuilt();
        assert graph != null;
        this.inputGraph = graph;
        return this;
    }

    public GraphBuilderExplicit addDerivedGraphProperty(Object property) {
        assert !isBuilt();
        assert property != null;
        this.derivedGraphProperties.add(property);
        return this;
    }

    public GraphBuilderExplicit addDerivedNodeProperty(Object property) {
        assert !isBuilt();
        assert property != null;
        this.derivedNodeProperties.add(property);
        return this;
    }

    public GraphBuilderExplicit addDerivedEdgeProperty(Object property) {
        assert !isBuilt();
        assert property != null;
        this.derivedEdgeProperties.add(property);
        return this;
    }
    
    public GraphBuilderExplicit addSink(BitSet sink) {
        assert !isBuilt();
        assert sink != null;
        sinks.add(sink);
        return this;
    }

    public GraphBuilderExplicit setUniformise(boolean uniformise) {
        assert !isBuilt();
        this.uniformise = uniformise;
        return this;
    }

    public GraphBuilderExplicit setReorder(boolean reorder) {
        assert !isBuilt();
        this.reorder = reorder;
        return this;
    }
    
    public GraphBuilderExplicit setBackward(boolean backward) {
        assert !isBuilt();
        this.backward = backward;
        return this;
    }

    public GraphBuilderExplicit setGraphBuilderMode(GraphBuilderMode mode) {
        assert !isBuilt();
        assert mode != null;
        this.graphBuilderMode = mode;
        return this;
    }

    public GraphBuilderExplicit setForNative(boolean useNative) {
        assert !isBuilt();
        setGraphBuilderMode(useNative
                ? GraphBuilderMode.SPARSE_NATIVE : GraphBuilderMode.SPARSE);
        return this;
    }

    public GraphBuilderExplicit build() throws EPMCException {
        assert !built;
        built = true;
        assert inputGraph != null;
        assert assertSinksValid(inputGraph, sinks);
        assert assertGraphProperties(inputGraph, derivedGraphProperties);
        assert assertNodeProperties(inputGraph, derivedNodeProperties);
        assert assertEdgeProperties(inputGraph, derivedEdgeProperties);
        ContextValue contextValue = inputGraph.getContextValue();
        processSinks(sinks);
        if (parts == null) {
            parts = prepareParts(inputGraph, reorder);
        }
        TIntList partsBegin = new TIntArrayList();
        this.inputToOutputNodes = TypeInteger.get(contextValue).getTypeArray().newValue();
        this.outputToInputNodes = TypeInteger.get(contextValue).getTypeArray().newValue();
        prepareInputToOutputNodes(inputGraph, parts, sinks, inputToOutputNodes, outputToInputNodes, partsBegin);
        prepareOutputToInputNodes(inputGraph, parts, sinks, inputToOutputNodes, outputToInputNodes, partsBegin);
        this.outputGraph = prepareGraph(inputGraph, sinks, inputToOutputNodes, uniformise, graphBuilderMode);
        prepareProperties(outputGraph, inputGraph, derivedGraphProperties, derivedNodeProperties, derivedEdgeProperties);
        prepareInitialNodes(outputGraph, inputGraph, inputToOutputNodes);
        prepareGraphProperties(outputGraph, inputGraph, derivedGraphProperties);
        Semantics semanticsType = inputGraph.getGraphPropertyObject(CommonProperties.SEMANTICS);
        if (SemanticsNonDet.isNonDet(semanticsType)) {
            prepareTransitionsNondet(outputGraph, inputGraph,
                    inputToOutputNodes, outputToInputNodes, sinks, parts, partsBegin,
                    derivedEdgeProperties, uniformise);
        } else if (!backward) {
            prepareTransitions(outputGraph, inputGraph,
                    inputToOutputNodes, outputToInputNodes, sinks, parts, partsBegin,
                    derivedEdgeProperties, uniformise);
        } else {
            assert !uniformise;
            prepareTransitionsBackward(outputGraph, inputGraph,
                    inputToOutputNodes, outputToInputNodes, sinks, parts, partsBegin,
                    derivedEdgeProperties);
        }
        prepareNodeProperties(outputGraph, inputGraph, derivedNodeProperties, outputToInputNodes);        
        return this;
    }

    private static void prepareNodeProperties(GraphExplicit outputGraph,
            GraphExplicit inputGraph, List<Object> nodeProperties, ValueArrayInteger outputToInputNodes)
                    throws EPMCException {
        int numOutputNodes = outputToInputNodes.size();
        NodeProperty[] inputProperties = new NodeProperty[nodeProperties.size()];
        NodeProperty[] outputProperties = new NodeProperty[nodeProperties.size()];
        int numProperties = 0;
        for (Object property : nodeProperties) {
            inputProperties[numProperties] = inputGraph.getNodeProperty(property);
            outputProperties[numProperties] = outputGraph.getNodeProperty(property);
            numProperties++;
        }
        for (int outputNode = 0; outputNode < numOutputNodes; outputNode++) {
            outputGraph.queryNode(outputNode);
            inputGraph.queryNode(outputToInputNodes.getInt(outputNode));
            for (int propNr = 0; propNr < numProperties; propNr++) {
                Value value = inputProperties[propNr].get(outputToInputNodes.getInt(outputNode));
                outputProperties[propNr].set(outputNode, value);
            }
        }
    }

    private static void prepareGraphProperties(GraphExplicit outputGraph,
            GraphExplicit inputGraph, List<Object> graphProperties)
                    throws EPMCException {
        for (Object property : graphProperties) {
            Value value = inputGraph.getGraphProperty(property);
            if (inputGraph.getGraphProperties().contains(property)) {
                outputGraph.setGraphProperty(property, value);
            }
        }
    }

    private GraphExplicit prepareGraph(GraphExplicit inputGraph, List<BitSet> sinkList, ValueArrayInteger inputToOutputNodes, boolean uniformise, GraphBuilderMode graphBuilderMode)
            throws EPMCException {
        GraphExplicit outputGraph = null;
        Semantics semanticsType = inputGraph.getGraphPropertyObject(CommonProperties.SEMANTICS);
        if (!SemanticsNonDet.isNonDet(semanticsType)) {
            BitSet sinks = computeSinks(inputGraph.getContextValue(), sinkList);
            int numStates = 0;
            int numTotalOut = 0;
          for (int inputNode = 0; inputNode < inputToOutputNodes.size(); inputNode++) {
                if (!sinks.get(inputNode)) {
                    numStates++;
                    inputGraph.queryNode(inputNode);
                    numTotalOut += inputGraph.getNumSuccessors(inputNode);
                    numTotalOut += uniformise ? 1 : 0;
                }
            }
            numStates += sinkList.size();
            numTotalOut += sinkList.size();
            outputGraph = new GraphExplicitSparse(inputGraph.getContextValue(), graphBuilderMode == GraphBuilderMode.SPARSE_NATIVE, numStates, numTotalOut);
        } else {
            int numStates = 0;
            int numTotalNondet = 0;
            int numTotalProb = 0;
            BitSet sinks = computeSinks(inputGraph.getContextValue(), sinkList);
            NodeProperty stateProp = inputGraph.getNodeProperty(CommonProperties.STATE);
            assert stateProp != null;
            for (int inputState = 0; inputState < inputToOutputNodes.getTotalSize(); inputState++) {
                inputGraph.queryNode(inputState);
                if (!stateProp.getBoolean(inputState)) {
                    continue;
                }
                if (!sinks.get(inputState)) {
                    numStates++;
                    int numStateSucc = inputGraph.getNumSuccessors(inputState);
                    numTotalNondet += numStateSucc;
                    for (int succNr = 0; succNr < numStateSucc; succNr++) {
                        inputGraph.queryNode(inputState);
                        int succState = inputGraph.getSuccessorNode(inputState, succNr);
                        inputGraph.queryNode(succState);
                        int numNdSucc = inputGraph.getNumSuccessors(succState);
                        numTotalProb += numNdSucc;
                    }
                }
            }
            numStates += sinkList.size();
            numTotalNondet += sinkList.size() * parts.size();
            numTotalProb += sinkList.size() * parts.size();
            outputGraph = new GraphExplicitSparseAlternate(inputGraph.getContextValue(), graphBuilderMode == GraphBuilderMode.SPARSE_NATIVE, numStates, numTotalNondet, numTotalProb);
        }
        return outputGraph;
    }

    private static void processSinks(List<BitSet> sinks) {
        List<BitSet> result = new ArrayList<>();
        for (BitSet sink : sinks) {
            if (sink.cardinality() > 0) {
                result.add(sink);
            }
        }
        sinks.clear();
        sinks.addAll(result);
    }

    private static boolean assertGraphProperties(GraphExplicit inputGraph, List<Object> graphProperties) {
        assert inputGraph != null;
        assert graphProperties != null;
        for (Object property : graphProperties) {
            assert property != null;
        }
        assert new THashSet<>(graphProperties).size()
        == graphProperties.size();
        return true;
    }
    
    private static boolean assertNodeProperties(GraphExplicit inputGraph,
            List<Object> nodeProperties) {
        assert inputGraph != null;
        assert nodeProperties != null;
        for (Object property : nodeProperties) {
            assert property != null;
        }
        assert new THashSet<>(nodeProperties).size()
        == nodeProperties.size() : new THashSet<>(nodeProperties) + " " + nodeProperties;
        return true;
    }

    private static boolean assertEdgeProperties(GraphExplicit inputGraph,
            List<Object> edgeProperties) {
        assert inputGraph != null;
        assert edgeProperties != null;
        for (Object property : edgeProperties) {
            assert property != null;
        }
        assert new THashSet<>(edgeProperties).size()
        == edgeProperties.size();
        return true;
    }

    private static List<BitSet> prepareParts(GraphExplicit inputGraph, boolean reorder)
            throws EPMCException {
        List<BitSet> result = new ArrayList<>();
        Semantics semanticsType = ValueObject.asObject(inputGraph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
        if (reorder && SemanticsNonDet.isNonDet(semanticsType)) {
            BitSet states = collectStates(inputGraph);
            BitSet nonStates = collectNonStates(inputGraph);
            result.add(states);
            result.add(nonStates);
        } else {
            BitSet nodes = UtilBitSet.newBitSetUnbounded();
            nodes.set(0, inputGraph.getNumNodes(), true);
            result.add(nodes);
        }
        return result;
    }

    private static void prepareTransitionsNondet(
            GraphExplicit outputGraph, GraphExplicit inputGraph,
            ValueArrayInteger inputToOutputNodes,
            ValueArrayInteger outputToInputNodes,
            List<BitSet> sinksList,
            List<BitSet> parts,
            TIntList partsBegin,
            List<Object> edgeProperties,
            boolean uniformise) throws EPMCException {
        EdgeProperty[] inputProperties = new EdgeProperty[edgeProperties.size()];
        EdgeProperty[] outputProperties = new EdgeProperty[edgeProperties.size()];
        int numProperties = 0;
        for (Object property : edgeProperties) {
            inputProperties[numProperties] = inputGraph.getEdgeProperty(property);
            outputProperties[numProperties] = outputGraph.getEdgeProperty(property);
            numProperties++;
        }

        BitSet sinks = computeSinks(inputGraph.getContextValue(), sinksList);
        int numOutputNodes = outputToInputNodes.size();
        NodeProperty stateProp = inputGraph.getNodeProperty(CommonProperties.STATE);
        int nextNondetNode = outputGraph.computeNumStates();
        for (int outputState = 0; outputState < numOutputNodes; outputState++) {
            int inputState = outputToInputNodes.getInt(outputState);
            assert inputState >= 0;
            inputGraph.queryNode(inputState);
            if (!stateProp.getBoolean(inputState)) {
                continue;
            }
            if (sinks.get(inputState)) {
                outputGraph.queryNode(outputState);
                outputGraph.prepareNode(outputState, 1);
                outputGraph.setSuccessorNode(outputState, 0, nextNondetNode);
                EdgeProperty weight = outputGraph.getEdgeProperty(CommonProperties.WEIGHT);
                TypeWeight typeWeight = TypeWeight.asWeight(weight.getType());
                weight.set(outputState, 0, typeWeight.getOne());
                outputGraph.queryNode(nextNondetNode);
                outputGraph.prepareNode(nextNondetNode, 1);
                outputGraph.setSuccessorNode(nextNondetNode, 0, outputState);
                weight.set(nextNondetNode, 0, typeWeight.getOne());
                nextNondetNode++;
            } else {
                int numStateSuccessors = inputGraph.getNumSuccessors(inputState);
                outputGraph.queryNode(outputState);
                outputGraph.prepareNode(outputState, numStateSuccessors + (uniformise ? 1 : 0));
                if (uniformise) {
                    outputGraph.setSuccessorNode(outputState, numStateSuccessors, outputState);
                }
                for (int nondetNr = 0; nondetNr < numStateSuccessors; nondetNr++) {
                    inputGraph.queryNode(inputState);
                    outputGraph.queryNode(outputState);
                    int inputNondet = inputGraph.getSuccessorNode(inputState, nondetNr);
                    outputGraph.setSuccessorNode(outputState, nondetNr, nextNondetNode);
                    for (int propNr = 0; propNr < numProperties; propNr++) {
                        EdgeProperty inputProp = inputProperties[propNr];
                        EdgeProperty outputProp = outputProperties[propNr];
                        outputProp.set(outputState, nondetNr, inputProp.get(inputState, nondetNr));
                    }
                    inputGraph.queryNode(inputNondet);
                    int numNondetSuccessors = inputGraph.getNumSuccessors(inputNondet);
                    outputGraph.queryNode(nextNondetNode);
                    outputGraph.prepareNode(nextNondetNode, numNondetSuccessors);
                    for (int succNr = 0; succNr < numNondetSuccessors; succNr++) {
                        int inputSucc = inputGraph.getSuccessorNode(inputNondet, succNr);
                        int outputSucc = inputToOutputNodes.getInt(inputSucc);
                        assert outputSucc >= 0 : outputSucc + " " + inputSucc + " " + succNr;
                        outputGraph.setSuccessorNode(nextNondetNode, succNr, outputSucc);
                        for (Object property : edgeProperties) {
                            if (inputGraph.getEdgeProperties().contains(property)) {
                                EdgeProperty inputProp = inputGraph.getEdgeProperty(property);
                                EdgeProperty outputProp = outputGraph.getEdgeProperty(property);
                                outputProp.set(nextNondetNode, succNr, inputProp.get(inputNondet, succNr));
                            }
                        }
                    }
                    nextNondetNode++;
                }
            }
        }
    }
    
    private static void prepareTransitions(
            GraphExplicit outputGraph, GraphExplicit inputGraph,
            ValueArrayInteger inputToOutputNodes,
            ValueArrayInteger outputToInputNodes,
            List<BitSet> sinksList,
            List<BitSet> parts,
            TIntList partsBegin,
            List<Object> edgeProperties,
            boolean uniformise) throws EPMCException {
        EdgeProperty[] inputProperties = new EdgeProperty[edgeProperties.size()];
        EdgeProperty[] outputProperties = new EdgeProperty[edgeProperties.size()];
        int numProperties = 0;
        for (Object property : edgeProperties) {
            inputProperties[numProperties] = inputGraph.getEdgeProperty(property);
            outputProperties[numProperties] = outputGraph.getEdgeProperty(property);
            numProperties++;
        }
        BitSet sinks = computeSinks(inputGraph.getContextValue(), sinksList);
        int numOutputNodes = outputToInputNodes.size();
        for (int outputNode = 0; outputNode < numOutputNodes; outputNode++) {
            int inputNode = outputToInputNodes.getInt(outputNode);
            assert inputNode >= 0;
            inputGraph.queryNode(inputNode);
            if (sinks.get(inputNode)) {
                int sinkNr = getListNr(inputNode, sinksList);
                int partNr = getListNr(inputNode, parts);
                int nextPart = (partNr + 1) % parts.size();
                int nextPartBegin = partsBegin.get(nextPart);
                outputGraph.queryNode(outputNode);
                outputGraph.prepareNode(outputNode, 1);
                outputGraph.setSuccessorNode(outputNode, 0, nextPartBegin + sinkNr);
                EdgeProperty weight = outputGraph.getEdgeProperty(CommonProperties.WEIGHT);
                TypeWeight typeWeight = TypeWeight.asWeight(weight.getType());
                weight.set(outputNode, 0, typeWeight.getOne());
            } else {
                int numSuccessors = inputGraph.getNumSuccessors(inputNode);
                outputGraph.queryNode(outputNode);
                outputGraph.prepareNode(outputNode, numSuccessors + (uniformise ? 1 : 0));
                if (uniformise) {
                    outputGraph.setSuccessorNode(outputNode, numSuccessors, outputNode);
                }
                for (int succNr = 0; succNr < numSuccessors; succNr++) {
                    int inputSuccessor = inputGraph.getSuccessorNode(inputNode, succNr);
                    int outputSuccessor = inputToOutputNodes.getInt(inputSuccessor);
                    outputGraph.setSuccessorNode(outputNode, succNr, outputSuccessor);
                    for (int propNr = 0; propNr < numProperties; propNr++) {
                        EdgeProperty inputProp = inputProperties[propNr];
                        EdgeProperty outputProp = outputProperties[propNr];
                        Value value = inputProp.get(inputNode, succNr);
                        outputProp.set(outputNode, succNr, value);
                    }
                }
            }
        }
    }

    private static void prepareTransitionsBackward(
            GraphExplicit outputGraph, GraphExplicit inputGraph,
            ValueArrayInteger inputToOutputNodes, ValueArrayInteger outputToInputNodes,
            List<BitSet> sinksList,
            List<BitSet> parts, TIntList partsBegin,
            List<Object> edgeProperties) throws EPMCException {
        BitSet sinks = computeSinks(inputGraph.getContextValue(), sinksList);
        int numOutputNodes = outputToInputNodes.size();
        ContextValue contextValue = inputGraph.getContextValue();
        ValueArrayInteger numInEdges = UtilValue.newArray(TypeInteger.get(contextValue).getTypeArray(), numOutputNodes);
        for (int outputNode = 0; outputNode < numOutputNodes; outputNode++) {
            int inputNode = outputToInputNodes.getInt(outputNode);
            assert inputNode >= 0;
            inputGraph.queryNode(inputNode);
            if (sinks.get(inputNode)) {
                int partNr = getListNr(inputNode, parts);
                int nextPart = (partNr + 1) % parts.size();
                int nextPartBegin = partsBegin.get(nextPart);
                numInEdges.set(numInEdges.getInt(nextPartBegin) + 1, nextPartBegin);
            } else {
                int numSuccessors = inputGraph.getNumSuccessors(inputNode);
                for (int succNr = 0; succNr < numSuccessors; succNr++) {
                    int inputSuccessor = inputGraph.getSuccessorNode(inputNode, succNr);
                    int outputSuccessor = inputToOutputNodes.getInt(inputSuccessor);
                    numInEdges.set(numInEdges.getInt(outputSuccessor) + 1, outputSuccessor);
                }
            }
        }
        for (int outputNode = 0; outputNode < numOutputNodes; outputNode++) {
            int numOut = numInEdges.getInt(outputNode);
            outputGraph.queryNode(outputNode);
            outputGraph.prepareNode(outputNode, numOut);
        }
        int numInEdgesotalSize = numInEdges.getTotalSize();
        for (int index = 0; index < numInEdgesotalSize; index++) {
            numInEdges.set(0, index);
        }

        for (int outputNode = 0; outputNode < numOutputNodes; outputNode++) {
            int inputNode = outputToInputNodes.getInt(outputNode);
            assert inputNode >= 0;
            inputGraph.queryNode(inputNode);
            if (sinks.get(inputNode)) {
                int partNr = getListNr(inputNode, parts);
                int nextPart = (partNr + 1) % parts.size();
                int nextPartBegin = partsBegin.get(nextPart);
                outputGraph.queryNode(nextPartBegin);
                EdgeProperty weight = outputGraph.getEdgeProperty(CommonProperties.WEIGHT);
                TypeWeight typeWeight = TypeWeight.asWeight(weight.getType());
                weight.set(nextPartBegin, numInEdges.getInt(nextPartBegin), typeWeight.getOne());
                numInEdges.set(numInEdges.getInt(nextPartBegin) + 1, nextPartBegin);
            } else {
                int numSuccessors = inputGraph.getNumSuccessors(inputNode);
                for (int succNr = 0; succNr < numSuccessors; succNr++) {
                    int inputSuccessor = inputGraph.getSuccessorNode(inputNode, succNr);
                    int outputSuccessor = inputToOutputNodes.getInt(inputSuccessor);
                    outputGraph.queryNode(outputSuccessor);
                    int outSn = numInEdges.getInt(outputSuccessor);
                    for (Object property : edgeProperties) {
                        if (inputGraph.getEdgeProperties().contains(property)) {
                            EdgeProperty inputProp = inputGraph.getEdgeProperty(property);
                            EdgeProperty outputProp = outputGraph.getEdgeProperty(property);
                            Value value = inputProp.get(inputNode, succNr);
                            outputProp.set(inputNode, outSn, value);
                        }
                    }
                    outputGraph.setSuccessorNode(outputSuccessor, outSn, outputNode);
                    numInEdges.set(numInEdges.getInt(outputSuccessor) + 1, outputSuccessor);
                }
            }
        }
    }

    private static BitSet computeSinks(ContextValue contextValue, List<BitSet> sinksList) {
        BitSet sinks = UtilBitSet.newBitSetUnbounded();
        for (BitSet sink : sinksList) {
            sinks.or(sink);
        }
        return sinks;
    }

    private static int getListNr(int inputNode, List<BitSet> list) {
        int sinkNr = 0;
        for (BitSet set : list) {
            if (set.get(inputNode)) {
                break;
            }
            sinkNr++;
        }
        assert sinkNr < list.size() : "not in one of the lists";
        return sinkNr;
    }

    private static void prepareInitialNodes(GraphExplicit outputGraph,
            GraphExplicit inputGraph, ValueArrayInteger inputToOutputNodes) {
        BitSet inputNodesInit = inputGraph.getInitialNodes();
        
        BitSet outputNodesInit = outputGraph.getInitialNodes();
        for (int inputNode = inputNodesInit.nextSetBit(0); inputNode >= 0; inputNode = inputNodesInit.nextSetBit(inputNode+1)) {
            int outputNode = inputToOutputNodes.getInt(inputNode);
            assert outputNode >= 0 : outputNode;
            outputNodesInit.set(outputNode);
        }        
    }

    private static void prepareInputToOutputNodes(GraphExplicit inputGraph, List<BitSet> parts, List<BitSet> sinkList,
            ValueArrayInteger inputToOutputNodes, Value outputToInputNodes, TIntList partsBegin)
            throws EPMCException {
        BitSet inputNodes = UtilBitSet.newBitSetUnbounded();
        inputNodes.set(0, inputGraph.getNumNodes(), true);
        inputToOutputNodes.setSize(inputNodes.length());
        int inputToOutputTotalSize = inputToOutputNodes.getTotalSize();
        for (int index = 0; index < inputToOutputTotalSize; index++) {
            inputToOutputNodes.set(-1, index);
        }

        int nextPart = 0;
        partsBegin.add(nextPart);
        nextPart = buildInputToOutputPart(inputGraph, nextPart, parts.get(0), sinkList, inputToOutputNodes);
        if (parts.size() > 2) {
            partsBegin.add(nextPart);
            nextPart = buildInputToOutputPart(inputGraph, nextPart, parts.get(1), sinkList, inputToOutputNodes);            
        }
        /*
        for (BitSet part : parts) {
            nextPart = buildInputToOutputPart(inputGraph, nextPart, part, sinkList, inputToOutputNodes);
            partsBegin.add(nextPart);
        }
        */
    }

    private static void prepareOutputToInputNodes(GraphExplicit inputGraph,
            List<BitSet> parts, List<BitSet> sinkList,
            ValueArrayInteger inputToOutputNodes,
            ValueArrayInteger outputToInputNodes, TIntList partsBegin)
                    throws EPMCException {
        int numOutputNodes = 0;
        int numInputNodes = inputGraph.getNumNodes();
        for (int inputNode = 0; inputNode < numInputNodes; inputNode++) {
            int outputNode = inputToOutputNodes.getInt(inputNode);
            numOutputNodes = Math.max(numOutputNodes, outputNode + 1);
        }
        
        outputToInputNodes.setSize(numOutputNodes);
        for (int inputNode = 0; inputNode < numInputNodes; inputNode++) {
            int outputNode = inputToOutputNodes.getInt(inputNode);
            if (outputNode >= 0) {
                outputToInputNodes.set(inputNode, outputNode);
            }
        }
    }

    private static void prepareProperties(GraphExplicit outputGraph,
            GraphExplicit inputGraph,
            List<Object> graphProperties, List<Object> nodeProperties, List<Object> edgeProperties)
                    throws EPMCException {
        for (Object property : graphProperties) {
            Type type = inputGraph.getGraphPropertyType(property);
            outputGraph.addSettableGraphProperty(property, type);
        }
        for (Object property : nodeProperties) {
            Type type = inputGraph.getNodePropertyType(property);
            outputGraph.addSettableNodeProperty(property, type);
        }
        for (Object property : edgeProperties) {
            Type type = inputGraph.getEdgePropertyType(property);
            outputGraph.addSettableEdgeProperty(property, type);
        }
    }

    private static int buildInputToOutputPart(GraphExplicit inputGraph, int partStart, BitSet part, List<BitSet> sinks, ValueArrayInteger inputToOutputNodes) throws EPMCException {
        int nextOutputNode = partStart;
        nextOutputNode += sinks.size();
        int numInputNodes = inputGraph.getNumNodes();
        for (int inputNode = 0; inputNode < numInputNodes; inputNode++) {
            if (part.get(inputNode)) {
                int outputNode = -1;
                int sinkNr = 0;
                boolean sinkNode = false;
                for (BitSet sink : sinks) {
                    if (sink.get(inputNode)) {
                        assert !sinkNode;
                        outputNode = partStart + sinkNr;
                        sinkNode = true;
                    }
                    sinkNr++;
                }
                if (!sinkNode) {
                    outputNode = nextOutputNode;
                    nextOutputNode++;
                }
                assert outputNode != -1;
                inputToOutputNodes.set(outputNode, inputNode);
            }
        }
        return nextOutputNode;
    }

    private static BitSet collectStates(GraphExplicit graph) throws EPMCException {
        BitSet states = UtilBitSet.newBitSetUnbounded();
        NodeProperty isState = graph.getNodeProperty(CommonProperties.STATE);
        int numInputNodes = graph.getNumNodes();
        for (int node = 0; node < numInputNodes; node++) {
            graph.queryNode(node);
            if (isState.getBoolean(node)) {
                states.set(node);
            }
        }

        return states;
    }

    private static BitSet collectNonStates(GraphExplicit graph) throws EPMCException {
        BitSet nonStates = UtilBitSet.newBitSetUnbounded();
        NodeProperty isState = graph.getNodeProperty(CommonProperties.STATE);
        int numNodes = graph.getNumNodes();
        for (int node = 0; node < numNodes; node++) {
            graph.queryNode(node);
            if (!isState.getBoolean(node)) {
                nonStates.set(node);
            }
        }

        return nonStates;
    }

    private boolean assertSinksValid(GraphExplicit inputGraph, List<BitSet> sinks) {
        if (sinks != null) {
            assert sinks != null;
            BitSet allNodes = UtilBitSet.newBitSetUnbounded();
            for (BitSet set : sinks) {
                assert set != null;
                for (int node = set.nextSetBit(0); node >= 0; node = set.nextSetBit(node+1)) {
                    assert node < inputGraph.getNumNodes();
                }
            }
            for (BitSet set : sinks) {
                BitSet testBitSet = allNodes.clone();
                testBitSet.and(set);
                assert testBitSet.cardinality() == 0;
                allNodes.or(set);
            }
        }
        return true;
    }

    public GraphExplicit getInputGraph() {
        return inputGraph;
    }

    public GraphExplicit getOutputGraph() {
        assert isBuilt();
        return outputGraph;
    }

    public int inputToOutputNode(int inputNode) {
        assert isBuilt();
        assert inputNode >= 0;
        assert inputNode < inputGraph.getNumNodes();
        return inputToOutputNodes.getInt(inputNode);
    }

    public int outputToInputNode(int outputNode) {
        assert isBuilt();
        assert outputNode >= 0;
        assert outputNode < outputGraph.getNumNodes();
        return outputToInputNodes.getInt(outputNode);
    }

    public void setParts(List<BitSet> parts) {
        this.parts = parts;
    }
    
    public GraphBuilderExplicit addDerivedGraphProperties(Iterable<? extends Object> properties) {
        assert !isBuilt();
        for (Object property : properties) {
            addDerivedGraphProperty(property);
        }
        return this;
    }
    
    public GraphBuilderExplicit addDerivedNodeProperties(Iterable<? extends Object> properties) {
        assert !isBuilt();
        for (Object property : properties) {
            addDerivedNodeProperty(property);
        }
        return this;
    }
    
    public GraphBuilderExplicit addDerivedEdgeProperties(Iterable<? extends Object> properties) {
        assert !isBuilt();
        for (Object property : properties) {
            addDerivedEdgeProperty(property);
        }
        return this;
    }

    public GraphBuilderExplicit addSinks(Iterable<? extends BitSet> sinks) {
        assert !isBuilt() : "already built";
        assert sinks != null : "sinks == null";
        for (BitSet sink : sinks) {
            assert sink != null : "sink == null";
        }
        for (BitSet sink : sinks) {
            addSink(sink);
        }
        return this;
    }

    public GraphBuilderExplicit setUniformise() {
        assert !isBuilt();
        setUniformise(true);
        return this;
    }
    
    public GraphBuilderExplicit setReorder() {
        assert !isBuilt();
        setReorder(true);
        return this;
    }


    public GraphBuilderExplicit setBackward() {
        assert !isBuilt();
        setReorder(true);
        return this;
    }

    public Options getOptions() {
        assert getInputGraph() != null;
        return getInputGraph().getOptions();
    }
}
