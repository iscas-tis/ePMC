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
import epmc.operator.OperatorDivide;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueInterval;

public final class OperatorEvaluatorDivideInterval implements OperatorEvaluator {
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
            if (operator != OperatorDivide.DIVIDE) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            for (Type type : types) {
                if (!TypeInterval.is(type) && !TypeReal.is(type) && !TypeInteger.is(type)) {
                    return null;
                }
            }
            if (!TypeInterval.is(types[0]) && !TypeInterval.is(types[0])) {
                return null;
            }
            return new OperatorEvaluatorDivideInterval(this);
        }
    }
    
    private final OperatorEvaluator divide;
    
    private OperatorEvaluatorDivideInterval(Builder builder) {
        divide = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE,
                TypeInterval.as(builder.types[0]).getEntryType(),
                TypeInterval.as(builder.types[1]).getEntryType());
    }

    @Override
    public Type resultType() {
        return TypeInterval.get();
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        for (Value operand : operands) {
            assert operand != null;
        }
        Value resultLower = ValueInterval.getLower(result);
        Value resultUpper = ValueInterval.getUpper(result);
        Value op1Lower = ValueInterval.getLower(operands[0]);
        Value op1Upper = ValueInterval.getUpper(operands[0]);
        Value op2Lower = ValueInterval.getLower(operands[1]);
        Value op2Upper = ValueInterval.getUpper(operands[1]);
        divide.apply(resultLower, op1Lower, op2Lower);
        divide.apply(resultUpper, op1Upper, op2Upper);
    }
}
