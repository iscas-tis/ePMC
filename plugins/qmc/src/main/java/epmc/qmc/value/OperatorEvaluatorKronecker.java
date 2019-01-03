package epmc.qmc.value;

import epmc.operator.Operator;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorSet;
import epmc.qmc.operator.OperatorKronecker;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;
import epmc.value.ValueAlgebra;

public final class OperatorEvaluatorKronecker implements OperatorEvaluator {
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
            if (operator != OperatorKronecker.KRONECKER) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            // TODO
            return new OperatorEvaluatorKronecker(this);
        }
    }

    private final TypeMatrix resultType;
    private final OperatorEvaluator multiplyNonMat;
    private final ValueMatrix resultBuffer;
    private final OperatorEvaluator set;
    private final OperatorEvaluator multiply;
    
    private OperatorEvaluatorKronecker(Builder builder) {
        resultType = TypeMatrix.get(TypeComplex.get());
        multiplyNonMat = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, builder.types[0], builder.types[1]);
        resultBuffer = resultType.newValue();
        set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeMatrix.get(TypeComplex.get()), TypeMatrix.get(TypeComplex.get()));
        multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeComplex.get(), TypeComplex.get());
    }

    @Override
    public Type resultType() {
        return resultType;
    }

    @Override
    public void apply(Value result, Value... operands) {
        if (!ValueMatrix.is(result)) {
            multiplyNonMat.apply(result, operands[0], operands[1]);
            return;
        }
        Value op1 = operands[0];
        Value op2 = operands[1];
        ValueMatrix op1Matrix = UtilValueQMC.castOrImport(ValueMatrix.as(result), op1, 0, true);
        ValueMatrix op2Matrix = UtilValueQMC.castOrImport(ValueMatrix.as(result), op2, 1, true);
        if (result == op1 || result == op2) {
            apply(resultBuffer, op1Matrix, op2Matrix);
            set.apply(result, resultBuffer);
            return;
        }
        ValueAlgebra entryAcc1 = TypeComplex.get().newValue();
        ValueAlgebra entryAcc2 = TypeComplex.get().newValue();
        ValueAlgebra entryAcc3 = TypeComplex.get().newValue();
        ValueMatrix resultMatrix = ValueMatrix.as(result);
        resultMatrix.setDimensions(op1Matrix.getNumRows() * op2Matrix.getNumRows(), op1Matrix.getNumColumns() * op2Matrix.getNumColumns());
        for (int row = 0; row < resultMatrix.getNumRows(); row++) {
            for (int column = 0; column < resultMatrix.getNumColumns(); column++) {
                op1Matrix.get(entryAcc1, row / op2Matrix.getNumRows(), column / op2Matrix.getNumColumns());
                op2Matrix.get(entryAcc2, row % op2Matrix.getNumRows(), column % op2Matrix.getNumColumns());
                multiply.apply(entryAcc3, entryAcc1, entryAcc2);
                resultMatrix.set(entryAcc3, row, column);
            }
        }
    }
}
