/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.prism.model;

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
