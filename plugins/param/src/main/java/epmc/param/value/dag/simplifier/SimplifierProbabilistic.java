package epmc.param.value.dag.simplifier;

import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.param.options.OptionsParam;
import epmc.param.value.dag.Dag;
import epmc.param.value.dag.OperatorType;

public final class SimplifierProbabilistic implements Simplifier {
    public final static class Builder implements Simplifier.Builder {
        private Dag dag;

        @Override
        public Builder setDag(Dag dag) {
            this.dag = dag;
            return this;
        }

        @Override
        public Simplifier build() {
            return new SimplifierProbabilistic(this);
        }
    }

    // TODO use
    // https://en.wikipedia.org/wiki/Schwartz%E2%80%93Zippel_lemma
    // to choose required number of bits

    private final static int INVALID = -1;
    
    private final Dag dag;
    private OperatorType type;
    private int operandLeft;
    private int operandRight;
    
    // TODO allow adding more lists of number to verify equality

    private final Evaluator evaluator;
    
    private OperatorType resultType;
    private int resultLeftOperand;
    private int resultRightOperand;

    private boolean probEval;

    private int resultNode;

    private SimplifierProbabilistic(Builder builder) {
        assert builder != null;
        assert builder.dag != null;
        dag = builder.dag;
        probEval = Options.get().get(OptionsParam.PARAM_DAG_USE_PROB_SIMPLIFIER);
        
        Evaluator.Builder evBuilder = UtilOptions.getInstance(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_NUMBER_TYPE);
        evaluator = evBuilder.setDag(dag).build();
    }

    @Override
    public void setType(OperatorType type) {
        this.type = type;
    }
    
    @Override
    public void setOperandLeft(int operandLeft) {
        this.operandLeft = operandLeft;
    }

    @Override
    public void setOperandRight(int operandRight) {
        this.operandRight = operandRight;
    }

    @Override
    public boolean simplify() {
        if (!probEval) {
            return false;
        }
        this.resultNode = evaluator.evaluate(type, operandLeft, operandRight);
        if (resultNode == INVALID) {
            return false;
        }
        setResultToOperand(resultNode);
        return true;
    }
        
    @Override
    public void tellNewEntry(int number) {
        if (!probEval) {
            return;
        }
        boolean changed = loadEntry(number);
        if (changed) {
            evaluator.evaluate(type, operandLeft, operandRight);
        }
        evaluator.commitResult();
    }

    private boolean loadEntry(int number) {
        boolean changed = false;
        OperatorType type = dag.getOperatorType(number);
        if (this.type != type) {
            changed = true;
            this.type = type;
        }
        
        switch (type) {
        case ADD_INVERSE:
            int addInverseOperand = dag.getOperand(number);
            if (this.operandLeft != addInverseOperand) {
                changed = true;
                this.operandLeft = addInverseOperand;
            }
            break;
        case MULTIPLY_INVERSE:
            int multiplyInverseOperand = dag.getOperand(number);
            if (this.operandLeft != multiplyInverseOperand) {
                changed = true;
                this.operandLeft = multiplyInverseOperand;
            }
            break;
        case ADD:
            int addLeftOperand = dag.getOperandLeft(number);
            int addRightOperand = dag.getOperandRight(number);
            if (this.operandLeft != addLeftOperand) {
                changed = true;
                this.operandLeft = addLeftOperand;
            }
            if (this.operandRight != addRightOperand) {
                changed = true;
                this.operandRight = addRightOperand;
            }
            break;
        case MULTIPLY:
            int multiplyLeftOperand = dag.getOperandLeft(number);
            int multiplyRightOperand = dag.getOperandRight(number);
            if (this.operandLeft != multiplyLeftOperand) {
                changed = true;
                this.operandLeft = multiplyLeftOperand;
            }
            if (this.operandRight != multiplyRightOperand) {
                changed = true;
                this.operandRight = multiplyRightOperand;
            }
            break;
        case NUMBER:
            int num = dag.getOperandLeft(number);
            int den = dag.getOperandRight(number);
            if (this.operandLeft != num) {
                changed = true;
                this.operandLeft = num;
            }
            if (this.operandRight != den) {
                changed = true;
                this.operandRight = den;
            }
            break;
        case PARAMETER:
            int parameter = dag.getParameterNumber(number);
            if (operandLeft != parameter) {
                changed = true;
                this.operandLeft = parameter;
            }
            break;
        default:
            assert false;
            break;
        }
        return changed;
    }

    private void setResultToOperand(int resultNumber) {
        resultType = dag.getOperatorType(resultNumber);
        resultLeftOperand = dag.getOperandLeft(resultNumber);
        resultRightOperand = dag.getOperandRight(resultNumber);
    }

    @Override
    public OperatorType getResultType() {
        return resultType;
    }

    @Override
    public int getResultOperandLeft() {
        return resultLeftOperand;
    }

    @Override
    public int getResultOperandRight() {
        return resultRightOperand;
    }
    
    @Override
    public int getResultNode() {
        return resultNode;
    }
}
