package epmc.propertysolverltlfg.automaton;


import java.util.HashMap;
import java.util.List;
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
import epmc.graph.explicit.NodeProperty;
import epmc.util.BitSet;
import epmc.value.Value;

/**
 * The abstract automaton class for Finite Deterministic Automaton 
 * use edges or states as accepting conditions 
 * @author Yong Li
 * */
public abstract class AutomatonDFA implements Automaton {

	public final static String IDENTIFIER = "dfa";
	protected final Map<Integer, AutomatonStateUtil> numberToStates = new HashMap<>();
	protected final AutomatonMaps numberToLabels = new AutomatonMaps();

	protected AutomatonDFAStateImpl initState;
	protected AutomatonDFAStateImpl succState;
	protected AutomatonDFALabelImpl succLabel;
	private int numLabels;
	protected GraphExplicit automaton;
	private Expression[] expressions;
	protected AutomatonRabin buechi;
	
	@Override
	public int getNumStates() {
		return automaton.getNumNodes();
	}
	
    protected AutomatonDFALabelImpl makeUnique(AutomatonDFALabelImpl label) {
    	return numberToLabels.makeUnique(label); 
    }
    
    abstract void putStateLabels(AutomatonDFAStateImpl state)
    throws EPMCException;
    abstract void putEdgeLabels(BitSet labeling);
    
    @Override
    public void queryState(Value[] modelState, int automatonState)
            throws EPMCException {
    	AutomatonDFAStateImpl state = (AutomatonDFAStateImpl)numberToState(automatonState);
        buechi.query(modelState);
        
        int succ = -1;
		automaton.queryNode(state.getNumber());
		putStateLabels(state);  // put state labels
        EdgeProperty labels = automaton.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);	       
		for (int succNr = 0; succNr < automaton.getNumSuccessors(); succNr++) {
			BuechiTransition trans = labels.getObject(succNr);
			if (trans.guardFulfilled()) {
				succ = automaton.getSuccessorNode(succNr); 
				BitSet labeling = trans.getLabeling();
				putEdgeLabels(labeling); // transition-based
				break;
			}
		}
        succState = (AutomatonDFAStateImpl) numberToState(succ);
    }

    protected final class AutomatonDFAStateImpl implements AutomatonStateUtil {
		private final AutomatonDFA automaton;
		private final int automatonState;

		AutomatonDFAStateImpl(AutomatonDFA automaton, int states) {
			this.automaton = automaton;
			this.automatonState = states;
		}

		AutomatonDFAStateImpl(AutomatonDFAStateImpl other) {
			this(other.getAutomaton(), other.automatonState);
		}

		@Override
		protected AutomatonDFAStateImpl clone() {
			return new AutomatonDFAStateImpl(this);
		}

		@Override
		public String toString() {
			return automatonState + "";
		}

		@Override
		public boolean equals(Object obj) {
			assert obj != null;
			if (!(obj instanceof AutomatonDFAStateImpl)) {
				return false;
			}
			AutomatonDFAStateImpl other = (AutomatonDFAStateImpl) obj;
			return this.automatonState == other.automatonState;
		}

		@Override
		public int hashCode() {
			return this.automatonState;
		}

		@Override
		public AutomatonDFA getAutomaton() {
			return automaton;
		}

		@Override
		public void setNumber(int number) {
		}

		@Override
		public int getNumber() {
			return this.automatonState;
		}

	}

	protected final class AutomatonDFALabelImpl implements AutomatonLabelUtil
	, RabinTransitionUtil{
		private final BitSet labels; 
		private int number ;
		
		AutomatonDFALabelImpl(BitSet labels) {
			this.labels = labels;
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
		public int hashCode() {
			return labels.hashCode();
		}

		@Override
		public String toString() {
			return labels.toString();
		}

		@Override
		public Expression getExpression() {
			return null;
		}

		@Override
		public BitSet getLabeling() {
			return labels;
		}

		@Override
		public boolean guardFulfilled() throws EPMCException {
			return true;
		}
		
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof AutomatonDFALabelImpl)) {
                return false;
            }
            AutomatonDFALabelImpl other = (AutomatonDFALabelImpl) obj;
            return this.labels.equals(other.labels);
        }

	}


	public void setBuechi(Buechi buechi) {
		assert buechi != null;
		assert buechi.getNumLabels() > 0;
		this.numLabels = buechi.getNumLabels();
		this.automaton = buechi.getGraph();
		initState = new AutomatonDFAStateImpl(this, 0);
		numberToStates.put(0, initState);
		this.expressions = buechi.getExpressions();
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	public AutomatonDFA(AutomatonRabin buechi) throws EPMCException {
		setBuechi(buechi);
		this.buechi = buechi;
	}

	@Override
	public int getInitState() {
		return initState.getNumber();
	}

	@Override
	public int getSuccessorState() {
		assert succState != null;
		return succState.getNumber();
	}

	@Override
	public int getSuccessorLabel() {
		assert succLabel != null;
		return succLabel.getNumber();
	}

	public int getNumLabels() {
		return numLabels;
	}

	GraphExplicit getGraph() {
		return automaton;
	}

	@Override
	public Expression[] getExpressions() {
		return expressions;
	}

	@Override
	public AutomatonLabelUtil numberToLabel(int number) {
		return numberToLabels.numberToLabel(number);
	}

	public int getStateLabel(int state) throws EPMCException {
		return getStateLabels(state).nextSetBit(0);
	}
	
	public BitSet getStateLabels(int state) throws EPMCException {
		assert state >= 0 && state < automaton.getNumNodes();
		NodeProperty nodeLabel = automaton
				.getNodeProperty(CommonProperties.AUTOMATON_LABEL);
		automaton.queryNode(state);
		RabinStateUtil stateUtil = nodeLabel.getObject();
		return stateUtil.getLabeling();
	}

    public List<AcceptanceCondition> getAccConditions() {
    	return buechi.getAcceptances();
    }
    
    public List<Expression> getAccExpressions() {
    	return buechi.getAccExpressions();
    }
	
	
	@Override
	public String toString() {
		return automaton.toString();
	}

	@Override
	public AutomatonStateUtil numberToState(int number) {
		AutomatonStateUtil state = numberToStates.get(number);
		if(state == null) {
			state = new AutomatonDFAStateImpl(this, number);
			numberToStates.put(number, state);
		}
		return state;
	}
	
	@Override
	public void close() {
	}

}
