package epmc.qmc.value;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorSetSuperoperatorAlgebra implements OperatorEvaluator {
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
            if (operator != OperatorSet.SET) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            if (TypeSuperOperator.is(types[0]) || !TypeAlgebra.is(types[0])) {
                return null;
            }
            if (!TypeSuperOperator.is(types[1])) {
                return null;
            }
            TypeAlgebra typeOperand = TypeAlgebra.as(types[0]);
            TypeAlgebra typeSuperoperatorEntry = TypeComplex.get();
            OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, typeOperand, typeSuperoperatorEntry);
            if (set == null) {
                return null;
            }
            return new OperatorEvaluatorSetSuperoperatorAlgebra(this);
        }
    }

    private final Type resultType;

    private OperatorEvaluatorSetSuperoperatorAlgebra(Builder builder) {
        resultType = builder.types[1];
    }

    @Override
    public Type resultType() {
        return resultType;
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        assert operands.length >= 1;
        ValueSuperOperator resultSuperoperator = ValueSuperOperator.as(result);
        ValueAlgebra operandAlgebra = ValueAlgebra.as(operands[0]);
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, operandAlgebra.getType(), TypeMatrix.get(TypeComplex.get()));
        set.apply(resultSuperoperator.getMatrix(), operandAlgebra);
    }
}
