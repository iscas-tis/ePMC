package epmc.param.value.dag;

final class EntryBuffer {
    private int resultNode;
    private OperatorType type;
    private int operandLeft;
    private int operandRight;
    
    EntryBuffer setResultNode(int resultNode) {
        this.resultNode = resultNode;
        return this;
    }
    
    EntryBuffer setType(OperatorType type) {
        this.type = type;
        return this;
    }
    
    EntryBuffer setOperandLeft(int operandLeft) {
        this.operandLeft = operandLeft;
        return this;
    }
    
    EntryBuffer setOperandRight(int operandRight) {
        this.operandRight = operandRight;
        return this;
    }

    int getResultNode() {
        return resultNode;
    }
    
    OperatorType getType() {
        return type;
    }
    
    int getOperand() {
        assert operandRight == 0;
        return operandLeft;
    }
    
    int getOperandLeft() {
        return operandLeft;
    }
    
    int getOperandRight() {
        return operandRight;
    }
}
