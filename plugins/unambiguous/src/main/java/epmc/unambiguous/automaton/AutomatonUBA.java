package epmc.unambiguous.automaton;

import epmc.automaton.Automaton;
import epmc.automaton.AutomatonLabelUtil;
import epmc.automaton.AutomatonMaps;
import epmc.automaton.AutomatonStateUtil;
import epmc.automaton.Buechi;
import epmc.error.EPMCException;
import epmc.expression.ContextExpression;
import epmc.expression.Expression;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;

import java.util.HashMap;
import java.util.Map;

import epmc.util.BitSet;
import epmc.value.Value;

/** generic buechi automaton */
public class AutomatonUBA implements Automaton {

	public final static String IDENTIFIER = "uba-explicit";
	private Buechi buechi = null;
	private Expression[] expressions;
	private GraphExplicit automaton;
	private int numLabels;
	private int trueState;
	private final Expression expression;

	private AutomatonUBAStateImpl initState;
	private BitSet succStates;
	private Map<Integer, AutomatonUBALabelImpl> succLabels;

	private final AutomatonMaps automatonMaps = new AutomatonMaps();
	
    public boolean isDeterministic() {
        return false;
    }
    
    public int getNumLabels() {
    	return numLabels;
    }

    public int getNumberSuccessors() {
        return succStates.cardinality();
    }
    
    public Buechi getBuechi() {
    	assert buechi != null;
    	return buechi;
    }

    public int getSuccessorState(int successorNumber) {
    	assert successorNumber >= 0;
    	assert successorNumber < getNumberSuccessors();
        int node = succStates.nextSetBit(0); /* first one */
        for(int i = 0 ; i < successorNumber ; i ++) {
        	node = succStates.nextSetBit(node + 1);
        }
        return node;
    }

    public int getSuccessorLabel(int successorNumber) {
        AutomatonUBALabelImpl label = succLabels.get(getSuccessorState(successorNumber));
        return label.getNumber();
    }
    
	public AutomatonUBA(Expression expression) throws EPMCException {

		assert expression != null;
		this.expression = expression;
		buechi = new UBAImpl(expression, expressions);
		automaton = buechi.getGraph();
		this.numLabels = buechi.getNumLabels();
		this.expressions = buechi.getExpressions();
		trueState = buechi.getTrueState();
		initState = (AutomatonUBAStateImpl) numberToState(getState(0));
	}

	private int getState(int automatonState) {
		return makeUnique(new AutomatonUBAStateImpl(this, automatonState))
				.getNumber();
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
		return automatonMaps.numberToLabel(number);
	}

	@Override
	public int getInitState() {
		return initState.getNumber();
	}

	@Override
	public Expression[] getExpressions() {
		return expressions;
	}

	@Override
	public void queryState(Value[] modelState, int automatonState)
			throws EPMCException {
		AutomatonUBAStateImpl ubaState = (AutomatonUBAStateImpl) numberToState(automatonState);
		buechi.query(modelState);
		lookupCache(ubaState, modelState);
		
		if (succStates != null) { /* found in cache */
			return;
		}
		
		BitSet succs = getContextExpression().getContextValue()
				.newBitSetBounded(automaton.getNumNodes());
		EdgeProperty labels = automaton
				.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);

		HashMap<Integer, AutomatonUBALabelImpl> labeling = new HashMap<>();

		automaton.queryNode(ubaState.getAutomatonState());
		for (int succNr = 0; succNr < automaton.getNumSuccessors(); succNr++) {
			UBATransition trans = labels.getObject(succNr);
			if (trans.guardFulfilled()) {
				int succ = automaton.getSuccessorNode(succNr);
				AutomatonUBAStateImpl succState = (AutomatonUBAStateImpl) numberToState(getState(succ));
				succs.set(succState.getNumber()); /* add successors */
				BitSet label = getContextExpression().getContextValue()
						.newBitSetBounded(numLabels);
				label.or(trans.getLabeling());
				AutomatonUBALabelImpl newlabs = new AutomatonUBALabelImpl(label);
				labeling.put(succState.getNumber(), makeUnique(newlabs));
			}
		}

		succStates = succs;
		succLabels = labeling;
		
		insertCache();
	}

	private void lookupCache(AutomatonUBAStateImpl ubaState, Value[] models) {
		BitSet guards = getContextValue().newBitSetBounded(models.length);
		for(int modelNr = 0; modelNr < models.length ; modelNr ++) {
			assert models[modelNr].isBoolean();
			if(models[modelNr].getBoolean()) guards.set(modelNr);
		}
		
        ubaCacheKey.state =  ubaState;
        ubaCacheKey.guards = guards;
        UBACacheValue entry = succCache.get(ubaCacheKey);
        if (entry != null) {
            succStates = entry.states;
            succLabels = entry.labeling;
        } else {
            succStates = null;
            succLabels = null;
        }
	}
	
    private void insertCache() {
        UBACacheValue value = new UBACacheValue(succStates, succLabels);
        succCache.put(ubaCacheKey.clone(), value);
    }
    

	@Override
	public ContextExpression getContextExpression() {
		return expression.getContext();
	}

	/**
	 * Below implements two helper classes needed in Automaton
	 * */
	private class AutomatonUBAStateImpl implements AutomatonUBAState,
			AutomatonStateUtil {

		private AutomatonUBA automaton;
		private int state;
		private int number;

		public AutomatonUBAStateImpl(AutomatonUBA automatonUBA,
				int automatonState) {
			this.automaton = automatonUBA;
			this.state = automatonState;
		}

		@Override
		public int getAutomatonState() {
			return state;
		}

		@Override
		public boolean isAccepting() {
			return false;
		}

		@Override
		public AutomatonUBA getAutomaton() {
			return automaton;
		}

		@Override
		public void setNumber(int number) {
			this.number = number;
		}

		@Override
		public int getNumber() {
			return number;
		}

		public boolean equals(Object obj) {
			assert obj != null;
			if (!(obj instanceof AutomatonUBAStateImpl))
				return false;
			AutomatonUBAStateImpl other = (AutomatonUBAStateImpl) obj;
			return this.state == other.state;
		}

		@Override
		public int hashCode() {
			return state;
		}

		@Override
		public String toString() {
			return "" + state;
		}

	}

	private class AutomatonUBALabelImpl implements AutomatonUBALabel,
			AutomatonLabelUtil {

		private final BitSet labels;
		private int number;

		AutomatonUBALabelImpl(BitSet labels) {
			this.labels = labels;
		}

		@Override
		public BitSet getLabel() {
			return labels;
		}

		@Override
		public int getPredecessor() {
			return 0;
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
			assert obj != null;
			if (!(obj instanceof AutomatonUBALabelImpl)) {
				return false;
			}
			AutomatonUBALabelImpl l = (AutomatonUBALabelImpl) obj;
			return labels.equals(l.labels);
		}

		@Override
		public int hashCode() {
			return labels.hashCode();
		}

		@Override
		public String toString() {
			return labels.toString();
		}

	}
	// -------- cache key, value 
	private HashMap<UBACacheKey,UBACacheValue> succCache = new HashMap<>();        /* cache map for successors */
    private UBACacheKey ubaCacheKey = new UBACacheKey();                           /* cache key for successos */
    private final class UBACacheKey implements Cloneable {
        private AutomatonUBAStateImpl state;            /* buechi state */
        private BitSet guards;        /* Fulfilled guards */
        
        @Override
        public int hashCode() {
            int hash = 0;
            hash = state.hashCode() + (hash << 6) + (hash << 16) - hash;
            hash = guards.hashCode() + (hash << 6) + (hash << 16) - hash;
            return hash;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof UBACacheKey)) {
                return false;
            }
            UBACacheKey other = (UBACacheKey) obj;
            return state.equals(other.state) && guards.equals(other.guards);
        }
        
        @Override
        protected UBACacheKey clone() {
        	UBACacheKey result = new UBACacheKey();
            result.state = state;
            result.guards = (BitSet) guards.clone();
            return result;
        }
    }
    
    private final class UBACacheValue {
        private final BitSet states;          /* state */
        private final Map<Integer, AutomatonUBALabelImpl> labeling;       /* transition label */
        
        public UBACacheValue(BitSet states, Map<Integer, AutomatonUBALabelImpl> labeling) {
            this.states = states;
            this.labeling = labeling;
        }
    }

}
