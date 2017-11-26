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

public final class ExpressionIdentifierStandard implements ExpressionIdentifier {
    public static boolean is(Expression expression) {
        return expression instanceof ExpressionIdentifierStandard;
    }

    public static ExpressionIdentifierStandard as(Expression expression) {
        if (is(expression)) {
            return (ExpressionIdentifierStandard) expression;
        } else {
            return null;
        }
    }	

    public final static class Builder {
        private Positional positional;
        private String name;
        private Object scope;

        public Builder setPositional(Positional positional) {
            this.positional = positional;
            return this;
        }

        private Positional getPositional() {
            return positional;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        private String getName() {
            return name;
        }

        public Builder setScope(Object scope) {
            this.scope = scope;
            return this;
        }

        private Object getScope() {
            return scope;
        }

        public ExpressionIdentifierStandard build() {
            return new ExpressionIdentifierStandard(this);
        }
    }

    private final Positional positional;
    private final String name;
    private final Object scope;

    private ExpressionIdentifierStandard(Builder builder) {
        assert builder != null;
        assert builder.getName() != null;
        this.positional = builder.getPositional();
        this.name = builder.getName();
        this.scope = builder.getScope();
    }

    ExpressionIdentifierStandard(List<Expression> children,
            String name, Object scope, Positional positional) {
        this.positional = positional;
        assert name != null;
        this.name = name;
        this.scope = scope;
    }

    public String getName() {
        return name;
    }

    public Object getScope() {
        return scope;
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
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = name.hashCode() + (hash << 6) + (hash << 16) - hash;
        if (scope != null) {
            hash = scope.hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        assert obj != null;
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        ExpressionIdentifierStandard other = (ExpressionIdentifierStandard) obj;
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
        if (!this.name.equals(other.name)) {
            return false;
        }
        if ((this.scope == null) != (other.scope == null)) {
            return false;
        }
        if (this.scope != null && !this.scope.equals(other.scope)) {
            return false;
        }

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
        builder.append(name);
        if (getPositional() != null) {
            builder.append(" (" + getPositional() + ")");
        }
        if (scope != null) {
            builder.append(" in ");
            builder.append(scope.toString());
        }
        return builder.toString();        
    }

    @Override
    public Expression replacePositional(Positional positional) {
        return new ExpressionIdentifierStandard.Builder()
                .setName(name)
                .setScope(scope)
                .setPositional(positional)
                .build();
    }
}
