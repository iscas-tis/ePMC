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

import epmc.error.Positional;
import epmc.expression.Expression;

// TODO complete documentation

/**
 * Time bound used in Until, Weak Until, Release etc. formulas.
 * 
 * @author Ernst Moritz Hahn
 */
public final class TimeBound {
    public final static class Builder {
        private Expression left;
        private Expression right;
        private Boolean leftOpen;
        private Boolean rightOpen;
        private Positional positional;

        public Builder setLeft(Expression left) {
            this.left = left;
            return this;
        }

        private Expression getLeft() {
            return left;
        }

        public Builder setRight(Expression right) {
            this.right = right;
            return this;
        }

        private Expression getRight() {
            return right;
        }

        public Builder setLeftOpen(Boolean leftOpen) {
            this.leftOpen = leftOpen;
            return this;
        }

        private Boolean isLeftOpen() {
            return leftOpen;
        }

        public Builder setRightOpen(Boolean rightOpen) {
            this.rightOpen = rightOpen;
            return this;
        }
        
        private Boolean isRightOpen() {
            return rightOpen;
        }

        public Builder setPositional(Positional positional) {
            this.positional = positional;
            return this;
        }
        
        private Positional getPositional() {
            return positional;
        }
        
        public TimeBound build() {
            return new TimeBound(this);
        }
    }

    /** left time bound */
    private final Expression left;
    /** right time bound */
    private final Expression right;
    /** whether time-interval is left-open */
    private final boolean leftOpen;
    /** whether time-interval is right-open */
    private final boolean rightOpen;
    /** positional information about time bound */
    private final Positional positional;

    private TimeBound(Builder builder) {
        assert builder != null;
        Expression left = builder.getLeft();
        if (left == null) {
            left = ExpressionLiteral.getZero();
        }
        Expression right = builder.getRight();
        if (right == null) {
            right = ExpressionLiteral.getPosInf();
        }
        Boolean leftOpen = builder.isLeftOpen();
        Boolean rightOpen = builder.isRightOpen();
        if (leftOpen == null) {
            leftOpen = false;
        }
        if (rightOpen == null && isPosInf(right)) {
            rightOpen = true;
        } else if (rightOpen == null) {
            rightOpen = false;
        }
        this.left = left;
        this.right = right;
        this.leftOpen = leftOpen;
        this.rightOpen = rightOpen;
        this.positional = builder.getPositional();
    }

    private boolean isPosInf(Expression expression) {
        assert expression != null;
        if (!ExpressionLiteral.is(expression)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = ExpressionLiteral.as(expression);
        if (!expressionLiteral.getType().equals(ExpressionTypeReal.TYPE_REAL)) {
            return false;
        }
        if (Double.valueOf(expressionLiteral.getValue()) != Double.POSITIVE_INFINITY) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (isUnbounded()) {
        } else if (!isLeftBounded()) {
            builder.append(rightOpen ? "<" : "<=");
            builder.append(leftBraceIfNeeded(right));
            builder.append(right);
            builder.append(rightBraceIfNeeded(right));
        } else if (!isRightBounded()) {
            builder.append(leftOpen ? ">" : ">=");
            builder.append(leftBraceIfNeeded(left));
            builder.append(left);
            builder.append(rightBraceIfNeeded(left));
        } else if (left.equals(right)) {
            builder.append("=");
            builder.append(leftBraceIfNeeded(left));
            builder.append(left);
            builder.append(rightBraceIfNeeded(left));
        } else {
            builder.append(leftOpen ? "]" : "[");
            builder.append(left);
            builder.append(",");
            builder.append(right);
            builder.append(rightOpen ? "[" : "]");
        }
        return builder.toString();
    }

    private static boolean needBracesForInequation(Expression expr) {
        return (!(expr instanceof ExpressionIdentifierStandard
                || expr instanceof ExpressionLiteral));
    }

    private String leftBraceIfNeeded(Expression expr) {
        if (needBracesForInequation(expr)) {
            return "(";
        } else {
            return "";
        }
    }

    private String rightBraceIfNeeded(Expression expr) {
        if (needBracesForInequation(expr)) {
            return ")";
        } else {
            return "";
        }
    }

    /**
     * Get left time bound.
     * 
     * @return left time bound
     */
    public Expression getLeft() {
        return left;
    }

    /**
     * Get right time bound.
     * 
     * @return right time bound
     */
    public Expression getRight() {
        return right;
    }

    /**
     * Check whether time interval is left-open.
     * 
     * @return whether time interval is left-open.
     */
    public boolean isLeftOpen() {
        return leftOpen;
    }

    /**
     * Check whether time interval is right-open.
     * 
     * @return whether time interval is right-open.
     */
    public boolean isRightOpen() {
        return rightOpen;
    }

    /**
     * Check whether the time bound is left-bounded.
     * That is, the left time bound &gt; 0 or the time interval is left open.
     * 
     * @return check whether time bound is left bounded
     */
    public boolean isLeftBounded() {
        if (isLeftOpen()) {
            return true;
        }
        if (!ExpressionLiteral.is(getLeft())) {
            return true;
        }
        ExpressionLiteral leftLit = ExpressionLiteral.as(getLeft());
        if (Double.valueOf(leftLit.getValue()) != 0.0) {
            return true;
        }
        return true;
    }

    /**
     * Check whether the time bound is right-bounded.
     * That is, the right time bound is &lt; &infin;.
     * 
     * @return check whether time bound is right bounded
     */
    public boolean isRightBounded() {
        if (!ExpressionLiteral.is(getRight())) {
            return true;
        }
        return !isPosInf(right);
    }

    /**
     * Check whether the time interval unbounded.
     * It is unbounded if it is neither left bounded nor right bounded.
     * 
     * @return whether the time interval is unbounded
     */
    public boolean isUnbounded() {
        return !isLeftBounded() && !isRightBounded();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TimeBound)) {
            return false;
        }
        TimeBound other = (TimeBound) obj;
        return this.left.equals(other.getLeft()) && this.right.equals(other.getRight())
                && this.leftOpen == other.isLeftOpen() &&
                this.rightOpen == other.isRightOpen();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = left.hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = right.hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = (leftOpen ? 1 : 0) + (hash << 6) + (hash << 16) - hash;
        hash = (rightOpen ? 1 : 0) + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
    
    public Positional getPositional() {
        return positional;
    }
}
