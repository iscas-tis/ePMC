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
import epmc.operator.OperatorDistance;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueReal;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorDistanceMatrix implements OperatorEvaluator {
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
            if (operator != OperatorDistance.DISTANCE) {
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
            return new OperatorEvaluatorDistanceMatrix(this);
        }
    }

    private final OperatorEvaluator set;
    private final OperatorEvaluator distance;
    private final Value accR;
    private final ValueAlgebra acc1;
    private final ValueAlgebra acc2;
    private final OperatorEvaluator max;
    private final ValueReal zeroReal;
    
    private OperatorEvaluatorDistanceMatrix(Builder builder) {
        TypeMatrix typeMatrix1 = TypeMatrix.as(builder.types[0]);
        TypeMatrix typeMatrix2 = TypeMatrix.as(builder.types[1]);
        set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
        distance = ContextValue.get().getEvaluator(OperatorDistance.DISTANCE,
                typeMatrix1.getEntryType(),
                typeMatrix2.getEntryType());
        OperatorEvaluator distance = ContextValue.get().getEvaluator(OperatorDistance.DISTANCE,
                typeMatrix1.getEntryType(),
                typeMatrix2.getEntryType());
        accR = distance.resultType().newValue();
        acc1 = typeMatrix1.getEntryType().newValue();
        acc2 = typeMatrix2.getEntryType().newValue();
        max = ContextValue.get().getEvaluator(OperatorMax.MAX, TypeReal.get(), TypeReal.get());
        zeroReal = UtilValue.newValue(TypeReal.get(), 0);
    }

    @Override
    public Type resultType() {
        return TypeReal.get();
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        for (Value operand : operands) {
            assert operand != null;
        }
        assert operands.length >= 2;
        ValueReal resultReal = ValueReal.as(result);
        ValueMatrix op1 = ValueMatrix.as(operands[0]);
        ValueMatrix op2 = ValueMatrix.as(operands[1]);
        if (op1.isDimensionsUnspecified() != op2.isDimensionsUnspecified()) {
            set.apply(resultReal, UtilValue.newValue(TypeReal.get(), UtilValue.POS_INF));
            return;
        }
        if (op1.isDimensionsUnspecified()) {
            op1.getValues().get(acc1, 0);
            op2.getValues().get(acc2, 0);
            distance.apply(result, acc1, acc2);
            return;
        }
        if (op1.getNumRows() != op2.getNumRows()) {
            set.apply(resultReal, UtilValue.newValue(TypeReal.get(), UtilValue.POS_INF));;
            return;
        }
        if (op1.getNumColumns() != op2.getNumColumns()) {
            set.apply(resultReal, UtilValue.newValue(TypeReal.get(), UtilValue.POS_INF));
            return;
        }
        set.apply(result, zeroReal);
        
        for (int entry = 0; entry < op1.getTotalSize(); entry++) {
            op1.getValues().get(acc1, entry);
            op2.getValues().get(acc2, entry);
            distance.apply(accR, acc1, acc2);
            max.apply(result, result, accR);
        }
    }
}
