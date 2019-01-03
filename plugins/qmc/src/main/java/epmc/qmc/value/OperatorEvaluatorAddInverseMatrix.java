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
import epmc.operator.OperatorAddInverse;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorAddInverseMatrix implements OperatorEvaluator {
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
            built = true;
            for (Type type : types) {
                assert type != null;
            }
            if (operator != OperatorAddInverse.ADD_INVERSE) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            for (Type type : types) {
                if (!TypeMatrix.is(type)) {
                    return null;
                }
            }
            return new OperatorEvaluatorAddInverseMatrix(this);
        }
    }

    private final TypeMatrix typeMatrix;
    private final ValueAlgebra entry1;
    private final ValueAlgebra entry2;
    private final OperatorEvaluator addInverse;
    private final Type resultType;

    private OperatorEvaluatorAddInverseMatrix(Builder builder) {
        typeMatrix = TypeMatrix.as(builder.types[0]);
        entry1 = typeMatrix.getEntryType().newValue();
        entry2 = typeMatrix.getEntryType().newValue();
        addInverse = ContextValue.get().getEvaluator(OperatorAddInverse.ADD_INVERSE, typeMatrix.getEntryType());
        resultType = builder.types[0];
    }

    @Override
    public Type resultType() {
        return resultType;
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        for (Value operand : operands) {
            assert operand != null;
        }
        ValueMatrix resultMatrix = ValueMatrix.as(result);
        ValueMatrix opMatrix = ValueMatrix.as(operands[0]);
        resultMatrix.setDimensions(opMatrix.getNumRows(), opMatrix.getNumRows());
        ValueArrayAlgebra opMatrixValues = opMatrix.getValues();
        ValueArrayAlgebra resultValues = resultMatrix.getValues();
        for (int index = 0; index < opMatrix.getTotalSize(); index++) {
            opMatrixValues.get(entry1, index);
            addInverse.apply(entry2, entry1);
            resultValues.set(entry2, index);
        }
    }
}
