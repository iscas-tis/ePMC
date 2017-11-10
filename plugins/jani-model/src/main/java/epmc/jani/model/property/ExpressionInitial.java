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

package epmc.jani.model.property;

import java.util.Collections;
import java.util.List;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;

// TODO check whether it's really a good idea to mark this as identifier
public final class ExpressionInitial implements ExpressionIdentifier {
    private final static String INITIAL = "\"initial\"";
    private final Positional positional;

    public ExpressionInitial(Positional positional) {
        this.positional = positional;
    }

    public static ExpressionInitial getExpressionInitial() {
        return new ExpressionInitial(null);
    }

    @Override
    public Expression replaceChildren(List<Expression> children) {
        return new ExpressionInitial(positional);
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
        return INITIAL;
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }    

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;

        return hash;
    }

    public String getName() {
        return INITIAL;
    }

    @Override
    public Expression replacePositional(Positional positional) {
        return new ExpressionInitial(positional);
    }
}
