package epmc.param.operatorevaluator.dag;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.param.value.dag.TypeDag;
import epmc.param.value.dag.ValueDag;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorSetDagDag implements OperatorEvaluator {
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
            if (operator != OperatorSet.SET) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            for (Type type : types) {
                if (!TypeDag.is(type)) {
                    return null;
                }                
            }
            return new EvaluatorSetDagDag(this);
        }
    }

    private final TypeDag typeDag;
    
    private EvaluatorSetDagDag(Builder builder) {
        assert builder != null;
        typeDag = TypeDag.as(builder.types[0]);
    }

    @Override
    public Type resultType() {
        return typeDag;
    }

    @Override
    public void apply(Value result, Value... operands) {
        ValueDag resultDag = ValueDag.as(result);
        assert ValueDag.is(operands[0]) : operands[0].getType() + " " + operands[0];
        int number = ValueDag.as(operands[0]).getNumber();
        resultDag.setNumber(number);
    }
}
