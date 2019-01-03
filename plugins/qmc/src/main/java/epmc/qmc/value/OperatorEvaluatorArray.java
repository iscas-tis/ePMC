package epmc.qmc.value;

import java.util.Arrays;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.qmc.operator.OperatorArray;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeInteger;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueInteger;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorArray implements OperatorEvaluator {
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
            if (operator != OperatorArray.ARRAY) {
                return null;
            }
            if (types.length < 1) {
                return null;
            }
            if (!TypeInteger.is(types[0])) {
                return null;
            }
            return new OperatorEvaluatorArray(this);
        }
    }

    private final TypeArray resultType;
    private final Value entryAcc1;
    private final OperatorEvaluator set;

    private OperatorEvaluatorArray(Builder builder) {
        Type[] upTypes = Arrays.copyOfRange(builder.types, 1, builder.types.length);
        resultType = UtilValue.upper(upTypes).getTypeArray();
        entryAcc1 = resultType.getEntryType().newValue();
        set = ContextValue.get().getEvaluator(OperatorSet.SET, builder.types[1], resultType.getEntryType());
    }

    @Override
    public Type resultType() {
        return resultType;
    }

    @Override
    public void apply(Value result, Value... values) {
        assert values != null;
        assert values.length > 0;
        int size = ValueInteger.as(values[0]).getInt();
        ValueArray resultArray = ValueArray.as(result);
        resultArray.setSize(size);
        for (int entryNr = 0; entryNr < size; entryNr++) {
            Value entry = values[1 + entryNr];
            set.apply(entryAcc1, entry);
            resultArray.set(entryAcc1, entryNr);
        }
    }
}
