package epmc.graph.explicit;

import java.io.Closeable;

import epmc.error.EPMCException;
import epmc.graph.LowLevel;
import epmc.graph.StateSet;
import epmc.util.BitSet;
import epmc.value.ContextValue;

public final class StateSetExplicit implements Closeable, Cloneable, StateSet {
    private final BitSet statesExplicit;
    private final int[] numberToState;
    private final ContextValue contextValue;
    private int size;

    // note: consumes arguments statesExplicit and statesDD
    public StateSetExplicit(LowLevel lowLevel, BitSet statesExplicit) {
        assert lowLevel != null;
        if (statesExplicit == null) {
            this.statesExplicit = null;
            this.numberToState = null;
            this.contextValue = lowLevel.getContextValue();
        } else {
            this.statesExplicit = statesExplicit;
            this.numberToState = new int[statesExplicit.cardinality()];
            this.contextValue = lowLevel.getContextValue();
            int index = 0;
            for (int state = statesExplicit.nextSetBit(0); state >= 0; state = statesExplicit.nextSetBit(state + 1)) {
                numberToState[index] = state;
                index++;
            }
            this.size = statesExplicit.cardinality();
        }
    }

    @Override
    public StateSetExplicit clone() {
        return this;
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof StateSetExplicit)) {
            return false;
        }
        StateSetExplicit other = (StateSetExplicit) obj;
        if (contextValue != other.getContextValue()) {
            return false;
        }
        if ((this.statesExplicit == null) != (other.statesExplicit == null)) {
            return false;
        }
        if (statesExplicit != null && !this.statesExplicit.equals(other.statesExplicit)) {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean isSubsetOf(StateSet states) throws EPMCException {
        assert states != null;
        assert contextValue == states.getContextValue();
        assert states instanceof StateSetExplicit;
        StateSetExplicit other = (StateSetExplicit) states;
        for (int state = statesExplicit.nextSetBit(0); state >= 0; state = statesExplicit.nextSetBit(state + 1)) {
            if (!(other.statesExplicit.get(state))) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public ContextValue getContextValue() {
        return contextValue;
    }

    public int getExplicitIthState(int i) {
        assert i >= 0;
        assert i < numberToState.length;
        return numberToState[i];
    }

    public boolean isExplicitContains(int state) {
        return statesExplicit.get(state);
    }

    public int getExplicitStateNr(int explicitIthState) {
        int number = 0;
        for (int state = statesExplicit.nextSetBit(0); state >= 0; state = statesExplicit.nextSetBit(state + 1)) {
            if (number == explicitIthState) {
                return state;
            }
            number++;
        }
        assert false;
        return -1;
    }

    public BitSet getStatesExplicit() {
        return statesExplicit;
    }
}
