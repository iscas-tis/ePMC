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
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.operator.OperatorNe;

public enum OperatorEvaluatorNe implements OperatorEvaluator {
    INSTANCE;

    @Override
    public Operator getOperator() {
        return OperatorNe.NE;
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
        return true;
    }

    @Override
    public Type resultType(Operator operator, Type... types) {
        assert operator != null;
        assert operator.equals(OperatorNe.NE);
        assert types != null;
        for (Type type : types) {
            assert type != null;
        }
        Type result = null;
        if (UtilValue.allTypesKnown(types) && UtilValue.upper(types) == null) {
            return null;
        }
        result = TypeBoolean.get();
        return result;
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        for (Value operand : operands) {
            assert operand != null;
        }
        ValueBoolean.asBoolean(result).set(!operands[0].isEq(operands[1]));
    }
}
