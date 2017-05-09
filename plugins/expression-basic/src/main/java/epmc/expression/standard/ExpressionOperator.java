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
import java.util.Iterator;
import java.util.List;

import epmc.value.OperatorNot;
import epmc.value.OperatorPow;
import epmc.value.OperatorSubtract;
import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.OperatorAdd;
import epmc.value.OperatorAddInverse;
import epmc.value.OperatorCeil;
import epmc.value.OperatorDivide;
import epmc.value.OperatorFloor;
import epmc.value.OperatorIte;
import epmc.value.OperatorLog;
import epmc.value.OperatorMax;
import epmc.value.OperatorMin;
import epmc.value.OperatorMod;
import epmc.value.OperatorMultiply;

/**
 * Expression to store an operator.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ExpressionOperator implements ExpressionPropositional {
    public final static class Builder {
        private Positional positional;
        private List<Expression> operands;
        private String operator;

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
        
        public Expression getOperand1() {
            return getOperands().get(0);
        }

        public Expression getOperand2() {
            return getOperands().get(1);
        }

        public Expression getOperand3() {
            return getOperands().get(2);
        }

        public Builder setOperands(Expression... operands) {
            this.operands = Arrays.asList(operands);
            return this;
        }
        
        public Builder setOperator(String operator) {
        	this.operator = operator;
        	return this;
        }

        public String getOperator() {
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
    private final String operator;

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
        return ContextValue.get().getOperator(operator);
    }    

    public String getOperatorId() {
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
    public Type getType(ExpressionToType expressionToType) throws EPMCException {
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
        result = ContextValue.get().getOperator(operator).resultType(opTypes);
        assert result != null : this + " ... " + this.getOperatorId() + "  " + this.getClass() + " " + Arrays.toString(opTypes);
        ensure(result != null, ProblemsExpression.VALUE_INCONSISTENT_INFO);
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
    
    public boolean isAdd() {
        return operator.equals(OperatorAdd.IDENTIFIER);
    }
    
    public boolean isSubtract() {
        return operator.equals(OperatorSubtract.IDENTIFIER);
    }
    
    public boolean isMultiply() {
        return operator.equals(OperatorMultiply.IDENTIFIER);
    }
    
    public boolean isDivide() {
        return operator.equals(OperatorDivide.IDENTIFIER);
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
        switch (operator) {
        case OperatorNot.IDENTIFIER:
        case OperatorAddInverse.IDENTIFIER:
            builder.append(operator);
            builder.append("(");
            builder.append(getOperand1());
            builder.append(")");
            break;
        case OperatorIte.IDENTIFIER:
            builder.append("(");
            builder.append(getOperand1());
            builder.append(")");
            builder.append(" ? ");
            builder.append("(");
            builder.append(getOperand2());
            builder.append(")");
            builder.append(" : ");
            builder.append("(");
            builder.append(getOperand3());
            builder.append(")");
            break;
        case OperatorMin.IDENTIFIER: case OperatorMax.IDENTIFIER: case OperatorPow.IDENTIFIER: case OperatorMod.IDENTIFIER: case OperatorLog.IDENTIFIER:
            builder.append(operator);
            builder.append("(");
            builder.append(getOperand1());
            builder.append(",");
            builder.append(getOperand2());
            builder.append(")");
            break;
        case OperatorFloor.IDENTIFIER: case OperatorCeil.IDENTIFIER:
            builder.append(operator);
            builder.append("(");
            builder.append(getOperand1());
            builder.append(")");
            break;
        default: {
            if (getChildren().size() == 1) {
                builder.append(operator);
                builder.append("(");
                builder.append(getChildren().get(0));
                builder.append(")");
            } else {
            Iterator<Expression> iter = getChildren().iterator();
            while (iter.hasNext()) {
                Expression child = iter.next();
                boolean needBraces = true;
                if (child instanceof ExpressionOperator) {
                    ExpressionOperator childOp = (ExpressionOperator) child;
                    if (operator == childOp.operator) {
                        needBraces = false;
                    }
                    if ((isAdd() || isSubtract())
                            && (childOp.isMultiply() || childOp.isDivide())) {
                        needBraces = false;
                    }
                }
                if (needBraces) {
                    builder.append("(");
                }
                builder.append(child);
                if (needBraces) {
                    builder.append(")");
                }
                if (iter.hasNext()) {
                    builder.append(" " + operator + " ");
                }
            }
            }
            break;
        }
        }
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
}
