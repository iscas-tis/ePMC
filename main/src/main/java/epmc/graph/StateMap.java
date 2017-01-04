package epmc.graph;

import java.io.Closeable;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.Value;

// TODO complete documentation

public interface StateMap extends Closeable, Cloneable {
    @Override
    void close();

    Type getType();

    int size();

    StateSet getStateSet();

    StateMap restrict(StateSet to) throws EPMCException;

    StateMap apply(Operator operator, StateMap other) throws EPMCException;

    StateMap clone();
    
    Value applyOver(Operator operator, StateSet over) throws EPMCException;
    
    boolean isConstant() throws EPMCException;

    void getRange(Value range, StateSet of) throws EPMCException;
    
    void getSomeValue(Value to, StateSet of) throws EPMCException;

    default void getSomeValue(Value to) throws EPMCException {
        assert to != null;
        getSomeValue(to, getStateSet());
    }
    
    default StateMap applyWith(Operator operator, StateMap operand)
            throws EPMCException {
        StateMap result = apply(operator, operand);
        close();
        operand.close();
        return result;
    }
    
    default Value getSomeValue() throws EPMCException {
        Value value = getType().newValue();
        getSomeValue(value);
        return value;
    }
    
    default ContextValue getContextValue() {
        return getType().getContext();
    }

    Value subsumeResult(StateSet initialStates) throws EPMCException;
}
