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
import epmc.operator.OperatorMultiply;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorMultiplySuperOperator implements OperatorEvaluator {
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
                if (!TypeSuperOperator.is(type)
                        && !TypeReal.is(type)
                        && !TypeInteger.is(type)) {
                    return null;
                }
            }
            if (!TypeSuperOperator.is(types[0]) && !TypeSuperOperator.is(types[1])) {
                return null;
            }
            return new OperatorEvaluatorMultiplySuperOperator(this);
        }
    }
    
    private final OperatorEvaluator multiplyMatrices;

    private OperatorEvaluatorMultiplySuperOperator(Builder builder) {
        multiplyMatrices = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeMatrix.get(TypeComplex.get()), TypeMatrix.get(TypeComplex.get()));
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
        multiply(resultS, operands[0], operands[1]);
    }
    
    public void multiply(ValueSuperOperator result, Value operand1, Value operand2) {
        assert operand1 != null;
        assert operand2 != null;
        if (ValueSuperOperator.is(operand1) && ValueSuperOperator.is(operand2)) {
            multiplySuperOperators(result, ValueSuperOperator.as(operand1), ValueSuperOperator.as(operand2));
        } else if (ValueSuperOperator.is(operand1) && ValueAlgebra.is(operand2)) {
            multiplySuperOperatorFactor(result, ValueSuperOperator.as(operand1), ValueAlgebra.as(operand2));
        } else if (ValueSuperOperator.is(operand2) && ValueAlgebra.is(operand1)) {
            multiplySuperOperatorFactor(result, ValueSuperOperator.as(operand2), ValueAlgebra.as(operand1));
        } else {
            assert false;
        }
    }

    private void multiplySuperOperatorFactor(ValueSuperOperator result, ValueSuperOperator superoperator, ValueAlgebra factor) {
        result.getMatrix().multiplyMatrixFactor(superoperator.getMatrix(), factor);
    }

    private void multiplySuperOperators(ValueSuperOperator result, ValueSuperOperator op1, ValueSuperOperator op2) {
        assert op1 != null;
        assert op2 != null;
        multiplyMatrices.apply(result.getMatrix(), op1.getMatrix(), op2.getMatrix());
    }
}
