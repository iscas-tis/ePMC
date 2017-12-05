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
 * A literal, such as booleans, numeric values, etc. Basically a container for
 * Value objects.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ExpressionLiteral implements ExpressionPropositional {
    public static boolean is(Expression expression) {
        return expression instanceof ExpressionLiteral;
    }

    public static ExpressionLiteral as(Expression expression) {
        if (is(expression)) {
            return (ExpressionLiteral) expression;
        } else {
            return null;
        }
    }

    public final static class Builder {
        private Positional positional;
        private String value;
        private ExpressionType type;

        public Builder setValue(String value) {
            this.value = value;
            return this;
        }

        private String getValue() {
            return value;
        }
        
        public Builder setType(ExpressionType type) {
            this.type = type;
            return this;
        }
        
        private ExpressionType getType() {
            return type;
        }

        public Builder setPositional(Positional positional) {
            this.positional = positional;
            return this;
        }

        private Positional getPositional() {
            return positional;
        }

        public ExpressionLiteral build() {
            return new ExpressionLiteral(this);
        }
    }

    private final String value;
    private final Positional positional;
    private final ExpressionType type;

    private ExpressionLiteral(Builder builder) {
        assert builder != null;
        assert builder.getValue() != null;
        assert builder.getType() != null;
        this.positional = builder.getPositional();
        this.value = builder.getValue();
        this.type = builder.getType();
    }

    public String getValue() {
        return value;
    }
    
    public ExpressionType getType() {
        return type;
    }

    @Override
    public Expression replaceChildren(List<Expression> children) {
        assert children != null;
        assert children.size() == 0;
        return this;
    }

    @Override
    public boolean isPropositional() {
        return true;
    }

    @Override
    public List<Expression> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public Positional getPositional() {
        return positional;
    }    

    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(value);
        builder.append("(");
        builder.append(type);
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
        ExpressionLiteral other = (ExpressionLiteral) obj;
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
        if (!this.value.equals(other.value)) {
            return false;
        }
        if (!this.type.equals(other.type)) {
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
        hash = value.hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = type.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    /**
     * Obtain expression representing positive infinity.
     * 
     * @return expression representing positive infinity
     */
    public static ExpressionLiteral getPosInf() {
        return new Builder()
                .setValue("Infinity")
                .setType(ExpressionTypeReal.TYPE_REAL)
                .build();
    }

    /**
     * Obtain expression representing the value &quot;1&quot;
     * 
     * @return expression representing the value &quot;1&quot;
     */
    public static Expression getOne() {
        return new Builder()
                .setValue("1")
                .setType(ExpressionTypeInteger.TYPE_INTEGER)
                .build();
    }

    /**
     * Obtain expression representing the value &quot;0&quot;
     * 
     * @return expression representing the value &quot;0&quot;
     */
    public static ExpressionLiteral getZero() {
        return new Builder()
                .setValue("0")
                .setType(ExpressionTypeInteger.TYPE_INTEGER)
                .build();
    }

    /**
     * Obtain expression representing the value &quot;true&quot;
     * 
     * @return expression representing the value &quot;true&quot;
     */
    public static ExpressionLiteral getTrue() {
        return new Builder()
                .setValue("true")
                .setType(ExpressionTypeBoolean.TYPE_BOOLEAN)
                .build();
    }

    /**
     * Obtain expression representing the value &quot;false&quot;
     * 
     * @return expression representing the value &quot;false&quot;
     */
    public static Expression getFalse() {
        return new Builder()
                .setValue("false")
                .setType(ExpressionTypeBoolean.TYPE_BOOLEAN)
                .build();
    }

    @Override
    public Expression replacePositional(Positional positional) {
        Builder builder = new ExpressionLiteral.Builder();
        builder.setValue(value);
        builder.setType(type);
        builder.setPositional(positional);
        return builder.build();
    }
}
