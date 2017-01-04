package epmc.jani.model.property;

import java.util.Collections;
import java.util.List;

import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.value.Type;
import epmc.value.TypeBoolean;

// TODO check whether it's really a good idea to mark this as identifier
public final class ExpressionInitial implements ExpressionIdentifier {
	private final static String INITIAL = "\"initial\"";
    private final Positional positional;
    
    public ExpressionInitial(Positional positional) {
        this.positional = positional;
    }
    
    public static ExpressionInitial getExpressionInitial() {
    	return new ExpressionInitial(null);
    }

    @Override
    public Expression replaceChildren(List<Expression> children) {
        return new ExpressionInitial(positional);
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
    	return INITIAL;
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
    
    public String getName() {
    	return INITIAL;
    }
}
