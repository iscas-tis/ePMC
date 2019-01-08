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
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorMultiplyMatrix implements OperatorEvaluator {
    private final static String SPACE = " ";
    
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
            if (operator != OperatorMultiply.MULTIPLY) {
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
            return new OperatorEvaluatorMultiplyMatrix(this);
        }
    }

    private final TypeMatrix resultType;
    private final ValueMatrix resultBuffer;
    private final OperatorEvaluator add;
    private final ValueAlgebra leftEntry;
    private final ValueAlgebra rightEntry;
    private final ValueAlgebra prodEntry;
    private final ValueAlgebra sumEntry;
    private final OperatorEvaluator set;
    private final OperatorEvaluator multiply;

    private OperatorEvaluatorMultiplyMatrix(Builder builder) {
        TypeAlgebra typeEntry1 = TypeMatrix.as(builder.types[0]).getEntryType();
        TypeAlgebra typeEntry2 = TypeMatrix.as(builder.types[1]).getEntryType();
        TypeAlgebra typeEntry = TypeInteger.get();
        if (TypeReal.is(typeEntry1) || TypeComplex.is(typeEntry1)) {
            typeEntry = typeEntry1;
        }
        if (!TypeComplex.is(typeEntry) && TypeReal.is(typeEntry2)
                || TypeComplex.is(typeEntry2)) {
            typeEntry = typeEntry1;
        }
        resultType = TypeMatrix.get(typeEntry);
        resultBuffer = resultType.newValue();
        multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY,
                TypeMatrix.as(builder.types[0]).getEntryType(),
                TypeMatrix.as(builder.types[1]).getEntryType());
        add = ContextValue.get().getEvaluator(OperatorAdd.ADD, multiply.resultType(), multiply.resultType());
        leftEntry = TypeMatrix.as(builder.types[0]).getEntryType().newValue();
        rightEntry = TypeMatrix.as(builder.types[1]).getEntryType().newValue();
        prodEntry = ValueAlgebra.as(multiply.resultType().newValue());
        sumEntry = ValueAlgebra.as(add.resultType().newValue());
        set = ContextValue.get().getEvaluator(OperatorSet.SET, sumEntry.getType(), sumEntry.getType());
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
        multiplyMatrices(resMatrix, op1Matrix, op2Matrix);
    }

    public static ValueMatrix asMatrix(Value result) {
        if (ValueMatrix.is(result)) {
            return (ValueMatrix) result;
        } else {
            return null;
        }
    }
    
    private void multiplyMatrices(ValueMatrix result, ValueMatrix op1, ValueMatrix op2) {
        assert op1 != null;
        assert op2 != null;
        if (!op1.isDimensionsUnspecified() && !op2.isDimensionsUnspecified()) {
            matrixMultiplyFullSpecified(result, op1, op2);
        } else if (!op1.isDimensionsUnspecified() && op2.isDimensionsUnspecified()) {
            matrixMultiplyLeftSpecified(result, op1, op2);
        } else if (op1.isDimensionsUnspecified() && !op2.isDimensionsUnspecified()) {
            matrixMultiplyRightSpecified(result, op1, op2);
        } else {
            matrixMultiplyNoneSpecified(result, op1, op2);
        }
    }

    private void matrixMultiplyFullSpecified(ValueMatrix result, ValueMatrix op1, ValueMatrix op2) {
        assert op1 != null;
        assert op2 != null;
        assert !op1.isDimensionsUnspecified();
        assert !op2.isDimensionsUnspecified();
        assert op1.getNumColumns() == op2.getNumRows()
                : op1.getNumColumns() + SPACE + op2.getNumRows();
        if (result == op1 || result == op2) {
            matrixMultiplyFullSpecified(resultBuffer, op1, op2);
            setMatrix(result, resultBuffer);
            return;
        }
        ValueAlgebra sumEntryZero = UtilValue.newValue(sumEntry.getType(), 0);
        result.setDimensions(op1.getNumRows(), op2.getNumColumns());
        for (int row = 0; row < op1.getNumRows(); row++) {
            for (int column = 0; column < op2.getNumColumns(); column++) {
                set.apply(sumEntry, sumEntryZero);
                for (int inner = 0; inner < op1.getNumColumns(); inner++) {
                    op1.get(leftEntry, row, inner);
                    op2.get(rightEntry, inner, column);
                    multiply.apply(prodEntry, leftEntry, rightEntry);
                    add.apply(sumEntry, sumEntry, prodEntry);
                }
                result.set(sumEntry, row, column);
            }
        }
    }

    private void matrixMultiplyLeftSpecified(ValueMatrix result, ValueMatrix op1, ValueMatrix op2) {
        assert op1 != null;
        assert op2 != null;
        assert !op1.isDimensionsUnspecified();
        assert op2.isDimensionsUnspecified();
        op2.getValues().get(rightEntry, 0);
        if (result != op1) {
            result.setDimensions(op1.getNumRows(), op1.getNumColumns());
        }
        for (int index = 0; index < op1.getValues().size(); index++) {
            op1.getValues().get(leftEntry, index);
            multiply.apply(prodEntry, leftEntry, rightEntry);
            result.getValues().set(prodEntry, index);
        }
    }

    private void matrixMultiplyRightSpecified(ValueMatrix result, ValueMatrix op1, ValueMatrix op2) {
        assert op1 != null;
        assert op2 != null;
        assert op1.isDimensionsUnspecified();
        assert !op2.isDimensionsUnspecified();
        op1.getValues().get(leftEntry, 0);
        if (result != op2) {
            result.setDimensions(op2.getNumRows(), op2.getNumColumns());
        }
        for (int index = 0; index < op2.getValues().size(); index++) {
            op2.getValues().get(rightEntry, index);
            multiply.apply(prodEntry, leftEntry, rightEntry);
            result.getValues().set(prodEntry, index);
        }
    }

    private void matrixMultiplyNoneSpecified(ValueMatrix result, ValueMatrix op1, ValueMatrix op2) {
        assert op1 != null;
        assert op2 != null;
        assert op1.isDimensionsUnspecified();
        assert op2.isDimensionsUnspecified();
        op1.getValues().get(leftEntry, 0);
        op2.getValues().get(rightEntry, 0);
        multiply.apply(prodEntry, leftEntry, rightEntry);
        result.setDimensionsUnspecified();
        result.getValues().set(prodEntry, 0);
    }
    
    private void setMatrix(ValueMatrix result, ValueMatrix matrix) {
        assert matrix != null;
        result.setDimensions(matrix.getNumRows(), matrix.getNumColumns());
        for (int index = 0; index < matrix.getValues().size(); index++) {
            matrix.getValues().get(prodEntry, index);
            result.getValues().set(prodEntry, index);
        }
    }
}
