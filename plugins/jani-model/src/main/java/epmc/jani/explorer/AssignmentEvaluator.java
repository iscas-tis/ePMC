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

import java.util.Map;

import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorCache;
import epmc.expression.standard.simplify.ContextExpressionSimplifier;
import epmc.expressionevaluator.ExpressionToType;
import epmc.jani.model.Assignment;
import epmc.jani.model.Variable;

public interface AssignmentEvaluator {
    interface Builder {
        Builder setAssignment(Assignment assignment);

        Builder setVariableMap(Map<Variable, Integer> variableMap);

        Builder setVariables(Expression[] variables);

        Builder setAutVarToLocal(Map<Expression, Expression> autVarToLocal);

        Builder setExpressionToType(ExpressionToType expressionToType);

        Builder setSimplifier(ContextExpressionSimplifier simplifier);

        Builder setEvaluatorCache(EvaluatorCache evaluatorCache);
        
        boolean canHandle();

        AssignmentEvaluator build();
    }

    void apply(NodeJANI node, NodeJANI successor);
}
