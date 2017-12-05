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

package epmc.expression.standard;

import java.util.ArrayList;
import java.util.List;

import epmc.error.Positional;
import epmc.expression.Expression;

/**
 * @author Ernst Moritz Hahn
 */
public final class ExpressionReward implements Expression {
    public static boolean is(Expression expression) {
        return expression instanceof ExpressionReward;
    }

    public static ExpressionReward as(Expression expression) {
        if (is(expression)) {
            return (ExpressionReward) expression;
        } else {
            return null;
        }
    }

    public final static class Builder {
        private Expression reward;
        private Expression reachSet;
        private Expression time;
        private Expression discount;
        private RewardType rewardType;
        private Positional positional;

        public Builder setReward(Expression reward) {
            this.reward = reward;
            return this;
        }

        private Expression getReward() {
            return reward;
        }

        public Builder setReachSet(Expression reachSet) {
            this.reachSet = reachSet;
            return this;
        }

        private Expression getReachSet() {
            return reachSet;
        }

        public Builder setTime(Expression time) {
            this.time = time;
            return this;
        }

        private Expression getTime() {
            return time;
        }

        public Builder setDiscount(Expression discount) {
            this.discount = discount;
            return this;
        }

        private Expression getDiscount() {
            return discount;
        }

        public Builder setRewardType(RewardType rewardType) {
            this.rewardType = rewardType;
            return this;
        }

        private RewardType getRewardType() {
            return rewardType;
        }

        public Builder setPositional(Positional positional) {
            this.positional = positional;
            return this;
        }

        private Positional getPositional() {
            return positional;
        }

        public ExpressionReward build() {
            return new ExpressionReward(this);
        }
    }

    private final static String SPACE = " ";
    private final static String COMMA = ",";
    private final static String LBRACE = "(";
    private final static String RBRACE = ")";

    private final Positional positional;
    private final RewardType type;
    private final Expression reward;
    private final Expression reachSet;
    private final Expression time;
    private final Expression discount;

    private ExpressionReward(Builder builder) {
        assert builder != null;
        assert builder.getRewardType() != null;
        assert builder.getReward() != null;
        this.positional = builder.getPositional();
        this.type = builder.getRewardType();
        this.reward = builder.getReward();
        Expression reachSet = builder.getReachSet();
        if (reachSet == null) {
            reachSet = ExpressionLiteral.getFalse();
        }
        this.reachSet = reachSet;
        Expression time = builder.getTime();
        if (time == null) {
            time = ExpressionLiteral.getPosInf();
        }
        this.time = time;
        Expression discount = builder.getDiscount();
        if (discount == null) {
            discount = ExpressionLiteral.getOne();
        }
        this.discount = discount;
    }

    ExpressionReward(List<Expression> children, RewardType type,
            Positional positional) {
        assert type != null;
        assert children.size() == 4;
        this.positional = positional;
        this.type = type;
        this.reward = children.get(0);
        this.reachSet = children.get(1);
        this.time = children.get(2);
        this.discount = children.get(3);
    }

    @Override
    public Expression replaceChildren(List<Expression> children) {
        //        // TODO works for now, not sure whether best way
        //        // purpose is to prevent labels being replaced
        //        Expression structure = getReward().getExpression();
        //        if (structure instanceof ExpressionIdentifier
        //        		&& ((ExpressionIdentifierStandard) structure).getName().startsWith(QUOT)) {
        //            List<Expression> oldChildren = children.subList(1, children.size());
        //            children = new ArrayList<>(children.size());
        //            children.add(structure);
        //            children.addAll(oldChildren);
        //        }
        //    	The above part has been commented out since reward labels have to be relabelled 
        //    	on exporting to JANI while they should not be relabelled for internal use; this 
        //    	is now considered inside PRISM2JANIConverter
        return new ExpressionReward.Builder()
                .setRewardType(type)
                .setReward(children.get(0))
                .setReachSet(children.get(1))
                .setTime(children.get(2))
                .setDiscount(children.get(3))
                .setPositional(positional)
                .build();
    }

    public RewardSpecification getReward() {
        return new RewardSpecificationImpl(reward);
    }

    public Expression getRewardReachSet() {
        return reachSet;
    }

    public Expression getTime() {
        return time;
    }

    public Expression getDiscount() {
        assert type == RewardType.DISCOUNTED;
        return discount;
    }

    public RewardType getRewardType() {
        return type;
    }

    @Override
    public List<Expression> getChildren() {
        List<Expression> result = new ArrayList<>();
        result.add(reward);
        result.add(reachSet);
        result.add(time);
        result.add(discount);
        return result;
    }

    @Override
    public Positional getPositional() {
        return positional;
    }


    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(type);
        if (type == RewardType.REACHABILITY) {
            builder.append(LBRACE);
            builder.append(getRewardReachSet());
            builder.append(RBRACE);
        } else if (type == RewardType.INSTANTANEOUS
                || type == RewardType.CUMULATIVE) {
            builder.append(LBRACE);
            builder.append(getTime());
            builder.append(RBRACE);
        } else if (type == RewardType.DISCOUNTED) {
            builder.append(getTime());
            builder.append(COMMA);
            builder.append(getDiscount());
        }
        if (getPositional() != null) {
            builder.append(SPACE + LBRACE + getPositional() + RBRACE);
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        ExpressionReward other = (ExpressionReward) obj;
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
        return this.type == other.type;
    }    

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        for (Expression expression : this.getChildren()) {
            assert expression != null;
            hash = expression.hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        hash = type.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public Expression replacePositional(Positional positional) {
        return new ExpressionReward.Builder()
                .setDiscount(discount)
                .setReachSet(reachSet)
                .setReward(reward)
                .setRewardType(type)
                .setTime(time)
                .setPositional(positional)
                .build();
    }
}
