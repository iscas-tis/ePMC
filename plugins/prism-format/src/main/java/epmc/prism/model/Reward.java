package epmc.prism.model;

import java.util.Map;

import epmc.error.Positional;
import epmc.expression.Expression;

//Notice: objects of this interface are immutable by purpose.
//Do not modify the interface to make them mutable.
public interface Reward {
    Positional getPositional();
    
    default boolean isStateReward() {
        return this instanceof StateReward;
    }
    
    default StateReward asStateReward() {
        assert isStateReward();
        return (StateReward) this;
    }
    
    default boolean isTransitionReward() {
        return this instanceof TransitionReward;
    }
    
    default TransitionReward asTransitionReward() {
        assert isTransitionReward();
        return (TransitionReward) this;
    }
    
    Reward replace(Map<Expression,Expression> map);
}
