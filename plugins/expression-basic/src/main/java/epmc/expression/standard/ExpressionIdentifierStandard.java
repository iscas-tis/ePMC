package epmc.expression.standard;

import java.util.Collections;
import java.util.List;

import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.value.Type;

public final class ExpressionIdentifierStandard implements ExpressionIdentifier {
	public static boolean isIdentifierStandard(Expression expression) {
		return expression instanceof ExpressionIdentifierStandard;
	}
	
	public static ExpressionIdentifierStandard asIdentifierStandard(Expression expression) {
		if (isIdentifierStandard(expression)) {
			return (ExpressionIdentifierStandard) expression;
		} else {
			return null;
		}
	}	
	
    public final static class Builder {
        private Positional positional;
        private String name;
        private Object scope;

        public Builder setPositional(Positional positional) {
            this.positional = positional;
            return this;
        }
        
        private Positional getPositional() {
            return positional;
        }
        
        public Builder setName(String name) {
            this.name = name;
            return this;
        }
        
        private String getName() {
            return name;
        }
        
        public Builder setScope(Object scope) {
            this.scope = scope;
            return this;
        }
        
        private Object getScope() {
            return scope;
        }
        
        public ExpressionIdentifierStandard build() {
            return new ExpressionIdentifierStandard(this);
        }
    }
    
    private final Positional positional;
    private final String name;
    private final Object scope;

    private ExpressionIdentifierStandard(Builder builder) {
        assert builder != null;
        assert builder.getName() != null;
        this.positional = builder.getPositional();
        this.name = builder.getName();
        this.scope = builder.getScope();
    }

    ExpressionIdentifierStandard(List<Expression> children,
            String name, Object scope, Positional positional) {
        this.positional = positional;
        assert name != null;
        this.name = name;
        this.scope = scope;
    }
    
    public String getName() {
        return name;
    }
    
    public Object getScope() {
        return scope;
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
    	return expressionToType.getType(this);
    }

    @Override
    public boolean isPropositional() {
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = name.hashCode() + (hash << 6) + (hash << 16) - hash;
        if (scope != null) {
            hash = scope.hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        assert obj != null;
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        ExpressionIdentifierStandard other = (ExpressionIdentifierStandard) obj;
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
        if (!this.name.equals(other.name)) {
            return false;
        }
        if ((this.scope == null) != (other.scope == null)) {
            return false;
        }
        if (this.scope != null && !this.scope.equals(other.scope)) {
            return false;
        }
        
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
        builder.append(name);
        if (getPositional() != null) {
            builder.append(" (" + getPositional() + ")");
        }
        if (scope != null) {
            builder.append(" in ");
            builder.append(scope.toString());
        }
        return builder.toString();        
    }
}
