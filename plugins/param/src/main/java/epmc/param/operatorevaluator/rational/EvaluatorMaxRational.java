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

package epmc.param.operatorevaluator.rational;

import java.math.BigInteger;

import epmc.operator.Operator;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorSet;
import epmc.param.value.rational.TypeRational;
import epmc.param.value.rational.ValueRational;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.ValueInteger;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorMaxRational implements OperatorEvaluator {
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
            if (operator != OperatorMax.MAX) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            if (TypeInteger.is(types[0]) && TypeInteger.is(types[1])) {
                return null;
            }
            if (!TypeRational.is(types[0])
                    && !TypeInteger.is(types[0])) {
                return null;
            }
            if (!TypeRational.is(types[1])
                    && !TypeInteger.is(types[1])) {
                return null;
            }
            return new EvaluatorMaxRational(this);
        }
    }

    private final boolean leftInteger;
    private final boolean rightInteger;
    private final OperatorEvaluator set;

    private EvaluatorMaxRational(Builder builder) {
        leftInteger = TypeInteger.is(builder.types[0]);
        rightInteger = TypeInteger.is(builder.types[1]);
        if (!leftInteger && !leftInteger) {
            set = ContextValue.get().getEvaluator(OperatorSet.SET,
                    builder.types[0],
                    builder.types[1]);
        } else {
            set = null;
        }
    }

    @Override
    public Type resultType() {
        return TypeRational.get();
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        for (Value operand : operands) {
            assert operand != null;
        }
        if (!leftInteger && !rightInteger) {
            applyAllRationals(ValueRational.as(result), ValueRational.as(operands[0]), ValueRational.as(operands[1]));
        } else if (leftInteger) {
            applyMixed(ValueRational.as(result), ValueRational.as(operands[1]), ValueInteger.as(operands[0]));
        } else if (rightInteger) {
            applyMixed(ValueRational.as(result), ValueRational.as(operands[0]), ValueInteger.as(operands[1]));            
        } else {
            assert false;
        }
    }

    private void applyAllRationals(ValueRational result, ValueRational op1, ValueRational op2) {
        if (op1.getDenominator().equals(op2.getDenominator())) {
            int cmp = op1.getNumerator().compareTo(op2.getNumerator());
            if (cmp >= 0) {
                set.apply(result, op1);
            } else {
                set.apply(result, op2);
            }
        } else {
            BigInteger left = op1.getNumerator().multiply(op2.getDenominator());
            BigInteger right = op2.getNumerator().multiply(op1.getDenominator());
            if (left.compareTo(right) >= 0) {
                set.apply(result, op1);
            } else {
                set.apply(result, op2);
            }
        }
    }
    
    private void applyMixed(ValueRational result, ValueRational op1, ValueInteger op2) {
        if (op1.getDenominator().equals(BigInteger.ONE)) {
            BigInteger right = BigInteger.valueOf(op2.getInt());
            if (op1.getNumerator().compareTo(right) >= 0) {
                set.apply(result, op1);
            } else {
                result.set(right, BigInteger.ONE);
            }
        } else {
            BigInteger right = BigInteger.valueOf(op2.getInt());
            BigInteger rightCmp = right
                    .multiply(op1.getDenominator());
            if (op1.getNumerator().compareTo(rightCmp) >= 0) {
                set.apply(result, op1);
            } else {
                result.set(right, BigInteger.ONE);
            }
        }
    }
}
