package epmc.param.operatorevaluator.dag;

import epmc.operator.Operator;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorSet;
import epmc.operator.OperatorSubtract;
import epmc.param.value.dag.Dag;
import epmc.param.value.dag.OperatorType;
import epmc.param.value.dag.TypeDag;
import epmc.param.value.dag.ValueDag;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorAddSubtractMultiplyDivideDag implements OperatorEvaluator {
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
            if (operator != OperatorAdd.ADD
                    && operator != OperatorSubtract.SUBTRACT
                    && operator != OperatorMultiply.MULTIPLY
                    && operator != OperatorDivide.DIVIDE) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            if (!TypeDag.is(types[0])
                    && !TypeDag.is(types[1])) {
                return null;
            }
            TypeDag typeDag;
            if (TypeDag.is(types[0])) {
                typeDag = TypeDag.as(types[0]);
            } else {
                typeDag = TypeDag.as(types[1]);
            }
            for (Type type : types) {
                if (!TypeDag.is(type)
                        && ContextValue.get().getEvaluatorOrNull(OperatorSet.SET, type, typeDag) == null) {
                    return null;
                }
            }
            if (!TypeDag.is(types[0])
                    && !TypeDag.is(types[1])) {
                return null;
            }
            return new EvaluatorAddSubtractMultiplyDivideDag(this);
        }
    }

    private static final int NUM_IMPORT_VALUES = 2;
    private final ValueDag importOperands[] = new ValueDag[NUM_IMPORT_VALUES];
    private final OperatorEvaluator importSet[] = new OperatorEvaluator[NUM_IMPORT_VALUES];
    private final TypeDag typeDag;
    private final Operator operator;

    private EvaluatorAddSubtractMultiplyDivideDag(Builder builder) {
        assert builder != null;
        TypeDag typeDag;
        if (TypeDag.is(builder.types[0])) {
            typeDag = TypeDag.as(builder.types[0]);
        } else {
            typeDag = TypeDag.as(builder.types[1]);
        }
        this.typeDag = typeDag;
        importOperands[0] = typeDag.newValue();
        importOperands[1] = typeDag.newValue();
        if (!TypeDag.is(builder.types[0])) {
            importSet[0] = ContextValue.get().getEvaluator(OperatorSet.SET, builder.types[0], typeDag);
        }
        if (!TypeDag.is(builder.types[1])) {
            importSet[1] = ContextValue.get().getEvaluator(OperatorSet.SET, builder.types[1], typeDag);
        }
        operator = builder.operator;
    }

    @Override
    public Type resultType() {
        return typeDag;
    }

    @Override
    public void apply(Value result, Value... operands) {
        ValueDag resultDag = ValueDag.as(result);
        int op1 = castOrImport(operands[0], 0).getNumber();
        int op2 = castOrImport(operands[1], 1).getNumber();
        Dag dag = typeDag.getDag();
        if (operator == OperatorAdd.ADD) {
            resultDag.setNumber(dag.apply(OperatorType.ADD, op1, op2));
        } else if (operator == OperatorSubtract.SUBTRACT) {
            int mOp2 = dag.apply(OperatorType.ADD_INVERSE, op2);
            resultDag.setNumber(dag.apply(OperatorType.ADD, op1, mOp2));
        } else if (operator == OperatorMultiply.MULTIPLY) {
            resultDag.setNumber(dag.apply(OperatorType.MULTIPLY, op1, op2));            
        } else if (operator == OperatorDivide.DIVIDE) {
            int mOp2 = dag.apply(OperatorType.MULTIPLY_INVERSE, op2);
            resultDag.setNumber(dag.apply(OperatorType.MULTIPLY, op1, mOp2));            
        } else {
            assert false;
        }
    }
    
    private ValueDag castOrImport(Value operand, int number) {
        assert operand != null;
        assert number >= 0;
        assert number < NUM_IMPORT_VALUES;
        if (importSet[number] == null) {
            ValueDag result = ValueDag.as(operand);
            return result;
        } else {
            importSet[number].apply(importOperands[number], operand);
            return importOperands[number];
        }
    }
}
