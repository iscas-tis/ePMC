package epmc.expression.standard;

import java.util.ArrayList;
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

public final class ExpressionMultiObjective implements Expression {
    public final static class Builder {
        private Positional positional;
        private List<Expression> operands;

        public Builder setPositional(Positional positional) {
            this.positional = positional;
            return this;
        }
        
        private Positional getPositional() {
            return positional;
        }

        public Builder setOperands(List<Expression> operands) {
            this.operands = operands;
            return this;
        }
        
        private List<Expression> getOperands() {
            return operands;
        }
        
        public ExpressionMultiObjective build() {
            return new ExpressionMultiObjective(this);
        }
    }
    
    private final Positional positional;
    private final List<Expression> children = new ArrayList<>();

    private ExpressionMultiObjective(Builder builder) {
        assert builder != null;
        assert builder.getOperands() != null;
        for (Expression operand : builder.getOperands()) {
            assert operand != null;
            // TODO
//          assert child instanceof ExpressionQuantifier : child.getClass();

        }
        this.children.addAll(builder.getOperands());
        this.positional = builder.getPositional();
    }
    
    public Expression getOperand1() {
        return getChildren().get(0);
    }

    public Expression getOperand2() {
        return getChildren().get(1);
    }

    public Expression getOperand3() {
        return getChildren().get(2);
    }

    public List<Expression> getOperands() {
        List<Expression> operands = new ArrayList<>();
        for (Expression child : getChildren()) {
            operands.add(child);
        }
        return Collections.unmodifiableList(operands);
    }
    
    @Override
    public Expression replaceChildren(List<Expression> children) {
        return new Builder()
                .setOperands(children)
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
        Expression op1 = getOperand1();
        if (isQuantEq(op1)) {
            return TypeWeight.get(contextValue);
        } else {
            return TypeBoolean.get(contextValue);
        }
    }
    
    @Override
    public List<Expression> getChildren() {
        return children;
    }

    @Override
    public Positional getPositional() {
        return positional;
    }
    
    @Override
    public final String toString() {
        List<Expression> children = getChildren();
        StringBuilder builder = new StringBuilder();
        builder.append("multi(");
        for (int i = 0; i < children.size(); i++) {
            builder.append(children.get(i));
            if (i < children.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append(")");
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
    
    private boolean isQuantEq(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionQuantifier)) {
            return false;
        }
        ExpressionQuantifier expressionQuantifier = (ExpressionQuantifier) expression;
        return expressionQuantifier.getCompareType().isEq();
    }
}
