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

import static epmc.error.UtilError.fail;

import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.ProblemsValueBasic;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeBoolean;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.operator.OperatorEq;
import epmc.value.operator.OperatorLt;
import epmc.value.operator.OperatorMin;

public enum OperatorEvaluatorMin implements OperatorEvaluator {
    INSTANCE;

    @Override
    public Operator getOperator() {
        return OperatorMin.MIN;
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
            if (!TypeAlgebra.isAlgebra(type)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Type resultType(Operator operator, Type... types) {
        assert operator != null;
        assert operator.equals(OperatorMin.MIN);
        assert types != null;
        for (Type type : types) {
            assert type != null;
        }
        return UtilValue.algebraicResultType(types);
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        for (Value operand : operands) {
            assert operand != null;
        }
        Value operand1 = operands[0];
        Value operand2 = operands[1];
        OperatorEvaluator eq = ContextValue.get().getOperatorEvaluator(OperatorEq.EQ, operand2.getType(), operand1.getType());
        OperatorEvaluator lt = ContextValue.get().getOperatorEvaluator(OperatorLt.LT, operand2.getType(), operand1.getType());
        ValueBoolean cmp = TypeBoolean.get().newValue();
        lt.apply(cmp, operand1, operand2);
        if (cmp.getBoolean()) {
            result.set(operand1);
            return;
        }
        lt.apply(cmp, operand2, operand1);
        if (cmp.getBoolean()) {
            result.set(operand2);
            return;
        }
        eq.apply(cmp, operand2, operand1);
        if (cmp.getBoolean()) {
            result.set(operand1);
            return;
        }
        fail(ProblemsValueBasic.VALUES_INCOMPARABLE);
    }
}
