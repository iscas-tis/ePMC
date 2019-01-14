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

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.Permutation;
import epmc.dd.SupportWalker;
import epmc.dd.SupportWalkerNodeMapInt;
import epmc.dd.Walker;
import epmc.graph.dd.GraphDD;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitWrapper;
import epmc.graph.explicit.NodeProperty;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorIsOne;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorSet;
import epmc.operator.OperatorSubtract;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.StopWatch;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
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
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntListIterator;

// TODO integrate transitions with presNodes for further speedup

public final class GraphBuilderDD implements Closeable {
    private boolean closed;
    private final Int2IntOpenHashMap varToNumber = new Int2IntOpenHashMap();
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
    private final IntArrayList presNumbers = new IntArrayList();
    private final IntArrayList nextNumbers = new IntArrayList();
    private final GraphDD graphDD;
    private boolean uniformise;
    private ValueAlgebra unifRate;
    private int[] nodeOrderMap;
    private Int2IntOpenHashMap[] actionsEnabled;
    private int[] nondetOffset;
    private final DD cubeDD;
    private final DD[] sinks;
    private final DD[] representants;
    private final boolean nondet;
    private final boolean stateEncoding;
    private final OperatorEvaluator isOne;
    private final ValueBoolean cmp;

    public GraphBuilderDD(GraphDD graphDD, List<DD> sinks, boolean nondet, boolean stateEncoding) {
        assert graphDD != null;
        assert assertSinks(sinks, graphDD);
        isOne = ContextValue.get().getEvaluator(OperatorIsOne.IS_ONE, TypeInteger.get());
        cmp = TypeBoolean.get().newValue();

        this.presVars = UtilBitSet.newBitSetUnbounded();
        this.nextVars = UtilBitSet.newBitSetUnbounded();
        this.nondet = nondet;
        this.stateEncoding = stateEncoding;
        if (nondet) {
            // TODO following seems not to be necessary anymore
//            sinks = Collections.emptyList();            
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
        ContextDD.get().dispose(nextRepresentants);
        nextNodes.dispose();
        ContextDD.get().dispose(nextSinks);

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
            actionsEnabled = new Int2IntOpenHashMap[numNonsinkNodes];
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

    public GraphBuilderDD(GraphDD graphDD, List<DD> sinks, boolean nondet) {
        this(graphDD, sinks, nondet, true);
    }

    private void countIntermediateStates(Int2IntOpenHashMap[] actionsEnabled) {
        if (transitions.isZero()) {
            return;
        }
        if (transitions.isLeaf()) {
            int presNode = presNodeNumber();
            int actionNumber = actionNumber();
            if(actionsEnabled[presNode] == null) {
                actionsEnabled[presNode] = new Int2IntOpenHashMap();
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
            DD[] sinks, DD[] representants, DD cube) {
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
    {
        DD[] representants = new DD[sinks.length];
        for (int number = 0; number < sinks.length; number++) {
            DD sinkAndStates = sinks[number].and(states);
            representants[number] = sinkAndStates.findSatWith(cube.clone());
        }
        return representants;
    }

    private static SupportWalker buildSinks(DD nodes, DD[] sinks,
            DD[] representants, DD cube, boolean stop)
    {
        DD nonSinks = nodes.andNot(sinks);
        DD combined = nonSinks.toMT();
        int number = 2;
        List<Value> stopWhere = new ArrayList<>();
        if (stop) {
            stopWhere.add(UtilValue.newValue(TypeInteger.get(), 0));
        }
        int sinkNumber = 0;
        for (DD sink : sinks) {
            if (stop) {
                stopWhere.add(UtilValue.newValue(TypeInteger.get(), number));
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
    {
        assert sinks != null;
        ContextDD contextDD = ContextDD.get();
        DD allSinks = contextDD.newConstant(false);
        for (DD dd : sinks) {
            assert dd.isBoolean();
            assert dd.isDisjoint(allSinks);
            allSinks = allSinks.orWith(dd.clone());
        }
        return true;
    }

    private int buildMaps(SupportWalker dd, SupportWalker support,
            SupportWalkerNodeMapInt lowMap,
            SupportWalkerNodeMapInt sumMap) {
        int result;
        if (support.isLeaf()) {
            assert dd.isLeaf();
            if (isOne == null) {
                System.out.println(" ===> " + dd.value().getType());
            }
            isOne.apply(cmp, dd.value());
            if (cmp.getBoolean()) {
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
        int leafValue = ValueInteger.as(presNodes.value()).getInt();
        if (leafValue == 1) {
            int number = presNumbers.topInt();
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
        int number = presNumbers.topInt();
        assert number < nodeOrderMap.length : number + " " + nodeOrderMap.length;
        return nodeOrderMap[number];
    }

    private int nextNodeNumber() {
        assert transitions.isLeaf();
        int leafValue = ValueInteger.as(nextNodes.value()).getInt();
        if (leafValue == 1) {
            int number = nextNumbers.topInt();
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
            presNumbers.push(presNumbers.topInt());
        } else if (cubeVar == nextNodesVar) {
            nextNodes.low();
            nextNumbers.push(nextNumbers.topInt());
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
            int number = presNumbers.topInt();
            assert lowMapPres.isSet();
            number += lowMapPres.getInt();
            presNumbers.push(number);
            presNodes.high();
        } else if (cubeVar == nextNodesVar) {
            int number = nextNumbers.topInt();
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
            presNumbers.popInt();
        } else if (nextVars.get(cubeVar)) {
            nextNumbers.popInt();
        } else {
            currentAction = (currentAction & 0x3FFFFFFF) << 1;
            actionDepth--;
        }
    }

    public GraphExplicit buildGraph() {
        assert !closed;
        Options options = Options.get();
        StopWatch timer = new StopWatch(true);
        Log log = options.get(OptionsMessages.LOG);
        log.send(MessagesGraph.CONVERTING_DD_GRAPH_TO_EXPLICIT);
        GraphExplicitWrapper graph = new GraphExplicitWrapper();
        graph.addSettableGraphProperty(CommonProperties.SEMANTICS, graphDD.getGraphProperty(CommonProperties.SEMANTICS).getType());
        graph.setGraphProperty(CommonProperties.SEMANTICS, graphDD.getGraphProperty(CommonProperties.SEMANTICS));
        graph.addSettableNodeProperty(CommonProperties.STATE, TypeBoolean.get());
        graph.addSettableNodeProperty(CommonProperties.PLAYER, TypeEnum.get(Player.class));
        graph.addSettableEdgeProperty(CommonProperties.WEIGHT, TypeWeight.get());
        OperatorEvaluator subtract = ContextValue.get().getEvaluator(OperatorSubtract.SUBTRACT, TypeWeight.get(), TypeWeight.get());
        TypeWeight typeWeight = TypeWeight.get();
        TypeBoolean typeBoolean = TypeBoolean.get();
        TypeEnum typePlayer = TypeEnum.get(Player.class);
        ValueAlgebra weight = typeWeight.newValue();
        ValueAlgebra zero = typeWeight.newValue();
        zero.set(0);
        ValueAlgebra one = typeWeight.newValue();
        one.set(1);
        ValueAlgebra sum = typeWeight.newValue();
        OperatorEvaluator eq = ContextValue.get().getEvaluator(OperatorEq.EQ, typeWeight, typeWeight);
        ValueBoolean cmp = TypeBoolean.get().newValue();
        unifRate = typeWeight.newValue();
        OperatorEvaluator divide = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, TypeWeight.get(), TypeWeight.get());
        ValueBoolean state = typeBoolean.newValue();
        ValueEnum player = typePlayer.newValue();
        ArrayList<IntArrayList> targets = new ArrayList<>(numNodesInclNondet);
        List<List<Value>> probs = new ArrayList<>(numNodesInclNondet);
        BitSet states = UtilBitSet.newBitSetUnbounded();
        OperatorEvaluator max = ContextValue.get().getEvaluator(OperatorMax.MAX, unifRate.getType(), sum.getType());
        for (int nodeNr = 0; nodeNr < numNodesInclNondet; nodeNr++) {
            targets.add(new IntArrayList());
            probs.add(new ArrayList<Value>());
        }
        buildGraphInternal(targets, probs, states);
        ddToBitSet(this.states, states);
        if (states.length() < numNodesInclNondet) {
            for (int sinkNr = 0; sinkNr < this.sinks.length; sinkNr++) {
                states.set(states.length());
            }
        }
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, sum.getType(), sum.getType());
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeWeight.get(), TypeWeight.get());
        if (uniformise) {
            set.apply(unifRate, zero);
            for (int nodeNr = 0; nodeNr < numNodesInclNondet; nodeNr++) {
                List<Value> thisProbs = probs.get(nodeNr);
                set.apply(sum, zero);
                for (Value value : thisProbs) {
                    add.apply(sum, sum, value);
                }
                max.apply(unifRate, unifRate, sum);
            }
        }
        NodeProperty nodePropertyState = graph.getNodeProperty(CommonProperties.STATE);
        NodeProperty nodePropertyPlayer = graph.getNodeProperty(CommonProperties.PLAYER);
        EdgeProperty edgePropertyWeight = graph.getEdgeProperty(CommonProperties.WEIGHT);
        for (int nodeNr = 0; nodeNr < numNodesInclNondet; nodeNr++) {
            IntArrayList thisTargets = targets.get(nodeNr);
            List<Value> thisProbs = probs.get(nodeNr);
            set.apply(sum, zero);
            for (Value value : thisProbs) {
                add.apply(sum, sum, value);
            }
            int numSuccessors = thisTargets.size();
            if (uniformise) {
                numSuccessors++;
            } else if (thisTargets.size() == 0) {
                numSuccessors = 1;
            }
            graph.prepareNode(nodeNr, numSuccessors);
            for (int succNr = 0; succNr < thisTargets.size(); succNr++) {
                int succNode = thisTargets.getInt(succNr);
                set.apply(weight, zero);
                eq.apply(cmp, sum, zero);
                if (cmp.getBoolean()) {
                    set.apply(weight, zero);
                } else {
                    set.apply(weight, thisProbs.get(succNr));
                    if (uniformise) {
                        divide.apply(weight, weight, unifRate);
                    }
                }
                graph.setSuccessorNode(nodeNr, succNr, succNode);
                edgePropertyWeight.set(nodeNr, succNr, weight);
            }
            if (uniformise) {
                graph.setSuccessorNode(nodeNr, thisTargets.size(), nodeNr);
                subtract.apply(weight, unifRate, sum);
                divide.apply(weight, weight, unifRate);
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

    private int[] computeNodeOrderMap() {
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

    private void buildGraphInternal(List<IntArrayList> targets,
            List<List<Value>> probs, BitSet states) {
        if (transitions.isZero()) {
            return;
        }
        if (transitions.isLeaf()) {
            int presNode = presNodeNumber();
            int nextNode = nextNodeNumber();
            isOne.apply(cmp, presNodes.value());
            if (cmp.getBoolean()) {
                Value value = value();
                if(nondet && stateEncoding) {
                    int actionNumber = actionNumber();
                    int nondetNodeNum = numNodes + nondetOffset[presNode] + actionsEnabled[presNode].get(actionNumber);
                    boolean addNondetState = true;
                    IntListIterator it = targets.get(presNode).iterator();
                    while(it.hasNext()) {
                        if (it.nextInt() == nondetNodeNum) {
                            addNondetState = false;
                            break;
                        }
                    }
                    if (addNondetState) {
                        targets.get(presNode).add(nondetNodeNum);
                        Value minusOne = value.getType().newValue();
                        ValueAlgebra.as(minusOne).set(-1);
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

    public BitSet ddToBitSet(DD target) {
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

    public ValueArray ddToValueArray(DD target) {
        assert !closed;
        Value entry = newValueWeight();
        ValueArray result = newValueArrayWeight(numNodes);
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, target.getType(), TypeWeight.get());
        ddToValueArray(target.supportWalker(presCubeDD), result, entry, set);
        return result;
    }

    private ValueArray newValueArrayWeight(int size) {
        TypeArray typeArray = TypeWeight.get().getTypeArray();
        return UtilValue.newArray(typeArray, size);
    }

    private void ddToValueArray(SupportWalker target, ValueArray array, Value entry,
            OperatorEvaluator set) {
        if (!isValidDDtoBitSet()) {
            return;
        }
        if (isLeafDDtoBitSet(target)) {
            int presNode = nodeNumber();
            set.apply(entry, target.value());
            array.set(entry, presNode);
        } else {
            lowDDToBitSet(target);
            ddToValueArray(target, array, entry, set);
            backDDToBitSet(target);
            highDDToBitSet(target);
            ddToValueArray(target, array, entry, set);
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
        presNumbers.push(presNumbers.topInt());
    }

    private void highDDToBitSet(SupportWalker target) {
        assert !presNodes.isLeaf();
        int number = presNumbers.topInt();
        number += lowMapPres.getInt();
        presNumbers.push(number);

        presNodes.high();
        target.high();
    }

    private void backDDToBitSet(SupportWalker target) {
        presNodes.back();
        target.back();
        presNumbers.popInt();
    }

    public DD valuesToDD(ValueArray explResult) {
        assert !closed;
        assert explResult != null;
        DD result = valuesToDDRec(explResult);
        Value sinkValue = explResult.getType().getEntryType().newValue();
        Value zero = UtilValue.newValue(ValueArrayAlgebra.as(explResult).getType().getEntryType(), 0);
        for (int sinkNumber = 0; sinkNumber < sinks.length; sinkNumber++) {
            DD sink = sinks[sinkNumber];
            explResult.get(sinkValue, numNonsinkNodes + sinkNumber);
            DD sinkValueDD = sink.toMT(sinkValue, zero);
            result = sink.clone().iteWith(sinkValueDD, result);
        }
        return result;
    }

    private DD valuesToDDRec(ValueArray explResult) {
        if (!isValidValuesToDD()) {
            return ContextDD.get().newConstant(0);
        }

        if (isLeafValuesToDD()) {
            int presNode = nodeNumber();
            if (presNode < numNonsinkNodes) {
                Value entry = explResult.getType().getEntryType().newValue();
                if (presNode < explResult.size()) {
                    explResult.get(entry, presNode);
                }
                return ContextDD.get().newConstant(entry);
            } else {
                Value value = explResult.getType().getEntryType().newValue();
                return ContextDD.get().newConstant(value);
            }
        } else {
            lowValuesToDD();
            DD low = valuesToDDRec(explResult);
            backValuesToDD();
            highValuesToDD();
            DD high = valuesToDDRec(explResult);
            backValuesToDD();
            DD result = ContextDD.get().variable(presNodes.variable()).ite(high, low);
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
        presNumbers.push(presNumbers.topInt());
    }

    private void highValuesToDD() {
        assert !presNodes.isLeaf();
        int number = presNumbers.topInt();
        number += lowMapPres.getInt();
        presNumbers.push(number);

        presNodes.high();
    }

    private void backValuesToDD() {
        presNodes.back();
        presNumbers.popInt();
    }

    public void setUniformise(boolean uniformise) {
        assert !closed;
        this.uniformise = uniformise;
    }

    public Value getUnifRate() {
        assert !closed;
        return this.unifRate;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        cubeDD.dispose();
        ContextDD.get().dispose(sinks);
        ContextDD.get().dispose(representants);
    }

    private Value newValueWeight() {
        return TypeWeight.get().newValue();
    }

}
