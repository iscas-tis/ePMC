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

import static epmc.expression.standard.ExpressionPropositional.is;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.graph.CommonProperties;
import epmc.graph.StateMap;
import epmc.graph.StateMapDD;
import epmc.graph.StateSet;
import epmc.graph.dd.GraphDD;
import epmc.graph.dd.StateSetDD;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;

public final class PropertySolverDDPropositional implements PropertySolver {
    public final static String IDENTIFIER = "propositional-dd";
    private ModelChecker modelChecker;
    private ExpressionToDD expressionToDD;
    private Expression property;
    private StateSet forStates;

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        this.modelChecker = modelChecker;
    }

    @Override
    public void setProperty(Expression property) {
        this.property = property;
    }

    @Override
    public void setForStates(StateSet forStates) {
        this.forStates = forStates;
    }

    @Override
    public StateMap solve() {
        assert property != null;
        assert forStates != null;
        DD value;
        if (modelChecker.getEngine() instanceof EngineDD) {
            GraphDD graphDD = modelChecker.getLowLevel();
            this.expressionToDD = graphDD.getGraphPropertyObject(CommonProperties.EXPRESSION_TO_DD);
        }

        if (ExpressionLiteral.is(property)) {
            value = ContextDD.get().newConstant(UtilEvaluatorExplicit.evaluate(property));
        } else {
            value = expressionToDD.translate(property);
        }
        StateMap result = new StateMapDD((StateSetDD) forStates.clone(), value);
        return result;
    }

    @Override
    public Set<Object> getRequiredGraphProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.EXPRESSION_TO_DD);
        return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredNodeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredEdgeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        return Collections.unmodifiableSet(required);
    }

    @Override
    public boolean canHandle() {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineDD)) {
            return false;
        }
        if (!is(property)) {
            return false;
        }
        return true;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
}
