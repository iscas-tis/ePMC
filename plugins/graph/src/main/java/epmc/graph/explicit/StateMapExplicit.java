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

package epmc.graph.explicit;

import java.io.Closeable;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.graph.Scheduler;
import epmc.graph.StateMap;
import epmc.graph.StateSet;
import epmc.operator.Operator;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorId;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorMin;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeBoolean;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueBoolean;
import epmc.value.ValueInterval;

public final class StateMapExplicit implements StateMap, Closeable, Cloneable {
    private final StateSetExplicit states;
    private final ValueArray valuesExplicit;
    private final Type type;
    private final TypeArray typeArray;
    private final Value helper;
    private final Value helper2;
    private final Scheduler scheduler;
    private int refs = 1;
    private final OperatorEvaluator eq;
    private final ValueBoolean cmp;

    public StateMapExplicit(StateSetExplicit states,
            ValueArray valuesExplicit) {
        this(states, valuesExplicit, null);
    }

    // note: consumes arguments states, valuesExplicit, and valuesDD
    public StateMapExplicit(StateSetExplicit states,
            ValueArray valuesExplicit, Scheduler scheduler) {
        this.valuesExplicit = valuesExplicit;
        this.states = states;
        this.type = valuesExplicit.getType().getEntryType();
        this.typeArray = type.getTypeArray();
        this.helper = type.newValue();
        this.helper2 = type.newValue();
        this.scheduler = scheduler;
        this.eq = ContextValue.get().getEvaluatorOrNull(OperatorEq.EQ, type, type);
        cmp = TypeBoolean.get().newValue();
    }

    @Override
    public StateMapExplicit clone() {
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
    }

    public Value getValuesExplicit() {
        assert !closed();
        assert valuesExplicit != null;
        return valuesExplicit;
    }

    public void getExplicitIthValue(Value value, int i) {
        assert !closed();
        assert value != null;
        assert i >= 0;
        assert i < valuesExplicit.size();
        valuesExplicit.get(value, i);
    }

    public int getExplicitIthState(int i) {
        assert !closed();
        assert i >= 0;
        assert i < valuesExplicit.size();
        return states.getExplicitIthState(i);
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
    public StateMapExplicit restrict(StateSet to) {
        assert !closed();
        assert to != null;
        assert to instanceof StateSetExplicit;
        assert ((StateSetExplicit) to).isSubsetOf(states);
        if (states.equals(to)) {
            return clone();
        }
        StateSetExplicit toExplicit = (StateSetExplicit) to;
        int oldStateNr = 0;
        Value helper = type.newValue();
        ValueArray resultValues = UtilValue.newArray(typeArray, to.size());
        for (int newStateNr = 0; newStateNr < to.size(); newStateNr++) {
            int newState = toExplicit.getExplicitIthState(newStateNr);
            int oldState = getExplicitIthState(oldStateNr);
            while (oldState != newState) {
                oldStateNr++;
                oldState = getExplicitIthState(oldStateNr);                
            }
            getExplicitIthValue(helper, oldStateNr);
            resultValues.set(helper, newStateNr);
        }
        return new StateMapExplicit((StateSetExplicit) to.clone(), resultValues);
    }

    @Override
    public String toString() {
        if (closed()) {
            return "(closed StateMap)";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (int i = 0; i < size(); i++) {
            builder.append(getExplicitIthState(i));
            builder.append("=");
            getExplicitIthValue(helper, i);
            builder.append(helper);
            if (i < size() - 1) {
                builder.append(", ");
            }
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    public StateMap apply(Operator identifier, StateMap operand) {
        assert !closed();
        assert identifier != null;
        assert operand != null;
        OperatorEvaluator evaluator = ContextValue.get().getEvaluator(identifier, getType(), operand.getType());
        assert evaluator != null;
        StateMapExplicit operandExplicit = (StateMapExplicit) operand;
        StateMap result = null;
        Type resultType = evaluator.resultType();
        TypeArray resultTypeArray = resultType.getTypeArray();
        assert states.equals(operand.getStateSet());
        ValueArray resultValues = UtilValue.newArray(resultTypeArray, states.size());
        Value res = resultType.newValue();
        Value op1 = type.newValue();
        Value op2 = operand.getType().newValue();
        for (int i = 0; i < size(); i++) {
            getExplicitIthValue(op1, i);
            operandExplicit.getExplicitIthValue(op2, i);
            evaluator.apply(res, op1, op2);
            resultValues.set(res, i);
        }
        result = new StateMapExplicit(states.clone(), resultValues);
        return result;
    }

    private Value toArray() {
        Set<Value> values = new LinkedHashSet<>();
        Value value = type.newValue();
        for (int i = 0; i < size(); i++) {
            getExplicitIthValue(value, i);
            if (!values.contains(value)) {
                values.add(UtilValue.clone(value));
            }
        }
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
    public boolean isConstant() {
        assert !closed();
        if (size() == 0) {
            return true;
        }
        getExplicitIthValue(helper2, 0);            
        for (int i = 0; i < size(); i++) {
            getExplicitIthValue(helper, i);
            eq.apply(cmp, helper, helper2);
            if (!cmp.getBoolean()) {
                return false;
            }
        }
        return true;
    }

    public void setExplicitIthValue(Value value, int i) {
        assert !closed();
        assert value != null;
        assert i >= 0;
        assert i < valuesExplicit.size();
        valuesExplicit.set(value, i);
    }

    private boolean closed() {
        return refs == 0;
    }

    @Override
    public Value applyOver(Operator identifier, StateSet over)
    {
        assert identifier != null;
        assert over != null;
        assert over instanceof StateSetExplicit;
        StateSetExplicit overExplicit = (StateSetExplicit) over;
        Value result = null;

        Value[] values = new Value[2];
        for (int stateNr = 0; stateNr < states.size(); stateNr++) {
            int state = states.getExplicitIthState(stateNr);
            if (overExplicit.isExplicitContains(state)) {
                if (result == null) {
                    result = valuesExplicit.getType().getEntryType().newValue();
                    valuesExplicit.get(result, stateNr);
                    values[0] = result;
                    values[1] = valuesExplicit.getType().getEntryType().newValue();
                } else {
                    valuesExplicit.get(values[1], stateNr);
                    Type[] types = new Type[values.length];
                    for (int i = 0; i < values.length; i++) {
                        types[i] = values[i].getType();
                    }
                    OperatorEvaluator evaluator = ContextValue.get().getEvaluator(identifier, types);
                    evaluator.apply(result, values);
                }
            }
        }
        return result;
    }

    @Override
    public void getRange(Value range, StateSet of) {
        Value min = applyOver(OperatorMin.MIN, of);
        Value max = applyOver(OperatorMax.MAX, of);
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
        set.apply(ValueInterval.as(range).getIntervalLower(), min);
        set.apply(ValueInterval.as(range).getIntervalUpper(), max);
    }

    private boolean isAllTrue(StateSet of) {
        Value result = applyOver(OperatorAnd.AND, of);
        return ValueBoolean.as(result).getBoolean();
    }    

    @Override
    public void getSomeValue(Value to, StateSet of) {
        Value result = applyOver(OperatorId.ID, of);
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, result.getType(), to.getType());
        set.apply(to, result);
    }

    @Override
    public Value subsumeResult(StateSet initialStates) {
        boolean takeFirstInitState = initialStates.size() == 1;
        Value entry = getType().newValue();
        if (takeFirstInitState) {
            getSomeValue(entry, initialStates);
            return entry;
        } else {
            Type type = getType();
            if (TypeBoolean.is(type)) {
                boolean allTrue = true;
                allTrue = isAllTrue(initialStates);
                return UtilValue.newValue(TypeBoolean.get(), allTrue);
            } else if (hasMinAndMaxElements(initialStates)) {
                Value range = TypeInterval.get().newValue();
                getRange(range, initialStates);
                return range;
            } else {
                StateMapExplicit initValues = restrict(initialStates);
                return initValues.toArray();
            }
        }
    }

    private boolean hasMinAndMaxElements(StateSet of) {
        if (TypeReal.is(getType())) {
            return true;
        }
        return false;
  //      getRange(TypeInterval.get().newValue(),
    //            getStateSet());
//        return true;
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }
}
