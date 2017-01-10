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

import epmc.value.ValueAlgebra;
import epmc.value.ValueInteger;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.Value;

// TODO complete documentation

/**
 * Time bound used in Until, Weak Until, Release etc. formulas.
 * 
 * @author Ernst Moritz Hahn
 */
public final class TimeBound {
	// TODO actually, we should get types as well as constants from somewhere
	private final static class ExpressionToTypeEmpty implements ExpressionToType {
		private final ContextValue contextValue;

		private ExpressionToTypeEmpty(ContextValue contextValue) {
			this.contextValue = contextValue;
		}
		
		public Type getType(Expression expression) throws EPMCException {
			assert expression != null;
			return null;
		}

		@Override
		public ContextValue getContextValue() {
			return contextValue;
		}
	}
	
    public final static class Builder {
        private ContextValue context;
        private Expression left;
        private Expression right;
        private Boolean leftOpen;
        private Boolean rightOpen;

        public Builder setContext(ContextValue context) {
            this.context = context;
            return this;
        }
        
        private ContextValue getContext() {
            return context;
        }
        
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
    
    private TimeBound(Builder builder) {
        assert builder != null;
        assert builder.getContext() != null;
        ContextValue context = builder.getContext();
        Expression left = builder.getLeft();
        if (left == null) {
            left = ExpressionLiteral.getZero(context);
        }
        Expression right = builder.getRight();
        if (right == null) {
            right = ExpressionLiteral.getPosInf(context);
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
    }

    private boolean isPosInf(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return ValueAlgebra.asAlgebra(expressionLiteral.getValue()).isPosInf();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        try {
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
        } catch (EPMCException e) {
            return e.toString();
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
     * @throws EPMCException in case a problem occurs during evaluation
     */
    public boolean isLeftBounded() throws EPMCException {
        if (isLeftOpen()) {
            return true;
        }
        if (!(getLeft() instanceof ExpressionLiteral)) {
            return true;
        }
        ExpressionLiteral leftLit = (ExpressionLiteral) getLeft();
        return !ValueAlgebra.asAlgebra(getValue(leftLit)).isZero();
    }

    /**
     * Check whether the time bound is right-bounded.
     * That is, the right time bound is &lt; &infin;.
     * 
     * @return check whether time bound is right bounded
     */
    public boolean isRightBounded() {
        if (!(getRight() instanceof ExpressionLiteral)) {
            return true;
        }
        return !ValueAlgebra.asAlgebra(getValue(getRight())).isPosInf();
    }

    /**
     * Check whether the time interval unbounded.
     * It is unbounded if it is neither left bounded nor right bounded.
     * 
     * @return whether the time interval is unbounded
     * @throws EPMCException in case a problem occurs during evaluation
     */
    public boolean isUnbounded() throws EPMCException {
        return !isLeftBounded() && !isRightBounded();
    }
    
    // TODO might provide additional context for constants etc.
    
    /**
     * Get left time bound as {@link Value}.
     * For this, the left time bound expression will be evaluated. If a problem
     * during this occurs (e.g. because of undefined constants), an exception
     * will be thrown.
     * 
     * @return left time bound as {@link Value}
     * @throws EPMCException in case a problem occurs during evaluation
     */
    public ValueAlgebra getLeftValue(ContextValue contextValue) throws EPMCException {
        return evaluateValue(contextValue, getLeft());
    }

    /**
     * Get right time bound as {@link Value}.
     * For this, the right time bound expression will be evaluated. If a problem
     * during this occurs (e.g. because of undefined constants), an exception
     * will be thrown.
     * 
     * @return right time bound as {@link Value}
     * @throws EPMCException in case a problem occurs during evaluation
     */
    public ValueAlgebra getRightValue(ContextValue contextValue) throws EPMCException {
    	assert contextValue != null;
        return evaluateValue(contextValue, getRight());
    }

    public int getLeftInt(ContextValue contextValue) throws EPMCException {
    	assert contextValue != null;
        return ValueInteger.asInteger(evaluateValue(contextValue, getLeft())).getInt();
    }

    public int getRightInt(ContextValue contextValue) throws EPMCException {
    	assert contextValue != null;
        return ValueInteger.asInteger(evaluateValue(contextValue, getRight())).getInt();
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
    
    private static ValueAlgebra evaluateValue(ContextValue contextValue, Expression expression) throws EPMCException {
    	assert contextValue != null;
        assert expression != null;
        EvaluatorExplicit evaluator = UtilEvaluatorExplicit.newEvaluator(expression,
        		new ExpressionToTypeEmpty(contextValue),
        		new Expression[0]);
        return ValueAlgebra.asAlgebra(evaluator.evaluate());
    }
    
    private static Value getValue(Expression expression) {
        assert expression != null;
        assert expression instanceof ExpressionLiteral;
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return expressionLiteral.getValue();
    }
}
