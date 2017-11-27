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

package epmc.value.operatorevaluator;

import epmc.operator.Operator;
import epmc.operator.OperatorIsZero;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeInterval;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.ValueInterval;

public final class OperatorEvaluatorIsZeroInterval implements OperatorEvaluator {
    public final static class Builder implements OperatorEvaluatorSimpleBuilder {
        private boolean built;
        private Operator operator;
        private Type[] types;

        @Override
        public void setOperator(Operator operator) {
            assert !built;
            this.operator = operator;
        }

        @Override
        public void setTypes(Type[] types) {
            assert !built;
            this.types = types;
        }

        @Override
        public OperatorEvaluator build() {
            assert !built;
            assert operator != null;
            assert types != null;
            for (Type type : types) {
                assert type != null;
            }
            built = true;
            if (operator != OperatorIsZero.IS_ZERO) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            for (Type type : types) {
                if (!TypeInterval.is(type)) {
                    return null;
                }
            }
            return new OperatorEvaluatorIsZeroInterval(this);
        }
    }

    private final OperatorEvaluator isZero;
    private final ValueBoolean cmp;
    
    private OperatorEvaluatorIsZeroInterval(Builder builder) {
        isZero = ContextValue.get().getEvaluator(OperatorIsZero.IS_ZERO,
                TypeInterval.as(builder.types[0]).getEntryType());
        cmp = TypeBoolean.get().newValue();
    }

    @Override
    public Type resultType() {
        return TypeBoolean.get();
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        for (Value operand : operands) {
            assert operand != null;
        }
        ValueInterval operand = ValueInterval.as(operands[0]);
        isZero.apply(cmp, operand.getIntervalLower());
        if (!cmp.getBoolean()) {
            ValueBoolean.as(result).set(false);
            return;
        }
        isZero.apply(cmp, operand.getIntervalUpper());
        if (!cmp.getBoolean()) {
            ValueBoolean.as(result).set(false);
            return;
        }
        ValueBoolean.as(result).set(true);
    }
}
