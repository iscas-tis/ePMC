package epmc.qmc.value;

import epmc.operator.Operator;
import epmc.qmc.operator.OperatorConjugate;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorConjugate implements OperatorEvaluator {
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
            if (operator != OperatorConjugate.CONJUGATE) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            Type upper = upper(types);
            if (!TypeAlgebra.is(upper)) {
                return null;
            }
            return new OperatorEvaluatorConjugate(this);
        }
    }

    private final Type resultType;

    private OperatorEvaluatorConjugate(Builder builder) {
        resultType = upper(builder.types);
    }

    @Override
    public Type resultType() {
        return resultType;
    }

    @Override
    public void apply(Value result, Value... operands) {
        UtilValueQMC.conjugate(result, operands[0]);
    }

    private static Type upper(Type... types) {
        Type upper = types[0];
        for (Type type : types) {
            if (upper != null) {
                upper = UtilValue.upper(upper, type);
            }
        }
        return upper;
    }
}
