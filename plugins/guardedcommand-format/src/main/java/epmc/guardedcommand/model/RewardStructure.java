package epmc.guardedcommand.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import epmc.error.Positional;
import epmc.expression.Expression;

//Notice: objects of this class are immutable by purpose.
//Do not modify the class to make them mutable.
public final class RewardStructure implements Iterable<Reward> {
    private final String name;
    private final List<Reward> rewards = new ArrayList<>();
    private final List<Reward> rewardsExt = Collections.unmodifiableList(rewards);
    private final List<StateReward> stateRewards = new ArrayList<>();
    private final List<StateReward> stateRewardsExt = Collections.unmodifiableList(stateRewards);
    private final List<TransitionReward> transRewards = new ArrayList<>();
    private final List<TransitionReward> transRewardsExt = Collections.unmodifiableList(transRewards);
    private final Positional positional;

    public RewardStructure(String name, List<Reward> rewards, Positional positional) {
        assert name != null;
        assert rewards != null;
        for (Reward reward : rewards) {
            assert reward != null;
        }
        this.name = name;
        this.rewards.addAll(rewards);
        this.positional = positional;
        for (Reward reward : rewards) {
            if (reward.isStateReward()) {
                stateRewards.add(reward.asStateReward());
            } else if (reward.isTransitionReward()) {
                transRewards.add(reward.asTransitionReward());
            } else {
                assert false;
            }
        }
    }
    
    public String getName() {
        return name;
    }
    
    List<Reward> getRewards() {
        return rewardsExt;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("rewards");
        if (!name.equals("")) {
            builder.append(" " + name + "");
        }
        builder.append("\n");
        for (Reward reward : rewards) {
            builder.append("  " + reward + ";\n");
        }
        builder.append("endrewards");
        return builder.toString();
    }
    
    public Positional getPositional() {
        return positional;
    }

    @Override
    public Iterator<Reward> iterator() {
        return rewards.iterator();
    }
    
    public List<StateReward> getStateRewards() {
        return stateRewardsExt;
    }
    
    public List<TransitionReward> getTransitionRewards() {
        return transRewardsExt;
    }
    
    RewardStructure replace(Map<Expression, Expression> map) {
        List<Reward> newRewards = new ArrayList<>();
        for (Reward rew : rewards) {
            newRewards.add(rew.replace(map));
        }
        return new RewardStructure(name, newRewards, getPositional());
    }
}
