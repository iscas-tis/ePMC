package epmc.expression.standard;

import static epmc.error.UtilError.ensure;

import java.util.ArrayList;
import java.util.List;

import epmc.value.TypeBoolean;
import epmc.value.TypeWeight;
import epmc.value.ValueBoolean;
import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.Value;

/**
 * @author Ernst Moritz Hahn
 */
public final class ExpressionQuantifier implements Expression {
    public final static class Builder {
        private ContextValue context;
        private Positional positional;
        private DirType dirType;
        private CmpType cmpType;
        private Expression quantified;
        private Expression compare;
        private Expression condition;

        public Builder setContext(ContextValue context) {
            this.context = context;
            return this;
        }
        
        private ContextValue getContext() {
            return context;
        }
        
        public Builder setPositional(Positional positional) {
            this.positional = positional;
            return this;
        }
        
        private Positional getPositional() {
            return positional;
        }
        
        public Builder setDirType(DirType dirType) {
            this.dirType = dirType;
            return this;
        }
        
        private DirType getDirType() {
            return dirType;
        }
        
        public Builder setCmpType(CmpType cmpType) {
            this.cmpType = cmpType;
            return this;
        }
        
        private CmpType getCmpType() {
            return cmpType;
        }
        
        public Builder setQuantified(Expression quantified) {
            this.quantified = quantified;
            return this;
        }
        
        private Expression getQuantified() {
            return quantified;
        }
        
        public Builder setCompare(Expression compare) {
            this.compare = compare;
            return this;
        }
        
        private Expression getCompare() {
            return compare;
        }
        
        public Builder setCondition(Expression condition) {
            this.condition = condition;
            return this;
        }
        
        private Expression getCondition() {
            return condition;
        }
        
        public ExpressionQuantifier build() {
            return new ExpressionQuantifier(this);
        }
    }

    public static boolean isQuantifier(Expression expression) {
    	return expression instanceof ExpressionQuantifier;
    }
    
    public static ExpressionQuantifier asQuantifier(Expression expression) {
    	if (isQuantifier(expression)) {
    		return (ExpressionQuantifier) expression;
    	} else {
    		return null;
    	}
    }
    
    private final ContextValue context;
    private final Positional positional;
    private final DirType dirType;
    private final CmpType cmpType;
    private final Expression quantified;
    private final Expression compare;
    private final Expression condition;
    
    private ExpressionQuantifier(Builder builder) {
        assert builder != null;
        assert builder.getContext() != null;
        if (builder.getCompare() == null) {
            builder.setCompare(ExpressionLiteral.getTrue(builder.getContext()));
        }
        if (builder.getCondition() == null) {
            builder.setCondition(ExpressionLiteral.getTrue(builder.getContext()));
        }
        assert builder.getDirType() != null;
        assert builder.getCmpType() != null;
        assert builder.getQuantified() != null;
        assert builder.getCmpType() != CmpType.IS
                || isTrue(builder.getCompare());
        assert isTrue(builder.getCondition());
        this.context = builder.getContext();
        this.positional = builder.getPositional();
        this.dirType = builder.getDirType();
        this.cmpType = builder.getCmpType();
        this.quantified = builder.getQuantified();
        this.compare = builder.getCompare();
        this.condition = builder.getCondition();
    }

    public DirType getDirType() {
        return dirType;
    }
    
    public CmpType getCompareType() {
        return cmpType;
    }
    
    public Expression getQuantified() {
        return quantified;
    }
    
    public Expression getCompare() {
        return compare;
    }
    
    public Expression getCondition() {
        return condition;
    }
    
    @Override
    public Expression replaceChildren(List<Expression> children) {
        return new Builder()
                .setCmpType(cmpType)
                .setDirType(dirType)
                .setPositional(positional)
                .setContext(context)
                .setQuantified(children.get(0))
                .setCompare(children.get(1))
                .setCondition(children.get(2))
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
        Type booleanType = TypeBoolean.get(contextValue);
        Expression condition = getCondition();
        Type conditionType = condition.getType(expressionToType);
        ensure(conditionType == null || TypeBoolean.isBoolean(conditionType),
                ProblemsExpression.EXPR_INCONSISTENT, "", condition);
        if (cmpType == CmpType.IS) {
            return TypeWeight.get(contextValue);
        } else {
            return booleanType;
        }
    }
    
    public boolean isDirMin() {
        return dirType == DirType.MIN;
    }

    public boolean isDirNone() {
        return dirType == DirType.NONE;
    }
    
    @Override
    public List<Expression> getChildren() {
        List<Expression> result = new ArrayList<>();
        result.add(quantified);
        result.add(compare);
        result.add(condition);
        return result;
    }

    @Override
    public Positional getPositional() {
        return positional;
    }
    
    
    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        Expression rewardStructure = null;
        if (getQuantified() instanceof ExpressionSteadyState) {
            builder.append("S");
        } else if (getQuantified() instanceof ExpressionReward) {
            builder.append("R");
            rewardStructure = ((ExpressionReward) getQuantified()).getReward().getExpression();
        } else {
            builder.append("P");
        }
        
        if (rewardStructure != null && !isTrue(rewardStructure)) {
            builder.append("{" + rewardStructure + "}");
        }
        builder.append(dirType);
        builder.append(cmpType);
        if (cmpType != CmpType.IS) {
            builder.append(getCompare());
        }
        builder.append("[");
        builder.append(getQuantified());
        if (!isTrue(getCondition())) {
            builder.append(" given ");
            builder.append(getCondition());
        }
        builder.append("]");
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
        ExpressionQuantifier other = (ExpressionQuantifier) obj;
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
        return this.dirType == other.dirType && this.cmpType == other.cmpType;
    }    
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        for (Expression expression : this.getChildren()) {
            assert expression != null;
            hash = expression.hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        hash = dirType.hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = cmpType.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
    
    private static boolean isTrue(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return ValueBoolean.isTrue(getValue(expressionLiteral));
    }
    
    private static Value getValue(Expression expression) {
        assert expression != null;
        assert expression instanceof ExpressionLiteral;
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return expressionLiteral.getValue();
    }

    public static boolean isDirTypeMin(ExpressionQuantifier expression) {
        assert expression != null;
        return computeQuantifierDirType(expression) == DirType.MIN;
    }

    public static DirType computeQuantifierDirType(ExpressionQuantifier expression) {
        assert expression != null;
        DirType dirType = expression.getDirType();
        if (dirType == DirType.NONE) {
            switch (expression.getCompareType()) {
            case IS: case EQ: case NE:
                break;
            case GT: case GE:
                dirType = DirType.MIN;
                break;
            case LT: case LE:
                dirType = DirType.MAX;
                break;
            default:
                assert false;
            }
        }
    
        return dirType;
    }
}