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

import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueInterval;
import epmc.value.operator.OperatorAbs;
import epmc.value.operator.OperatorAdd;
import epmc.value.operator.OperatorDistance;
import epmc.value.operator.OperatorMax;
import epmc.value.operator.OperatorSubtract;

public enum OperatorEvaluatorDistanceInterval implements OperatorEvaluator {
    INSTANCE;

    @Override
    public Operator getOperator() {
        return OperatorDistance.DISTANCE;
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
        if (!TypeInterval.isInterval(types[0])
                && !TypeInterval.isInterval(types[1])) {
            return false;
        }
        for (Type type : types) {
            if (!TypeReal.isReal(type)
                    && !TypeInteger.isInteger(type)
                    && !TypeInterval.isInterval(type)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Type resultType(Operator operator, Type... types) {
        assert operator != null;
        assert operator.equals(OperatorAdd.ADD);
        assert types != null;
        for (Type type : types) {
            assert type != null;
        }
        return TypeReal.get();
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        for (Value operand : operands) {
            assert operand != null;
        }
        assert operands.length >= 2;
        Value op1Lower = ValueInterval.getLower(operands[0]);
        Value op1Upper = ValueInterval.getUpper(operands[0]);
        Value op2Lower = ValueInterval.getLower(operands[1]);
        Value op2Upper = ValueInterval.getUpper(operands[1]);
        OperatorEvaluator subtract = ContextValue.get().getOperatorEvaluator(OperatorSubtract.SUBTRACT, TypeReal.get(), TypeReal.get());
        OperatorEvaluator abs = ContextValue.get().getOperatorEvaluator(OperatorAbs.ABS, TypeReal.get());
        Value resLower = TypeReal.get().newValue();
        Value resUpper = TypeReal.get().newValue();
        subtract.apply(resLower, op1Lower, op2Lower);
        abs.apply(resLower, resLower);
        subtract.apply(resUpper, op1Upper, op2Upper);
        abs.apply(resUpper, resUpper);
        OperatorEvaluator max = ContextValue.get().getOperatorEvaluator(OperatorMax.MAX, TypeReal.get(), TypeReal.get());
        max.apply(result, resLower, resUpper);
    }
}
