package epmc.kretinsky.automaton;

import epmc.automaton.Automaton;
import epmc.automaton.AutomatonStateUtil;
import epmc.expression.Expression;

final class AutomatonMojmirState implements AutomatonStateUtil {
    private final Automaton observer;
    private final Expression expression;
    private int number;
    
    AutomatonMojmirState(Automaton observer, Expression formula) {
        this.observer = observer;
        assert observer != null;
        assert formula != null;
        this.expression = formula;
    }

    AutomatonMojmirState(AutomatonMojmirState other) {
        this(other.getAutomaton(), other.expression);
    }
    
    @Override
    protected AutomatonStateUtil clone() {
        return new AutomatonMojmirState(this);
    };
    
    Expression getExpression() {
        return expression;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(expression.toString(false, false).trim());
        return builder.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof AutomatonMojmirState)) {
            return false;
        }
        AutomatonMojmirState other = (AutomatonMojmirState) obj;
        return expression.equals(other.expression);
    }
    
    @Override
    public int hashCode() {
        return expression.hashCode();
    }

    @Override
    public Automaton getAutomaton() {
        return observer;
    }

    @Override
    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public int getNumber() {
        return this.number;
    }
}
