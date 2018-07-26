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

package epmc.param.operatorevaluator.interval;

import epmc.operator.Operator;
import epmc.operator.OperatorLt;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorPow;
import epmc.operator.OperatorSet;
import epmc.param.operator.OperatorNextDown;
import epmc.param.operator.OperatorNextUp;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.ValueInteger;
import epmc.value.ValueInterval;
import epmc.value.ValueReal;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorPowInterval implements OperatorEvaluator {
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
            if (operator != OperatorPow.POW) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            if (!TypeInterval.is(types[0])) {
                return null;
            }
            if (!TypeInteger.is(types[1])) {
                return null;
            }
            return new EvaluatorPowInterval(this);
        }
    }

    private final TypeInterval resultType;
    private final OperatorEvaluator powReal;
    private final OperatorEvaluator setReal;
    private final OperatorEvaluator nextDown;
    private final OperatorEvaluator nextUp;
    private final OperatorEvaluator ltReal;
    private final boolean widen;
    private final ValueReal lowerPow;
    private final ValueReal upperPow;
    private final ValueReal zero;
    private final ValueBoolean cmp;
    private final OperatorEvaluator maxReal;

    private EvaluatorPowInterval(Builder builder) {
        resultType = TypeInterval.as(builder.types[0]);
        TypeReal typeReal = resultType.getEntryType();
        TypeInteger typeInteger = TypeInteger.as(builder.types[1]);
        powReal = ContextValue.get().getEvaluator(OperatorPow.POW, typeReal, typeInteger);
        setReal = ContextValue.get().getEvaluator(OperatorSet.SET, typeReal, typeReal);
        nextDown = ContextValue.get().getEvaluatorOrNull(OperatorNextDown.NEXT_DOWN, typeReal);
        nextUp = ContextValue.get().getEvaluatorOrNull(OperatorNextUp.NEXT_UP, typeReal);
        widen = nextDown != null && nextUp != null;
        lowerPow = typeReal.newValue();
        upperPow = typeReal.newValue();
        zero = UtilValue.newValue(typeReal, 0);
        ltReal = ContextValue.get().getEvaluator(OperatorLt.LT, typeReal, typeReal);
        cmp = TypeBoolean.get().newValue();
        maxReal = ContextValue.get().getEvaluator(OperatorMax.MAX, typeReal, typeReal);
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
        ValueInterval operand1 = ValueInterval.as(operands[0]);
        ValueInteger operand2 = ValueInteger.as(operands[1]);
        ValueInterval resultInterval = ValueInterval.as(result);
        int powInt = operand2.getInt();
        powReal.apply(lowerPow, operand1.getIntervalLower(), operand2);
        powReal.apply(upperPow, operand1.getIntervalUpper(), operand2);
        if (powInt % 2 == 1) {
            setReal.apply(resultInterval.getIntervalLower(), lowerPow);
            setReal.apply(resultInterval.getIntervalUpper(), upperPow);
        } else {
            ltReal.apply(cmp, operand1.getIntervalLower(), zero);
            boolean lowerLtZero = cmp.getBoolean();
            ltReal.apply(cmp, zero, operand1.getIntervalUpper());
            boolean upperGtZero = cmp.getBoolean();
            if (lowerLtZero && upperGtZero) {
                setReal.apply(resultInterval.getIntervalLower(), zero);
                maxReal.apply(resultInterval.getIntervalUpper(), lowerPow, upperPow);
            } else if (lowerLtZero) {
                setReal.apply(resultInterval.getIntervalLower(), upperPow);
                setReal.apply(resultInterval.getIntervalUpper(), lowerPow);
            } else {
                setReal.apply(resultInterval.getIntervalLower(), lowerPow);
                setReal.apply(resultInterval.getIntervalUpper(), upperPow);                
            }
        }
        if (widen) {
            nextDown.apply(resultInterval.getIntervalLower(), resultInterval.getIntervalLower());
            nextUp.apply(resultInterval.getIntervalUpper(), resultInterval.getIntervalUpper());
        }
    }
}
