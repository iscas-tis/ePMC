package epmc.qmc.value;

import epmc.operator.Operator;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorSet;
import epmc.qmc.operator.OperatorQeval;
import epmc.qmc.operator.OperatorQprob;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArray;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueReal;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorQprob implements OperatorEvaluator {
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
            if (operator != OperatorQprob.QPROB) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            if (!(types[0] instanceof TypeSuperOperator)
                    && !TypeAlgebra.is(types[0])) {
                return null;
            }
            if (!TypeArray.is(types[1])
                    && !TypeAlgebra.is(types[1])) {
                return null;
            }
            return new OperatorEvaluatorQprob(this);
        }
    }

    private OperatorEvaluatorQprob(Builder builder) {
    }

    @Override
    public Type resultType() {
        return TypeReal.get();
    }

    @Override
    public void apply(Value result, Value... operands) {
        Value superoperator = operands[0];
        Value operator = operands[1];
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
        if (result == superoperator || result == operator) {
            ValueMatrix resultBuffer = ValueMatrix.as(result.getType().newValue());
            apply(resultBuffer, superoperator, operator);
            set.apply(result, resultBuffer);
            return;
        }
        TypeReal typeReal = TypeReal.get();
        ValueMatrix qprobQevalResult = TypeMatrix.get(TypeComplex.get()).newValue();
        ValueReal qprobAcc = typeReal.newValue();

        OperatorEvaluator evaluator = ContextValue.get().getEvaluator(OperatorQeval.QEVAL, superoperator.getType(), operator.getType());
        evaluator.apply(qprobQevalResult, superoperator, operator);
        Value[] eigen = Eigen.eigenvalues(qprobQevalResult);
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, result.getType(), eigen[0].getType());
        set.apply(result, UtilValue.newValue(typeReal, 0));
        // TODO check!
        for (Value entry : eigen) {
            add.apply(qprobAcc, result, entry);
            set.apply(result, qprobAcc);
        }        
    }
}
