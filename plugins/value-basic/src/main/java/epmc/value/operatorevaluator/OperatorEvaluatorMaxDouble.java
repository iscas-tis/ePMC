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
import epmc.value.TypeDouble;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.ValueDouble;
import epmc.value.ValueInteger;
import epmc.value.operator.OperatorMax;

public enum OperatorEvaluatorMaxDouble implements OperatorEvaluator {
    INSTANCE;

    @Override
    public Operator getOperator() {
        return OperatorMax.MAX;
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
        if (TypeInteger.isInteger(types[0]) && TypeInteger.isInteger(types[1])) {
            return false;
        }
        if (!TypeDouble.isDouble(types[0])
                && !TypeInteger.isInteger(types[0])) {
            return false;
        }
        if (!TypeDouble.isDouble(types[1])
                && !TypeInteger.isInteger(types[1])) {
            return false;
        }
        return true;
    }

    @Override
    public Type resultType(Operator operator, Type... types) {
        assert operator != null;
        assert operator.equals(OperatorMax.MAX);
        assert types != null;
        for (Type type : types) {
            assert type != null;
        }
        return TypeDouble.get();
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        for (Value operand : operands) {
            assert operand != null;
        }
        double op1 = getDouble(operands[0]);
        double op2 = getDouble(operands[1]);
        ValueDouble.asDouble(result).set(Math.max(op1, op2));
    }

    private static double getDouble(Value value) {
        assert value != null;
        assert ValueDouble.isDouble(value) || ValueInteger.isInteger(value)
        : value.getType();
        if (ValueDouble.isDouble(value)) {
            return ValueDouble.asDouble(value).getDouble();
        } else if (ValueInteger.isInteger(value)) {
            return ValueInteger.asInteger(value).getInt();
        } else {
            assert false;
            return Double.NaN;
        }
    }
}
