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

import epmc.expression.Expression;

public final class RewardSpecificationImpl implements RewardSpecification {
    private final Expression expression;

    public RewardSpecificationImpl(Expression expression) {
        this.expression = expression;
    }

    @Override
    public Expression getExpression() {
        return expression;
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof RewardSpecificationImpl)) {
            return false;
        }
        RewardSpecificationImpl other = (RewardSpecificationImpl) obj;
        if (!this.expression.equals(other.expression)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = expression.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("rewardstructure(");
        builder.append(expression);
        builder.append(")");
        return builder.toString();
    }
}
