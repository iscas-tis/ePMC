package epmc.automaton;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.util.BitSet;
import epmc.value.ValueBoolean;

final class BuechiTransitionImpl implements BuechiTransition {
    private final Expression expression;
    private final BitSet labeling;
    private ValueBoolean result;
    
    BuechiTransitionImpl(Expression expression, BitSet labeling) {
        this.expression = expression;
        this.labeling = labeling;
    }
    
    @Override
    public Expression getExpression() {
        return expression;
    }
    
    @Override
    public BitSet getLabeling() {
        return labeling;
    }

    public void setResult(ValueBoolean result) {
        this.result = result;
    }
    
    @Override
    public String toString() {
        return expression + "\n" + labeling;
    }
    
    @Override
    public boolean guardFulfilled() throws EPMCException {
        return result.getBoolean();
    }
}
