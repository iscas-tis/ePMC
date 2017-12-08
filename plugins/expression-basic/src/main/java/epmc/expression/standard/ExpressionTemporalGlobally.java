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
import java.util.Collections;
import java.util.List;

import epmc.error.Positional;
import epmc.expression.Expression;

public final class ExpressionTemporalGlobally implements Expression {
    public final static class Builder {
        private Positional positional;
        private Expression operand;
        private TimeBound timeBound;

        public Builder setPositional(Positional positional) {
            this.positional = positional;
            return this;
        }

        private Positional getPositional() {
            return positional;
        }

        public Builder setOperand(Expression operand) {
            this.operand = operand;
            return this;
        }

        private Expression getOperand() {
            return operand;
        }
        
        public Builder setTimeBound(TimeBound timeBound) {
            this.timeBound = timeBound;
            return this;
        }
        
        private TimeBound getTimeBound() {
            return timeBound;
        }
        
        public ExpressionTemporalGlobally build() {
            return new ExpressionTemporalGlobally(this);
        }
    }
    
    public static boolean is(Object expression) {
        return expression instanceof ExpressionTemporalGlobally;
    }

    public static ExpressionTemporalGlobally as(Expression expression) {
        if (is(expression)) {
            return (ExpressionTemporalGlobally) expression;
        } else {
            return null;
        }
    }
    
    private final Positional positional;
    private final TimeBound timeBound;
    private final Expression operand;

    private ExpressionTemporalGlobally(Builder builder) {
        assert builder != null;
        assert builder.getOperand() != null;
        assert builder.getTimeBound() != null;
        this.positional = builder.getPositional();
        this.operand = builder.getOperand();
        this.timeBound = builder.getTimeBound();
    }

    public Expression getOperand() {
        return operand;
    }

    public TimeBound getTimeBound() {
        return timeBound;
    }    

    @Override
    public Expression replaceChildren(List<Expression> children) {
        assert children != null;
        assert children.size() == 3;
        TimeBound newTimeBound = new TimeBound.Builder()
                .setLeftOpen(timeBound.isLeftOpen())
                .setLeft(children.get(1))
                .setRightOpen(timeBound.isRightOpen())
                .setRight(children.get(2))
                .build();
        return new ExpressionTemporalGlobally.Builder()
                .setOperand(children.get(0))
                .setTimeBound(newTimeBound)
                .setPositional(positional)
                .build();
    }

    @Override
    public List<Expression> getChildren() {
        List<Expression> children = new ArrayList<>();
        children.add(operand);
        children.add(timeBound.getLeft());
        children.add(timeBound.getRight());
        return Collections.unmodifiableList(children);
    }

    @Override
    public Positional getPositional() {
        return positional;
    }

    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("G ");
        builder.append("(");
        builder.append(getChildren().get(0));
        builder.append(")");
        if (getPositional() != null) {
            builder.append(" (" + getPositional() + ")");
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ExpressionTemporalGlobally)) {
            return false;
        }
        ExpressionTemporalGlobally other = (ExpressionTemporalGlobally) obj;
        if (!this.operand.equals(other.operand)) {
            return false;
        }
        if (!this.timeBound.equals(other.timeBound)) {
            return false;
        }
        return true;
    }    

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        for (Expression expression : this.getChildren()) {
            assert expression != null;
            hash = expression.hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        hash = timeBound.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public Expression replacePositional(Positional positional) {
        return new ExpressionTemporalGlobally.Builder()
                .setOperand(operand)
                .setTimeBound(timeBound)
                .setPositional(positional)
                .build();
    }
}
