package epmc.expression.standard;

import epmc.expression.Expression;

final class RewardSpecificationImpl implements RewardSpecification {
    private final Expression expression;
    
    RewardSpecificationImpl(Expression expression) {
        this.expression = expression;
    }
    
    @Override
    public Expression getExpression() {
        return expression;
    }
    
    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof RewardSpecificationImpl)) {
            return false;
        }
        RewardSpecificationImpl other = (RewardSpecificationImpl) obj;
        if (!this.expression.equals(other.expression)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = expression.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("rewardstructure(");
        builder.append(expression);
        builder.append(")");
        return builder.toString();
    }
}
