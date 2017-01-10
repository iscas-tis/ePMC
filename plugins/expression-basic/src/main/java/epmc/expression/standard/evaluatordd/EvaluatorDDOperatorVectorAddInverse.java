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

import epmc.value.OperatorAddInverse;
import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.value.ContextValue;

public final class EvaluatorDDOperatorVectorAddInverse implements EvaluatorDD {
    public final static String IDENTIFIER = "operator-vector-addinverse";
    
    private Map<Expression, VariableDD> variables;
    private Expression expression;
    private DD dd;
    private List<DD> vector;
    private boolean closed;

	private ContextValue context;
    
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setVariables(Map<Expression, VariableDD> variables) {
        this.variables = variables;
    }

    @Override
    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public boolean canHandle() throws EPMCException {
        return UtilEvaluatorDD.canIntegerVectorOperator(context, expression, OperatorAddInverse.IDENTIFIER, variables);
    }

    @Override
    public void build() throws EPMCException {
        vector = UtilEvaluatorDD.applyVector(context, expression, variables, getContextDD()::twoCplAddInverse);
        dd = UtilEvaluatorDD.applyVectorSingleDD(context, vector, expression, variables);
    }

    @Override
    public DD getDD() throws EPMCException {
        dd = UtilEvaluatorDD.getDD(dd, vector, expression);
        assert dd != null;
        return dd;
    }

    @Override
    public List<DD> getVector() {
        return vector;
    }

    @Override
    public void close() {
        closed = UtilEvaluatorDD.close(closed, dd, vector);
    }

    private ContextDD getContextDD() throws EPMCException {
        return ContextDD.get(context);
    }

	@Override
	public void setContextValue(ContextValue context) {
		this.context = context;
	}
}
