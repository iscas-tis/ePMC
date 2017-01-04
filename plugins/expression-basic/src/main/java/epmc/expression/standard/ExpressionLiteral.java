package epmc.expression.standard;

import java.util.Collections;
import java.util.List;

import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.UtilValue;

/**
 * A literal, such as booleans, numeric values, etc. Basically a container for
 * Value objects.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ExpressionLiteral implements ExpressionPropositional {
	public static boolean isLiteral(Expression expression) {
		return expression instanceof ExpressionLiteral;
	}
	
	public static ExpressionLiteral asLiteral(Expression expression) {
		if (isLiteral(expression)) {
			return (ExpressionLiteral) expression;
		} else {
			return null;
		}
	}
	
    public final static class Builder {
        private Value value;
        private Positional positional;

        public Builder setValue(Value value) {
            this.value = value;
            return this;
        }
        
        private Value getValue() {
            return value;
        }
        
        public Builder setPositional(Positional positional) {
            this.positional = positional;
            return this;
        }
        
        private Positional getPositional() {
            return positional;
        }
        
        public ExpressionLiteral build() {
            return new ExpressionLiteral(this);
        }
    }
    
    private final Value value;
    private final Positional positional;

    private ExpressionLiteral(Builder builder) {
        assert builder != null;
        assert builder.getValue() != null;
        this.positional = builder.getPositional();
        if (builder.getValue() != null) {
            this.value = UtilValue.clone(builder.getValue());
        } else {
            throw new RuntimeException();
        }
        this.value.setImmutable();
    }

    public Value getValue() {
        return value;
    }
    
    @Override
    public Expression replaceChildren(List<Expression> children) {
        assert children != null;
        assert children.size() == 0;
        return this;
    }

    @Override
    public Type getType(ExpressionToType expressionToType) throws EPMCException {
    	assert expressionToType != null;
    	/*
        Type result = expressionToType.getType(this);
        if (result != null) {
            return result;
        }
        */
        return value.getType();
    }
    
    @Override
    public boolean isPropositional() {
        return true;
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
        StringBuilder builder = new StringBuilder();
        builder.append(value);
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
        ExpressionLiteral other = (ExpressionLiteral) obj;
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
        return this.value.equals(other.value);
    }    
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        for (Expression expression : this.getChildren()) {
            assert expression != null;
            hash = expression.hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        hash = value.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    /**
     * Obtain expression representing positive infinity.
     * 
     * @return expression representing positive infinity
     */
    public static ExpressionLiteral getPosInf(ContextValue context) {
        ExpressionLiteral posInfExpr = new Builder()
                .setValue(TypeReal.get(context).getPosInf())
                .build();
        return posInfExpr;
    }

    /**
     * Obtain expression representing the value &quot;1&quot;
     * 
     * @return expression representing the value &quot;1&quot;
     */
    public static Expression getOne(ContextValue context) {
        ExpressionLiteral oneExpr = new Builder()
                .setValue(TypeInteger.get(context).getOne())
                .build();
        return oneExpr;
    }

    /**
     * Obtain expression representing the value &quot;0&quot;
     * 
     * @return expression representing the value &quot;0&quot;
     */
    public static ExpressionLiteral getZero(ContextValue context) {
        ExpressionLiteral zeroExpr = new Builder()
                .setValue(TypeInteger.get(context).getZero())
                .build();
        return zeroExpr;
    }

    /**
     * Obtain expression representing the value &quot;true&quot;
     * 
     * @return expression representing the value &quot;true&quot;
     */
    public static ExpressionLiteral getTrue(ContextValue contextValue) {
        ExpressionLiteral trueExpr = new Builder()
                .setValue(TypeBoolean.get(contextValue).getTrue())
                .build();
        return trueExpr;
    }

    /**
     * Obtain expression representing the value &quot;false&quot;
     * 
     * @return expression representing the value &quot;false&quot;
     */
    public static Expression getFalse(ContextValue contextValue) {
        ExpressionLiteral falseExpr = new Builder()
                .setValue(TypeBoolean.get(contextValue).getFalse())
                .build();
        return falseExpr;
    }
}
