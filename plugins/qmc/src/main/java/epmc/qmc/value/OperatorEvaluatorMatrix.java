package epmc.qmc.value;

import java.util.Arrays;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.qmc.operator.OperatorMatrix;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueInteger;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorMatrix implements OperatorEvaluator {
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
            if (operator != OperatorMatrix.MATRIX) {
                return null;
            }
            if (types.length < 1) {
                return null;
            }
            return new OperatorEvaluatorMatrix(this);
        }
    }

    private final TypeMatrix resultType;
    private final Value entryAcc1;
    private final OperatorEvaluator[] set;

    private OperatorEvaluatorMatrix(Builder builder) {
        Type[] upTypes = Arrays.copyOfRange(builder.types, 2, builder.types.length);
        TypeAlgebra upper = (TypeAlgebra) UtilValue.upper(upTypes);
        resultType = TypeMatrix.get(upper);
        entryAcc1 = resultType.getEntryType().newValue();
        set = new OperatorEvaluator[builder.types.length - 2];
        for (int index = 0; index < builder.types.length - 2; index++) {
            set[index] = ContextValue.get().getEvaluator(OperatorSet.SET,
                    builder.types[index + 2],
                    entryAcc1.getType());
        }
    }

    @Override
    public Type resultType() {
        return resultType;
    }

    @Override
    public void apply(Value result, Value... values) {
        assert values.length > 0;
        int numRows = ValueInteger.as(values[0]).getInt();
        int numCols = ValueInteger.as(values[1]).getInt();
        ValueMatrix resultMatrix = ValueMatrix.as(result);
        resultMatrix.setDimensions(numRows, numCols);
        int index = 0;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                Value entry = values[2 + index];
                set[index].apply(entryAcc1, entry);
                resultMatrix.set(entryAcc1, row, col);
                index++;
            }
        }
    }
}
