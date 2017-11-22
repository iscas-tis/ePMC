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

package epmc.expression.standard.evaluatordd;

import java.util.List;
import java.util.Map;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionOperator;
import epmc.operator.Operator;

public final class EvaluatorDDOperatorGeneral implements EvaluatorDD {
    public final static String IDENTIFIER = "operator-general";

    private Map<Expression, VariableDD> variables;
    private Expression expression;
    private ExpressionOperator expressionOperator;
    private DD dd;
    private boolean closed;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setVariables(Map<Expression, VariableDD> variables) {
        assert this.variables == null;
        this.variables = variables;
    }

    @Override
    public void setExpression(Expression expression) {
        assert this.expression == null;
        this.expression = expression;
        if (expression instanceof ExpressionOperator) {
            expressionOperator = (ExpressionOperator) expression;
        }
    }

    @Override
    public boolean canHandle() {
        assert expression != null;
        assert variables != null;
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        if (variables.containsKey(expression)) {
            return false;
        }
        return true;
    }

    @Override
    public void build() {
        Operator operator = expressionOperator.getOperator();
        DD[] dds = new DD[expressionOperator.getOperands().size()];
        for (int i = 0; i < expressionOperator.getOperands().size(); i++) {
            Expression operand = expressionOperator.getOperands().get(i);
            EvaluatorDD evaluator = UtilEvaluatorDD.newEvaluator(operand, variables);
            dds[i] = evaluator.getDD();
            assert dds[i] != null : expressionOperator.getOperands().get(i) + " " + i + " " + expression;
        }
        this.dd = getContextDD().apply(operator, dds);
    }

    @Override
    public DD getDD() {
        return dd;
    }

    @Override
    public List<DD> getVector() {
        return null;
    }

    @Override
    public void close() {
        closed = UtilEvaluatorDD.close(closed, dd, null);
    }

    private ContextDD getContextDD() {
        return ContextDD.get();
    }
}
