package epmc.guardedcommand.model;

import java.util.Map;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.UtilExpressionStandard;

//Notice: objects of this class are immutable by purpose.
//Do not modify the class to make them mutable.
public final class TransitionReward implements Reward {
	private final static String LBRACK = "[";
	private final static String RBRACK = "]";
	private final static String SPACE = " ";
	private final static String COLON = ":";
	
    private Positional positional;
    private final String label;
    private final Expression guard;
    private final Expression value;

    public TransitionReward(String label, Expression guard, Expression value, Positional positional) {
        assert label != null;
        assert guard != null;
        assert value != null;
        this.positional = positional;
        this.label = label;
        this.guard = guard;
        this.value = value;
    }
    
    public String getLabel() {
        return label;
    }
    
    public Expression getGuard() {
        return guard;
    }
    
    public Expression getValue() {
        return value;
    }

    @Override
    public String toString() {
        return LBRACK + label + RBRACK + SPACE + guard
        		+ SPACE + COLON + SPACE + value;
    }

    @Override
    public Reward replace(Map<Expression, Expression> map) {
        Expression newGuard = UtilExpressionStandard.replace(guard, map);
        Expression newValue = UtilExpressionStandard.replace(value, map);
        return new TransitionReward(label, newGuard, newValue, getPositional());
    }

    @Override
    public Positional getPositional() {
        return positional;
    }
}
