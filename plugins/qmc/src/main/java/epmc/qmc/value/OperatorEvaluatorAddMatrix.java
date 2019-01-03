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
import epmc.operator.OperatorAdd;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorAddMatrix implements OperatorEvaluator {
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
            if (operator != OperatorAdd.ADD) {
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
            return new OperatorEvaluatorAddMatrix(this);
        }
    }

    private final TypeMatrix resultType;
    private final ValueAlgebra entryAcc1;
    private final ValueAlgebra entryAcc2;
    private final ValueAlgebra entryAcc3;    
    private final OperatorEvaluator add;

    private OperatorEvaluatorAddMatrix(Builder builder) {
        TypeAlgebra typeEntry1 = TypeMatrix.as(builder.types[0]).getEntryType();
        TypeAlgebra typeEntry2 = TypeMatrix.as(builder.types[0]).getEntryType();
        TypeAlgebra typeEntry = TypeInteger.get();
        if (TypeReal.is(typeEntry1) || TypeComplex.is(typeEntry1)) {
            typeEntry = typeEntry1;
        }
        if (!TypeComplex.is(typeEntry) && TypeReal.is(typeEntry2)
                || TypeComplex.is(typeEntry2)) {
            typeEntry = typeEntry1;
        }
        resultType = TypeMatrix.get(typeEntry);
        entryAcc1 = resultType.getEntryType().newValue();
        entryAcc2 = resultType.getEntryType().newValue();
        entryAcc3 = resultType.getEntryType().newValue();
        add = ContextValue.get().getEvaluator(OperatorAdd.ADD, resultType.getEntryType(), resultType.getEntryType());
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
        ValueMatrix resMatrix = asMatrix(result);
        ValueMatrix op1Matrix = asMatrix(operands[0]);
        ValueMatrix op2Matrix = asMatrix(operands[1]);
        if (!op1Matrix.isDimensionsUnspecified()
                && !op2Matrix.isDimensionsUnspecified()) {
            subFullSpecified(resMatrix, op1Matrix, op2Matrix);
        } else if (!op1Matrix.isDimensionsUnspecified() &&
                op2Matrix.isDimensionsUnspecified()) {
            subOneSpecified(resMatrix, op1Matrix, op2Matrix);
        } else if (op1Matrix.isDimensionsUnspecified() &&
                !op2Matrix.isDimensionsUnspecified()) {
            subOneSpecified(resMatrix, op2Matrix, op1Matrix);          
        } else {
            subNoneSpecified(resMatrix, op1Matrix, op2Matrix);
        }
    }

    public static ValueMatrix asMatrix(Value result) {
        if (ValueMatrix.is(result)) {
            return (ValueMatrix) result;
        } else {
            return null;
        }
    }
    
    private void subFullSpecified(ValueMatrix result, ValueMatrix op1, ValueMatrix op2) {
        assert !op1.isDimensionsUnspecified();
        assert !op2.isDimensionsUnspecified();
        if (result != op1 && result != op2) {
            result.setDimensions(op1.getNumRows(), op1.getNumColumns());
        }
        for (int index = 0; index < op1.getValues().size(); index++) {
            op1.getValues().get(entryAcc1, index);
            op2.getValues().get(entryAcc2, index);
            add.apply(entryAcc3, entryAcc1, entryAcc2);
            result.getValues().set(entryAcc3, index);
        }
    }

    private void subOneSpecified(ValueMatrix result, ValueMatrix op1, ValueMatrix op2) {
        assert !op1.isDimensionsUnspecified();
        assert op2.isDimensionsUnspecified();
        ValueAlgebra op2entry = entryAcc2;
        op2.getValues().get(op2entry, 0);
        if (result != op1) {
            result.setDimensions(op1.getNumRows(), op1.getNumColumns());
        }
        int numRows = result.getNumRows();
        int numColumns = result.getNumColumns();
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numColumns; col++) {
                ValueAlgebra entryAcc = entryAcc1;
                op1.get(entryAcc, row, col);
                if (row == col) {
                    add.apply(entryAcc, entryAcc, op2entry);
                }
                result.set(entryAcc, row, col);
            }
        }
    }
    
    private void subNoneSpecified(ValueMatrix resMatrix, ValueMatrix op1Matrix, ValueMatrix op2Matrix) {
        op1Matrix.getValues().get(entryAcc1, 0);
        op2Matrix.getValues().get(entryAcc2, 0);
        resMatrix.setDimensionsUnspecified();
        add.apply(entryAcc3, entryAcc1, entryAcc2);
        resMatrix.getValues().set(entryAcc3, 0);
    }

}
