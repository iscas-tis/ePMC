package epmc.automata.determinisation;

import epmc.automaton.AutomatonRabin;
import epmc.automaton.AutomatonSafra;
import epmc.automaton.Buechi;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.util.BitSet;
import epmc.value.ContextValue;
import epmc.value.Value;

public final class AutomatonScheweRabin implements AutomatonRabin, AutomatonSafra {
	public final static class Builder implements AutomatonSafra.Builder, AutomatonRabin.Builder {
		private Buechi buechi;
		private BitSet init;

		@Override
		public Builder setBuechi(Buechi buechi) {
			this.buechi = buechi;
			return this;
		}
		
		private Buechi getBuechi() {
			return buechi;
		}

		@Override
		public Builder setInit(BitSet initialStates) {
			this.init = initialStates;
			return this;
		}
		
		private BitSet getInit() {
			return init;
		}
		
		@Override
		public AutomatonScheweRabin build() throws EPMCException {
			return new AutomatonScheweRabin(this);
		}
	}
	
    public final static String IDENTIFIER = "schewe-rabin";

    private final AutomatonSchewe inner;
    
    private AutomatonScheweRabin(Builder builder) throws EPMCException {
    	AutomatonSchewe.Builder scheweBuilder = new AutomatonSchewe.Builder();
    	scheweBuilder.setBuechi(builder.getBuechi());
    	scheweBuilder.setInit(builder.getInit());
    	scheweBuilder.setParity(false);
    	this.inner = scheweBuilder.build();
	}

	@Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void close() {
        inner.close();
    }

    @Override
    public int getInitState() {
        return inner.getInitState();
    }

    @Override
    public void queryState(Value[] modelState, int automatonState)
            throws EPMCException {
        inner.queryState(modelState, automatonState);
    }

    @Override
    public int getNumStates() {
        return inner.getNumStates();
    }

    @Override
    public Object numberToState(int number) {
        return inner.numberToState(number);
    }

    @Override
    public Object numberToLabel(int number) {
        return inner.numberToLabel(number);
    }

    @Override
    public Expression[] getExpressions() {
        return inner.getExpressions();
    }

    @Override
    public int getSuccessorState() {
        return inner.getSuccessorState();
    }

    @Override
    public int getSuccessorLabel() {
        return inner.getSuccessorLabel();
    }

    @Override
    public int getNumPairs() {
        return inner.getNumPairs();
    }
    
    @Override
    public String toString() {
        return inner.toString();
    }

    @Override
    public ContextValue getContextValue() {
    	return inner.getContextValue();
    }
    
    @Override
    public Buechi getBuechi() {
        return inner.getBuechi();
    }
}
