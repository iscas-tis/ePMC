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
import java.util.List;

import epmc.error.Positional;
import epmc.expression.Expression;

/**
 * @author Ernst Moritz Hahn
 */
public final class ExpressionQuantifier implements Expression {
    public final static class Builder {
        private Positional positional;
        private DirType dirType;
        private CmpType cmpType;
        private Expression quantified;
        private Expression compare;
        private Expression condition;

        public Builder setPositional(Positional positional) {
            this.positional = positional;
            return this;
        }

        private Positional getPositional() {
            return positional;
        }

        public Builder setDirType(DirType dirType) {
            this.dirType = dirType;
            return this;
        }

        private DirType getDirType() {
            return dirType;
        }

        public Builder setCmpType(CmpType cmpType) {
            this.cmpType = cmpType;
            return this;
        }

        private CmpType getCmpType() {
            return cmpType;
        }

        public Builder setQuantified(Expression quantified) {
            this.quantified = quantified;
            return this;
        }

        private Expression getQuantified() {
            return quantified;
        }

        public Builder setCompare(Expression compare) {
            this.compare = compare;
            return this;
        }

        private Expression getCompare() {
            return compare;
        }

        public Builder setCondition(Expression condition) {
            this.condition = condition;
            return this;
        }

        private Expression getCondition() {
            return condition;
        }

        public ExpressionQuantifier build() {
            return new ExpressionQuantifier(this);
        }
    }

    public static boolean is(Expression expression) {
        return expression instanceof ExpressionQuantifier;
    }

    public static ExpressionQuantifier as(Expression expression) {
        if (is(expression)) {
            return (ExpressionQuantifier) expression;
        } else {
            return null;
        }
    }

    private final Positional positional;
    private final DirType dirType;
    private final CmpType cmpType;
    private final Expression quantified;
    private final Expression compare;
    private final Expression condition;

    private ExpressionQuantifier(Builder builder) {
        assert builder != null;
        if (builder.getCompare() == null) {
            builder.setCompare(ExpressionLiteral.getTrue());
        }
        if (builder.getCondition() == null) {
            builder.setCondition(ExpressionLiteral.getTrue());
        }
        assert builder.getDirType() != null;
        assert builder.getCmpType() != null;
        assert builder.getQuantified() != null;
        assert builder.getCmpType() != CmpType.IS
                || isTrue(builder.getCompare());
        assert isTrue(builder.getCondition());
        this.positional = builder.getPositional();
        this.dirType = builder.getDirType();
        this.cmpType = builder.getCmpType();
        this.quantified = builder.getQuantified();
        this.compare = builder.getCompare();
        this.condition = builder.getCondition();
    }

    public DirType getDirType() {
        return dirType;
    }

    public CmpType getCompareType() {
        return cmpType;
    }

    public Expression getQuantified() {
        return quantified;
    }

    public Expression getCompare() {
        return compare;
    }

    public Expression getCondition() {
        return condition;
    }

    @Override
    public Expression replaceChildren(List<Expression> children) {
        return new Builder()
                .setCmpType(cmpType)
                .setDirType(dirType)
                .setPositional(positional)
                .setQuantified(children.get(0))
                .setCompare(children.get(1))
                .setCondition(children.get(2))
                .build();
    }

    public boolean isDirMin() {
        return dirType == DirType.MIN;
    }

    public boolean isDirNone() {
        return dirType == DirType.NONE;
    }

    @Override
    public List<Expression> getChildren() {
        List<Expression> result = new ArrayList<>();
        result.add(quantified);
        result.add(compare);
        result.add(condition);
        return result;
    }

    @Override
    public Positional getPositional() {
        return positional;
    }


    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        Expression rewardStructure = null;
        if (getQuantified() instanceof ExpressionSteadyState) {
            builder.append("S");
        } else if (getQuantified() instanceof ExpressionReward) {
            builder.append("R");
            rewardStructure = ((ExpressionReward) getQuantified()).getReward().getExpression();
        } else {
            builder.append("P");
        }

        if (rewardStructure != null && !isTrue(rewardStructure)) {
            builder.append("{" + rewardStructure + "}");
        }
        builder.append(dirType);
        builder.append(cmpType);
        if (cmpType != CmpType.IS) {
            builder.append(getCompare());
        }
        builder.append("[");
        builder.append(getQuantified());
        if (!isTrue(getCondition())) {
            builder.append(" given ");
            builder.append(getCondition());
        }
        builder.append("]");
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
        ExpressionQuantifier other = (ExpressionQuantifier) obj;
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
        return this.dirType == other.dirType && this.cmpType == other.cmpType;
    }    

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        for (Expression expression : this.getChildren()) {
            assert expression != null;
            hash = expression.hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        hash = dirType.hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = cmpType.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    private static boolean isTrue(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return Boolean.valueOf(getValue(expressionLiteral));
    }

    private static String getValue(Expression expression) {
        assert expression != null;
        assert expression instanceof ExpressionLiteral;
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return expressionLiteral.getValue();
    }

    public static boolean isDirTypeMin(ExpressionQuantifier expression) {
        assert expression != null;
        return computeQuantifierDirType(expression) == DirType.MIN;
    }

    public static DirType computeQuantifierDirType(ExpressionQuantifier expression) {
        assert expression != null;
        DirType dirType = expression.getDirType();
        if (dirType == DirType.NONE) {
            switch (expression.getCompareType()) {
            case IS: case EQ: case NE:
                break;
            case GT: case GE:
                dirType = DirType.MIN;
                break;
            case LT: case LE:
                dirType = DirType.MAX;
                break;
            default:
                assert false;
            }
        }

        return dirType;
    }

    @Override
    public Expression replacePositional(Positional positional) {
        return new ExpressionQuantifier.Builder()
                .setCmpType(cmpType)
                .setCompare(compare)
                .setCondition(condition)
                .setDirType(dirType)
                .setQuantified(quantified)
                .setPositional(positional)
                .build();
    }
}
