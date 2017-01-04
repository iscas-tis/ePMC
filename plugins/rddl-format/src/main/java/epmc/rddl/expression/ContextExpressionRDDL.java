package epmc.rddl.expression;

import java.util.List;

import epmc.expression.Expression;
import epmc.rddl.value.ContextValueRDDL;
import epmc.rddl.value.TypeRDDLObject;
import epmc.value.ContextValue;
import epmc.value.Operator;

public final class ContextExpressionRDDL {
    private final ContextValueRDDL contextValueRDDL;

    public ContextExpressionRDDL(ContextValueRDDL contextValueRDDL) {
        assert contextValueRDDL != null;
        this.contextValueRDDL = contextValueRDDL;
    }
    
    public ContextValueRDDL getContextValueRDDL() {
        return contextValueRDDL;
    }
    
    public ContextValue getContextValue() {
        return contextValueRDDL.getContextValue();
    }
    
    public ExpressionRDDLQuantifier newExpressionQuantifier(
            Operator quantifierType,
            List<Expression> parameters,
            List<TypeRDDLObject> ranges,
            Expression initialValue,
            Expression over) {
        return new ExpressionRDDLQuantifier(this, quantifierType,
                parameters, ranges, initialValue, over, null);
    }
    
    public ExpressionRDDLQuantifiedIdentifier newExpressionQuantifiedIdentifier(
            Expression name, List<Expression> parameters) {
        return new ExpressionRDDLQuantifiedIdentifier(this, name, parameters, null);        
    }
}
