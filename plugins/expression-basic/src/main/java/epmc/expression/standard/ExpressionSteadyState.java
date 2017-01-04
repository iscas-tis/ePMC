package epmc.expression.standard;

import static epmc.error.UtilError.ensure;

import java.util.Collections;
import java.util.List;

import epmc.value.TypeBoolean;
import epmc.value.TypeWeight;
import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.value.ContextValue;
import epmc.value.Type;

/**
 * @author Ernst Moritz Hahn
 */
public final class ExpressionSteadyState implements Expression {
    public final static class Builder {
        private Positional positional;
        private Expression states;

        public Builder setPositional(Positional positional) {
            this.positional = positional;
            return this;
        }
        
        private Positional getPositional() {
            return positional;
        }
        
        public Builder setStates(Expression states) {
            this.states = states;
            return this;
        }
        
        private Expression getStates() {
            return states;
        }
        
        public ExpressionSteadyState build() {
            return new ExpressionSteadyState(this);
        }
    }
    
    private final Positional positional;
    private final Expression operand;

    private ExpressionSteadyState(Builder builder) {
        assert builder != null;
        assert builder.getStates() != null;
        this.positional = builder.getPositional();
        this.operand = builder.getStates();
    }

    @Override
    public Expression replaceChildren(List<Expression> children) {
        assert children.size() == 1;
        return new Builder()
                .setStates(children.get(0))
                .setPositional(positional)
                .build();
    }
    
    @Override
    public Type getType(ExpressionToType expressionToType) throws EPMCException {
    	assert expressionToType != null;
    	ContextValue contextValue = expressionToType.getContextValue();
        Type result = expressionToType.getType(this);
        if (result != null) {
            return result;
        }
        ensure(TypeBoolean.isBoolean(operand.getType(expressionToType)),
                ProblemsExpression.EXPR_INCONSISTENT, "", operand);
        return TypeWeight.get(contextValue);
    }
    
    public Expression getOperand1() {
        return operand;
    }
    
    @Override
    public List<Expression> getChildren() {
        return Collections.singletonList(operand);
    }

    @Override
    public Positional getPositional() {
        return positional;
    }    
    
    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(operand);
        if (getPositional() != null) {
            builder.append(" (" + getPositional() + ")");
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Expression other = (Expression) obj;
        List<Expression> thisChildren = this.getChildren();
        List<Expression> otherChildren = other.getChildren();
        if (thisChildren.size() != otherChildren.size()) {
            return false;
        }
        for (int entry = 0; entry < thisChildren.size(); entry++) {
            if (!thisChildren.get(entry).equals(otherChildren.get(entry))) {
                return false;
            }
        }
        return true;
    }    
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        for (Expression expression : this.getChildren()) {
            assert expression != null;
            hash = expression.hashCode() + (hash << 6) + (hash << 16) - hash;
        }        
        return hash;
    }
}
