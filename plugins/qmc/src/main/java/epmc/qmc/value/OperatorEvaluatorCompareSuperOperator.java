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
import epmc.operator.OperatorGe;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorLe;
import epmc.operator.OperatorLt;
import epmc.operator.OperatorNe;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeBoolean;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueReal;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorCompareSuperOperator implements OperatorEvaluator {
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
            if (operator != OperatorEq.EQ
                    && operator != OperatorLe.LE
                    && operator != OperatorLt.LT
                    && operator != OperatorGe.GE
                    && operator != OperatorGt.GT
                    && operator != OperatorNe.NE) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            if (!TypeSuperOperator.is(types[0]) && !TypeSuperOperator.is(types[1])) {
                return null;
            }
            for (Type type : types) {
                if (!TypeSuperOperator.is(type)
                        && !TypeAlgebra.is(type)) {
                    return null;
                }
            }
            return new OperatorEvaluatorCompareSuperOperator(this);
        }
    }

    private final ValueSuperOperator importSuperOperator;
    private final OperatorEvaluator set;
    private final ValueReal diff;
    private final OperatorEvaluator compare;
    private final ValueReal zeroReal;
    
    private OperatorEvaluatorCompareSuperOperator(Builder builder) {
        if (!TypeSuperOperator.is(builder.types[0])) {
            importSuperOperator = TypeSuperOperator.get().newValue();
            set = ContextValue.get().getEvaluator(OperatorSet.SET, builder.types[0], TypeSuperOperator.get());
        } else if (!TypeSuperOperator.is(builder.types[1])) {
            importSuperOperator = TypeSuperOperator.get().newValue();
            set = ContextValue.get().getEvaluator(OperatorSet.SET, builder.types[1], TypeSuperOperator.get());
        } else {
            importSuperOperator = null;
            set = null;
        }
        diff = TypeReal.get().newValue();
        compare = ContextValue.get().getEvaluator(builder.operator, TypeReal.get(), TypeReal.get());
        zeroReal = UtilValue.newValue(TypeReal.get(), 0);
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
        ValueSuperOperator operator1 = ValueSuperOperator.as(operands[0]);
        if (operator1 == null) {
            operator1 = importSuperOperator;
            set.apply(operator1, operands[0]);
        }
        ValueSuperOperator operator2 = ValueSuperOperator.as(operands[1]);
        if (operator2 == null) {
            operator2 = importSuperOperator;
            set.apply(operator2, operands[1]);
        }
        ValueSuperOperator.maxEigenDiff(diff, operator1, operator2);
        compare.apply(result, diff, zeroReal);
    }
}
