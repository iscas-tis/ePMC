package epmc.param.operatorevaluator.rational;

import java.math.BigInteger;

import epmc.operator.Operator;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorGe;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorLe;
import epmc.operator.OperatorLt;
import epmc.param.value.rational.TypeRational;
import epmc.param.value.rational.ValueRational;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorCmpRational implements OperatorEvaluator {
    public final static class Builder implements OperatorEvaluatorSimpleBuilder {
        private Operator operator;
        private Type[] types;

        @Override
        public void setOperator(Operator operator) {
            this.operator = operator;
        }

        @Override
        public void setTypes(Type[] types) {
            this.types = types;
        }

        @Override
        public OperatorEvaluator build() {
            assert operator != null;
            assert types != null;
            for (Type type : types) {
                assert type != null;
            }
            if (operator != OperatorLt.LT
                    && operator != OperatorLe.LE
                    && operator != OperatorEq.EQ
                    && operator != OperatorGe.GE
                    && operator != OperatorGt.GT) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            if (!TypeRational.is(types[0])) {
                return null;
            }
            if (!TypeRational.is(types[1])) {
                return null;
            }
            return new EvaluatorCmpRational(this);
        }
        
    }

    private final TypeBoolean resultType;
    private final Operator operator;
    
    private EvaluatorCmpRational(Builder builder) {
        assert builder != null;
        assert builder.operator != null;
        assert builder.types != null;
        operator = builder.operator;
        resultType = TypeBoolean.get();
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
        ValueRational op1 = ValueRational.as(operands[0]);
        ValueRational op2 = ValueRational.as(operands[1]);
        BigInteger numLeft = op1.getNumerator();
        BigInteger denLeft = op1.getDenominator();
        
        BigInteger numRight = op2.getNumerator();
        BigInteger denRight = op2.getDenominator();
        
        BigInteger left = numLeft.multiply(denRight);
        BigInteger right = numRight.multiply(denLeft);
        int cmp = left.compareTo(right);
        
        ValueBoolean resultBoolean = ValueBoolean.as(result);
        if (operator == OperatorLt.LT) {
            resultBoolean.set(cmp < 0);
        } else if (operator == OperatorLe.LE) {
            resultBoolean.set(cmp <= 0);
        } else if (operator == OperatorEq.EQ) {
            resultBoolean.set(cmp == 0);            
        } else if (operator == OperatorGe.GE) {
            resultBoolean.set(cmp >= 0);
        } else if (operator == OperatorGt.GT) {
            resultBoolean.set(cmp > 0);
        } else {
            assert false;
        }
    }
}
