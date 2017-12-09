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

package epmc.propertysolver;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionPropositional;
import epmc.expression.standard.ExpressionTemporalFinally;
import epmc.graph.explicit.GraphExplicit;
import epmc.graphsolver.GraphSolverConfigurationExplicit;
import epmc.graphsolver.UtilGraphSolver;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.util.BitSet;
import epmc.value.ValueArrayAlgebra;

// utility class for computing reachability
public final class UtilReachability {

    /** collect the atomic propositions in the given formula which will be later used in
	    building the graph of the model. */
    public static Set<Expression> collectReachabilityInner(Expression expression) {
        if (ExpressionTemporalFinally.is(expression)) {
            ExpressionTemporalFinally expressionTemporal = ExpressionTemporalFinally.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectReachabilityInner(expressionTemporal.getOperand()));
            return result;
        } else {
            return Collections.singleton(expression);			
        }
    }

    /** Since we only consider the reachability here,
	the formula we consider is in form F a */
    public static boolean isReachability(Expression pathProp) {
        if (!ExpressionTemporalFinally.is(pathProp)) {
            return false;
        }
        ExpressionTemporalFinally asFinally = ExpressionTemporalFinally.as(pathProp);
        // check whether operand is a propositional formula
        if (!ExpressionPropositional.is(asFinally.getOperand())) {
            return false;
        }

        return true;
    }

    // solve the linear equation system
    public static ValueArrayAlgebra computeReachabilityProbability(
            GraphExplicit graph, BitSet oneStates) {

        GraphSolverConfigurationExplicit configuration = UtilGraphSolver.newGraphSolverConfigurationExplicit();
        GraphSolverObjectiveExplicitUnboundedReachability objective = new GraphSolverObjectiveExplicitUnboundedReachability();
        objective.setMin(false);
        objective.setGraph(graph);
        objective.setTarget(oneStates);
        //objective.setZeroSink(zeroStates);
        configuration.setObjective(objective);
        configuration.solve();

        return objective.getResult();

    }

    private UtilReachability() {
    }
}
