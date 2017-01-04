package epmc.expression.standard;

import epmc.expression.Expression;

public final class SMGPlayerImpl implements SMGPlayer {
    private final Expression expression;
    
    SMGPlayerImpl(Expression expression) {
        this.expression = expression;
    }
    
    @Override
    public Expression getExpression() {
        return expression;
    }
    
    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof SMGPlayerImpl)) {
            return false;
        }
        SMGPlayerImpl other = (SMGPlayerImpl) obj;
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
        builder.append("smgplayer(");
        builder.append(expression);
        builder.append(")");
        return builder.toString();
    }
}
