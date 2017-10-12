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
import epmc.value.TypeBoolean;
import epmc.value.TypeDouble;
import epmc.value.TypeInteger;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.ValueDouble;
import epmc.value.operator.OperatorIte;

public enum OperatorEvaluatorIteDouble implements OperatorEvaluator {
    INSTANCE;

    @Override
    public Operator getOperator() {
        return OperatorIte.ITE;
    }

    @Override
    public boolean canApply(Type... types) {
        assert types != null;
        for (Type type : types) {
            assert type != null;
        }
        if (types.length != 3) {
            return false;
        }
        if (!TypeBoolean.isBoolean(types[0])) {
            return false;
        }
        if (!TypeDouble.isDouble(types[1]) && !TypeInteger.isInteger(types[1])) {
            return false;
        }
        if (!TypeDouble.isDouble(types[2]) && !TypeInteger.isInteger(types[2])) {
            return false;
        }
        if (!TypeDouble.isDouble(types[1]) && !TypeDouble.isDouble(types[2])) {
            return false;
        }
        return true;
    }

    @Override
    public Type resultType(Type... types) {
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
        ValueDouble resultDouble = ValueDouble.asDouble(result);
        if (ValueBoolean.asBoolean(operands[0]).getBoolean()) {
            resultDouble.set(UtilValue.getDoubleOrInt(operands[1]));
        } else {
            resultDouble.set(UtilValue.getDoubleOrInt(operands[2]));
        }
    }
}
