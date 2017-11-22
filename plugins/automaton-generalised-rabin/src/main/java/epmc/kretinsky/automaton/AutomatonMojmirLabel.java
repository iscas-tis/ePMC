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

package epmc.kretinsky.automaton;

import epmc.automaton.AutomatonLabelUtil;
import epmc.expression.Expression;

public class AutomatonMojmirLabel implements AutomatonLabelUtil {
    private int number;
    private final Expression expression;

    AutomatonMojmirLabel(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return this.expression;
    }

    @Override
    public int getNumber() {
        return this.number;
    }

    @Override
    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public int hashCode() {
        return expression.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof AutomatonMojmir)) {
            return false;
        }
        AutomatonMojmirLabel other = (AutomatonMojmirLabel) obj;
        return this.expression.equals(other.expression);
    }

    @Override
    public String toString() {
        return expression.toString(true, false);
    }
}
