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

public final class ExpressionMultiObjective implements Expression {
    public final static class Builder {
        private Positional positional;
        private List<Expression> operands;

        public Builder setPositional(Positional positional) {
            this.positional = positional;
            return this;
        }

        private Positional getPositional() {
            return positional;
        }

        public Builder setOperands(List<Expression> operands) {
            this.operands = operands;
            return this;
        }

        private List<Expression> getOperands() {
            return operands;
        }

        public ExpressionMultiObjective build() {
            return new ExpressionMultiObjective(this);
        }
    }

    private final Positional positional;
    private final List<Expression> children = new ArrayList<>();
    private final List<Expression> childrenExternal = Collections.unmodifiableList(children);

    private ExpressionMultiObjective(Builder builder) {
        assert builder != null;
        assert builder.getOperands() != null;
        for (Expression operand : builder.getOperands()) {
            assert operand != null;
            // TODO
            //          assert child instanceof ExpressionQuantifier : child.getClass();

        }
        this.children.addAll(builder.getOperands());
        this.positional = builder.getPositional();
    }

    public Expression getOperand1() {
        return getChildren().get(0);
    }

    public Expression getOperand2() {
        return getChildren().get(1);
    }

    public Expression getOperand3() {
        return getChildren().get(2);
    }

    public List<Expression> getOperands() {
        return childrenExternal;
    }

    @Override
    public Expression replaceChildren(List<Expression> children) {
        return new Builder()
                .setOperands(children)
                .setPositional(positional)
                .build();
    }

    @Override
    public List<Expression> getChildren() {
        return children;
    }

    @Override
    public Positional getPositional() {
        return positional;
    }

    @Override
    public final String toString() {
        List<Expression> children = getChildren();
        StringBuilder builder = new StringBuilder();
        builder.append("multi(");
        for (int i = 0; i < children.size(); i++) {
            builder.append(children.get(i));
            if (i < children.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append(")");
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
        return new ExpressionMultiObjective.Builder()
                .setOperands(children)
                .setPositional(positional)
                .build();
    }
}
