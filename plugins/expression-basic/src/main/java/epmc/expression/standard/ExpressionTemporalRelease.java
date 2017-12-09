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

public final class ExpressionTemporalRelease implements Expression {
    public final static class Builder {
        private Positional positional;
        private Expression operandLeft;
        private Expression operandRight;
        private TimeBound timeBound;

        public Builder setPositional(Positional positional) {
            this.positional = positional;
            return this;
        }

        private Positional getPositional() {
            return positional;
        }

        public Builder setOperandLeft(Expression operandLeft) {
            this.operandLeft = operandLeft;
            return this;
        }

        private Expression getOperandLeft() {
            return operandLeft;
        }
        
        public Builder setOperandRight(Expression operandRight) {
            this.operandRight = operandRight;
            return this;
        }

        private Expression getOperandRight() {
            return operandRight;
        }

        public Builder setTimeBound(TimeBound timeBound) {
            this.timeBound = timeBound;
            return this;
        }
        
        private TimeBound getTimeBound() {
            return timeBound;
        }
        
        public ExpressionTemporalRelease build() {
            return new ExpressionTemporalRelease(this);
        }
    }
    
    public static boolean is(Object expression) {
        return expression instanceof ExpressionTemporalRelease;
    }

    public static ExpressionTemporalRelease as(Expression expression) {
        if (is(expression)) {
            return (ExpressionTemporalRelease) expression;
        } else {
            return null;
        }
    }
    
    private final Positional positional;
    private final TimeBound timeBound;
    private final Expression operandLeft;
    private final Expression operandRight;

    private ExpressionTemporalRelease(Builder builder) {
        assert builder != null;
        assert builder.getOperandLeft() != null;
        assert builder.getOperandRight() != null;
        assert builder.getTimeBound() != null;
        this.positional = builder.getPositional();
        this.operandLeft = builder.getOperandLeft();
        this.operandRight = builder.getOperandRight();
        this.timeBound = builder.getTimeBound();
    }

    public Expression getOperandLeft() {
        return operandLeft;
    }

    public Expression getOperandRight() {
        return operandRight;
    }

    public TimeBound getTimeBound() {
        return timeBound;
    }    

    @Override
    public Expression replaceChildren(List<Expression> children) {
        assert children != null;
        assert children.size() == 4;
        TimeBound newTimeBound = new TimeBound.Builder()
                .setLeftOpen(timeBound.isLeftOpen())
                .setLeft(children.get(2))
                .setRightOpen(timeBound.isRightOpen())
                .setRight(children.get(3))
                .build();
        return new ExpressionTemporalRelease.Builder()
                .setOperandLeft(children.get(0))
                .setOperandRight(children.get(1))
                .setTimeBound(newTimeBound)
                .setPositional(positional)
                .build();
    }

    @Override
    public List<Expression> getChildren() {
        List<Expression> children = new ArrayList<>();
        children.add(operandLeft);
        children.add(operandRight);
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
        builder.append("(");
        builder.append(operandLeft);
        builder.append(") R (");
        builder.append(operandRight);
        builder.append(")");
        if (getPositional() != null) {
            builder.append(" (" + getPositional() + ")");
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ExpressionTemporalRelease)) {
            return false;
        }
        ExpressionTemporalRelease other = (ExpressionTemporalRelease) obj;
        if (!this.operandLeft.equals(other.operandLeft)) {
            return false;
        }
        if (!this.operandRight.equals(other.operandRight)) {
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
        return new ExpressionTemporalRelease.Builder()
                .setOperandLeft(operandLeft)
                .setOperandRight(operandRight)
                .setTimeBound(timeBound)
                .setPositional(positional)
                .build();
    }
}
