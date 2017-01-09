package epmc.expression.standard.evaluatorexplicit;

import epmc.error.EPMCException;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.value.Value;

public interface EvaluatorExplicitInteger extends EvaluatorExplicit {
    public interface Builder extends EvaluatorExplicit.Builder {
        @Override
        EvaluatorExplicitInteger build() throws EPMCException;
    }
    
    int evaluateInteger(Value... values) throws EPMCException;
}
