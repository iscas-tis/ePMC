package epmc.param.operatorevaluator.gmp;

import com.sun.jna.Memory;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.param.points.Side;
import epmc.param.points.UtilPoints;
import epmc.param.value.gmp.GMP;
import epmc.param.value.gmp.MPFRSingleMemory;
//import epmc.param.value.gmp.GMP;
import epmc.param.value.gmp.TypeMPQ;
import epmc.param.value.gmp.ValueMPQ;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeDouble;
import epmc.value.TypeInterval;
import epmc.value.Value;
import epmc.value.ValueDouble;
import epmc.value.ValueInterval;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorSetMPQDoubleFraction implements OperatorEvaluator {
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
            if (!TypeInterval.is(types[0])) {
                return null;
            }
            if (!TypeMPQ.is(TypeInterval.as(types[0]).getEntryType())) {
                return null;
            }
            if (!TypeInterval.is(types[1])) {
                return null;
            }
            if (!TypeDouble.is(TypeInterval.as(types[1]).getEntryType())) {
                return null;
            }
            return new EvaluatorSetMPQDoubleFraction(this);
        }
    }

    private final TypeInterval resultType;
    private final Memory resultMemory;
    private final MPFRSingleMemory buffer;
    
    private EvaluatorSetMPQDoubleFraction(Builder builder) {
        assert builder != null;
        assert builder.operator != null;
        assert builder.types != null;
        resultType = TypeInterval.as(builder.types[1]);
        resultMemory = new Memory(GMP.MPQ_T_SIZE * 2);
        buffer = new MPFRSingleMemory();
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
        ValueInterval valueInterval = ValueInterval.as(operands[0]);
        ValueInterval resultInterval = ValueInterval.as(result);
        ValueMPQ valueLowerRational = ValueMPQ.as(valueInterval.getIntervalLower());
        ValueMPQ valueUpperRational = ValueMPQ.as(valueInterval.getIntervalUpper());
        ValueDouble resultLowerDouble = ValueDouble.as(resultInterval.getIntervalLower());
        ValueDouble resultUpperDouble = ValueDouble.as(resultInterval.getIntervalUpper());
        GMP.gmp_util_mpq_interval_to_double_interval(resultMemory, valueLowerRational.getContent(), valueUpperRational.getContent(), buffer);
        resultLowerDouble.set(resultMemory.getDouble(0));
        resultUpperDouble.set(resultMemory.getDouble(Double.BYTES));
    }
}
