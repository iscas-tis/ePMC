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

package epmc.sexpression;

public final class UtilSExpression {
    public static SExpression newSExpression(String atomic) {
        assert atomic != null;
        return new SExpression(null, atomic);
    }

    public static SExpression newSExpression(SExpression... children) {
        assert children != null;
        for (SExpression child : children) {
            assert child != null;
        }
        return new SExpression(children.clone(), null);
    }

    public static SExpressionParser newParser() {
        return new SExpressionParser();
    }

    private UtilSExpression() {
    }
}
