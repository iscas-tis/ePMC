package epmc.graph;

import java.io.Closeable;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.dd.DD;
import epmc.error.EPMCException;
import epmc.graph.StateMap;
import epmc.graph.StateSet;
import epmc.graph.dd.StateSetDD;
import epmc.value.Operator;
import epmc.value.OperatorAnd;
import epmc.value.OperatorId;
import epmc.value.OperatorMax;
import epmc.value.OperatorMin;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeBoolean;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueBoolean;

public final class StateMapDD implements StateMap, Closeable, Cloneable {
    private final StateSetDD states;
    private final DD valuesDD;
    private final Type type;
    private int refs = 1;
    
    // note: consumes arguments states, valuesExplicit, and valuesDD
    public StateMapDD(StateSetDD states, DD valuesDD) {
        this.valuesDD = valuesDD;
        this.states = states;
        this.type = valuesDD.getType();
    }
        
    @Override
    public StateMapDD clone() {
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
        states.close();
        if (valuesDD != null) {
            valuesDD.dispose();
        }
    }
    
    public DD getValuesDD() {
        assert !closed();
        assert valuesDD != null;
        return valuesDD;
    }

    @Override
    public int size() {
        assert !closed();
        return states.size();
    }

    @Override
    public Type getType() {
        assert !closed();
        return type;
    }
    
    @Override
    public StateSet getStateSet() {
        return states;
    }

    @Override
    public StateMapDD restrict(StateSet to) throws EPMCException {
        assert !closed();
        assert to != null;
        assert to.getContextValue() == getContextValue();
        assert to.isSubsetOf(states);
        if (states.equals(to)) {
            return clone();
        }
        assert to instanceof StateSetDD;
        StateMapDD result = new StateMapDD((StateSetDD) to.clone(), valuesDD.clone());
        return result;
    }
    
    @Override
    public String toString() {
        if (closed()) {
            return "(closed StateMap)";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(states.getStatesDD());
        builder.append(valuesDD);
        builder.append("}");
        return builder.toString();
    }
    
    @Override
    public StateMap apply(Operator operator, StateMap operand)
            throws EPMCException {
        assert !closed();
        assert operator != null;
        assert operand != null;
        StateMap result = null;
        StateMapDD operandDD = (StateMapDD) operand;
        DD values = valuesDD.apply(operandDD.getValuesDD(), operator);
        result = new StateMapDD(states.clone(), values);
        return result;
    }

    private Value toArray() throws EPMCException {
        Set<Value> values = new LinkedHashSet<>();
        valuesDD.collectValues(values, states.getStatesDD());
        TypeArray typeArray = type.getTypeArray();
        ValueArray result = UtilValue.newArray(typeArray, values.size());
        int i = 0;
        for (Value v : values) {
            result.set(v, i);
            i++;
        }
        return result;
    }
    
    @Override
    public boolean isConstant() throws EPMCException {
        assert !closed();
        if (size() == 0) {
            return true;
        }
        return valuesDD.isLeaf();
    }
    
    private boolean closed() {
        return refs == 0;
    }

    @Override
    public Value applyOver(Operator operator, StateSet over) throws EPMCException {
        assert !closed();
        assert operator != null;
        assert over != null;
        assert over instanceof StateSetDD;
        StateSetDD overDD = (StateSetDD) over;
        return valuesDD.applyOverSat(operator, overDD.getStatesDD());
    }
    
    @Override
    public void getRange(Value range, StateSet of) throws EPMCException {
        Value min = applyOver(getContextValue().getOperator(OperatorMin.IDENTIFIER), of);
        Value max = applyOver(getContextValue().getOperator(OperatorMax.IDENTIFIER), of);
        range.set(TypeInterval.get(getContextValue()).newValue(min, max));
    }
    
    private boolean isAllTrue(StateSet of) throws EPMCException {
        Value result = applyOver(getContextValue().getOperator(OperatorAnd.IDENTIFIER), of);
        return ValueBoolean.asBoolean(result).getBoolean();
    }    
    
    @Override
    public void getSomeValue(Value to, StateSet of) throws EPMCException {
        Value result = applyOver(getContextValue().getOperator(OperatorId.IDENTIFIER), of);
        to.set(result);
    }

    @Override
    public Value subsumeResult(StateSet initialStates) throws EPMCException {
        boolean takeFirstInitState = initialStates.size() == 1;
        Value entry = getType().newValue();
        if (takeFirstInitState) {
            getSomeValue(entry, initialStates);
            return entry;
        } else {
            Type info = getType();
            if (TypeBoolean.isBoolean(info)) {
                boolean allTrue = true;
                allTrue = isAllTrue(initialStates);
                return allTrue
                        ? TypeBoolean.get(getContextValue()).getTrue()
                        : TypeBoolean.get(getContextValue()).getFalse();
            } else if (hasMinAndMaxElements(initialStates)) {
                Value range = TypeInterval.get(getContextValue()).newValue();
                getRange(range, initialStates);
                return range;
            } else {
                StateMapDD initValues = restrict(initialStates);
                return initValues.toArray();
            }
        }
    }
    
    private boolean hasMinAndMaxElements(StateSet of) throws EPMCException {
        if (TypeReal.isReal(getType())) {
            return true;
        }
        try {
            getRange(TypeInterval.get(getContextValue()).newValue(),
                    getStateSet());
        } catch (EPMCException e) {
            return false;
        }
        return true;
    }
}
