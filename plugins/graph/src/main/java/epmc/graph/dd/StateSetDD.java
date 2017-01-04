package epmc.graph.dd;

import java.io.Closeable;

import epmc.dd.DD;
import epmc.error.EPMCException;
import epmc.graph.LowLevel;
import epmc.graph.StateSet;
import epmc.value.ContextValue;

public final class StateSetDD implements Closeable, Cloneable, StateSet {
    private final DD statesDD;
    private int refs = 1;
    private final ContextValue contextValue;
    private LowLevel lowLevel;

    // note: consumes arguments statesExplicit and statesDD
    public StateSetDD(LowLevel lowLevel, DD statesDD) {
        this.contextValue = lowLevel.getContextValue();
        this.lowLevel = lowLevel;
        this.statesDD = statesDD;
    }

    @Override
    public StateSetDD clone() {
        refs++;
        return this;
    }
    
    @Override
    public void close() {
        if (closed()) {
            return;
        }
        refs--;
        if (refs > 0) {
            return;
        }
        if (statesDD != null) {
            statesDD.dispose();
        }
    }
    
    @Override
    public int size() {
        assert !closed();
        GraphDD graphDD = (GraphDD) lowLevel;
        return statesDD.countSat(graphDD.getPresCube()).intValue();
    }
    
    @Override
    public boolean equals(Object obj) {
        assert !closed();
        assert obj != null;
        if (!(obj instanceof StateSetDD)) {
            return false;
        }
        StateSetDD other = (StateSetDD) obj;
        if (contextValue != other.getContextValue()) {
            return false;
        }
        if ((this.statesDD == null) != (other.statesDD == null)) {
            return false;
        }
        if (statesDD != null && !this.statesDD.equals(other.statesDD)) {
            return false;
        }        
        return true;
    }
    
    @Override
    public boolean isSubsetOf(StateSet states) throws EPMCException {
        assert !closed();
        assert states != null;
        assert contextValue == states.getContextValue();
        assert states instanceof StateSetDD;
        StateSetDD other = (StateSetDD) states;
        return statesDD.andNot(other.statesDD).isFalseWith();
    }
    
    @Override
    public ContextValue getContextValue() {
        assert !closed();
        return contextValue;
    }

    public DD getStatesDD() {
        return statesDD;
    }
    
    private boolean closed() {
        return refs == 0;
    }

}
