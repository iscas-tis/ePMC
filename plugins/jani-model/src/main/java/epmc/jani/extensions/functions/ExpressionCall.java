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

package epmc.jani.extensions.functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionPropositional;

/**
 * Expression to store an operator.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ExpressionCall implements ExpressionIdentifier {
    public final static class Builder {
        private Positional positional;
        private List<Expression> operands;
        private String function;

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

        public Builder setOperands(Expression... operands) {
            this.operands = Arrays.asList(operands);
            return this;
        }

        public Builder setFunction(String function) {
            this.function = function;
            return this;
        }
        
        public String getFunction() {
            return function;
        }
        
        public ExpressionCall build() {
            return new ExpressionCall(this);
        }
    }

    public static boolean is(Expression expression) {
        return expression instanceof ExpressionCall;
    }

    public static ExpressionCall as(Expression expression) {
        if (is(expression)) {
            return (ExpressionCall) expression;
        } else {
            return null;
        }
    }

    private final List<Expression> operands = new ArrayList<>();
    private final String function;
    private final Positional positional;

    private ExpressionCall(Builder builder) {
        assert builder != null;
        for (Expression child : builder.getOperands()) {
            assert child != null;
        }
        this.operands.addAll(builder.getOperands());
        this.function = builder.getFunction();
        this.positional = builder.getPositional();
    }

    // public methods

    public List<Expression> getOperands() {
        return getChildren();
    }

    @Override
    public Expression replaceChildren(List<Expression> children) {
        return new ExpressionCall.Builder()
                .setOperands(children)
                .setFunction(function)
                .setPositional(positional)
                .build();
    }

    @Override
    public boolean isPropositional() {
        for (Expression operand : getOperands()) {
            if (!ExpressionPropositional.is(operand)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public List<Expression> getChildren() {
        return operands;
    }

    @Override
    public Positional getPositional() {
        return positional;
    }


    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("call:" + function);
        builder.append("(");
        for (int index = 0; index < operands.size(); index++) {
            builder.append(operands.get(index));
            if (index < operands.size() - 1) {
                builder.append(",");
            }
        }
        builder.append(")");
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
        ExpressionCall other = (ExpressionCall) obj;
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
        if (!function.equals(other.function)) {
            return false;
        }
        return true;
    }    

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = function.hashCode() + (hash << 6) + (hash << 16) - hash;
        for (Expression expression : this.getChildren()) {
            assert expression != null;
            hash = expression.hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }

    @Override
    public Expression replacePositional(Positional positional) {
        return new ExpressionCall.Builder()
                .setOperands(operands)
                .setFunction(function)
                .setPositional(positional)
                .build();
    }

    public String getFunction() {
        return function;
    }
}
