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
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.CmpType;
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.RewardSpecification;
import epmc.expression.standard.RewardType;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.graph.CommonProperties;
import epmc.graph.StateMap;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.StateMapExplicit;
import epmc.graph.explicit.StateSetExplicit;
import epmc.graphsolver.GraphSolverConfigurationExplicit;
import epmc.graphsolver.UtilGraphSolver;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBounded;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.operator.Operator;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;

// TODO check whether this works for JANI MDPs - probably not

public final class PropertySolverExplicitRewardInstantaneous implements PropertySolver {
    public final static String IDENTIFIER = "reward-explicit-instantaneous";
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
    public Set<Object> getRequiredGraphProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.SEMANTICS);
        return Collections.unmodifiableSet(required);
    }


    @Override
    public Set<Object> getRequiredNodeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.STATE);
        required.add(CommonProperties.PLAYER);
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        ExpressionReward quantifiedReward = (ExpressionReward) propertyQuantifier.getQuantified();
        required.add(quantifiedReward.getReward());
        RewardType rewardType = quantifiedReward.getRewardType();
        if (rewardType.isReachability()) {
            required.addAll(modelChecker.getRequiredNodeProperties(quantifiedReward.getRewardReachSet(), forStates));
        }
        return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredEdgeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.WEIGHT);
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        required.add(((ExpressionReward) (propertyQuantifier.getQuantified())).getReward());
        return Collections.unmodifiableSet(required);
    }

    @Override
    public StateMap solve() {
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        ExpressionReward quantifiedProp = (ExpressionReward) propertyQuantifier.getQuantified();
        DirType dirType = ExpressionQuantifier.computeQuantifierDirType(propertyQuantifier);
        boolean min = dirType == DirType.MIN;
        StateMap result = doSolve(quantifiedProp, (StateSetExplicit) forStates, min);
        if (propertyQuantifier.getCompareType() != CmpType.IS) {
            StateMap compare = modelChecker.check(propertyQuantifier.getCompare(), forStates);
            Operator op = propertyQuantifier.getCompareType().asExOpType();
            result = result.applyWith(op, compare);
        }
        return result;
    }

    public StateMap doSolve(Expression property, StateSetExplicit states, boolean min)
    {
        assert property != null;
        assert states != null;
        RewardSpecification rewardStructure = ((ExpressionReward) property).getReward();
        NodeProperty stateReward = graph.getNodeProperty(rewardStructure);
        EdgeProperty transReward = graph.getEdgeProperty(rewardStructure);
        assert stateReward != null;
        assert transReward != null;
        ExpressionReward propertyReward = (ExpressionReward) property;
        ValueAlgebra time = ValueAlgebra.as(UtilEvaluatorExplicit.evaluate(propertyReward.getTime()));
        ValueArrayAlgebra values = UtilValue.newArray(TypeWeight.get().getTypeArray(), graph.getNumNodes());
        for (int graphNode = 0; graphNode < graph.getNumNodes(); graphNode++) {
            Value reward = stateReward.get(graphNode);
            values.set(reward, graphNode);
        }
        GraphSolverConfigurationExplicit configuration = UtilGraphSolver.newGraphSolverConfigurationExplicit();
        GraphSolverObjectiveExplicitBounded objective = new GraphSolverObjectiveExplicitBounded();
        objective.setGraph(graph);
        objective.setMin(min);
        objective.setValues(values);
        objective.setTime(time);
        configuration.setObjective(objective);
        configuration.solve();
        values = objective.getResult();
        StateMapExplicit result = valuesToResult(values, states);

        return result;
    }

    private StateMapExplicit valuesToResult(ValueArray values, StateSetExplicit states) {
        assert values != null;
        assert states != null;
        Type typeWeight = TypeWeight.get();
        TypeArray typeArray = TypeWeight.get().getTypeArray();
        ValueArray resultValues = UtilValue.newArray(typeArray, states.size());
        Value entry = typeWeight.newValue();
        for (int i = 0; i < states.size(); i++) {
            values.get(entry, i);
            resultValues.set(entry, i);
        }
        return UtilGraph.newStateMap(states.clone(), resultValues);
    }

    @Override
    public boolean canHandle() {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineExplicit)) {
            return false;
        }
        if (!(property instanceof ExpressionQuantifier)) {
            return false;
        }
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        if (!(propertyQuantifier.getQuantified() instanceof ExpressionReward)) {
            return false;
        }
        ExpressionReward quantifiedReward = (ExpressionReward) propertyQuantifier.getQuantified();
        if (quantifiedReward.getRewardType() != RewardType.INSTANTANEOUS) {
            return false;
        }
        return true;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
}
