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

import epmc.value.ValueEnum;
import epmc.value.ValueInteger;
import epmc.value.ValueNumBitsKnown;
import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.expressionevaluator.ExpressionToType;
import epmc.value.Type;
import epmc.value.Value;

public class EvaluatorDDLiteral implements EvaluatorDD {
    public final static String IDENTIFIER = "literal";

    private Expression expression;
    private DD dd;
    private List<DD> vector;
    private boolean closed;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setVariables(Map<Expression, VariableDD> variables) {
        assert variables != null;
    }

    @Override
    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public boolean canHandle() {
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        return true;
    }

    @Override
    public void build() {
        Value value = UtilEvaluatorExplicit.evaluate(expression);
        boolean useVector = false;
        //        boolean useVector = options.getBoolean(OptionsExpressionBasic.DD_EXPRESSION_VECTOR);
        ContextDD contextDD = ContextDD.get();
        if (useVector && ValueInteger.is(value)) {
            this.vector = contextDD.twoCplFromInt(ValueInteger.as(value).getInt());
        } else if (useVector && ValueEnum.is(value)) {
            int numBits = ValueNumBitsKnown.getNumBits(value);
            int number = ValueEnum.as(value).getEnum().ordinal();
            this.vector = contextDD.twoCplFromInt(number, numBits);
        } else {
            this.dd = contextDD.newConstant(value);
        }
    }

    @Override
    public DD getDD() {
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
}
