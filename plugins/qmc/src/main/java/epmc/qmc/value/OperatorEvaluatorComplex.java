package epmc.qmc.value;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.qmc.operator.OperatorComplex;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorComplex implements OperatorEvaluator {
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
            if (operator != OperatorComplex.COMPLEX) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            return new OperatorEvaluatorComplex(this);
        }
    }

    private final OperatorEvaluator set1;
    private final OperatorEvaluator set2;
    
    private OperatorEvaluatorComplex(Builder builder) {
        set1 = ContextValue.get().getEvaluator(OperatorSet.SET, builder.types[0], TypeReal.get());
        set2 = ContextValue.get().getEvaluator(OperatorSet.SET, builder.types[0], TypeReal.get());
    }

    @Override
    public Type resultType() {
        return TypeComplex.get();
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        assert operands.length >= 2;
        assert operands[0] != null;
        assert operands[1] != null;
        ValueComplex complexResult = ValueComplex.as(result);
        set1.apply(complexResult.getRealPart(), operands[0]);
        set2.apply(complexResult.getImagPart(), operands[1]);
    }
}
