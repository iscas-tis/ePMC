package epmc.param.operatorevaluator.dag;

import epmc.operator.Operator;
import epmc.operator.OperatorAddInverse;
import epmc.operator.OperatorMultiplyInverse;
import epmc.param.value.dag.Dag;
import epmc.param.value.dag.OperatorType;
import epmc.param.value.dag.TypeDag;
import epmc.param.value.dag.ValueDag;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorAddInverseMultiplyInverseDag implements OperatorEvaluator {
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
            if (operator != OperatorAddInverse.ADD_INVERSE
                    && operator != OperatorMultiplyInverse.MULTIPLY_INVERSE) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            for (Type type : types) {
                if (!TypeDag.is(type)) {
                    return null;
                }                
            }
            return new EvaluatorAddInverseMultiplyInverseDag(this);
        }
    }

    private final TypeDag typeDag;
    private final Operator operator;

    private EvaluatorAddInverseMultiplyInverseDag(Builder builder) {
        assert builder != null;
        this.typeDag = TypeDag.as(builder.types[0]);
        operator = builder.operator;
    }

    @Override
    public Type resultType() {
        return typeDag;
    }

    @Override
    public void apply(Value result, Value... operands) {
        ValueDag resultDag = ValueDag.as(result);
        int number = ValueDag.as(operands[0]).getNumber();
        Dag dag = typeDag.getDag();
        if (operator == OperatorAddInverse.ADD_INVERSE) {
            resultDag.setNumber(dag.apply(OperatorType.ADD_INVERSE, number));
        } else if (operator == OperatorMultiplyInverse.MULTIPLY_INVERSE) {
            resultDag.setNumber(dag.apply(OperatorType.MULTIPLY_INVERSE, number));
        } else {
            assert false;
        }
    }
}
