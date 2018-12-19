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

import epmc.automaton.Automaton;
import epmc.automaton.AutomatonLabelUtil;
import epmc.automaton.AutomatonMaps;
import epmc.automaton.AutomatonStateUtil;
import epmc.automaton.Buechi;
import epmc.automaton.BuechiSubsetCache;
import epmc.automaton.BuechiTransition;
import epmc.expression.Expression;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.Value;

public final class AutomatonBreakpoint implements Automaton {
    public final static class Builder implements Automaton.Builder {
        private Buechi buechi;
        private AutomatonSubsetState init;

        @Override
        public Builder setBuechi(Buechi buechi) {
            this.buechi = buechi;
            return this;
        }

        private Buechi getBuechi() {
            return buechi;
        }

        public Builder setInit(AutomatonSubsetState init) {
            this.init = init;
            return this;
        }

        private AutomatonSubsetState getInit() {
            return init;
        }

        @Override
        public AutomatonBreakpoint build() {
            return new AutomatonBreakpoint(this);
        }

    }

    private final AutomatonMaps<AutomatonBreakpointState,AutomatonBreakpointLabelEnum> automatonMaps = new AutomatonMaps<>();
    public final static String IDENTIFIER = "breakpoint";

    private enum AutomatonBreakpointLabelEnum implements AutomatonBreakpointLabel, AutomatonLabelUtil {
        ACCEPTING,
        REJECTING,
        NEUTRAL
        ;

        @Override
        public int getNumber() {
            return ordinal();
        }

        @Override
        public void setNumber(int number) {
        }

        @Override
        public boolean isNeutral() {
            return this == NEUTRAL;
        }

        @Override
        public boolean isAccepting() {
            return this == ACCEPTING;
        }

        @Override
        public boolean isRejecting() {
            return this == REJECTING;
        }
    }

    private AutomatonBreakpointState succState;
    private AutomatonBreakpointLabelEnum succLabel;
    private final int numLabels;
    private final GraphExplicit automaton;
    private final AutomatonBreakpointState initState;
    private final int trueState;
    private final Expression[] expressions;
    private final Buechi buechi;
    private final BuechiSubsetCache<AutomatonBreakpointState, AutomatonBreakpointLabelEnum> cache;

    private AutomatonBreakpoint(Builder builder) {
        this.buechi = builder.getBuechi();
        BitSet initStates = null;
        if (builder.getInit() == null) {
            initStates = buechi.getGraph().getInitialNodes();
        } else {
            initStates = builder.getInit().getStates();
        }
        this.numLabels = buechi.getNumLabels();
        this.automaton = buechi.getGraph();
        this.initState = makeUnique(new AutomatonBreakpointState(initStates));
        this.trueState = buechi.getTrueState();
        this.expressions = buechi.getExpressions();
        cache = new BuechiSubsetCache<>(buechi);
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public int getInitState() {
        return initState.getNumber();
    }

    @Override
    public void queryState(Value[] modelState, int automatonState) {
        AutomatonBreakpointState breakpointState = (AutomatonBreakpointState) numberToState(automatonState);
        buechi.query(modelState);
        lookupCache(breakpointState);
        if (succState != null) {
            return;
        }
        BitSet succs = UtilBitSet.newBitSetUnbounded(automaton.getNumNodes());
        BitSet succsAcc = UtilBitSet.newBitSetUnbounded(automaton.getNumNodes());
        BitSet succsChildren = UtilBitSet.newBitSetUnbounded(automaton.getNumNodes());
        int succAcceptance;
        EdgeProperty labels = automaton.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);

        for (int state = 0; state < automaton.getNumNodes(); state++) {
            if (breakpointState.getState(state)) {
                for (int succNr = 0; succNr < automaton.getNumSuccessors(state); succNr++) {
                    int succState = automaton.getSuccessorNode(state, succNr);
                    BuechiTransition trans = labels.getObject(state, succNr);
                    if (trans.guardFulfilled()) {
                        succs.set(succState);
                        if (trans.getLabeling().get(breakpointState.getAcceptance())) {
                            succsAcc.set(succState);
                        }
                        if (breakpointState.getChild(state)) {
                            succsChildren.set(succState);
                        }
                    }
                }
            }
        }
        succsAcc.or(succsChildren);
        if (succsAcc.equals(succs)) {
            succsChildren.clear();
            succAcceptance = (breakpointState.getAcceptance() + 1) % numLabels;
            succLabel = AutomatonBreakpointLabelEnum.ACCEPTING;
        } else {
            if (succsChildren.cardinality() == 0) {
                succLabel = AutomatonBreakpointLabelEnum.REJECTING;
            } else {
                succLabel = AutomatonBreakpointLabelEnum.NEUTRAL;
            }
            succsChildren = succsAcc;
            succAcceptance = breakpointState.getAcceptance();
        }
        /* note that we make transitions with emtpy R set rejecting rather
         * than removing them completely as in the paper, because complete
         * automata are more convenient to work with.
         * */
        if (succs.cardinality() == 0) {
            succLabel = AutomatonBreakpointLabelEnum.REJECTING;
        }
        reduceSuccs(succs, succsChildren);
        succState = new AutomatonBreakpointState(succs, succAcceptance, succsChildren);
        succState = makeUnique(succState);
        succLabel = makeUnique(succLabel);
        insertCache();
    }

    private void lookupCache(AutomatonBreakpointState breakpointState) {
        BuechiSubsetCache<AutomatonBreakpointState, AutomatonBreakpointLabelEnum>.CacheValue entry = cache.lookup(breakpointState);
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
     * Reduce successors of certain states.
     * E.g. the state corresponding to LTL formula "true" only needs to have
     * transitions to itself but not to other states. For many case studies this
     * reduction is necessary to obtain usable results.
     *
     * @param succs
     * @param succsChildren
     */
    private void reduceSuccs(BitSet succs, BitSet succsChildren) {
        if (trueState != -1 && succs.get(trueState)) {
            succs.clear();
            succs.set(trueState);
        }
        if (trueState != -1 && succsChildren.get(trueState)) {
            succsChildren.clear();
            succsChildren.set(trueState);
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

    int getNumLabels() {
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
    public int getNumStates() {
        return automatonMaps.getNumStates();
    }

    protected AutomatonBreakpointState makeUnique(AutomatonBreakpointState state) {
        return automatonMaps.makeUnique(state);
    }

    protected AutomatonBreakpointLabelEnum makeUnique(AutomatonBreakpointLabelEnum label) {
        return automatonMaps.makeUnique(label);
    }

    @Override
    public AutomatonStateUtil numberToState(int number) {
        return automatonMaps.numberToState(number);
    }

    @Override
    public AutomatonLabelUtil numberToLabel(int number) {
        return AutomatonBreakpointLabelEnum.values()[number];
    }

}
