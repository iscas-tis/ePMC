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

package epmc.propertysolver.ltllazy.automata;

import java.util.HashMap;

import epmc.automaton.Automaton;
import epmc.automaton.AutomatonLabelUtil;
import epmc.automaton.AutomatonMaps;
import epmc.automaton.AutomatonStateBuechiSubset;
import epmc.automaton.AutomatonStateUtil;
import epmc.automaton.Buechi;
import epmc.automaton.BuechiSubsetCache;
import epmc.automaton.BuechiTransition;
import epmc.expression.Expression;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;
import epmc.util.UtilBitSet;
import epmc.value.Value;

public final class AutomatonSubset implements Automaton {
    public final static class Builder implements Automaton.Builder {
        private Buechi buechi;

        @Override
        public Builder setBuechi(Buechi buechi) {
            this.buechi = buechi;
            return this;
        }

        private Buechi getBuechi() {
            return buechi;
        }

        @Override
        public AutomatonSubset build() {
            return new AutomatonSubset(this);
        }

    }

    private final AutomatonMaps<AutomatonStateBuechiImpl,AutomatonSubsetLabelImpl> automatonMaps = new AutomatonMaps<>();
    public final static String IDENTIFIER = "subset";

    private AutomatonSubset(Builder builder) {
        buechi = builder.getBuechi();
        this.numLabels = buechi.getNumLabels();
        this.automaton = buechi.getGraph();
        BitSet initBitSet = new BitSetUnboundedLongArray();
        initBitSet.or(buechi.getGraph().getInitialNodes());
        initState = (AutomatonStateBuechiImpl) numberToState(getState(initBitSet));
        trueState = buechi.getTrueState();
        this.expressions = buechi.getExpressions();
        guardsValid = UtilBitSet.newBitSetUnbounded();
        cache = new BuechiSubsetCache<>(buechi);
    }

    @Override
    public int getNumStates() {
        return automatonMaps.getNumStates();
    }

    protected AutomatonStateBuechiImpl makeUnique(AutomatonStateBuechiImpl state) {
        return automatonMaps.makeUnique(state);
    }

    protected AutomatonSubsetLabelImpl makeUnique(AutomatonSubsetLabelImpl label) {
        return automatonMaps.makeUnique(label);
    }

    @Override
    public AutomatonStateUtil numberToState(int number) {
        return automatonMaps.numberToState(number);
    }

    private final class AutomatonStateBuechiImpl implements AutomatonSubsetState, AutomatonStateBuechiSubset, AutomatonStateUtil {
        private final AutomatonSubset automaton;
        private final BitSet states;
        private int number;

        AutomatonStateBuechiImpl(AutomatonSubset automaton, BitSet states) {
            assert states != null;
            this.automaton = automaton;
            this.states = states;
        }

        AutomatonStateBuechiImpl(AutomatonStateBuechiImpl other) {
            this(other.getAutomaton(), other.states);
        }

        @Override
        protected AutomatonStateBuechiImpl clone() {
            return new AutomatonStateBuechiImpl(this);
        }

        @Override
        public String toString() {
            return states.toString();
        }

        boolean get(int index) {
            assert index >= 0;
            return states.get(index);
        }

        @Override
        public boolean equals(Object obj) {
            assert obj != null;
            if (!(obj instanceof AutomatonStateBuechiImpl)) {
                return false;
            }
            AutomatonStateBuechiImpl other = (AutomatonStateBuechiImpl) obj;
            return this.states.equals(other.states);
        }

        @Override
        public int hashCode() {
            return states.hashCode();
        }

        @Override
        public
        BitSet getStates() {
            return states;
        }

        @Override
        public AutomatonSubset getAutomaton() {
            return automaton;
        }

        @Override
        public void setNumber(int number) {
            this.number = number;
        }

        @Override
        public int getNumber() {
            return this.number;
        }
    }


    private final class AutomatonSubsetLabelImpl implements AutomatonSubsetLabel, AutomatonLabelUtil {
        private final BitSet under;
        private final BitSet over;
        private int number;

        AutomatonSubsetLabelImpl(AutomatonSubset ltl2ba, BitSet under, BitSet over) {
            this.under = under;
            this.over = over;
        }

        @Override
        public BitSet getUnder() {
            return under;
        }

        @Override
        public BitSet getOver() {
            return over;
        }

        @Override
        public int getNumber() {
            return number;
        }

        @Override
        public void setNumber(int number) {
            this.number = number;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof AutomatonSubsetLabelImpl)) {
                return false;
            }
            AutomatonSubsetLabelImpl other = (AutomatonSubsetLabelImpl) obj;
            return this.under.equals(other.under) && this.over.equals(other.over);
        }

        @Override
        public int hashCode() {
            int hash = 0;
            hash = under.hashCode() + (hash << 6) + (hash << 16) - hash;
            hash = over.hashCode() + (hash << 6) + (hash << 16) - hash;
            return hash;
        }

        @Override
        public String toString() {
            return under + " / " + over;
        }


    }

    private final class SubsetCacheKey implements Cloneable {
        private AutomatonSubsetState state;
        private BitSet guards;

        @Override
        public int hashCode() {
            int hash = 0;
            hash = state.hashCode() + (hash << 6) + (hash << 16) - hash;
            hash = guards.hashCode() + (hash << 6) + (hash << 16) - hash;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SubsetCacheKey)) {
                return false;
            }
            SubsetCacheKey other = (SubsetCacheKey) obj;
            return state.equals(other.state) && guards.equals(other.guards);
        }

        @Override
        protected SubsetCacheKey clone() {
            SubsetCacheKey result = new SubsetCacheKey();
            result.state = state;
            result.guards = guards.clone();
            return result;
        }
    }

    private final class SubsetCacheValue {
        private final AutomatonStateBuechiImpl state;
        private final AutomatonSubsetLabelImpl labeling;

        SubsetCacheValue(AutomatonStateBuechiImpl state, AutomatonSubsetLabelImpl labeling) {
            this.state = state;
            this.labeling = labeling;
        }
    }

    private final AutomatonStateBuechiImpl initState;
    private AutomatonStateBuechiImpl succState;
    private AutomatonSubsetLabelImpl succLabel;
    private final int numLabels;
    private final GraphExplicit automaton;
    private final int trueState;
    private final Expression[] expressions;
    private final Buechi buechi;
    private final HashMap<SubsetCacheKey,SubsetCacheValue> succCache = new HashMap<>();
    private final BitSet guardsValid;
    private final SubsetCacheKey subsetCacheKey = new SubsetCacheKey();
    private final BuechiSubsetCache<AutomatonStateBuechiImpl, AutomatonSubsetLabelImpl> cache;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    public int getState(BitSet states) {
        return makeUnique(new AutomatonStateBuechiImpl(this, states)).getNumber();
    }

    @Override
    public int getInitState() {
        return initState.getNumber();
    }

    @Override
    public void queryState(Value[] modelState, int automatonState) {
        AutomatonStateBuechiImpl subsetState = (AutomatonStateBuechiImpl) numberToState(automatonState);
        buechi.query(modelState);
        lookupCache(subsetState);
        if (succState != null) {
            return;
        }

        BitSet succs = UtilBitSet.newBitSetUnbounded(automaton.getNumNodes());
        BitSet over = UtilBitSet.newBitSetUnbounded(numLabels);
        BitSet under = UtilBitSet.newBitSetUnbounded(numLabels);
        under.set(0, numLabels);
        EdgeProperty labels = automaton.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        for (int node = 0; node < automaton.getNumNodes(); node++) {
            if (subsetState.get(node)) {
                for (int succNr = 0; succNr < automaton.getNumSuccessors(node); succNr++) {
                    BuechiTransition trans = labels.getObject(node, succNr);
                    if (trans.guardFulfilled()) {
                        int succ = automaton.getSuccessorNode(node, succNr);
                        succs.set(succ);
                        over.or(trans.getLabeling());
                        under.and(trans.getLabeling());
                    }
                }
            }
        }
        if (succs.cardinality() == 0) {
            under = UtilBitSet.newBitSetUnbounded();
            over = UtilBitSet.newBitSetUnbounded();
        }
        AutomatonSubsetLabelImpl lab = new AutomatonSubsetLabelImpl(this, under, over);
        reduceSuccs(succs);
        succLabel = makeUnique(lab);
        succState = (AutomatonStateBuechiImpl) numberToState(getState(succs));
        insertCache();
    }

    private void lookupCache(AutomatonStateBuechiImpl rabinState) {
        BuechiSubsetCache<AutomatonStateBuechiImpl, AutomatonSubsetLabelImpl>.CacheValue entry = cache.lookup(rabinState);
        if (entry != null) {
            succState = entry.getState();
            succLabel = entry.getLabeling();
        } else {
            succState = null;
            succLabel = null;
        }
    }

    private void insertCache() {
        SubsetCacheValue value = new SubsetCacheValue(succState, succLabel);
        succCache.put(subsetCacheKey.clone(), value);
    }

    private void reduceSuccs(BitSet succs) {
        if (trueState != -1 && succs.get(trueState)) {
            succs.clear();
            succs.set(trueState);
        }
    }

    @Override
    public int getSuccessorState() {
        return succState.getNumber();
    }

    @Override
    public int getSuccessorLabel() {
        return succLabel.getNumber();
    }

    public int getNumLabels() {
        return numLabels;
    }

    GraphExplicit getGraph() {
        return automaton;
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
}
