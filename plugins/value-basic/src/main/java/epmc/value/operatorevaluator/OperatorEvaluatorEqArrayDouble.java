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
import epmc.operator.OperatorEq;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeArrayDouble;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueArrayDoubleJava;
import epmc.value.ValueBoolean;

public final class OperatorEvaluatorEqArrayDouble implements OperatorEvaluator {
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
            if (operator != OperatorEq.EQ) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            for (Type type : types) {
                if (!TypeArrayDouble.isArrayDouble(type)) {
                    return null;
                }
            }
            return new OperatorEvaluatorEqArrayDouble(this);
        }
    }

    private OperatorEvaluatorEqArrayDouble(Builder builder) {
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
        ValueArrayDoubleJava op1 = ValueArrayDoubleJava.as(operands[0]);
        ValueArrayDoubleJava op2 = ValueArrayDoubleJava.as(operands[1]);
        ValueBoolean resultBoolean = ValueBoolean.as(result);
        if (op1.size() != op2.size()) {
            resultBoolean.set(false);
            return;
        }
        double[] arr1 = op1.getDoubleArray();
        double[] arr2 = op2.getDoubleArray();
        for (int i = 0; i < arr1.length; i++) {
            if (Math.abs(arr1[i] - arr2[i]) >= 1E-6) {
                resultBoolean.set(false);
                return;
            }
        }
        resultBoolean.set(true);
        return;
    }
}
