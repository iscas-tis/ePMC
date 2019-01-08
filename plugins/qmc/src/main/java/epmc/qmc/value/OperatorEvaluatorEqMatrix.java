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

package epmc.qmc.value;

import epmc.operator.Operator;
import epmc.operator.OperatorEq;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorEqMatrix implements OperatorEvaluator {
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
                if (!TypeMatrix.is(type)) {
                    return null;
                }
            }
            return new OperatorEvaluatorEqMatrix(this);
        }
    }

    private final Value acc1;
    private final Value acc2;
    private final OperatorEvaluator eq;

    private OperatorEvaluatorEqMatrix(Builder builder) {
        Type type1 = TypeMatrix.as(builder.types[0]).getEntryType();
        Type type2 = TypeMatrix.as(builder.types[1]).getEntryType();
        acc1 = type1.newValue();
        acc2 = type1.newValue();
        eq = ContextValue.get().getEvaluatorOrNull(OperatorEq.EQ, type1, type2);
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
        ValueBoolean resultBoolean = ValueBoolean.as(result);
        ValueMatrix matrix1 = ValueMatrix.as(operands[0]);
        ValueMatrix matrix2 = ValueMatrix.as(operands[1]);
        if (matrix1.getNumRows() != matrix2.getNumRows()
                || matrix1.getNumColumns() != matrix2.getNumColumns()) {
            resultBoolean.set(false);
            return;
        }
        int numRows = matrix1.getNumRows();
        int numColumns = matrix2.getNumColumns();
        resultBoolean.set(true);
        for (int row = 0; row < numRows; row++) {
            for (int column = 0; column < numColumns; column++) {
                matrix1.get(acc1, row, column);
                matrix2.get(acc2, row, column);
                eq.apply(resultBoolean, acc1, acc2);
                if (!resultBoolean.getBoolean()) {
                    return;
                }
            }
        }
        resultBoolean.set(true);
    }

}
