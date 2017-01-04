package epmc.guardedcommand.model;

import java.util.Map;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.UtilExpressionStandard;

//Notice: objects of this class are immutable by purpose.
//Do not modify the class to make them mutable.
public final class StateReward implements Reward {
    private Positional positional;
    private final Expression guard;
    private final Expression value;
    
    public StateReward(Expression guard, Expression value, Positional positional) {
        this.positional = positional;
        assert guard != null;
        assert value != null;
        this.guard = guard;
        this.value = value;
    }
    
    public Expression getGuard() {
        return guard;
    }
    
    public Expression getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return guard + " : " + value;
    }

    @Override
    public Reward replace(Map<Expression, Expression> map) {
        Expression newGuard = UtilExpressionStandard.replace(guard, map);
        Expression newValue = UtilExpressionStandard.replace(value, map);
        return new StateReward(newGuard, newValue, getPositional());
    }

    @Override
    public Positional getPositional() {
        return positional;
    }
}
