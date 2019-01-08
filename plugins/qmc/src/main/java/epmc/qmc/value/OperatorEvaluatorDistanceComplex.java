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
import epmc.operator.OperatorAbs;
import epmc.operator.OperatorDistance;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorSubtract;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorDistanceComplex implements OperatorEvaluator {
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
            if (!TypeComplex.is(types[0])
                    && !TypeComplex.is(types[1])) {
                return null;
            }
            for (Type type : types) {
                if (!TypeReal.is(type)
                        && !TypeInteger.is(type)
                        && !TypeComplex.is(type)) {
                    return null;
                }
            }
            return new OperatorEvaluatorDistanceComplex(this);
        }
    }

    private final TypeReal resultType;
    private final OperatorEvaluator subtract;
    private final OperatorEvaluator abs;
    private final Value resReal;
    private final Value resImag;
    private final OperatorEvaluator max;
    
    private OperatorEvaluatorDistanceComplex(Builder builder) {
        resultType = TypeReal.get();
        subtract = ContextValue.get().getEvaluator(OperatorSubtract.SUBTRACT, TypeReal.get(), TypeReal.get());
        abs = ContextValue.get().getEvaluator(OperatorAbs.ABS, TypeReal.get());
        resReal = TypeReal.get().newValue();
        resImag = TypeReal.get().newValue();
        max = ContextValue.get().getEvaluator(OperatorMax.MAX, TypeReal.get(), TypeReal.get());
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
        assert operands.length >= 2;
        Value op1Real = ValueComplex.getReal(operands[0]);
        Value op1Imag = ValueComplex.getImag(operands[0]);
        Value op2Real = ValueComplex.getReal(operands[1]);
        Value op2Imag = ValueComplex.getImag(operands[1]);
        subtract.apply(resReal, op1Real, op2Real);
        abs.apply(resReal, resReal);
        subtract.apply(resImag, op1Imag, op2Imag);
        abs.apply(resImag, resImag);
        max.apply(result, resReal, resImag);
    }
}
