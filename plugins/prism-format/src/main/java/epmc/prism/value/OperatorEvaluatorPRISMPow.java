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

package epmc.prism.value;

import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeDouble;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueDouble;
import epmc.value.ValueInteger;
import epmc.value.ValueReal;

public enum OperatorEvaluatorPRISMPow implements OperatorEvaluator {
    INSTANCE;

    @Override
    public Operator getOperator() {
        return OperatorPRISMPow.PRISM_POW;
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
        if (!TypeInteger.isInteger(types[0]) && !TypeDouble.isDouble(types[0])) {
            return false;
        }
        if (!TypeInteger.isInteger(types[1]) && !TypeDouble.isDouble(types[1])) {
            return false;
        }
        return true;
    }

    @Override
    public Type resultType(Type... types) {
        assert types != null;
        assert types.length == 2 : types.length;
        boolean allInteger = true;
        for (Type type : types) {
            allInteger &= TypeInteger.isInteger(type);
        }
        if (allInteger) {
            return TypeInteger.get();
        } else {
            return TypeReal.get();
        }
    }

    @Override
    public void apply(Value result, Value... operands) {
        if (ValueInteger.isInteger(result)) {
            int value1 = ValueInteger.asInteger(operands[0]).getInt();
            int value2 = ValueInteger.asInteger(operands[1]).getInt();
            ValueInteger.asInteger(result).set((int) Math.pow(value1, value2));
        } else if (ValueReal.isReal(result)) {
            double value1 = ValueDouble.isDouble(operands[0]) ? ValueDouble.asDouble(operands[0]).getDouble()
                    : ValueInteger.asInteger(operands[0]).getInt();
            double value2 = ValueDouble.isDouble(operands[1]) ? ValueDouble.asDouble(operands[1]).getDouble()
                    : ValueInteger.asInteger(operands[1]).getInt();
            ValueDouble.asDouble(result).set(Math.pow(value1, value2));
        } else {
            assert false : result.getType();
        }
    }
}
