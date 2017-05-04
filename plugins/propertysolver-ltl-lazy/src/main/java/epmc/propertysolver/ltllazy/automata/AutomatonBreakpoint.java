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
import java.util.Map;

import epmc.automaton.Automaton;
import epmc.automaton.AutomatonLabelUtil;
import epmc.automaton.AutomatonMaps;
import epmc.automaton.AutomatonStateUtil;
import epmc.automaton.Buechi;
import epmc.automaton.BuechiTransition;
import epmc.error.EPMCException;
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
		public AutomatonBreakpoint build() throws EPMCException {
			return new AutomatonBreakpoint(this);
		}
		
	}
	
    private final AutomatonMaps automatonMaps = new AutomatonMaps();
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
    
    private final class BreakpointCacheKey implements Cloneable {
        private AutomatonBreakpointState state;
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
            assert obj != null;
            if (!(obj instanceof BreakpointCacheKey)) {
                return false;
            }
            BreakpointCacheKey other = (BreakpointCacheKey) obj;
            return state.equals(other.state) && guards.equals(other.guards);
        }
        
        @Override
        protected BreakpointCacheKey clone() {
            BreakpointCacheKey result = new BreakpointCacheKey();
            result.state = state;
            result.guards = guards.clone();
            return result;
        }
    }
    
    private final class BreakpointCacheValue {
        private final AutomatonBreakpointState state;
        private final AutomatonBreakpointLabelEnum labeling;
        
        BreakpointCacheValue(AutomatonBreakpointState state,
                AutomatonBreakpointLabelEnum labeling) {
            assert state != null;
            assert labeling != null;
            this.state = state;
            this.labeling = labeling;
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
    private final Map<BreakpointCacheKey,BreakpointCacheValue> succCache = new HashMap<>();
    private final BitSet guardsValid;
    private final BreakpointCacheKey breakpointCacheKey = new BreakpointCacheKey();

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
        this.initState = makeUnique(new AutomatonBreakpointState(this, initStates));
        this.trueState = buechi.getTrueState();
        this.expressions = buechi.getExpressions();
    	guardsValid = UtilBitSet.newBitSetUnbounded();
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
    public void queryState(Value[] modelState, int automatonState)
            throws EPMCException {
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
        succState = new AutomatonBreakpointState(this, succs, succAcceptance, succsChildren);
        succState = makeUnique(succState);
        succLabel = makeUnique(succLabel);
        insertCache();
    }
    
    private void lookupCache(AutomatonBreakpointState breakpointState)
            throws EPMCException {
        int entryNr = 0;
        EdgeProperty labels = automaton.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        for (int state = 0; state < automaton.getNumNodes(); state++) {
            boolean stateSet  = breakpointState.getStates().get(state);
            for (int succNr = 0; succNr < automaton.getNumSuccessors(state); succNr++) {
                BuechiTransition trans = labels.getObject(state, succNr);
                guardsValid.set(entryNr, stateSet && trans.guardFulfilled());
                entryNr++;
            }
        }
        breakpointCacheKey.state = breakpointState;
        breakpointCacheKey.guards = guardsValid;
        BreakpointCacheValue entry = succCache.get(breakpointCacheKey);
        if (entry != null) {
            succState = entry.state;
            succLabel = entry.labeling;
        } else {
            succState = null;
            succLabel = null;
        }
    }

    private void insertCache() {
        BreakpointCacheValue value = new BreakpointCacheValue(succState, succLabel);
        succCache.put(breakpointCacheKey.clone(), value);
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

    protected <T extends AutomatonStateUtil> T makeUnique(T state) {
        return automatonMaps.makeUnique(state);
    }

    protected <T extends AutomatonLabelUtil> T makeUnique(T label) {
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
