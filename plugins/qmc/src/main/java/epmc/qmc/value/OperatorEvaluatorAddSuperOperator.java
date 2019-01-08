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
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorAddSuperOperator implements OperatorEvaluator {
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
                if (!TypeSuperOperator.is(type)
                        && !TypeComplex.is(type)
                        && !TypeReal.is(type)
                        && !TypeInteger.is(type)) {
                    return null;
                }
            }
            if (!TypeSuperOperator.is(types[0])
                    && !TypeSuperOperator.is(types[1])) {
                return null;
            }
            return new OperatorEvaluatorAddSuperOperator(this);
        }
    }

    private final ValueSuperOperator importSuperOperator;
    private final OperatorEvaluator add;

    private OperatorEvaluatorAddSuperOperator(Builder builder) {
        add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeMatrix.get(TypeComplex.get()), TypeMatrix.get(TypeComplex.get()));
        importSuperOperator = TypeSuperOperator.get().newValue();
    }

    @Override
    public Type resultType() {
        return TypeSuperOperator.get();
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        for (Value operand : operands) {
            assert operand != null;
        }
        ValueSuperOperator resultS = ValueSuperOperator.as(result);
        ValueSuperOperator op1 = ValueSuperOperator.as(operands[0]);
        if (op1 == null) {
            OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, operands[0].getType(), importSuperOperator.getType());
            set.apply(importSuperOperator, operands[0]);
            op1 = importSuperOperator;
        }
        ValueSuperOperator op2 = ValueSuperOperator.as(operands[1]);
        if (op2 == null) {
            OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, operands[1].getType(), importSuperOperator.getType());
            set.apply(importSuperOperator, operands[1]);
            op2 = importSuperOperator;
        }
        add.apply(resultS.getMatrix(), op1.getMatrix(), op2.getMatrix());
    }
}
