package epmc.param.operatorevaluator.dag;

import epmc.operator.Operator;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorPow;
import epmc.operator.OperatorSet;
import epmc.param.value.dag.TypeDag;
import epmc.param.value.dag.ValueDag;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.ValueInteger;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorPowDagInt implements OperatorEvaluator {
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
            assert types != null;
            assert operator != null;
            if (operator != OperatorPow.POW) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            if (!TypeDag.is(types[0])) {
                return null;
            }
            if (!TypeInteger.is(types[1])) {
                return null;
            }
            return new EvaluatorPowDagInt(this);
        }
        
    }

    private final OperatorEvaluator multiply;
    private final OperatorEvaluator set;
    private final ValueDag resultDag;
    private final TypeDag typeDag;

    private EvaluatorPowDagInt(Builder builder) {
        assert builder != null;
        typeDag = TypeDag.as(builder.types[0]);
        resultDag = typeDag.newValue();
        multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, typeDag, typeDag);
        set = ContextValue.get().getEvaluator(OperatorSet.SET, typeDag, typeDag);
    }

    @Override
    public Type resultType() {
        return typeDag;
    }

    @Override
    public void apply(Value result, Value... operands) {
        ValueDag operand1 = ValueDag.as(operands[0]);
        ValueInteger operand2 = ValueInteger.as(operands[1]);
        int op2Int = operand2.getInt();
        assert op2Int >= 0 : op2Int; // TODO could support negative values
        resultDag.set(1);
        for (int i = 0; i < op2Int; i++) {
            multiply.apply(resultDag, resultDag, operand1);
        }
        set.apply(result, resultDag);
    }
}
