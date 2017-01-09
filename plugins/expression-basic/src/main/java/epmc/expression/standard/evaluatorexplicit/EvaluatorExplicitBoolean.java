package epmc.expression.standard.evaluatorexplicit;

import epmc.error.EPMCException;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.value.Value;

public interface EvaluatorExplicitBoolean extends EvaluatorExplicit {
    public interface Builder extends EvaluatorExplicit.Builder {
        @Override
        EvaluatorExplicitBoolean build() throws EPMCException;
    }
    
    boolean evaluateBoolean(Value... values) throws EPMCException;
}
