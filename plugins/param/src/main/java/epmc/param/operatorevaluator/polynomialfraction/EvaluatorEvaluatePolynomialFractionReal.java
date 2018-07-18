package epmc.param.operatorevaluator.polynomialfraction;

import epmc.operator.Operator;
import epmc.operator.OperatorDivide;
import epmc.param.value.polynomial.ValuePolynomial;
import epmc.param.value.polynomialfraction.ValuePolynomialFraction;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueReal;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorEvaluatePolynomialFractionReal implements OperatorEvaluator {
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
        public EvaluatorEvaluatePolynomialFractionReal build() {
            assert operator != null;
            assert types != null;
            for (Type type : types) {
                assert type != null;
            }
            if (!(operator instanceof ValuePolynomialFraction)) {
                return null;
            }
            ValuePolynomialFraction operator = (ValuePolynomialFraction) this.operator;
            int numParameters = operator.getType().getParameterSet().getNumParameters();
            if (types.length != numParameters) {
                return null;
            }
            for (Type type : types) {
                if (!TypeReal.is(type)) {
                    return null;
                }
            }
            return new EvaluatorEvaluatePolynomialFractionReal(this);
        }
        
    }
    
    private final OperatorEvaluator divideReal;
    private final OperatorEvaluator evalNum;
    private final OperatorEvaluator evalDen;
    private final ValueReal resultNum;
    private final ValueReal resultDen;
    private final TypeReal resultType;
    
    private EvaluatorEvaluatePolynomialFractionReal(Builder builder) {
        assert builder != null;
        assert builder.operator != null;
        assert builder.operator instanceof ValuePolynomialFraction;
        ValuePolynomialFraction operator = (ValuePolynomialFraction) builder.operator;
        operator.adjustNumParameters();
        ValuePolynomial num = operator.getNumerator();
        ValuePolynomial den = operator.getDenominator();
        evalNum = ContextValue.get().getEvaluator(num, builder.types);
        evalDen = ContextValue.get().getEvaluator(den, builder.types);
        resultType = TypeReal.as(UtilValue.upper(builder.types));
        resultNum = resultType.newValue();
        resultDen = resultType.newValue();
        divideReal = ContextValue.get()
                .getEvaluator(OperatorDivide.DIVIDE,
                        resultType, resultType);
    }

    @Override
    public Type resultType() {
        return resultType;
    }

    @Override
    public void apply(Value result, Value... operands) {
        evalNum.apply(resultNum, operands);
        evalDen.apply(resultDen, operands);
        divideReal.apply(result, resultNum, resultDen);
    }
}
