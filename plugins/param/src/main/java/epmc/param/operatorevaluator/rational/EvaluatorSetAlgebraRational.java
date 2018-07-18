package epmc.param.operatorevaluator.rational;

import epmc.operator.Operator;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorSet;
import epmc.param.value.rational.TypeRational;
import epmc.param.value.rational.ValueRational;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.Value;
import epmc.value.ValueSetString;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorSetAlgebraRational implements OperatorEvaluator {
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
            if (operator != OperatorSet.SET) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            if (!TypeRational.is(types[0])) {
                return null;
            }
            if (!TypeAlgebra.is(types[1])) {
                return null;
            }
            return new EvaluatorSetAlgebraRational(this);
        }
        
    }

    private final TypeAlgebra resultType;
    private final OperatorEvaluator divide;
    private final ValueSetString numerator;
    private final ValueSetString denominator;
    
    private EvaluatorSetAlgebraRational(Builder builder) {
        assert builder != null;
        assert builder.operator != null;
        assert builder.types != null;
        resultType = TypeAlgebra.as(builder.types[1]);
        divide = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, resultType, resultType);
        numerator = ValueSetString.as(resultType.newValue());
        denominator = ValueSetString.as(resultType.newValue());
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
        ValueRational valueRational = ValueRational.as(operands[0]);
        numerator.set(valueRational.getNumerator().toString());
        denominator.set(valueRational.getDenominator().toString());
        divide.apply(result, numerator, denominator);
    }
}
