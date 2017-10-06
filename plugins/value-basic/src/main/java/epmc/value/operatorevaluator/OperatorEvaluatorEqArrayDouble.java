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

import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeArrayDouble;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueArrayDoubleJava;
import epmc.value.ValueBoolean;
import epmc.value.operator.OperatorEq;

public enum OperatorEvaluatorEqArrayDouble implements OperatorEvaluator {
    INSTANCE;

    @Override
    public Operator getOperator() {
        return OperatorEq.EQ;
    }

    @Override
    public boolean canApply(Type... types) {
        assert types != null;
        for (Type type : types) {
            assert type != null;
        }
        if (types.length != 2) {
            return false;
        }
        for (Type type : types) {
            if (!TypeArrayDouble.isArrayDouble(type)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Type resultType(Type... types) {
        assert types != null;
        for (Type type : types) {
            assert type != null;
        }
        return TypeBoolean.get();
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        for (Value operand : operands) {
            assert operand != null;
        }
        ValueArrayDoubleJava op1 = ValueArrayDoubleJava.asValueArrayDoubleJava(operands[0]);
        ValueArrayDoubleJava op2 = ValueArrayDoubleJava.asValueArrayDoubleJava(operands[1]);
        ValueBoolean resultBoolean = ValueBoolean.asBoolean(result);
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
