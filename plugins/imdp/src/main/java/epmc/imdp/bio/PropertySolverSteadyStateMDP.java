package epmc.imdp.bio;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionSteadyState;
import epmc.graph.CommonProperties;
import epmc.graph.SemanticsMDP;
import epmc.graph.StateMap;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.StateMapExplicit;
import epmc.graph.explicit.StateSetExplicit;
import epmc.graphsolver.GraphSolverConfigurationExplicit;
import epmc.graphsolver.UtilGraphSolver;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.value.TypeBoolean;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueBoolean;

public final class PropertySolverSteadyStateMDP implements PropertySolver {
    public final static String IDENTIFIER = "steady-state-mdp";
    private ModelChecker modelChecker;
    private ExpressionQuantifier quantifier;
    private StateSet forStates;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        this.modelChecker = modelChecker;
    }

    @Override
    public void setProperty(Expression property) {
        this.quantifier = null;
        ExpressionQuantifier quantifier = ExpressionQuantifier.as(property);
        if (quantifier == null) {
            return;
        }
        if (!ExpressionSteadyState.is(quantifier.getQuantified())) {
            return;
        }
        this.quantifier = quantifier;
    }

    @Override
    public void setForStates(StateSet forStates) {
        this.forStates = forStates;
    }

    @Override
    public boolean canHandle() {
        if (modelChecker.getModel().getSemantics() != SemanticsMDP.MDP) {
            return false;
        }
        if (quantifier == null) {
            return false;
        }
        return true;
    }

    @Override
    public Set<Object> getRequiredGraphProperties() {
        Set<Object> required = new HashSet<>();
        required.add(CommonProperties.SEMANTICS);
        return required;
    }

    @Override
    public Set<Object> getRequiredNodeProperties() {
        Set<Object> required = new HashSet<>();
        required.add(CommonProperties.STATE);
        required.add(CommonProperties.PLAYER);
        ExpressionSteadyState quantified = ExpressionSteadyState.as(quantifier.getQuantified());
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        required.addAll(modelChecker.getRequiredNodeProperties(quantified.getOperand1(), allStates));
        return required;
    }

    @Override
    public Set<Object> getRequiredEdgeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.WEIGHT);
        return required;
    }

    @Override
    public StateMap solve() {
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        ExpressionSteadyState steadyState = ExpressionSteadyState.as(quantifier.getQuantified());
        StateMapExplicit innerResult = (StateMapExplicit) modelChecker.check(steadyState.getOperand1(), allStates);
        ValueArrayAlgebra rewards = TypeWeight.get().getTypeArray().newValue();
        GraphExplicit graph = modelChecker.getLowLevel();
        rewards.setSize(graph.computeNumStates());
        ValueBoolean value = TypeBoolean.get().newValue();
        ValueAlgebra one = UtilValue.newValue(TypeWeight.get(), 1);
        ValueAlgebra zero = UtilValue.newValue(TypeWeight.get(), 0);
        for (int stateNr = 0; stateNr < innerResult.size(); stateNr++) {
            int state = innerResult.getExplicitIthState(stateNr);
            innerResult.getExplicitIthValue(value, stateNr);
            rewards.set(value.getBoolean() ? one : zero , state);
        }
        GraphSolverObjectiveSteadyStateStateOnly objective = new GraphSolverObjectiveSteadyStateStateOnly();
        objective.setGraph(graph);
        objective.setRewards(rewards);
        objective.setMin(quantifier.isDirMin());
        GraphSolverConfigurationExplicit configuration = UtilGraphSolver.newGraphSolverConfigurationExplicit();
        configuration.setObjective(objective);
        configuration.solve();
        return new StateMapExplicit((StateSetExplicit) forStates.clone(), objective.getResult());
//        System.out.println(objective.getResult());
//        new StateMapExplicit(null, valuesExplicit)
        // TODO Auto-generated method stub
  //      return null;
    }

}
