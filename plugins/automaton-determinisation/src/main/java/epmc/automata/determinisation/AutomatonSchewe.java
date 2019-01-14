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

package epmc.automata.determinisation;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;

import java.util.Arrays;

import epmc.automaton.AutomatonExporter;
import epmc.automaton.AutomatonExporterDot;
import epmc.automaton.AutomatonLabelUtil;
import epmc.automaton.AutomatonMaps;
import epmc.automaton.AutomatonParity;
import epmc.automaton.AutomatonRabin;
import epmc.automaton.AutomatonSafra;
import epmc.automaton.AutomatonStateUtil;
import epmc.automaton.Buechi;
import epmc.automaton.BuechiSubsetCache;
import epmc.automaton.BuechiTransition;
import epmc.expression.Expression;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.util.BitSet;
import epmc.util.HashingStrategyArrayInt;
import epmc.util.UtilBitSet;
import epmc.value.Value;

final class AutomatonSchewe implements AutomatonRabin, AutomatonParity, AutomatonSafra {
    public final static class Builder implements AutomatonParity.Builder {
        private Buechi buechi;
        private BitSet init;
        private boolean parity;

        @Override
        public Builder setBuechi(Buechi buechi)  {
            this.buechi = buechi;
            return this;
        }

        private Buechi getBuechi() {
            return buechi;
        }

        public Builder setInit(BitSet init) {
            this.init = init;
            return this;
        }

        private BitSet getInit() {
            return init;
        }

        public Builder setParity(boolean parity) {
            this.parity = parity;
            return this;
        }

        private boolean isParity() {
            return parity;
        }

        @Override
        public AutomatonSchewe build() {
            return new AutomatonSchewe(this);
        }

    }

    private final boolean useAutomatonMapsCache = false;
    private final AutomatonMaps<AutomatonScheweState,AutomatonScheweLabeling> automatonMaps = new AutomatonMaps<>();
    private AutomatonScheweState succState;
    private int succStateNumber;
    private AutomatonScheweLabeling succLabel;
    private int succLabelNumber;
    private final int numLabels;
    private final GraphExplicit buechiGraph;
    private final AutomatonScheweState initState;
    private final Object2IntOpenCustomHashMap<int[]> nodeNumbers = new Object2IntOpenCustomHashMap<>(HashingStrategyArrayInt.getInstance());
    private final Buechi buechi;
    private final Expression[] expressions;
    private boolean parity;
    private final BitSet prioritiesSeen;
    private final BuechiSubsetCache<AutomatonScheweState,AutomatonScheweLabeling> cache;

    private AutomatonSchewe(Builder builder) {
        assert builder != null;
        assert builder.getBuechi() != null;
        nodeNumbers.defaultReturnValue(-1);
        this.parity = builder.isParity();
        buechi = builder.getBuechi();
        this.numLabels = buechi.getNumLabels();
        this.buechiGraph = buechi.getGraph();
        BitSet init = builder.getInit();
        if (init == null) {
            init = UtilBitSet.newBitSetUnbounded();
            init.or(buechi.getGraph().getInitialNodes());
        }
        init = init.clone();
        this.initState = makeUnique(new AutomatonScheweState(this, init, null));
        this.expressions = buechi.getExpressions();
        this.prioritiesSeen = UtilBitSet.newBitSetUnbounded();
        if (useAutomatonMapsCache) {
            this.automatonMaps.initialiseCache(expressions);
        }
        cache = new BuechiSubsetCache<>(buechi);
    }

    @Override
    public int getNumStates() {
        return automatonMaps.getNumStates();
    }

    private AutomatonScheweState makeUnique(AutomatonScheweState state) {
        return automatonMaps.makeUnique(state);
    }

    private AutomatonScheweLabeling makeUnique(AutomatonScheweLabeling label) {
        return automatonMaps.makeUnique(label);
    }

    @Override
    public AutomatonStateUtil numberToState(int number) {
        return automatonMaps.numberToState(number);
    }

    @Override
    public int getInitState() {
        return initState.getNumber();
    }

    @Override
    public void queryState(Value[] modelState, int automatonState) {
        assert modelState != null;
        long combined;
        if (useAutomatonMapsCache) {
            combined = automatonMaps.lookupSuccessorEntry(modelState, automatonState);
        } else {
            combined = -1L;
        }
        if (combined != -1L) {
            succStateNumber = AutomatonMaps.getSuccessorState(combined);
            succLabelNumber = AutomatonMaps.getSuccessorLabel(combined);
            return;
        }
        AutomatonScheweState scheweState = (AutomatonScheweState) numberToState(automatonState);
        buechi.query(modelState);
        lookupCache(scheweState);
        if (succState != null) {
            succStateNumber = succState.getNumber();
            succLabelNumber = succLabel.getNumber();
            if (useAutomatonMapsCache) {
                automatonMaps.insertSuccessorEntry(modelState, automatonState, succStateNumber, succLabelNumber);
            }
            return;
        }
        int[] nextIndex = new int[1];
        succState = prepareSuccessor(scheweState, nextIndex);
        nextIndex[0]++;
        rawUpdate(scheweState, succState);
        sproutNewChildren(scheweState, succState, nextIndex);
        BitSet steal = UtilBitSet.newBitSetUnbounded(buechiGraph.getNumNodes());
        stealLabels(succState, steal);
        int[] nodeNumber = new int[buechiGraph.getNumNodes() + 1];
        BitSet accepting = UtilBitSet.newBitSetUnbounded();
        BitSet stable = UtilBitSet.newBitSetUnbounded();
        int[] minAcc = new int[1];
        minAcc[0] = Integer.MAX_VALUE;
        removeAndAccept(succState, nodeNumber, 0, accepting, minAcc);
        int[] minRej = new int[1];
        minRej[0] = Integer.MAX_VALUE;
        removeAndStable(succState, nodeNumber, 0, stable, minRej);
        if (parity) {
            adjustIndices(succState);
        }
        if (succState.getStates().isEmpty()) {
            minAcc[0] = Integer.MAX_VALUE;
            minRej[0] = 2;
            accepting.clear();
            stable.clear();
        }
        int priority = 0;
        if (parity) {
            assert minAcc[0] != minRej[0] || minAcc[0] == Integer.MAX_VALUE
                    : minAcc[0] + " " + minRej[0];
            assert minAcc[0] >= 0 : minAcc[0];
            assert minRej[0] >= 0 : minRej[0];
            if (minAcc[0] == Integer.MAX_VALUE && minRej[0] == Integer.MAX_VALUE) {
                priority = Integer.MAX_VALUE;
            } else {
                priority = minAcc[0] < minRej[0] ? 2*minAcc[0] : 2*minRej[0]-1;
            }
            assert priority >= 0 : priority + " " + minAcc[0] + " " + minRej[0];
        }
        succLabel = new AutomatonScheweLabeling(this, accepting, stable, priority);
        if (priority != Integer.MAX_VALUE && !prioritiesSeen.get(priority)) {
            prioritiesSeen.set(priority);
        }
        succState = makeUnique(succState);
        succLabel = makeUnique(succLabel);
        insertCache();
        succStateNumber = succState.getNumber();
        succLabelNumber = succLabel.getNumber();
        if (useAutomatonMapsCache) {
            automatonMaps.insertSuccessorEntry(modelState, automatonState, succStateNumber, succLabelNumber);
        }
    }

    private static void adjustIndices(AutomatonScheweState state) {
        BitSet oldIndices = UtilBitSet.newBitSetUnbounded();
        Int2ObjectOpenHashMap<AutomatonScheweState> stateToOldIndex =
                new Int2ObjectOpenHashMap<>();
        collectIndices(state, oldIndices, stateToOldIndex);
        int newIndex = 0;
        for (int oldIndex = oldIndices.nextSetBit(0); oldIndex >= 0;
                oldIndex = oldIndices.nextSetBit(oldIndex+1)) {
            stateToOldIndex.get(oldIndex).setIndex(newIndex);
            newIndex++;
        }
    }

    private static void collectIndices(AutomatonScheweState state, BitSet indices,
            Int2ObjectOpenHashMap<AutomatonScheweState> stateToOldIndex) {
        int index = state.getIndex();
        indices.set(index);
        stateToOldIndex.put(index, state);
        for (AutomatonScheweState child : state.getChildren()) {
            collectIndices(child, indices, stateToOldIndex);
        }
    }

    private void lookupCache(AutomatonScheweState scheweState) {
        BuechiSubsetCache<AutomatonScheweState, AutomatonScheweLabeling>.CacheValue entry = cache.lookup(scheweState);
        if (entry != null) {
            succState = entry.getState();
            succLabel = entry.getLabeling();
        } else {
            succState = null;
            succLabel = null;
        }
    }

    private void insertCache() {
        cache.insert(succState, succLabel);
    }

    /**
     * Prepares successor of state.
     * The state obtained has the same nodes as the old one plus a new child
     * for each existing node. In the new state, only the structure for the
     * children is created, the state labeling is null after this function call.
     */
    private AutomatonScheweState prepareSuccessor(AutomatonScheweState state,
            int[] nextIndex) {
        AutomatonScheweState[] children = state.getChildren();
        final int numChildren = children.length;
        AutomatonScheweState[] newChildren = new AutomatonScheweState[numChildren + 1];
        for (int child = 0; child < children.length; child++) {
            newChildren[child] = prepareSuccessor(children[child], nextIndex);
        }
        AutomatonScheweState result = new AutomatonScheweState(this);
        if (parity) {
            int index = state.getIndex();
            result.setIndex(index);
            nextIndex[0] = Math.max(nextIndex[0], index);
        }
        result.setChildren(newChildren);
        result.setAcceptance(state.getAcceptance());
        return result;
    }

    /**
     * Performs the raw update step of Schewe's determinisation algorithm.
     * For all nodes, compute the set of states reached by the action read from
     * model state from any state the node is labeled with.
     * 
     * @param scheweState present state
     * @param succState successor state
     */
    private void rawUpdate(AutomatonScheweState scheweState,
            AutomatonScheweState succState) {
        BitSet succs = UtilBitSet.newBitSetUnbounded(buechiGraph.getNumNodes());
        EdgeProperty labels = buechiGraph.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        for (int state = 0; state < buechiGraph.getNumNodes(); state++) {
            if (scheweState.getStates().get(state)) {
                for (int succNr = 0; succNr < buechiGraph.getNumSuccessors(state); succNr++) {
                    BuechiTransition trans = labels.getObject(state, succNr);
                    if (trans.guardFulfilled()) {
                        int succ = buechiGraph.getSuccessorNode(state, succNr);
                        succs.set(succ);
                    }
                }
            }
        }
        succState.setStates(succs);
        AutomatonScheweState[] oldChildren = scheweState.getChildren();
        AutomatonScheweState[] newChildren = succState.getChildren();
        for (int child = 0; child < oldChildren.length; child++) {
            rawUpdate(oldChildren[child], newChildren[child]);
        }
    }

    private void sproutNewChildren(AutomatonScheweState scheweState,
            AutomatonScheweState succState, int[] nextIndex)
    {
        BitSet succs = UtilBitSet.newBitSetUnbounded(buechiGraph.getNumNodes());
        EdgeProperty labels = buechiGraph.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        for (int state = 0; state < buechiGraph.getNumNodes(); state++) {
            if (scheweState.getStates().get(state)) {
                for (int succNr = 0; succNr < buechiGraph.getNumSuccessors(state); succNr++) {
                    BuechiTransition trans = labels.getObject(state, succNr);
                    BitSet label = trans.getLabeling();
                    int succ = buechiGraph.getSuccessorNode(state, succNr);
                    if (label.get(scheweState.getAcceptance())
                            && trans.guardFulfilled()) {
                        succs.set(succ);
                    }
                }
            }
        }
        AutomatonScheweState newState = new AutomatonScheweState(this);
        if (parity) {
            newState.setIndex(nextIndex[0]);
            nextIndex[0]++;
        }
        newState.setStates(succs);
        newState.setChildren(new AutomatonScheweState[0]);
        AutomatonScheweState[] oldChildren = scheweState.getChildren();
        AutomatonScheweState[] newChildren = succState.getChildren();
        for (int child = 0; child < oldChildren.length; child++) {
            sproutNewChildren(oldChildren[child], newChildren[child], nextIndex);
        }
        newChildren[newChildren.length - 1] = newState;
    }

    private void stealLabels(AutomatonScheweState succState, BitSet steal) {
        succState.getStates().andNot(steal);
        AutomatonScheweState[] children = succState.getChildren();
        for (int child = 0; child < children.length; child++) {
            stealLabels(children[child], steal);
        }
        steal.or(succState.getStates());
    }

    private void removeAndAccept(AutomatonScheweState succState,
            int[] nodeNumber, int size, BitSet accepting, int[] minAcc) {
        if (succState.getStates().isEmpty()) {
            return;
        }
        AutomatonScheweState[] children = succState.getChildren();
        BitSet union = UtilBitSet.newBitSetUnbounded(buechiGraph.getNumNodes());
        for (AutomatonScheweState child : children) {
            union.or(child.getStates());
        }
        if (succState.getStates().equals(union)) {
            succState.setChildren(new AutomatonScheweState[0]);
            succState.setAcceptance((succState.getAcceptance() + 1) % numLabels);
            if (parity) {
                minAcc[0] = Math.min(minAcc[0], succState.getIndex());
            } else {
                accepting.set(nodeToNumber(nodeNumber, size));
            }
        } else {
            for (int child = 0; child < children.length; child++) {
                nodeNumber[size] = child;
                removeAndAccept(children[child], nodeNumber, size + 1, accepting, minAcc);
            }
        }
    }

    private void removeAndStable(AutomatonScheweState succState,
            int[] nodeNumber, int size, BitSet stable, int[] minRej) {
        if (nodeNumber != null) {
            if (!parity) {
                stable.set(nodeToNumber(nodeNumber, size));
            }
        } else if (parity) {
            minRej[0] = Math.min(minRej[0], succState.getIndex());
        }
        AutomatonScheweState[] children = succState.getChildren();
        int numNewChildren = 0;
        for (AutomatonScheweState child : children) {
            if (!child.getStates().isEmpty()) {
                numNewChildren++;
            }
        }
        AutomatonScheweState[] newChildren = new AutomatonScheweState[numNewChildren];
        int newChildIndex = 0;
        for (int childNr = 0; childNr < children.length; childNr++) {
            AutomatonScheweState child = children[childNr];
            if (!child.getStates().isEmpty()) {
                newChildren[newChildIndex] = child;
                if (nodeNumber != null) {
                    nodeNumber[size] = childNr;
                }
                newChildIndex++;
            } else {
                nodeNumber = null;
            }
            removeAndStable(child, nodeNumber, size + 1, stable, minRej);
        }
        succState.setChildren(newChildren);
    }

    private int nodeToNumber(int[] node, int size) {
        int[] entry = Arrays.copyOf(node, size);
        int number = nodeNumbers.getInt(entry);
        if (number == -1) {
            number = nodeNumbers.size();
            nodeNumbers.put(entry, number);
        }
        return number;
    }

    @Override
    public int getSuccessorState() {
        return succStateNumber;
    }

    @Override
    public int getSuccessorLabel() {
        return succLabelNumber;
    }

    @Override
    public int getNumPairs() {
        return nodeNumbers.size();
    }

    @Override
    public Buechi getBuechi() {
        return buechi;
    }

    @Override
    public Expression[] getExpressions() {
        return expressions;
    }

    @Override
    public AutomatonLabelUtil numberToLabel(int number) {
        return automatonMaps.numberToLabel(number);
    }

    boolean isParity() {
        return parity;
    }

    @Override
    public int getNumPriorities() {
        return prioritiesSeen.length();
    }

    @Override
    public String toString() {
        AutomatonExporter exporter = new AutomatonExporterDot();
        exporter.setAutomaton(this);
        return exporter.toString();
    }
}
