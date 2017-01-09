package epmc.kretinsky.automaton;

import epmc.automaton.AutomatonLabelUtil;
import epmc.expression.Expression;

public class AutomatonMojmirLabel implements AutomatonLabelUtil {
    private int number;
    private final Expression expression;

    AutomatonMojmirLabel(Expression expression) {
        this.expression = expression;
    }
    
    public Expression getExpression() {
        return this.expression;
    }
    
    @Override
    public int getNumber() {
        return this.number;
    }

    @Override
    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public int hashCode() {
        return expression.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof AutomatonMojmir)) {
            return false;
        }
        AutomatonMojmirLabel other = (AutomatonMojmirLabel) obj;
        return this.expression.equals(other.expression);
    }
    
    @Override
    public String toString() {
        return expression.toString(true, false);
    }
}
