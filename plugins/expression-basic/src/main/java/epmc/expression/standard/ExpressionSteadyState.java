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

import java.util.Collections;
import java.util.List;

import epmc.error.Positional;
import epmc.expression.Expression;

/**
 * @author Ernst Moritz Hahn
 */
public final class ExpressionSteadyState implements Expression {
    public static boolean is(Expression expression) {
        return expression instanceof ExpressionSteadyState;
    }

    public static ExpressionSteadyState as(Expression expression) {
        if (!is(expression)) {
            return null;
        }
        return (ExpressionSteadyState) expression;
    }

    public final static class Builder {
        private Positional positional;
        private Expression states;

        public Builder setPositional(Positional positional) {
            this.positional = positional;
            return this;
        }

        private Positional getPositional() {
            return positional;
        }

        public Builder setStates(Expression states) {
            this.states = states;
            return this;
        }

        private Expression getStates() {
            return states;
        }

        public ExpressionSteadyState build() {
            return new ExpressionSteadyState(this);
        }
    }

    private final Positional positional;
    private final Expression operand;

    private ExpressionSteadyState(Builder builder) {
        assert builder != null;
        assert builder.getStates() != null;
        this.positional = builder.getPositional();
        this.operand = builder.getStates();
    }

    @Override
    public Expression replaceChildren(List<Expression> children) {
        assert children.size() == 1;
        return new Builder()
                .setStates(children.get(0))
                .setPositional(positional)
                .build();
    }

    public Expression getOperand1() {
        return operand;
    }

    @Override
    public List<Expression> getChildren() {
        return Collections.singletonList(operand);
    }

    @Override
    public Positional getPositional() {
        return positional;
    }    

    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(operand);
        if (getPositional() != null) {
            builder.append(" (" + getPositional() + ")");
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Expression other = (Expression) obj;
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
        return hash;
    }

    @Override
    public Expression replacePositional(Positional positional) {
        return new ExpressionSteadyState.Builder()
                .setStates(operand)
                .setPositional(positional)
                .build();
    }
}
