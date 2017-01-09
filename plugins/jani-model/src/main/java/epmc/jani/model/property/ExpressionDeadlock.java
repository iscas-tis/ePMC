package epmc.jani.model.property;

import java.util.Collections;
import java.util.List;

import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.value.Type;
import epmc.value.TypeBoolean;

public final class ExpressionDeadlock implements Expression {
	private final static String DEADLOCK = "\"deadlock\"";
    private final Positional positional;
    
    public ExpressionDeadlock(Positional positional) {
        this.positional = positional;
    }

    @Override
    public Expression replaceChildren(List<Expression> children) {
        return new ExpressionDeadlock(positional);
    }

    @Override
    public Type getType(ExpressionToType expressionToType) throws EPMCException {
    	assert expressionToType != null;
        Type result = expressionToType.getType(this);
        if (result != null) {
            return result;
        }
    	return TypeBoolean.get(expressionToType.getContextValue());
    }
    
    @Override
    public List<Expression> getChildren() {
    	return Collections.emptyList();
    }

    @Override
    public Positional getPositional() {
        return positional;
    }
    
    @Override
    public final String toString() {
    	return DEADLOCK;
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }    
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        
        return hash;
    }
}
