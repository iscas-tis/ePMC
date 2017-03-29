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

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.Permutation;
import epmc.dd.SupportWalker;
import epmc.dd.SupportWalkerNodeMapInt;
import epmc.dd.Walker;
import epmc.error.EPMCException;
import epmc.graph.dd.GraphDD;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitWrapper;
import epmc.graph.explicit.NodeProperty;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.StopWatch;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.TypeArray;
import epmc.value.TypeBoolean;
import epmc.value.TypeEnum;
import epmc.value.TypeInteger;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.ValueEnum;
import epmc.value.ValueInteger;

// TODO integrate transitions with presNodes for further speedup

public final class GraphBuilderDD implements Closeable {
    private boolean closed;
    private final TIntIntMap varToNumber = new TIntIntHashMap();
    private final SupportWalkerNodeMapInt lowMapPres;
    private final SupportWalkerNodeMapInt lowMapNext;
    private final int numNonsinkNodes;
    private final int numNodes;
    private final int numNodesInclNondet;
    private final SupportWalker presNodes;
    private final SupportWalker nextNodes;
    private final SupportWalker states;
    private final SupportWalker transitions;
    private final SupportWalker presCube;
    private final SupportWalker nextCube;
    private final DD presCubeDD;
    private final BitSet presVars;
    private final BitSet nextVars;
    private int currentAction = 0;
    private int actionDepth = 0;
    private final TIntStack presNumbers = new TIntArrayStack();
    private final TIntStack nextNumbers = new TIntArrayStack();
    private final ContextDD contextDD;
    private final GraphDD graphDD;
    private boolean uniformise;
    private ValueAlgebra unifRate;
    private int[] nodeOrderMap;
    private TIntIntMap[] actionsEnabled;
    private int[] nondetOffset;
    private final DD cubeDD;
    private final DD[] sinks;
    private final DD[] representants;
    private final boolean nondet;
    private final boolean stateEncoding;

    public GraphBuilderDD(GraphDD graphDD, List<DD> sinks, boolean nondet, boolean stateEncoding) throws EPMCException {
        assert graphDD != null;
        assert assertSinks(sinks, graphDD);
        this.presVars = UtilBitSet.newBitSetUnbounded();
        this.nextVars = UtilBitSet.newBitSetUnbounded();
        this.nondet = nondet;
        this.stateEncoding = stateEncoding;
        if (nondet) {
            // TODO HACK
            sinks = Collections.emptyList();            
        }
        this.sinks = new DD[sinks.size()];
        for (int i = 0; i < sinks.size(); i++) {
            this.sinks[i] = sinks.get(i).clone();
        }
        DD nextCube = graphDD.getNextCube();
        DD presCube = graphDD.getPresCube();
        this.presCubeDD = presCube;
        this.graphDD = graphDD;
        if (nondet) {
            this.cubeDD = presCube.and(nextCube, graphDD.getActionCube());
        } else {
            this.cubeDD = presCube.and(nextCube);
        }
        this.representants = chooseRepresentants(this.sinks, presCube,
                graphDD.getNodeProperty(CommonProperties.STATE));
        int varNr = 0;
        for (Walker walk = presCube.walker(); !walk.isLeaf(); walk.high()) {
            presVars.set(walk.variable());
            varToNumber.put(walk.variable(), varNr);
            varNr++;
        }
        varNr = 0;
        for (Walker walk = nextCube.walker(); !walk.isLeaf(); walk.high()) {
            nextVars.set(walk.variable());
            varToNumber.put(walk.variable(), varNr);
            varNr++;
        }

        Permutation swap = graphDD.getSwapPresNext();

        DD presNodes = graphDD.getNodeSpace().clone();
        this.presNodes = buildSinks(presNodes, this.sinks, representants, presCube, true);
        lowMapPres = this.presNodes.newNodeMapInt();
        SupportWalkerNodeMapInt sumMapPres = this.presNodes.newNodeMapInt();
        this.presCube = presCube.supportWalker();
        int numNodesComputed = buildMaps(this.presNodes, this.presCube,
                this.lowMapPres, sumMapPres);
        numNonsinkNodes = numNodesComputed;
        if (nondet) {
            numNodesComputed += sinks.size() * 2;
        } else {
            numNodesComputed += sinks.size();
        }
        numNodes = numNodesComputed;
        
        this.presNumbers.push(0);

        this.contextDD = graphDD.getContextDD();

        
        DD oldPresNodes = presNodes;
        presNodes = presNodes.andNot(this.sinks);
        oldPresNodes.dispose();
        DD nextNodes = presNodes.permute(swap);
        DD[] nextSinks = new DD[this.sinks.length];
        for (int sinkNr = 0; sinkNr < this.sinks.length; sinkNr++) {
            nextSinks[sinkNr] = this.sinks[sinkNr].permute(swap);
        }
        DD[] nextRepresentants = new DD[representants.length];
        for (int i = 0; i < representants.length; i++) {
            nextRepresentants[i] = representants[i].permute(swap);
        }
        this.nextNodes = buildSinks(nextNodes, nextSinks, nextRepresentants,
                nextCube, false);
        contextDD.dispose(nextRepresentants);
        nextNodes.dispose();
        contextDD.dispose(nextSinks);
        
        this.nextCube = this.presCube.permute(swap);
        lowMapNext = this.nextNodes.newNodeMapInt();
        SupportWalkerNodeMapInt sumMapNext = this.nextNodes.newNodeMapInt();
        buildMaps(this.nextNodes, this.nextCube, this.lowMapNext, sumMapNext);
        this.nextNumbers.push(0);
        this.nodeOrderMap = computeNodeOrderMap();
        DD states = graphDD.getNodeProperty(CommonProperties.STATE);
        this.states = states.supportWalker(presCube, false, false);
        this.transitions = buildTransitions(graphDD, presNodes, this.sinks,
                this.representants, cubeDD);
        presNodes.dispose();
        
        if(nondet && stateEncoding) {
            actionsEnabled = new TIntIntMap[numNonsinkNodes];
            nondetOffset = new int[numNonsinkNodes];
            countIntermediateStates(actionsEnabled);
            int offset = 0;
            for(int i = 0; i < numNonsinkNodes; i++) {
                nondetOffset[i] = offset;
                offset += actionsEnabled[i].size();
            }
            numNodesComputed += offset;
        }
        numNodesInclNondet = numNodesComputed;
    }
    
    public GraphBuilderDD(GraphDD graphDD, List<DD> sinks, boolean nondet) throws EPMCException {
        this(graphDD, sinks, nondet, true);
    }
    
    private void countIntermediateStates(TIntIntMap[] actionsEnabled) {
        if (transitions.isZero()) {
            return;
        }
        if (transitions.isLeaf()) {
            int presNode = presNodeNumber();
            int actionNumber = actionNumber();
            if(actionsEnabled[presNode] == null) {
                actionsEnabled[presNode] = new TIntIntHashMap();
            }
            actionsEnabled[presNode].putIfAbsent(actionNumber, actionsEnabled[presNode].size());
        } else {
            lowTrans();
            countIntermediateStates(actionsEnabled);
            backTrans();
            highTrans();
            countIntermediateStates(actionsEnabled);
            backTrans();
        }
    }
    
    private SupportWalker buildTransitions(GraphDD graphDD, DD nodeSpace,
            DD[] sinks, DD[] representants, DD cube) throws EPMCException {
        DD transition = graphDD.getEdgeProperty(CommonProperties.WEIGHT).clone();
        DD actionCube = graphDD.getActionCube();
        if (!this.nondet) {
            transition = transition.abstractSumWith(actionCube.clone());
        }
        DD nonSinkSpace = nodeSpace.andNot(sinks).toMTWith();
        DD nonSinkTrans = transition.clone().multiplyWith(nonSinkSpace);
        return nonSinkTrans.supportWalkerWith(cube);
    }

    private static DD[] chooseRepresentants(DD[] sinks, DD cube, DD states)
            throws EPMCException {
        DD[] representants = new DD[sinks.length];
        for (int number = 0; number < sinks.length; number++) {
            DD sinkAndStates = sinks[number].and(states);
            representants[number] = sinkAndStates.findSatWith(cube.clone());
        }
        return representants;
    }

    private static SupportWalker buildSinks(DD nodes, DD[] sinks,
            DD[] representants, DD cube, boolean stop)
            throws EPMCException {
        DD nonSinks = nodes.andNot(sinks);
        DD combined = nonSinks.toMT();
        int number = 2;
        ContextValue contextValue = nodes.getContextValue();
        List<Value> stopWhere = new ArrayList<>();
        if (stop) {
            stopWhere.add(UtilValue.newValue(TypeInteger.get(contextValue), 0));
        }
        int sinkNumber = 0;
        for (DD sink : sinks) {
            if (stop) {
                stopWhere.add(UtilValue.newValue(TypeInteger.get(contextValue), number));
                DD choice = representants[sinkNumber];
                combined = combined.addWith(choice.toMT(number, 0));
            } else {
                combined = combined.addWith(sink.toMT(number, 0));
            }
            sinkNumber++;
            number++;
        }
        SupportWalker result = combined.supportWalker(cube, stopWhere);
        combined.dispose();
        
        return result;
    }

    private static boolean assertSinks(List<DD> sinks, GraphDD graphDD)
            throws EPMCException {
        assert sinks != null;
        ContextDD contextDD = graphDD.getContextDD();
        DD allSinks = contextDD.newConstant(false);
        for (DD dd : sinks) {
            assert dd.getContext() == contextDD;
            assert dd.isBoolean();
            assert dd.isDisjoint(allSinks);
            allSinks = allSinks.orWith(dd.clone());
        }
        return true;
    }

    private static int buildMaps(SupportWalker dd, SupportWalker support,
            SupportWalkerNodeMapInt lowMap,
            SupportWalkerNodeMapInt sumMap) {
        int result;
        if (support.isLeaf()) {
            assert dd.isLeaf();
            if (ValueAlgebra.asAlgebra(dd.value()).isOne()) {
                result = 1;
            } else {
                result = 0;
            }
            lowMap.set(0);
            sumMap.set(result);
        } else if (dd.isLeaf()) {
            lowMap.set(0);
            sumMap.set(0);
            result = 0;
        } else if (sumMap.isSet()) {
            result = sumMap.getInt();
        } else {
            assert dd.variable() == support.variable();
            dd.low();
            support.high();
            int low = buildMaps(dd, support, lowMap, sumMap);
            dd.back();
            dd.high();
            int high = buildMaps(dd, support, lowMap, sumMap);
            dd.back();
            support.back();
            result = low + high;
            lowMap.set(low);
            sumMap.set(result);
        }
        return result;
    }

    int getNumNodes() {
        return numNodes;
    }
    
    private int nodeNumber() {
        assert presNodes.isLeaf();
        int leafValue = ValueInteger.asInteger(presNodes.value()).getInt();
        if (leafValue == 1) {
            int number = presNumbers.peek();
            assert number < nodeOrderMap.length : number + " " + nodeOrderMap.length;
            return nodeOrderMap[number];
        } else {
            return leafValue - 2 + numNonsinkNodes;
        }
    }
    
    private int actionNumber() {
        return currentAction >>> (31 - actionDepth);
    }
    
    private int presNodeNumber() {
        assert presNodes.isLeaf();
        int number = presNumbers.peek();
        assert number < nodeOrderMap.length : number + " " + nodeOrderMap.length;
        return nodeOrderMap[number];
    }

    private int nextNodeNumber() throws EPMCException {
        assert transitions.isLeaf();
        int leafValue = ValueInteger.asInteger(nextNodes.value()).getInt();
        if (leafValue == 1) {
            int number = nextNumbers.peek();
            assert number < nodeOrderMap.length : number + " " + nodeOrderMap.length;
            return nodeOrderMap[number];
        } else {
            return leafValue - 2 + numNonsinkNodes;
        }
    }

    private Value value() {
        return transitions.value();
    }
    
    private void lowTrans() {
        int cubeVar = transitions.variable();
        int presNodesVar = presNodes.variable();
        int nextNodesVar = nextNodes.variable();
        if (cubeVar == presNodesVar) {
            presNodes.low();
            presNumbers.push(presNumbers.peek());
        } else if (cubeVar == nextNodesVar) {
            nextNodes.low();
            nextNumbers.push(nextNumbers.peek());
        } else {
            currentAction = currentAction >>> 1;
            actionDepth++;
        }
        transitions.low();
    }
    
    void highTrans() {
        int cubeVar = transitions.variable();
        int presNodesVar = presNodes.variable();
        int nextNodesVar = nextNodes.variable();
        if (cubeVar == presNodesVar) {
            int number = presNumbers.peek();
            assert lowMapPres.isSet();
            number += lowMapPres.getInt();
            presNumbers.push(number);
            presNodes.high();
        } else if (cubeVar == nextNodesVar) {
            int number = nextNumbers.peek();
            assert lowMapNext.isSet();
            number += lowMapNext.getInt();
            nextNumbers.push(number);
            nextNodes.high();
        } else {
            currentAction = (currentAction >>> 1) | 0x40000000;
            actionDepth++;
        }
        transitions.high();
    }

    void backTrans() {
   		transitions.back();
        int cubeVar = transitions.variable();
        if (presVars.get(cubeVar)) {
            presNodes.back();
        } else if (nextVars.get(cubeVar)) {
            nextNodes.back();
        }
        if (presVars.get(cubeVar)) {
            presNumbers.pop();
        } else if (nextVars.get(cubeVar)) {
            nextNumbers.pop();
        } else {
            currentAction = (currentAction & 0x3FFFFFFF) << 1;
            actionDepth--;
        }
    }

    public GraphExplicit buildGraph() throws EPMCException {
        assert !closed;
        Options options = graphDD.getOptions();
        StopWatch timer = new StopWatch(true);
        Log log = options.get(OptionsMessages.LOG);
        log.send(MessagesGraph.CONVERTING_DD_GRAPH_TO_EXPLICIT);
        GraphExplicitWrapper graph = new GraphExplicitWrapper(graphDD.getContextValue());
        graph.addSettableGraphProperty(CommonProperties.SEMANTICS, graphDD.getGraphProperty(CommonProperties.SEMANTICS).getType());
        graph.setGraphProperty(CommonProperties.SEMANTICS, graphDD.getGraphProperty(CommonProperties.SEMANTICS));
        graph.addSettableNodeProperty(CommonProperties.STATE, TypeBoolean.get(contextDD.getContextValue()));
        graph.addSettableNodeProperty(CommonProperties.PLAYER, TypeEnum.get(contextDD.getContextValue(), Player.class));
        graph.addSettableEdgeProperty(CommonProperties.WEIGHT, TypeWeight.get(contextDD.getContextValue()));
        ContextValue contextValue = graphDD.getContextValue();
        TypeWeight typeWeight = TypeWeight.get(contextValue);
        TypeBoolean typeBoolean = TypeBoolean.get(contextValue);
        TypeEnum typePlayer = TypeEnum.get(contextValue, Player.class);
        ValueAlgebra weight = typeWeight.newValue();
        ValueAlgebra zero = typeWeight.getZero();
        ValueAlgebra one = typeWeight.getOne();
        ValueAlgebra sum = typeWeight.newValue();
        unifRate = typeWeight.newValue();
        ValueBoolean state = typeBoolean.newValue();
        ValueEnum player = typePlayer.newValue();
        ArrayList<TIntList> targets = new ArrayList<>(numNodesInclNondet);
        List<List<Value>> probs = new ArrayList<>(numNodesInclNondet);
        BitSet states = UtilBitSet.newBitSetUnbounded();

        for (int nodeNr = 0; nodeNr < numNodesInclNondet; nodeNr++) {
            targets.add(new TIntArrayList());
            probs.add(new ArrayList<Value>());
        }
        buildGraphInternal(targets, probs, states);
        ddToBitSet(this.states, states);
        if (states.length() < numNodesInclNondet) {
            for (int sinkNr = 0; sinkNr < this.sinks.length; sinkNr++) {
                states.set(states.length());
            }
        }
        if (uniformise) {
            unifRate.set(zero);
            for (int nodeNr = 0; nodeNr < numNodesInclNondet; nodeNr++) {
                List<Value> thisProbs = probs.get(nodeNr);
                sum.set(zero);
                for (Value value : thisProbs) {
                    sum.add(sum, value);
                }
                unifRate.max(unifRate, sum);
            }
        }
        NodeProperty nodePropertyState = graph.getNodeProperty(CommonProperties.STATE);
        NodeProperty nodePropertyPlayer = graph.getNodeProperty(CommonProperties.PLAYER);
        EdgeProperty edgePropertyWeight = graph.getEdgeProperty(CommonProperties.WEIGHT);
        for (int nodeNr = 0; nodeNr < numNodesInclNondet; nodeNr++) {
            TIntList thisTargets = targets.get(nodeNr);
            List<Value> thisProbs = probs.get(nodeNr);
            sum.set(zero);
            for (Value value : thisProbs) {
                sum.add(sum, value);
            }
            int numSuccessors = thisTargets.size();
            if (uniformise) {
                numSuccessors++;
            } else if (thisTargets.size() == 0) {
                numSuccessors = 1;
            }
            graph.queryNode(nodeNr);
            graph.prepareNode(nodeNr, numSuccessors);
            for (int succNr = 0; succNr < thisTargets.size(); succNr++) {
                int succNode = thisTargets.get(succNr);
                weight.set(zero);
                if (sum.isEq(zero)) {
                    weight.set(zero);
                } else {
                    weight.set(thisProbs.get(succNr));
                    if (uniformise) {
                        weight.divide(weight, unifRate);
                    }
                }
                graph.setSuccessorNode(nodeNr, succNr, succNode);
                edgePropertyWeight.set(nodeNr, succNr, weight);
            }
            if (uniformise) {
                graph.setSuccessorNode(nodeNr, thisTargets.size(), nodeNr);
                weight.subtract(unifRate, sum);
                weight.divide(weight, unifRate);
                edgePropertyWeight.set(nodeNr, thisTargets.size(), weight);
            } else if (thisTargets.size() == 0) {
                graph.setSuccessorNode(nodeNr, 0, nodeNr);
                edgePropertyWeight.set(nodeNr, 0, one);
            }

            state.set(states.get(nodeNr));
            player.set((!this.nondet || !states.get(nodeNr)) ? Player.STOCHASTIC : Player.ONE);
            nodePropertyState.set(nodeNr, state);
            nodePropertyPlayer.set(nodeNr, player);
        }
        log.send(MessagesGraph.CONVERTING_DD_GRAPH_TO_EXPLICIT_DONE, timer.getTimeSeconds());
        GraphBuilderExplicit explicitBuilder = new GraphBuilderExplicit();
        explicitBuilder.setInputGraph(graph);
        explicitBuilder.addDerivedGraphProperties(graph.getGraphProperties());
        explicitBuilder.addDerivedNodeProperties(graph.getNodeProperties());
        explicitBuilder.addDerivedEdgeProperties(graph.getEdgeProperties());
        explicitBuilder.build();
        return explicitBuilder.getOutputGraph();
    }
    
    private int[] computeNodeOrderMap() throws EPMCException {
        this.nodeOrderMap = new int[numNodes];
        for (int i = 0; i < nodeOrderMap.length; i++) {
            this.nodeOrderMap[i] = i;
        }
        BitSet states = ddToBitSet(graphDD.getNodeProperty(CommonProperties.STATE));
        int[] nodeOrderMap = new int[numNodes];
        int nodeNumber = 0;
        for (int node = states.nextSetBit(0); node >= 0;
                node = states.nextSetBit(node+1)) {
            nodeOrderMap[node] = nodeNumber;
            nodeNumber++;
        }
        for (int node = states.nextClearBit(0); node >= 0 && node < numNodes;
                node = states.nextClearBit(node+1)) {
            nodeOrderMap[node] = nodeNumber;
            nodeNumber++;
        }

        return nodeOrderMap;
    }

    private void buildGraphInternal(List<TIntList> targets,
            List<List<Value>> probs, BitSet states) throws EPMCException {
        if (transitions.isZero()) {
            return;
        }
        if (transitions.isLeaf()) {
            int presNode = presNodeNumber();
            int nextNode = nextNodeNumber();
            if (ValueAlgebra.asAlgebra(presNodes.value()).isOne()) {
                Value value = value();
                if(nondet && stateEncoding) {
                    int actionNumber = actionNumber();
                    int nondetNodeNum = numNodes + nondetOffset[presNode] + actionsEnabled[presNode].get(actionNumber);
                    boolean addNondetState = true;
                    TIntIterator it = targets.get(presNode).iterator();
                    while(it.hasNext()) {
                        if(it.next() == nondetNodeNum) {
                            addNondetState = false;
                            break;
                        }
                    }
                    if (addNondetState) {
                        targets.get(presNode).add(nondetNodeNum);
                        Value minusOne = value.getType().newValue();
                        ValueAlgebra.asAlgebra(minusOne).set(-1);
                        probs.get(presNode).add(minusOne);
                    }
                    targets.get(nondetNodeNum).add(nextNode);
                    probs.get(nondetNodeNum).add(value);
                } else {
                    targets.get(presNode).add(nextNode);
                    probs.get(presNode).add(value);
                }
            }
        } else {
            lowTrans();
            buildGraphInternal(targets, probs, states);
            backTrans();
            highTrans();
            buildGraphInternal(targets, probs, states);
            backTrans();
        }
    }

    public BitSet ddToBitSet(DD target) throws EPMCException {
        assert !closed;
        assert target != null;
        BitSet result = UtilBitSet.newBitSetUnbounded(numNodes);
        ddToBitSet(target.supportWalker(presCubeDD, false, false), result);
        return result;
    }
    
    private void ddToBitSet(SupportWalker target, BitSet bitset) {
        if (!isValidDDtoBitSet()) {
            return;
        }
        if (isLeafDDtoBitSet(target)) {
            int presNode = nodeNumber();
            bitset.set(presNode, target.isTrue());
        } else {
            lowDDToBitSet(target);
            ddToBitSet(target, bitset);
            backDDToBitSet(target);
            highDDToBitSet(target);
            ddToBitSet(target, bitset);
            backDDToBitSet(target);
        }
    }
    
    public ValueArray ddToValueArray(DD target) throws EPMCException {
        assert !closed;
        Value entry = newValueWeight();
        ValueArray result = newValueArrayWeight(numNodes);
        ddToValueArray(target.supportWalker(presCubeDD), result, entry);
        return result;
    }
    
    private ValueArray newValueArrayWeight(int size) {
        TypeArray typeArray = TypeWeight.get(getContextValue()).getTypeArray();
        return UtilValue.newArray(typeArray, size);
    }
    
    private void ddToValueArray(SupportWalker target, ValueArray array, Value entry)
            throws EPMCException {
        if (!isValidDDtoBitSet()) {
            return;
        }
        if (isLeafDDtoBitSet(target)) {
            int presNode = nodeNumber();
            entry.set(target.value());
            array.set(entry, presNode);
        } else {
            lowDDToBitSet(target);
            ddToValueArray(target, array, entry);
            backDDToBitSet(target);
            highDDToBitSet(target);
            ddToValueArray(target, array, entry);
            backDDToBitSet(target);
        }
    }

    private boolean isValidDDtoBitSet() {
        return !presNodes.isZero();
    }

    private boolean isLeafDDtoBitSet(SupportWalker target) {
        return presNodes.isLeaf();
    }

    private void lowDDToBitSet(SupportWalker target) {
        assert !presNodes.isLeaf();
        presNodes.low();
        target.low();
        presNumbers.push(presNumbers.peek());
    }

    private void highDDToBitSet(SupportWalker target) {
        assert !presNodes.isLeaf();
        int number = presNumbers.peek();
        number += lowMapPres.getInt();
        presNumbers.push(number);
        
        presNodes.high();
        target.high();
    }

    private void backDDToBitSet(SupportWalker target) {
        presNodes.back();
        target.back();
        presNumbers.pop();
    }

    public DD valuesToDD(ValueArray explResult) throws EPMCException {
        assert !closed;
        assert explResult != null;
        assert contextDD.getContextValue() == explResult.getType().getContext();
        assert explResult.getNumDimensions() == 1;
        DD result = valuesToDDRec(explResult);
        Value sinkValue = explResult.getType().getEntryType().newValue();
        Value zero = UtilValue.newValue(ValueArrayAlgebra.asArrayAlgebra(explResult).getType().getEntryType(), 0);
        for (int sinkNumber = 0; sinkNumber < sinks.length; sinkNumber++) {
            DD sink = sinks[sinkNumber];
            explResult.get(sinkValue, numNonsinkNodes + sinkNumber);
            DD sinkValueDD = sink.toMT(sinkValue, zero);
            result = sink.clone().iteWith(sinkValueDD, result);
        }
        return result;
    }
    
    private DD valuesToDDRec(ValueArray explResult) throws EPMCException {
        if (!isValidValuesToDD()) {
            return contextDD.newConstant(0);
        }
        
        if (isLeafValuesToDD()) {
            int presNode = nodeNumber();
            if (presNode < numNonsinkNodes) {
                Value entry = explResult.getType().getEntryType().newValue();
                if (presNode < explResult.size()) {
                    explResult.get(entry, presNode);
                }
                return contextDD.newConstant(entry);
            } else {
                Value value = explResult.getType().getEntryType().newValue();
                return contextDD.newConstant(value);
            }
        } else {
            lowValuesToDD();
            DD low = valuesToDDRec(explResult);
            backValuesToDD();
            highValuesToDD();
            DD high = valuesToDDRec(explResult);
            backValuesToDD();
            DD result = contextDD.variable(presNodes.variable()).ite(high, low);
            low.dispose();
            high.dispose();
            return result;
        }
    }

    private boolean isValidValuesToDD() {
        return !presNodes.isZero();
    }

    private boolean isLeafValuesToDD() {
        return presNodes.isLeaf();
    }

    private void lowValuesToDD() {
        assert !presNodes.isLeaf();
        presNodes.low();
        presNumbers.push(presNumbers.peek());
    }

    private void highValuesToDD() {
        assert !presNodes.isLeaf();
        int number = presNumbers.peek();
        number += lowMapPres.getInt();
        presNumbers.push(number);
        
        presNodes.high();
    }

    private void backValuesToDD() {
        presNodes.back();
        presNumbers.pop();
    }

    public void setUniformise(boolean uniformise) {
        assert !closed;
        this.uniformise = uniformise;
    }

    public Value getUnifRate() {
        assert !closed;
        return this.unifRate;
    }
    
    private ContextValue getContextValue() {
    	return this.contextDD.getContextValue();
    }
    
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        cubeDD.dispose();
        contextDD.dispose(sinks);
        contextDD.dispose(representants);
    }
    
    private Value newValueWeight() {
        return TypeWeight.get(getContextValue()).newValue();
    }

}
