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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.graph.StateMap;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.StateSetExplicit;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.value.Type;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;

public final class PropertySolverExplicitPropositional implements PropertySolver {
    public final static String IDENTIFIER = "propositional-explicit";
    private ModelChecker modelChecker;
    private GraphExplicit graph;
    private Expression property;
    private StateSet forStates;

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        this.modelChecker = modelChecker;
        if (modelChecker.getEngine() instanceof EngineExplicit) {
            this.graph = modelChecker.getLowLevel();
        }
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
        List<Expression> identifiers = new ArrayList<>();
        identifiers.addAll(UtilExpressionStandard.collectIdentifiers(property));
        EvaluatorExplicit evaluator = UtilEvaluatorExplicit.newEvaluator(property, graph, identifiers.toArray(new Expression[0]));
        Value[] values = new Value[identifiers.size()];

        Type type = evaluator.getType();
        assert type != null : property;
        ValueArray resultValues = UtilValue.newArray(type.getTypeArray(), forStates.size());
        StateSetExplicit forStatesExplicit = (StateSetExplicit) forStates;
        NodeProperty[] nodeProperties = new NodeProperty[identifiers.size()];
        for (int idNr = 0; idNr < identifiers.size(); idNr++) {
            nodeProperties[idNr] = graph.getNodeProperty(identifiers.get(idNr));
            assert nodeProperties[idNr] != null : identifiers.get(idNr);
        }
        int forStateSize = forStates.size();
        for (int stateNr = 0; stateNr < forStateSize; stateNr++) {
            int state = forStatesExplicit.getExplicitIthState(stateNr);
            for (int idNr = 0; idNr < nodeProperties.length; idNr++) {
                values[idNr] = nodeProperties[idNr].get(state);
            }
            evaluator.setValues(values);
            evaluator.evaluate();
            Value entry = evaluator.getResultValue();
            resultValues.set(entry, stateNr);
        }

        return UtilGraph.newStateMap(forStatesExplicit.clone(), resultValues);
    }

    @Override
    public boolean canHandle() {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineExplicit)) {
            return false;
        }
        if (!is(property)) {
            return false;
        }
        return true;
    }

    @Override
    public Set<Object> getRequiredGraphProperties() {
        Set<Object> required = new LinkedHashSet<>();
        return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredNodeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.addAll(UtilExpressionStandard.collectIdentifiers(property));
        return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredEdgeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        return Collections.unmodifiableSet(required);
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
}
