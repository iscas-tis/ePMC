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
import epmc.operator.OperatorIsOne;
import epmc.operator.OperatorIsZero;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorIsOneMatrix implements OperatorEvaluator {
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
            if (operator != OperatorIsOne.IS_ONE) {
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
            return new OperatorEvaluatorIsOneMatrix(this);
        }
    }

    private final ValueAlgebra entryAcc;
    private final ValueBoolean cmpOne;
    private final ValueBoolean cmpZero;
    private final OperatorEvaluator isOne;
    private final OperatorEvaluator isZero;
    private final ValueBoolean cmp;
    
    private OperatorEvaluatorIsOneMatrix(Builder builder) {
        entryAcc = TypeMatrix.as(builder.types[0]).getEntryType().newValue();
        cmpOne = TypeBoolean.get().newValue();
        cmpZero = TypeBoolean.get().newValue();
        isOne = ContextValue.get().getEvaluator(OperatorIsOne.IS_ONE, entryAcc.getType());
        isZero = ContextValue.get().getEvaluator(OperatorIsZero.IS_ZERO, entryAcc.getType());
        cmp = TypeBoolean.get().newValue();
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
        ValueMatrix operand = ValueMatrix.as(operands[0]);
        ValueBoolean.as(result).set(isOne(operand));
    }
    
    private boolean isOne(ValueMatrix op) {
        if (op.isDimensionsUnspecified()) {
            return isOneDimensionsUnspecified(op);
        } else {
            return isOneDimensionsSpecified(op);          
        }
    }

    private boolean isOneDimensionsUnspecified(ValueMatrix op) {
        assert op.isDimensionsUnspecified();
        op.getValues().get(entryAcc, 0);
        isOne.apply(cmp, entryAcc);
        return cmp.getBoolean();
    }

    private boolean isOneDimensionsSpecified(ValueMatrix op) {
        assert !op.isDimensionsUnspecified();
        for (int row = 0; row < op.getNumRows(); row++) {
            for (int col = 0; col < op.getNumColumns(); col++) {
                op.get(entryAcc, row, col);
                isOne.apply(cmpOne, entryAcc);
                isZero.apply(cmpZero, entryAcc);
                if (row == col ? !cmpOne.getBoolean() : !cmpZero.getBoolean()) {
                    return false;
                }
            }
        }
        return true;
    }
}
