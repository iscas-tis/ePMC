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
import epmc.value.operator.OperatorDivide;

public enum OperatorEvaluatorDivideInterval implements OperatorEvaluator {
    INSTANCE;

    @Override
    public Operator getOperator() {
        return OperatorDivide.DIVIDE;
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
            if (!TypeInterval.isInterval(type) && !TypeReal.isReal(type) && !TypeInteger.isInteger(type)) {
                return false;
            }
        }
        if (!TypeInterval.isInterval(types[0]) && !TypeInterval.isInterval(types[0])) {
            return false;
        }
        return true;
    }

    @Override
    public Type resultType(Operator operator, Type... types) {
        assert operator != null;
        assert operator.equals(OperatorDivide.DIVIDE);
        assert types != null;
        for (Type type : types) {
            assert type != null;
        }
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
        OperatorEvaluator divide = ContextValue.get().getOperatorEvaluator(OperatorDivide.DIVIDE, op1Lower.getType(), op2Lower.getType());
        divide.apply(resultLower, op1Lower, op2Lower);
        divide.apply(resultUpper, op1Upper, op2Upper);
    }
}
