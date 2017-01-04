package epmc.automaton;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.util.BitSet;

public interface BuechiTransition {
    Expression getExpression();
    
    BitSet getLabeling();

    boolean guardFulfilled() throws EPMCException;
}
