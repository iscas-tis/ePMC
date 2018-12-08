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

package epmc.jani.explorer;

import java.util.Arrays;

import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.graph.explorer.ExplorerEdgeProperty;
import epmc.value.Type;
import epmc.value.Value;

final class PropertyEdgeExpression implements ExplorerEdgeProperty {
    private final EvaluatorExplicit evaluator;
    private final Type type;
    private Value[] successorValues;
    private final Value[] values;

    PropertyEdgeExpression(ExplorerJANI explorer, Expression[] identifiers, Expression expression, Type type) {
        assert explorer != null;
        assert expression != null;
        expression = UtilExpressionStandard.replace(expression, explorer.getModel().getConstants());
        evaluator = UtilEvaluatorExplicit.newEvaluator(expression, explorer, identifiers);
        if (type == null) {
            type = evaluator.getType();
        }
        this.type = type;
        values = new Value[identifiers.length];
        successorValues = new Value[this.values.length * 1];
    }

    @Override
    public Value get(int successor) {
        for (int valueNr = 0; valueNr < values.length; valueNr++) {
            values[valueNr] = successorValues[values.length * successor + valueNr];
        }
        evaluator.setValues(values);
        evaluator.evaluate();
        return evaluator.getResultValue();
    }

    public void setVariableValues(Value[] values, int successor) {
        ensureSuccessorValuesLengh(successor);
        for (int valueNr = 0; valueNr < values.length; valueNr++) {
            successorValues[this.values.length * successor + valueNr]
                    = values[valueNr];
        }
    }

    @Override
    public Type getType() {
        return type;
    }
    
    private void ensureSuccessorValuesLengh(int successor) {
        int newLength = successorValues.length / values.length;
        if (newLength > successor) {
            return;
        }
        while (newLength <= successor) {
            newLength *= 2;
        }
        successorValues = Arrays.copyOf(successorValues, newLength * values.length);
    }
}
