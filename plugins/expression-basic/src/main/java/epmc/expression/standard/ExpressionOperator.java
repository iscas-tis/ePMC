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

import static epmc.error.UtilError.ensure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;

/**
 * Expression to store an operator.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ExpressionOperator implements ExpressionPropositional {
    public final static class Builder {
        private Positional positional;
        private List<Expression> operands;
        private Operator operator;

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

        public Builder setOperator(Operator operator) {
            this.operator = operator;
            return this;
        }

        public Operator getOperator() {
            return operator;
        }

        public ExpressionOperator build() {
            return new ExpressionOperator(this);
        }
    }

    public static boolean isOperator(Expression expression) {
        return expression instanceof ExpressionOperator;
    }

    public static ExpressionOperator asOperator(Expression expression) {
        if (isOperator(expression)) {
            return (ExpressionOperator) expression;
        } else {
            return null;
        }
    }

    private final Positional positional;
    private final List<Expression> operands = new ArrayList<>();
    private final Operator operator;

    private ExpressionOperator(Builder builder) {
        assert builder != null;
        assert builder.getOperator() != null;
        for (Expression child : builder.getOperands()) {
            assert child != null;
        }
        this.operands.addAll(builder.getOperands());
        if (builder.getOperator() != null) {
            this.operator = builder.getOperator();
        } else {
            throw new RuntimeException();
        }
        this.positional = builder.getPositional();
    }

    // public methods

    public List<Expression> getOperands() {
        return getChildren();
    }

    public Expression getOperand1() {
        return getOperands().get(0);
    }

    public Expression getOperand2() {
        return getOperands().get(1);
    }

    public Expression getOperand3() {
        return getOperands().get(2);
    }

    public Operator getOperator() {
        return operator;
    }

    @Override
    public Expression replaceChildren(List<Expression> children) {
        return new ExpressionOperator.Builder()
                .setOperands(children)
                .setOperator(operator)
                .setPositional(positional)
                .build();
    }

    @Override
    public Type getType(ExpressionToType expressionToType) {
        assert expressionToType != null;
        Type result = expressionToType.getType(this);
        if (result != null) {
            return result;
        }

        List<Expression> operands = getOperands();
        Type[] opTypes = new Type[operands.size()];
        for (int opNr = 0; opNr < opTypes.length; opNr++) {
            Expression child = operands.get(opNr);
            Type childType = child.getType(expressionToType);
            assert childType != null : this + "    in    " + child + " " + child.getClass() + " " + expressionToType;
            opTypes[opNr] = childType;
        }
        OperatorEvaluator evaluator = ContextValue.get().getEvaluator(operator, opTypes);
        ensure(evaluator != null, ProblemsExpression.EXPRESSION_INCONSISTENT_OPERATOR, positional, Arrays.toString(opTypes));
        result = evaluator.resultType(opTypes);
        assert result != null : this + " ... " + this.getOperator() + "  " + this.getClass() + " " + Arrays.toString(opTypes);
        return result;
    }

    @Override
    public boolean isPropositional() {
        for (Expression operand : getOperands()) {
            if (!ExpressionPropositional.isPropositional(operand)) {
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
        builder.append(operator);
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
        ExpressionOperator other = (ExpressionOperator) obj;
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
        return this.operator.equals(other.operator);
    }    

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        for (Expression expression : this.getChildren()) {
            assert expression != null;
            hash = expression.hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        hash = operator.hashCode() + (hash << 6) + (hash << 16) - hash;            
        return hash;
    }

    @Override
    public Expression replacePositional(Positional positional) {
        return new ExpressionOperator.Builder()
                .setOperands(operands)
                .setOperator(operator)
                .setPositional(positional)
                .build();
    }
}
